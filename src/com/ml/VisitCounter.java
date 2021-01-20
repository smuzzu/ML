package com.ml;

/**
 * Created by Muzzu on 8/18/2019.
 */


import com.ml.utils.Counters;
import com.ml.utils.DatabaseHelper;
import com.ml.utils.HTMLParseUtils;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class VisitCounter {


    static int PAUSE_MILLISECONDS = 130;
    static int PAUSE_ON_ERRPR_MILLISECONDS = 3000;
    static int INTERVAL_SIZE=5000;

    static String[] usuarios = new String[] {
            "ACACIAYLENGA",
            "QUEFRESQUETE",
            "SOMOS_MAS"
    };


    public synchronized static HashMap<String,Integer> processVisits(Date date1, Date date2, ArrayList<String> productIds, boolean DEBUG){
        CloseableHttpClient httpClient=HttpUtils.buildHttpClient();

        HashMap<String,Integer> result = new HashMap<String,Integer>();
        ArrayList<String> zeroVisitsList=new ArrayList<String>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate1 = dateFormat.format(date1);
        String strDate2 = dateFormat.format(date2);
        String dateOnQuery = "&date_from=" + strDate1 + "T00:00:00.000-00:00&date_to=" + strDate2 + "T23:59:00.000-00:00";

        int i=0;

        while (i<productIds.size()) {
            String allProductIDsStr="";
            for (int j = 0; j < 10 && i < productIds.size(); j++) {
                String productId=productIds.get(i);
                allProductIDsStr+=productId+",";
                i++;
            }

            allProductIDsStr=allProductIDsStr.substring(0,allProductIDsStr.length()-1);
            String url = "https://api.mercadolibre.com/items/visits?ids="+allProductIDsStr+dateOnQuery;
            String htmlString= HttpUtils.getHTMLStringFromPage(url,httpClient,DEBUG,true);
            if (!HttpUtils.isOK(htmlString)) {
                // hacemos pausa por si es problema de red
                try {
                    Thread.sleep(PAUSE_ON_ERRPR_MILLISECONDS);
                } catch (InterruptedException e) {
                    Logger.log(e);
                }
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                httpClient = null;
                httpClient = HttpUtils.buildHttpClient();
            }

            htmlString=HttpUtils.getHTMLStringFromPage(url,httpClient,DEBUG,true); // just 1 retry
            boolean processItems=true;
            int pos1=0;
            int pos2=0;
            String productId=null;
            String quantityStr=null;
            int quantity=0;
            while (processItems) {
                pos1 = htmlString.indexOf("MLA", pos1);
                if (pos1 < 0) {
                    processItems = false;
                    continue;
                }

                pos2 = htmlString.indexOf("\"", pos1);
                productId = htmlString.substring(pos1, pos2);
                //productId = productId.substring(0, 3) + "-" + productId.substring(3);
                pos1 = htmlString.indexOf("visits", pos1);
                pos1 += 8;
                pos2 = htmlString.indexOf(",", pos1);
                quantityStr = htmlString.substring(pos1, pos2);
                quantity = Integer.parseInt(quantityStr);


                if (quantity == 0) {
                    zeroVisitsList.add(productId);
                }
                result.put(productId,quantity);
            }
        }
        return result;
    }


    public static HashMap<String,Integer> retriveAllVisits(ArrayList<String> allProductIDs, Date date, String dateOnQuery, boolean SAVE, boolean DEBUG, String DATABASE) {
        int count = 0;

        ArrayList<String> tenProductIDs = new ArrayList<String>();
        HashMap<String,Integer> visitsHashMap = null;
        HashMap<String,Integer> result = new HashMap<String,Integer>();

        for (String productId : allProductIDs) {
            count++;

            tenProductIDs.add(productId);

            if (count >= 10) {
                visitsHashMap= retrive10Visits(date, dateOnQuery, tenProductIDs, SAVE,DEBUG,DATABASE);
                result.putAll(visitsHashMap);
                tenProductIDs = new ArrayList<String>();
                count = 0;
            }
        }
        if (tenProductIDs.size() > 0) {  //processing last record
            visitsHashMap= retrive10Visits(date, dateOnQuery, tenProductIDs, SAVE,DEBUG,DATABASE);
            result.putAll(visitsHashMap);
        }
        return result;
    }

    private static HashMap<String,Integer> retrive10Visits(Date date, String dateOnQuery, ArrayList<String> tenProductIDs, boolean SAVE, boolean DEBUG, String DATABASE) {
        int logCountHelper=0;
        String lineLog="";
        HashMap<String,Integer> result = new HashMap<String,Integer>();

        int runnerCount = Counters.getGlobalRunnerCount();

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        String allProductIDsStr="";
        for (String productId:tenProductIDs){
            productId = productId.substring(0, 3) + productId.substring(4);//removing the minus sign
            allProductIDsStr+=productId+",";
        }
        allProductIDsStr=allProductIDsStr.substring(0,allProductIDsStr.length()-1);

        String url = "https://api.mercadolibre.com/items/visits?ids="+allProductIDsStr+dateOnQuery;

        int resto = runnerCount % 3;
        String usuario = usuarios[resto];


        if (DEBUG) {
            Logger.log("Usuario: " + usuario);
            System.out.println("Usuario: " + usuario);
        }

        //todo usar usuarios rotativos
        JSONObject visitsArray = HttpUtils.getJsonObjectUsingToken(url,httpClient,usuario,true);

        if (visitsArray==null || !visitsArray.has("elArray")){
            // hacemos pausa por si es problema de red

            boolean b=false;

            try {
                Thread.sleep(PAUSE_ON_ERRPR_MILLISECONDS);
            } catch (InterruptedException e) {
                Logger.log(e);
            }
            Logger.log(" visitsArray is null " + url);
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            httpClient = null;
            httpClient = HttpUtils.buildHttpClient();

            visitsArray = HttpUtils.getJsonObjectUsingToken(url,httpClient,usuario,true); // just 1 retry
            if (visitsArray==null || !visitsArray.has("elArray")) {
                httpClient=HttpUtils.buildHttpClient();
                return result;
            }

        }

        JSONArray elArray = visitsArray.getJSONArray("elArray");

        for (int i=0; i<elArray.length(); i++){
            JSONObject visitsObject = (JSONObject)elArray.get(i);
            String id = visitsObject.getString("item_id");
            String formattedId = HTMLParseUtils.getFormatedId(id);
            int totalVisits = visitsObject.getInt("total_visits");


            result.put(formattedId,totalVisits);

            logCountHelper++;
            if (logCountHelper>5){
                logCountHelper=0;
                lineLog=lineLog.substring(0,lineLog.length()-1);
                System.out.println(lineLog);
                Logger.log(lineLog);
                lineLog="";
            }

            lineLog+="   "+formattedId + " " + String.format("%05d", totalVisits) + "   |";


        }
        if (lineLog.length()>0) {
            lineLog=lineLog.substring(0,lineLog.length()-1);
            System.out.println(lineLog);
            Logger.log(lineLog);
        }

        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClient=null;

        //hacemos una pausa de entre PAUSE_MILLISECONDS y 10% mas
        int min =PAUSE_MILLISECONDS;
        int max = (int) (PAUSE_MILLISECONDS*1.1);
        long random = (long) (Math.random() * (max - min + 1) + min);
        try {
            Thread.sleep(random);
        } catch (InterruptedException e) {
            Logger.log(e);
        }

        return result;

    }


    public static void updateVisits(String database, boolean SAVE, boolean DEBUG) {

        String msg = "\nProcesando Visitas";
        System.out.println(msg);
        Logger.log(msg);
        Counters.initGlobalRunnerCount();

        Connection connection = DatabaseHelper.getSelectConnection(database);
        ArrayList<String> allProductIDs = new ArrayList<String>();
        Date date1=null;
        Date date2=null;
        String dateOnQueryStr=null;
        try {

            PreparedStatement datesPreparedStatement = connection.prepareStatement("SELECT fecha FROM public.movimientos group by fecha order by fecha desc");
            ResultSet rs = datesPreparedStatement.executeQuery();
            if (rs == null) {
                msg = "Error getting dates";
                System.out.println(msg);
                Logger.log(msg);
            }
            if (!rs.next()) {
                msg = "Error getting dates II";
                System.out.println(msg);
                Logger.log(msg);
            }
            date2 = rs.getDate(1);
            if (!rs.next()) {
                msg = "Error getting dates III";
                System.out.println(msg);
                Logger.log(msg);
            }
            date1 = rs.getDate(1);

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String strDate1 = dateFormat.format(date1);
            String strDate2 = dateFormat.format(date2);
            dateOnQueryStr = "&date_from=" + strDate1 + "T00:00:00.000-00:00&date_to=" + strDate2 + "T23:59:00.000-00:00";


            PreparedStatement selectPreparedStatement = connection.prepareStatement("SELECT idproducto FROM public.movimientos WHERE fecha=? and (visitas is null or visitas = 0)");
            selectPreparedStatement.setDate(1, date2);


            rs = selectPreparedStatement.executeQuery();
            if (rs == null) {
                msg = "Error getting latest movements "+date2;
                System.out.println(msg);
                Logger.log(msg);
            }

            while (rs.next()) {
                String productId = rs.getString(1);
                allProductIDs.add(productId);
            }


        } catch (SQLException e) {
            e.printStackTrace();
            Logger.log(e);
        }


        msg = "Procesando "+allProductIDs.size()+" registros";
        System.out.println(msg);
        Logger.log(msg);

        for (int i=0; i<allProductIDs.size(); i+=INTERVAL_SIZE){
            int from=i;
            int to=from+INTERVAL_SIZE;
            if (to>allProductIDs.size()){
                to=allProductIDs.size();
            }
            List<String> interval=allProductIDs.subList(from,to);
            HashMap visitsHashMap = retriveAllVisits(new ArrayList<String>(interval), date2, dateOnQueryStr,SAVE,DEBUG,database);
            if (SAVE) {
                msg = "Guardando "+INTERVAL_SIZE+" registros";
                System.out.println(msg);
                Logger.log(msg);
                for (Object key : visitsHashMap.keySet()) {
                    String formattedId = (String) key;
                    Integer totalVisits= (Integer) visitsHashMap.get(formattedId);
                    DatabaseHelper.updateVisitOnDatabase(formattedId, totalVisits, date2, database);
                }
            }
        }


        msg="Visitas Procesadas: "+allProductIDs.size();
        System.out.println(msg);
        Logger.log(msg);

    }

    public static void main (String args[]){
        updateVisits("ML6",true,false);
        updateVisits("ML6",true,false);
        updateVisits("ML6",true,false);

    }


}
