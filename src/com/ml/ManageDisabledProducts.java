package com.ml;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ml.utils.DatabaseHelper;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;

import com.ml.utils.SData;
import com.ml.utils.TokenUtils;
import org.apache.http.impl.client.CloseableHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;



public class ManageDisabledProducts {


    static CloseableHttpClient globalClient = null;
    static Connection globalSelectConnection = null;
    static Connection globalUpadteConnection = null;

    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static void execute(String database){

        globalClient = HttpUtils.buildHttpClient();

        globalSelectConnection = DatabaseHelper.getSelectConnection(database);

        globalUpadteConnection=DatabaseHelper.getDisableProductConnection(database);



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


    public static void main(String[] args) {
        String hostname = TokenUtils.getHostname();
        if (hostname!=null && hostname.equals(SData.getHostname1())) {
            execute("ML1");
            execute("ML2");
        }else {
            execute("ML6");
        }

    }
}
