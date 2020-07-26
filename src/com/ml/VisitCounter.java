package com.ml;

/**
 * Created by Muzzu on 8/18/2019.
 */

import com.ml.utils.Counters;
import com.ml.utils.DatabaseHelper;
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

import com.ml.utils.Logger;
import com.ml.utils.HttpUtils;

public class VisitCounter extends Thread {


    //parametros
    private Date date;
    private boolean SAVE;
    private boolean DEBUG;
    private ArrayList<String> productIds;
    private String dateOnQuery;
    private String database;

    VisitCounter(ArrayList<String> productIds, Date date, String dateOnQuery, boolean SAVE, boolean DEBUG, String database){
        this.productIds=productIds;
        this.date=date;
        this.dateOnQuery=dateOnQuery;
        this.SAVE=SAVE;
        this.DEBUG=DEBUG;
        this.database=database;
    }

    static int TIMEOUT_MIN = 10;
    static int MAX_THREADS_VISITS = 30;

    public ArrayList<String> getZeroVisitsList(){
        return this.zeroVisitsList;
    }

    public void resetZeroVisitsList(){
        this.zeroVisitsList.clear();
    }

    private static volatile ArrayList<String> zeroVisitsList=new ArrayList<String>();

    public void run(){

        int logCountHelper=0;
        String lineLog="";

        String runnerID="R"+ Counters.getGlobalRunnerCount();

        String msg =null;
        if (DEBUG) {
            msg = "XXXXXXXXXXXXXX Iniciando " + runnerID;
            System.out.println(msg);
            Logger.log(msg);
        }

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        String allProductIDsStr="";
        for (String productId:productIds){
            productId = productId.substring(0, 3) + productId.substring(4);//removing the minus sign
            allProductIDsStr+=productId+",";
        }
        allProductIDsStr=allProductIDsStr.substring(0,allProductIDsStr.length()-1);

        String url = "https://api.mercadolibre.com/items/visits?ids="+allProductIDsStr+dateOnQuery;

        String htmlString= HttpUtils.getHTMLStringFromPage(url,httpClient,DEBUG);

        if (!HttpUtils.isOK(htmlString)) {
            // hacemos pausa por si es problema de red
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Logger.log(e);
            }
            Logger.log(runnerID + " hmlstring from page visits is null " + url);
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            httpClient = null;
            httpClient = HttpUtils.buildHttpClient();
        }

        htmlString=HttpUtils.getHTMLStringFromPage(url,httpClient,DEBUG); // just 1 retry
        if (!HttpUtils.isOK(htmlString)) {
            httpClient=null;
            return;
        }

        boolean processItems=true;
        int pos1=0;
        int pos2=0;
        String productId=null;
        String quantityStr=null;
        int quantity=0;
        while (processItems){
            pos1=htmlString.indexOf("MLA",pos1);
            if (pos1<0){
                processItems=false;
                continue;
            }

            pos2=htmlString.indexOf("\"",pos1);
            productId=htmlString.substring(pos1,pos2);
            productId=productId.substring(0,3)+"-"+productId.substring(3);
            pos1=htmlString.indexOf("visits",pos1);
            pos1+=8;
            pos2=htmlString.indexOf(",",pos1);
            quantityStr=htmlString.substring(pos1,pos2);
            quantity=Integer.parseInt(quantityStr);

            logCountHelper++;
            if (logCountHelper>5){
                logCountHelper=0;
                lineLog=lineLog.substring(0,lineLog.length()-1);
                System.out.println(lineLog);
                Logger.log(lineLog);
                lineLog="";
            }

            lineLog+="   "+productId + " " + String.format("%05d", quantity) + "   |";

            if (quantity==0){
                zeroVisitsList.add(productId);
                continue;
            }
            if (SAVE) {
                DatabaseHelper.updateVisitOnDatabase(productId, quantity, date, database);
            }
        }
        if (lineLog.length()>0) {
            lineLog=lineLog.substring(0,lineLog.length()-1);
            System.out.println(lineLog);
            Logger.log(lineLog);
        }

