package com.ml;

/**
 * Created by Muzzu on 8/18/2019.
 */


import com.ml.utils.Counters;
import com.ml.utils.DatabaseHelper;
import com.ml.utils.HTMLParseUtils;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.SData;
import com.ml.utils.TokenUtils;
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

public class VisitCounter extends Thread {


    static int PAUSE_MILLISECONDS = 130;
    static int PAUSE_ON_ERRPR_MILLISECONDS = 3000;
    static int INTERVAL_SIZE=5000;
    static int TIMEOUT_MIN=10;
    static int MAX_THREADS=9;

    static String visitsQueryURL =null;
    static HashMap<String,Integer> allVisitsHashMap = new HashMap<String,Integer>();

    static String visitsQueryURLForOneDay =
            "https://api.mercadolibre.com/items/[PUBLICATION_ID]/visits/time_window?last=1&unit=day&ending=[YYYY-MM-DD]";
    static String visitsQueryURLForDayRange =
            "https://api.mercadolibre.com/items/visits?ids=[PUBLICATION_ID]&date_from=[FROM-YYYY-MM-DD]&date_to=[TO-YYYY-MM-DD]";

    String publicationId=null;

    public static String[] usuarios = new String[] {
            SData.getAcaciaYLenga(),
            SData.getQuefresquete(),
            SData.getSomosMas(),
            SData.getMarianaTest()
    };


    public static String getVisitsQueryURLForOneDay(Date date){
        long oneDayInMilliseconds = 86400000L; //this will add a complete day on milliseconds
        Date followingDay = new Date(date.getTime() + oneDayInMilliseconds);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String nextDayStr = dateFormat.format(followingDay);
        return visitsQueryURLForOneDay.replace("[YYYY-MM-DD]",nextDayStr);
    }

    public static String getVisitsQueryURLForDayRange(Date dateFrom, Date dateTo){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateFromStr = dateFormat.format(dateFrom);
        String dateToStr = dateFormat.format(dateTo);
        return visitsQueryURLForDayRange.replace("[FROM-YYYY-MM-DD]",dateFromStr).replace("[TO-YYYY-MM-DD]",dateToStr);
    }

    private static String getVisitsQueryURLWithId(String publicationId){
        return visitsQueryURL.replace("[PUBLICATION_ID]",HTMLParseUtils.getUnformattedId(publicationId));
    }


    public static HashMap<String,Integer> retriveAllVisits(ArrayList<String> allProductIDs, String visitsQueryURLParam) {
        visitsQueryURL = visitsQueryURLParam;
        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
        long currentTime;
        long timeoutTime;


        for (String productId : allProductIDs) {

            VisitCounter visitCounter = new VisitCounter();
            threadArrayList.add(visitCounter);
            visitCounter.publicationId=productId;
            visitCounter.start();
            currentTime = System.currentTimeMillis();
            timeoutTime = currentTime + TIMEOUT_MIN * 60l * 1000l;

            while (MAX_THREADS < (Thread.activeCount() - 1)) {

                try {
                    Thread.sleep(10l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentTime = System.currentTimeMillis();
                if (currentTime > timeoutTime) {
                    String errorMsg="Error de timeout.  Paso demasiado tiempo sin terminar de procesar las visitsa de " + productId;
                    System.out.println(errorMsg);
                    Logger.log(errorMsg);
                    System.exit(0);
                }
            }
        }

        for (Thread thread : threadArrayList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return allVisitsHashMap;
    }




    public void run() {

        CloseableHttpClient httpClient=HttpUtils.buildHttpClient();

        int runnerCount = Counters.getGlobalRunnerCount();

        String url = getVisitsQueryURLWithId(publicationId);

        int resto = runnerCount % usuarios.length;
        String usuario = usuarios[resto];

        int retries = 0;
        boolean retry = true;

        JSONObject visitsObject = null;

        while (retry && retries < 5) {
            retries++;

            visitsObject = HttpUtils.getJsonObjectUsingToken(url,httpClient,usuario,false);

            //String url2="https://api.mercadolibre.com/items/"+HTMLParseUtils.getUnformattedId(this.publicationId)+"/visits/time_window?last=7&unit=day&ending=2021-07-31";
            //JSONObject visitsObject2 = HttpUtils.getJsonObjectUsingToken(url2,httpClient,usuario,false);

            if (visitsObject!=null && visitsObject.has("results") && !visitsObject.isNull("results")){
                JSONArray resultsArray = visitsObject.getJSONArray("results");
                if (resultsArray!=null && !resultsArray.isEmpty()){
                    JSONObject resultObject=resultsArray.getJSONObject(0);
                    if (resultObject!=null && resultObject.has("total") && !resultObject.isNull("total")){
                        int total=resultObject.getInt("total");
                        boolean b=false;
                    }
                }
            }

            if (visitsObject==null) {
                // hacemos pausa por si es problema de red
                try {
                    Thread.sleep(PAUSE_ON_ERRPR_MILLISECONDS * retries * retries);
                } catch (InterruptedException e) {
                    Logger.log(e);
                }
                Logger.log(" visitsObject is null " + url);
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                httpClient = null;
                httpClient = HttpUtils.buildHttpClient();

            } else {
                retry=false;
            }
        }
        if (visitsObject==null) {
           return;
        }

        int totalVisits = visitsObject.getInt("total_visits");
        System.out.println("R"+runnerCount+" "+publicationId+" "+totalVisits);

        allVisitsHashMap.put(publicationId,totalVisits);

        //hacemos una pausa de entre PAUSE_MILLISECONDS y PAUSE_MILLISECONDS * 3
        int min =PAUSE_MILLISECONDS;
        int max = (int) (PAUSE_MILLISECONDS*3);
        long random = (long) (Math.random() * (max - min + 1) + min);
        try {
            Thread.sleep(random);
        } catch (InterruptedException e) {
            Logger.log(e);
        }

    }


    public static void updateVisits(String database, boolean SAVE) {

        String msg = "\nProcesando Visitas";
        System.out.println(msg);
        Logger.log(msg);
        Counters.initGlobalRunnerCount();
        DatabaseHelper.initUpdateVisits();

        Connection connection = DatabaseHelper.getSelectConnection(database);
        ArrayList<String> allProductIDs = new ArrayList<String>();
        Date date1=null;
        Date date2=null;
        String visitsQueryURLParam=null;

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

            visitsQueryURLParam=getVisitsQueryURLForDayRange(date1,date2);

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
            HashMap visitsHashMap = retriveAllVisits(new ArrayList<String>(interval), visitsQueryURLParam);
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

        String hostname = TokenUtils.getHostname();
        if (hostname!=null && hostname.equals(SData.getHostname1())) {
            updateVisits("ML1", true);
            updateVisits("ML2", true);
            updateVisits("ML1", true);
            updateVisits("ML2", true);
        }else {
            updateVisits("ML6", true);
            updateVisits("ML6", true);
        }
    }


}
