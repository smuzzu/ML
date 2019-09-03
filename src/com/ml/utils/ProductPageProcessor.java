package com.ml.utils;

import com.ml.MercadoLibre01;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class ProductPageProcessor extends Thread {

    private String url;
    private boolean SAVE;
    private boolean DEBUG;
    private String DATABASE;

    static Connection globalUpadteConnection2 = null;
    static int MAX_THREADS=17;
    static int TIMEOUT_MIN=10;


    public ProductPageProcessor(String url, boolean SAVE, boolean DEBUG, String DATABASE){
        this.url=url;
        this.SAVE=SAVE;
        this.DEBUG=DEBUG;
        this.DATABASE=DATABASE;
    }


    public void run() {
        String runnerID = "R" + Counters.getGlobalRunnerCount();

        String msg = runnerID + " procesando posible pausado "+ url;
        if (DEBUG) {
            Logger.log(msg);
        }
        System.out.println(msg);

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        String htmlString = HttpUtils.getHTMLStringFromPage(url, httpClient, DEBUG);

        if (!HttpUtils.isOK(htmlString)) { //un reintento mas que suficiente aca
            // hacemos pausa por si es problema de red
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Logger.log(e);
            }
            Logger.log(runnerID + " hmlstring from possible paused page is null " + url);
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            httpClient = null;
            httpClient = HttpUtils.buildHttpClient();
            htmlString = HttpUtils.getHTMLStringFromPage(url, httpClient, DEBUG);
        }

        boolean disable=false;
        int totalSold=0;
        String productId = HTMLParseUtils.getProductIdFromURL(url);
        if (!HttpUtils.isOK(htmlString)) {
            Logger.log(htmlString); // todo sacar
            disable=true;
        }else {
            totalSold = HTMLParseUtils.getTotalSold(htmlString, url);
            if (totalSold <=0) {
                disable=true;
            }
        }

        if (disable){
            msg="Deshabilitando producto "+url;
            System.out.println(msg);
            Logger.log(msg);
            if (SAVE) {
                disableProduct(productId, DATABASE);
            }
            return;
        }

        int previousTotalSold = MercadoLibre01.getTotalSold(productId, DATABASE);
        if (totalSold>0 && totalSold != previousTotalSold) { //actualizar
            int newSold = totalSold - previousTotalSold;

            boolean officialStore=HTMLParseUtils.getOfficialStore(htmlString);

            String seller=HTMLParseUtils.getSeller(htmlString,officialStore,url);

            String lastQuestion=HTMLParseUtils.getLastQuestion(htmlString);

            String previousLastQuestion = MercadoLibre01.getLastQuestion(productId,DATABASE);
            ArrayList<String> newQuestionsList= HttpUtils.getNewQuestionsFromPreviousLastQuestion(url,httpClient,runnerID,DEBUG,previousLastQuestion);
            int newQuestions = newQuestionsList.size();

            String title=HTMLParseUtils.getTitle(htmlString,url);

            int reviews=HTMLParseUtils.getReviews(htmlString,url);

            double stars=0l;
            if (reviews>0) {
                stars=HTMLParseUtils.getStars(htmlString, url);
            }

            double price=HTMLParseUtils.getPrice(htmlString,url);

            int shipping = HTMLParseUtils.getShipping(htmlString);

            int discount = HTMLParseUtils.getDiscount(htmlString,url);

            boolean premium = HTMLParseUtils.getPremium(htmlString);

            Counters.incrementGlobalNewsCount();

            msg = runnerID+" new sale. productID: " + productId + " quantity: " + newSold;
            System.out.println(msg);
            Logger.log(msg);

            if (SAVE) {
                MercadoLibre01.updateProductAddActivity(productId, seller, officialStore, totalSold, newSold, title, url, reviews, stars, price, newQuestions, lastQuestion, 0, shipping, discount, premium);
            }

        }


    }

    private static synchronized Connection getUpdateConnection(String database) {
        if (globalUpadteConnection2 == null) {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/" + database;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "password");
            try {
                globalUpadteConnection2 = DriverManager.getConnection(url, props);
                globalUpadteConnection2.setAutoCommit(true);
            } catch (SQLException e) {
                Logger.log("I couldn't make an update connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalUpadteConnection2;
    }

    public static void processPossiblyPausedProducts(String database,Connection selectConnection, Date globalDate, ArrayList<String> globalProcesedProductList, boolean SAVE, boolean DEBUG) {

        String msg="*** Procesando pausados  / novedades antes del proceso "+Counters.getGlobalNewsCount();;
        System.out.println(msg);
        Logger.log(msg);

        String productId=null;
        String productUrl=null;
        ArrayList<String> possiblyPausedProductList = new ArrayList<String>();
        try{
            PreparedStatement datesPreparedStatement = selectConnection.prepareStatement("SELECT fecha FROM public.movimientos group by fecha order by fecha desc");
            ResultSet rs = datesPreparedStatement.executeQuery();
            if (rs == null) {
                msg = "Error getting dates B";
                System.out.println(msg);
                Logger.log(msg);
            }
            if (!rs.next()) {
                msg = "Error getting dates B II";
                System.out.println(msg);
                Logger.log(msg);
            }
            if (!rs.next()) {
                msg = "Error getting dates B III";
                System.out.println(msg);
                Logger.log(msg);
            }
            Date previousWeekRunDate= rs.getDate(1);

            PreparedStatement globalSelectPossiblyPaused = null;
            //option 1
            globalSelectPossiblyPaused = selectConnection.prepareStatement("SELECT id,url FROM public.productos WHERE lastupdate<? and deshabilitado=false");
            globalSelectPossiblyPaused.setDate(1,globalDate);

            //option 2
            //globalSelectPossiblyPaused = connection.prepareStatement("SELECT id,url FROM public.productos WHERE lastupdate=? and deshabilitado=false");
            //globalSelectPossiblyPaused.setDate(1, previousWeekRunDate);

            msg="revisando pausados "+" - "+ globalSelectPossiblyPaused.toString();
            System.out.println(msg);
            Logger.log(msg);

            ResultSet rs2 = globalSelectPossiblyPaused.executeQuery();
            if (rs2==null){
                Logger.log("Couldn't get Possibly Paused Products");
                return;
            }

            if (globalProcesedProductList==null){
                msg="Error en globalProcesedProductList !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\r\n Procesando toda la base";
                System.out.println(msg);
                Logger.log(msg);
                globalProcesedProductList=new ArrayList<String>();
            }

            while (rs2.next()){
                productId=rs2.getString(1);
                if (!globalProcesedProductList.contains(productId)) {
                    productUrl = rs2.getString(2);
                    possiblyPausedProductList.add(productUrl);
                }
            }
        }catch(SQLException e){
            Logger.log("Couldn't get Possibly Paused Products II");
            Logger.log(e);
        }
        //return possiblyPausedProductList;
        int possiblePausedTotal=0;
        if (globalProcesedProductList!=null){
            possiblePausedTotal=globalProcesedProductList.size();
        }
        msg="posibles pausados: "+possiblyPausedProductList.size()+" de "+possiblePausedTotal;
        System.out.println(msg);
        Logger.log(msg);

        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
        long currentTime;
        long timeoutTime;

        Counters.initGlobalRunnerCount();
        for (String url:possiblyPausedProductList){
            //processArticle(url,0,possiblyPausedProductList,)


            ProductPageProcessor productPageProcessor = new ProductPageProcessor(url, SAVE, DEBUG, database);
            threadArrayList.add(productPageProcessor);
            productPageProcessor.start();
            currentTime = System.currentTimeMillis();
            timeoutTime = currentTime + TIMEOUT_MIN * 60l * 1000l;

            while (MAX_THREADS < (Thread.activeCount()-1)) {

                try {
                    Thread.sleep(10l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentTime = System.currentTimeMillis();
                if (currentTime > timeoutTime) {
                    System.out.println("Error en de timeout.  Demasiado tiempo sin terminar de procesar un producto pausado entre " + MAX_THREADS + " visitas");
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
    }



    private static synchronized void disableProduct(String productId,String database){
        int registrosModificados=0;
        Connection updateConnection=getUpdateConnection(database);
        try {
            PreparedStatement preparedStatement = updateConnection.prepareStatement("update productos set deshabilitado = true where id = ?");
            preparedStatement.setString(1, productId);
            registrosModificados = preparedStatement.executeUpdate();
            //incrementGlobalDisableCount();
        } catch (SQLException e) {
            Logger.log("Error deshabilitando producto "+productId);
            Logger.log(e);
        }

        if (registrosModificados < 1) {
            Logger.log("Couldn't no pudo deshabilitar ningun producto de empresa " + productId);
        }

    }




}