        if (DEBUG) {
            msg = "XXXXXXXXXXXXXX Este es el fin " + runnerID;
            System.out.println(msg);
            Logger.log(msg);
        }

        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClient=null;

    }

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
            String htmlString= HttpUtils.getHTMLStringFromPage(url,httpClient,DEBUG);
            if (!HttpUtils.isOK(htmlString)) {
                // hacemos pausa por si es problema de red
                try {
                    Thread.sleep(5000);
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

            htmlString=HttpUtils.getHTMLStringFromPage(url,httpClient,DEBUG); // just 1 retry
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


    private static ArrayList<String> processAllVisits(ArrayList<String> allProductIDs, Date date, String dateOnQuery, boolean SAVE, boolean DEBUG, String DATABASE) {
        int count = 0;

        ArrayList<String> tenProductIDs = new ArrayList<String>();
        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();


        for (String productId : allProductIDs) {
            count++;

            tenProductIDs.add(productId);

            if (count >= 10) {
                process10Visits(date, dateOnQuery, tenProductIDs, threadArrayList,SAVE,DEBUG,DATABASE);
                tenProductIDs = new ArrayList<String>();
                count = 0;
            }
        }
        if (tenProductIDs.size() > 0) {  //processing last record
            process10Visits(date, dateOnQuery, tenProductIDs, threadArrayList,SAVE,DEBUG,DATABASE);
        }

        for (Thread thread : threadArrayList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //clone
        ArrayList<String> zeroVisitsList=new ArrayList<String>();
        if (threadArrayList.size()>0){
            VisitCounter aVisitCounter = (VisitCounter)threadArrayList.get(0);
            for (String productIdWithZeroVisits: aVisitCounter.getZeroVisitsList()){
                zeroVisitsList.add(productIdWithZeroVisits);
            }
            aVisitCounter.resetZeroVisitsList();
        }else {
            String msg="No product with 0 vitists";
            System.out.println(msg);
            Logger.log(msg);
        }

        return zeroVisitsList;

    }

    private static void process10Visits(Date date, String dateOnQuery, ArrayList<String> tenProductIDs, ArrayList<Thread> threadArrayList, boolean SAVE, boolean DEBUG, String DATABASE) {
        long currentTime;
        long timeoutTime;

        VisitCounter visitCounter = new VisitCounter(tenProductIDs, date, dateOnQuery, SAVE, DEBUG, DATABASE);
        threadArrayList.add(visitCounter);
        visitCounter.start();
        currentTime = System.currentTimeMillis();
        timeoutTime = currentTime + TIMEOUT_MIN * 60l * 1000l;

        while (MAX_THREADS_VISITS < Thread.activeCount()) {

            try {
                Thread.sleep(10l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentTime = System.currentTimeMillis();
            if (currentTime > timeoutTime) {
                System.out.println("Error en de timeout.  Demasiado tiempo sin terminar de procesar una visita entre " + MAX_THREADS_VISITS + " visitas");
                System.exit(0);
            }
        }
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


            PreparedStatement selectPreparedStatement = connection.prepareStatement("SELECT idproducto FROM public.movimientos WHERE fecha=?");
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

        ArrayList<String> zeroVisitsList=processAllVisits(allProductIDs, date2, dateOnQueryStr,SAVE,DEBUG,database);
        msg="Reintentando los ceros";
        System.out.println(msg);
        Logger.log(msg);
        zeroVisitsList=processAllVisits(zeroVisitsList, date2, dateOnQueryStr,SAVE,DEBUG,database); //insistimos 2 veces mas cuando visitas devuelve cero
        System.out.println(msg);
        Logger.log(msg);
        processAllVisits(zeroVisitsList, date2, dateOnQueryStr,SAVE,DEBUG,database);

        msg="Visitas Procesadas: "+allProductIDs.size();
        System.out.println(msg);
        Logger.log(msg);

    }

}
