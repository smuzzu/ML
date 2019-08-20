package com.ml;

/**
 * Created by Muzzu on 8/18/2019.
 */

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.entity.StrictContentLengthStrategy;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;

public class VisitCounter extends Thread {


    //parametros
    private Date date;
    private boolean SAVE;
    private boolean DEBUG;
    private ArrayList<String> productIds;
    private String dateOnQuery;
    private String database;


    static int globalRunnerCount;

    VisitCounter(ArrayList<String> productIds, Date date, String dateOnQuery, boolean SAVE, boolean DEBUG, String database){
        this.productIds=productIds;
        this.date=date;
        this.dateOnQuery=dateOnQuery;
        this.SAVE=SAVE;
        this.DEBUG=DEBUG;
        this.database=database;
    }

    public ArrayList<String> getZeroVisitsList(){
        return this.zeroVisitsList;
    }

    public void resetZeroVisitsList(){
        this.zeroVisitsList.clear();
    }

    private static synchronized int getGlobalRunnerCount() {
        return ++globalRunnerCount;
    }

    private static volatile ArrayList<String> zeroVisitsList=new ArrayList<String>();

    private static PreparedStatement globalUpdateVisits = null;

    public void run(){

        String runnerID="R"+getGlobalRunnerCount();

        String msg =null;
        if (DEBUG) {
            msg = "XXXXXXXXXXXXXX Iniciando " + runnerID;
            System.out.println(msg);
            MercadoLibre01.log(msg);
        }

        CloseableHttpClient httpClient = MercadoLibre01.buildHttpClient();

        String allProductIDsStr="";
        for (String productId:productIds){
            productId = productId.substring(0, 3) + productId.substring(4);//removing the minus sign
            allProductIDsStr+=productId+",";
        }
        allProductIDsStr=allProductIDsStr.substring(0,allProductIDsStr.length()-1);

        String url = "https://api.mercadolibre.com/items/visits?ids="+allProductIDsStr+dateOnQuery;

        String htmlString=MercadoLibre01.getHTMLStringFromPage(url,httpClient);

        if (htmlString == null) {
            // hacemos pausa por si es problema de red
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                MercadoLibre01.log(e);
            }
            MercadoLibre01.log(runnerID + " hmlstring from page visits is null " + url);
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            httpClient = null;
            httpClient = MercadoLibre01.buildHttpClient();
        }

        htmlString=MercadoLibre01.getHTMLStringFromPage(url,httpClient); // just 1 retry
        if (htmlString == null) {
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

            msg = productId + " " + quantity;
            System.out.println(msg);
            MercadoLibre01.log(msg);

            if (quantity==0){
                zeroVisitsList.add(productId);
                continue;
            }
            if (SAVE) {
                updateVisits(productId, quantity, date, database);
            }
        }
        if (DEBUG) {
            msg = "XXXXXXXXXXXXXX Este es el fin " + runnerID;
            System.out.println(msg);
            MercadoLibre01.log(msg);
        }

        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClient=null;

    }

    private static synchronized void updateVisits(String productId,int quantity, Date date, String database){

        if (globalUpdateVisits ==null) {
            Connection connection = MercadoLibre01.getUpdateConnection(database);
            try {
                globalUpdateVisits = connection.prepareStatement("update public.movimientos set visitas=? where idproducto=? and fecha =?");
                globalUpdateVisits.setDate(3,date);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            globalUpdateVisits.setInt(1,quantity);
            globalUpdateVisits.setString(2,productId);


            int updatedRecords=globalUpdateVisits.executeUpdate();
            globalUpdateVisits.getConnection().commit();

            if (updatedRecords!=1){
                MercadoLibre01.log("Error updating visits "+productId+" "+ quantity + " " +date);
            }



        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
