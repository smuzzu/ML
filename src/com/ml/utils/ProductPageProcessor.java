package com.ml.utils;

import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;

import java.io.IOException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ProductPageProcessor extends Thread {

    private String url;
    private int sellerId;
    private int page;
    private int ranking;
    private boolean SAVE;
    private boolean DEBUG;
    private String DATABASE;
    private Date globalDate;
    private boolean localRun;

    static int MAX_THREADS=20;
    static int TIMEOUT_MIN=10;


    public ProductPageProcessor(String url, int sellerId, int page, int ranking, boolean save, boolean debug, String DATABASE, Date globalDate, boolean localRun){
        this.url=url;
        this.sellerId=sellerId;
        this.page=page;
        this.ranking=ranking;
        this.SAVE=save;
        this.DEBUG=debug;
        this.DATABASE=DATABASE;
        this.globalDate=globalDate;
        this.localRun=localRun;
    }


    public void run() {
        String runnerID = "R" + Counters.getGlobalRunnerCount();

        String msg = runnerID + " procesando posible pausado "+ url;
        if (DEBUG) {
            Logger.log(msg);
        }

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        String htmlString = HttpUtils.getHTMLStringFromPage(url, httpClient, DEBUG, true);

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
            htmlString = HttpUtils.getHTMLStringFromPage(url, httpClient, DEBUG, true);
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
            Counters.incrementGlobalDisableCount();
            System.out.println(msg);
            Logger.log(msg);
            if (SAVE) {
                DatabaseHelper.disableProduct(productId, DATABASE);
            }
            return;
        }

        Date lastUpdate = DatabaseHelper.fetchLastUpdate(productId,DATABASE);
        if (lastUpdate != null){
            boolean sameDate = Counters.isSameDate(lastUpdate, this.globalDate);
            if (sameDate){
                return; //este ya lo hicimos
            }
        }

        int previousTotalSold = DatabaseHelper.fetchTotalSold(productId, DATABASE);
        if (totalSold != previousTotalSold) { //actualizar o agregar
            int newSold = totalSold - previousTotalSold;

            boolean officialStore=HTMLParseUtils.getOfficialStore(htmlString);

            String seller=HTMLParseUtils.getSeller(htmlString,officialStore,url);

            String lastQuestion=HTMLParseUtils.getLastQuestion(htmlString);

            String previousLastQuestion = DatabaseHelper.fetchLastQuestion(productId,DATABASE);
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

            if (previousTotalSold>0) { //actualizar
                msg = runnerID+" new sale. productID: " + productId + " quantity: " + newSold;
                System.out.println(msg);
                Logger.log(msg);
                if (SAVE && !localRun) {
                    DatabaseHelper.updateProductAddActivity(DATABASE, false, globalDate, productId, seller, officialStore, totalSold, newSold, title, url, reviews, stars, price, newQuestions, lastQuestion, 0, shipping, discount, premium);
                }
            }else {//nuevo registro. agregar
                msg = runnerID+" new product " + newSold + " " + url;
                System.out.println(msg);
                Logger.log(msg);
                if (SAVE && !localRun) {
                    DatabaseHelper.insertProduct(DATABASE,false,globalDate,productId, seller, totalSold, lastQuestion, url, officialStore);
                }
            }

        }


    }

    public static void processPossiblyPausedProducts(String database, Date globalDate, ArrayList<String> globalProcesedProductList, boolean SAVE, boolean DEBUG) {

        String msg="*** Procesando pausados  / novedades antes del proceso "+Counters.getGlobalNewsCount();;
        System.out.println(msg);
        Logger.log(msg);

        Connection selectConnection = DatabaseHelper.getSelectConnection(database);

        String productId=null;
        String productUrl=null;
        ArrayList<String> possiblyPausedProductList = new ArrayList<String>();
        boolean localRun=false; //localRun=true do not registerSale
        if (globalProcesedProductList==null){
            msg="Error en globalProcesedProductList !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\r\n Procesando toda la base";
            System.out.println(msg);
            Logger.log(msg);
            globalProcesedProductList=new ArrayList<String>();
            localRun=true;
        }

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
            globalSelectPossiblyPaused = selectConnection.prepareStatement("SELECT id,url FROM public.productos WHERE lastupdate<? and deshabilitado=false order by lastupdate");
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
            ProductPageProcessor productPageProcessor = new ProductPageProcessor(url, 0, 0,0 ,SAVE, DEBUG, database,globalDate,localRun);
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
        msg="total de deshabilitados: "+Counters.getGlobalDisableCount();
        System.out.println(msg);
        Logger.log(msg);
    }





    public static void main(String[] args){

        String DATABASE="ML2";
        boolean SAVE=true;
        boolean DEBUG=false;
        HashMap<Integer,String> sellerIds=new HashMap<Integer, String>();
        Date globalDate= Date.valueOf("2019-08-21");
//        ProductPageProcessor.processPossiblyPausedProducts(DATABASE, globalDate,null,SAVE,DEBUG);

        Connection selectConnection = DatabaseHelper.getSelectConnection(DATABASE);
        try{
            PreparedStatement globalSelectPossiblyPaused = null;
            //option 1

            String START_FROM="MLA-768627928";

            globalSelectPossiblyPaused = selectConnection.prepareStatement("SELECT url,proveedor,tiendaoficial,idproducto FROM movimientos WHERE fecha='2020-09-08' and idproducto<'"+START_FROM+"' order by idproducto");

            String msg="Buscando pausados en databasse"+" - "+ globalSelectPossiblyPaused.toString();
            System.out.println(msg);
            Logger.log(msg);

            ResultSet rs2 = globalSelectPossiblyPaused.executeQuery();
            if (rs2==null){
                System.out.println("Couldn't get Possibly Paused Products");
            }

            CloseableHttpClient client=HttpUtils.buildHttpClient();
            while (rs2.next()) {
                String proveedor = rs2.getString(2);
                String idproducto = rs2.getString(4);
                JSONObject productObj = HttpUtils.getJsonObjectWithoutToken("https://api.mercadolibre.com/items/" + HTMLParseUtils.getUnformattedId(idproducto), client, false);
                int sellerID = productObj.getInt("seller_id");
                String nickname = null;
                if (sellerIds.containsKey(sellerID)) {
                    nickname = sellerIds.get(sellerID);
                } else{
                    JSONObject sellerObj = HttpUtils.getJsonObjectWithoutToken("https://api.mercadolibre.com/users/" + sellerID, client, false);
                    nickname = sellerObj.getString("nickname");
                    sellerIds.put(sellerID,nickname);
                }
                if (nickname==null || nickname.isEmpty()){
                    boolean kaka=true;
                }
                if (proveedor==null || !proveedor.equals(nickname)){
                    String msg1 = "update movimientos set proveedor ='"+nickname+"' where idproducto='"+idproducto+"' and fecha='2020-09-08';";
                    String msg2 = "update productos set proveedor ='"+nickname+"' where id='"+idproducto+"';";
                    System.out.println(msg1);
                    System.out.println(msg2);
                    boolean kaka=true;
                }
            }
        }catch(SQLException e){
            Logger.log("Couldn't get Possibly Paused Products II");
            Logger.log(e);
        }


    }



}
