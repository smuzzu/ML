package com.ml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.io.UnsupportedEncodingException;

import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.ml.utils.DatabaseHelper;
import com.ml.utils.HttpUtils;
import com.ml.utils.Item;
import com.ml.utils.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.apache.http.impl.client.CloseableHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;



public class ManageDisabledProducts {

    static String DATABASE = "ML2";

    static CloseableHttpClient globalClient = null;
    static Connection globalSelectConnection = null;
    static Connection globalUpadteConnection = null;
    static BufferedWriter globalLogger = null;

    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) {


        globalClient = HttpUtils.buildHttpClient();

        globalSelectConnection = DatabaseHelper.getSelectConnection(DATABASE);

        globalUpadteConnection=DatabaseHelper.getDisableProductConnection(DATABASE);



        HashMap<String, String> statusHashMap = new HashMap<String, String>();
        HashMap<String, Long> statusTotalHashMap = new HashMap<String, Long>();
        HashMap<String, String> urlHashMap =new HashMap<String, String>();


        PreparedStatement updatePrepredStatement=null;
        PreparedStatement selectPreparedStatement = null;
        ResultSet selectResultSet = null;
        int count = 0;
        int count2 = 0;
        int count3 = 0;
        int count4 = 0;
        String itemsIds = "";


        try {
            selectPreparedStatement = globalSelectConnection.prepareStatement("select id, url from productos where deshabilitado=true order by lastupdate desc");
            //selectPreparedStatement = globalSelectConnection.prepareStatement("select id, url from productos where deshabilitado=false order by lastupdate desc");
            updatePrepredStatement= globalUpadteConnection.prepareStatement("update productos set deshabilitado=false, url=? where id = ?");
            selectResultSet = selectPreparedStatement.executeQuery();

            while (selectResultSet.next()) {
                count3++;
                String id = selectResultSet.getString(1);
                String url = selectResultSet.getString(2);
                String unformatedId = "MLA" + id.substring(4);
                urlHashMap.put(id,url);
                itemsIds += unformatedId + ",";

                count++;
                if (count == 20) {
                    System.out.println(count3);
                    itemsIds = itemsIds.substring(0, itemsIds.length() - 1);
                    String itemsUrl = "https://api.mercadolibre.com/items?ids=" + itemsIds;
                    JSONObject jsonObject = HttpUtils.getJsonObjectWithoutToken(itemsUrl, globalClient, true);
                    if (jsonObject != null) {
                        JSONArray jsonArray = jsonObject.getJSONArray("elArray");
                        for (int j = 0; j < jsonArray.length(); j++) {
                            JSONObject itemObject2 = jsonArray.getJSONObject(j);
                            int code = itemObject2.getInt("code");
                            JSONObject productObj = itemObject2.getJSONObject("body");

                            if (code == 200 && productObj != null) {
                                if (productObj.has("permalink") && !productObj.isNull("permalink")) {
                                    boolean update = true;

                                    String id2 = productObj.getString("id");
                                    String formattedId2="MLA-"+id2.substring(3);
                                    String permalink = productObj.getString("permalink");
                                    String status = productObj.getString("status");
                                    String baseUrl=urlHashMap.get(formattedId2);

                                    if (status.equals("paused")) {
                                        String lastupdatedStr = productObj.getString("last_updated").substring(0, 10);
                                        java.util.Date updateDate = dateFormat.parse(lastupdatedStr);
                                        Date limitDate = DatabaseHelper.getTwoHundredSeventyDaysBefore();
                                        if (updateDate.before(limitDate)) {
                                            update = false;
                                        }
                                    }
                                    if (status.equals("closed") || status.equals("inactive")) {
                                        update = false;
                                    }

/*
                                    if (permalink==null || baseUrl==null || baseUrl.equals(permalink)){
                                        update=false;
                                    }
                                    if (baseUrl==null && permalink!=null){
                                        update=true;
                                    }
*/

                                    if (update) {
                                        count4++;
                                        updatePrepredStatement.setString(1, permalink);
                                        updatePrepredStatement.setString(2, formattedId2);
                                        String sql = "update productos set deshabilitado=false, url='" + permalink + "' where id = '" + formattedId2 + "';";
                                        System.out.println(sql);
                                        Logger.writeOnFile("sqlsml1.txt",sql);
                                        updatePrepredStatement.executeUpdate();

                                    }

                                    if (!statusHashMap.containsKey(status)) {
                                        statusHashMap.put(status, permalink);
                                        statusTotalHashMap.put(status, 0L);
                                    }
                                    long newTotal = statusTotalHashMap.get(status) + 1;
                                    statusTotalHashMap.put(status, newTotal);
                                }
                            }

                        }
                    }
                    count = 0;
                    itemsIds = "";
                    urlHashMap =new HashMap<String, String>();
                }

                count2++;
                if (count2 == 400) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Logger.log(e);
                    }
                    globalClient = HttpUtils.buildHttpClient();
                    count2 = 0;
                }


            }
        } catch (SQLException sQLException) {
            Logger.log(sQLException);
            sQLException.printStackTrace();
            System.exit(0);
        }
        catch (ParseException parseException){
            Logger.log(parseException);
            parseException.printStackTrace();
            System.exit(0);
        }


        Iterator keyItarator = statusHashMap.keySet().iterator();
        System.out.println("Registros procesados "+count3+" // actualizados "+count4);
        while (keyItarator.hasNext()){
            String status = (String) keyItarator.next();
            String url = statusHashMap.get(status);
            Long total = statusTotalHashMap.get(status);
            System.out.println(status+" "+total+" "+url);
        }
    }

}
