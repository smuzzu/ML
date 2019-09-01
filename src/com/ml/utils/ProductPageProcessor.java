package com.ml.utils;

import com.ml.MercadoLibre01;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class ProductPageProcessor extends Thread {

    private String url;
    private boolean SAVE;
    private boolean DEBUG;
    private String DATABASE;

    static Connection globalUpadteConnection2 = null;

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
            System.out.println(msg);
        }
        Logger.log(msg);

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        int retries = 0;
        String htmlString = HttpUtils.getHTMLStringFromPage(url, httpClient, DEBUG);

        while (htmlString == null && retries < 2) { // TODO VOLVER A 5 intentos ****************
            retries++;
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
        if (htmlString == null) {
            httpClient = null;
            disable=true;
        }
        if (htmlString.indexOf("viendo una nueva publicaci")>0){//hubo un redirect a otra cosa
            disable=true;
        }
        int totalSold = HTMLParseUtils.getTotalSold(htmlString, url);
        if (totalSold <=-0) {
            disable=true;
        }

        String productId = HTMLParseUtils.getProductIdFromURL(url);

        if (disable){
            disableProduct(productId,DATABASE);
        }

        int previousTotalSold = MercadoLibre01.getTotalSold(productId, DATABASE);
        if (totalSold>0 && totalSold != previousTotalSold) { //actualizar
            int newSold = totalSold - previousTotalSold;

            boolean officialStore=HTMLParseUtils.getOfficialStore(htmlString);

            String seller=HTMLParseUtils.getSeller(htmlString,officialStore,url);

            String lastQuestion=HTMLParseUtils.getLastQuestion(htmlString);

            String previousLastQuestion = MercadoLibre01.getLastQuestion(productId,DATABASE);
            ArrayList<String> newQuestionsList= HttpUtils.getNewQuestionsFromPreviousLastQuestion(htmlString,url,httpClient,runnerID,DEBUG,previousLastQuestion);
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
