package com.ml.utils;

import com.ml.MercadoLibre01;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

import java.util.ArrayList;

public class ProductPageProcessor extends Thread {

    private String url;
    private boolean SAVE;
    private boolean DEBUG;
    private String DATABASE;


    public ProductPageProcessor(String url, boolean SAVE, boolean DEBUG, String DATABASE){
        this.url=url;
        this.SAVE=SAVE;
        this.DEBUG=DEBUG;
        this.DATABASE=DATABASE;
    }


    public void run() {
        String runnerID = "R" + Counters.getGlobalRunnerCount();

        String msg = null;
        if (DEBUG) {
            msg = "XXXXXXXXXXXXXX Iniciando " + runnerID;
            System.out.println(msg);
            Logger.log(msg);
        }

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        int retries = 0;
        String htmlString = HttpUtils.getHTMLStringFromPage(url, httpClient, DEBUG);

        while (htmlString == null && retries < 5) {
            retries++;
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
            htmlString = HttpUtils.getHTMLStringFromPage(url, httpClient, DEBUG);
        }

        if (htmlString == null) {
            httpClient = null;
            return;
        }

        int totalSold = HTMLParseUtils.getTotalSold(htmlString, url);
        if (totalSold == -1) {
            return;
        }

        String productId = HTMLParseUtils.getProductIdFromURL(url);
        int previousTotalSold = MercadoLibre01.getTotalSold(productId, DATABASE);
        if (totalSold != previousTotalSold) { //actualizar
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

            if (officialStore){
                boolean b = false;
            }
            if (premium){
                boolean b = false;
            }
            if (productId==null || seller==null || url==null || title==null){
                boolean b = false;
            }
            if (totalSold==0 || newSold==0){
                boolean b = false;
            }
            if (shipping!=0){
                boolean b = false;
            }
            if (newQuestions==0){
                boolean b = false;
            }
            if (reviews>0 && stars==0) {
                boolean b = false;
            }
            if (price==0){
                boolean b = false;
            }
            if (discount>0){
                boolean b = false;
            }


            if (SAVE) {
                MercadoLibre01.updateProductAddActivity(productId, seller, officialStore, totalSold, newSold, title, url, reviews, stars, price, newQuestions, lastQuestion, 0, shipping, discount, premium);
            }

        }


    }


}
