package com.ml.utils;

import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;

public class ProductPageProcessor extends Thread {

    private Item item;
    private boolean SAVE;
    private boolean DEBUG;
    private String DATABASE;
    private Date globalDate;
    private int minimunSales;

    public ProductPageProcessor(Item item, boolean save, boolean debug, String DATABASE, Date globalDate,
                                int minimunSales){
        this.item=item;
        this.SAVE=save;
        this.DEBUG=debug;
        this.DATABASE=DATABASE;
        this.globalDate=globalDate;
        this.minimunSales=minimunSales;
    }


    public void run() {
        String runnerID=null;
        try {
            runnerID = "R" + Counters.getGlobalRunnerCount();

            String msg = runnerID + " procesando posible pausado " + item.permalink;
            if (DEBUG) {
                Logger.log(msg);
            }

            String formatedId = HTMLParseUtils.getFormatedId(item.id);

            String productPage=getProductPage(item.permalink,DEBUG,runnerID);
            if (productPage==null){
                return;
            }

            boolean disable = getDisable(productPage);

            if (disable) {
                msg = "Deshabilitando producto " + item.permalink;
                System.out.println(msg);
                Logger.log(msg);
                Counters.incrementGlobalDisableCount();
                if (SAVE) {
                    DatabaseHelper.disableProduct(formatedId, DATABASE);
                }
                return;
            }

            item.totalSold=getTotalSold(productPage,item.permalink);
            if (item.totalSold<minimunSales){
                return;
            }

            String htmlString=getHtmlString(productPage);

            Item dbItem = DatabaseHelper.fetchLastUpdate(formatedId, DATABASE);
            if (dbItem != null) {
                Date lastUpdate = dbItem.lastUpdate;
                if (lastUpdate != null) {
                    boolean sameDate = Counters.isSameDate(lastUpdate, globalDate);
                    if (sameDate) {
                        return; //este ya lo hicimos
                    }
                }
            }


            item.stock=getFetchStock(runnerID, htmlString,item.permalink,DEBUG);

            boolean promoted = HTMLParseUtils.isPromoted(htmlString);

            boolean mostSold = HTMLParseUtils.isMostSold(htmlString);


            int previousTotalSold = 0;
            int previousStock=0;
            if (dbItem!=null) {
                previousTotalSold=dbItem.totalSold;
                previousStock=dbItem.stock;
            }
            if (item.totalSold != previousTotalSold || item.stock!=previousStock || mostSold) { //todo guardamos nuevas reviews y/o nuevas preguntas? visitas?
                int newSold = item.totalSold - previousTotalSold;
                int difStock = item.stock - previousStock;

                String lastQuestion = HTMLParseUtils.getLastQuestion(htmlString);

                String previousLastQuestion = DatabaseHelper.fetchLastQuestion(formatedId, DATABASE);
                ArrayList<String> newQuestionsList = HttpUtils.getNewQuestionsFromPreviousLastQuestion(item.permalink, formatedId, runnerID, DEBUG, previousLastQuestion);
                int newQuestions = newQuestionsList.size();

                item.reviews = HTMLParseUtils.getReviews(htmlString, item.permalink);

                if (item.reviews > 0) {
                    item.stars = HTMLParseUtils.getStars(htmlString, item.permalink);
                }

                if (item.variations.size()>1){
                    HashMap<String,Integer> attributesMap = new HashMap<String,Integer>();
                    for (String variation: item.variations){
                        variation=variation.substring(0,variation.indexOf("|"));
                        if (attributesMap.containsKey(variation)){//todo no suma bien aca
                            Integer quantity = attributesMap.get(variation);
                            quantity++;
                            attributesMap.replace(variation,quantity);
                        }else {
                            attributesMap.put(variation,1);
                        }
                    }
                    HashMap<String,Integer> filteredAttributesMap = new HashMap<String,Integer>();
                    for (String variation: attributesMap.keySet()){
                        int quantity =attributesMap.get(variation);
                        if (quantity>1){
                            filteredAttributesMap.put(variation,quantity);
                        }
                    }
                    attributesMap.clear();
                    int totalAtrributes=filteredAttributesMap.size();
                    String selectedAttribute=null;
                    if (totalAtrributes>0) {
                        if (totalAtrributes==1){
                            selectedAttribute=(String)filteredAttributesMap.keySet().toArray()[0];
                        }else {
                            int minQuantity = Integer.MAX_VALUE;
                            String minAttribute = null;
                            for (String variation : filteredAttributesMap.keySet()) {
                                int quantity = filteredAttributesMap.get(variation);
                                if (minQuantity > quantity) {
                                    minQuantity = quantity;
                                    minAttribute = variation;
                                }
                            }
                            selectedAttribute=minAttribute;
                        }
                        ArrayList<String> variationsArrayList = new ArrayList<String>();
                        for (String variation: item.variations){
                            if (variation.startsWith(selectedAttribute)){
                                int pos =variation.indexOf("|")+1;
                                variation=variation.substring(pos);
                                variationsArrayList.add(variation);
                            }
                        }
                    }
                }

                Counters.incrementGlobalNewsCount();

                if (previousTotalSold > 0) { //actualizar
                    msg = runnerID + " update productID: " + formatedId + " sold: " + newSold + " / diff stock: "+difStock+" / most sold:"+mostSold;
                    System.out.println(msg);
                    Logger.log(msg);
                    if (SAVE) {
                        DatabaseHelper.updateProductAddActivity(DATABASE, false, globalDate, formatedId, item.sellerName, item.sellerId, item.officialStore, item.totalSold, newSold, item.stock,difStock , item.title, item.permalink, item.reviews, item.stars, item.price, newQuestions, lastQuestion, item.ranking, item.shipping, item.discount, item.premium, mostSold);
                    }
                } else {//nuevo registro. agregar
                    msg = runnerID + " new product " + newSold+ "/"+ item.stock  + " " + item.permalink;
                    System.out.println(msg);
                    Logger.log(msg);
                    if (SAVE) {
                        DatabaseHelper.insertProduct(DATABASE, globalDate, formatedId, item.sellerName, item.sellerId, item.totalSold, item.stock, lastQuestion, item.permalink, item.officialStore);
                    }
                }
            }

        }catch (Exception e){
            String msg = "Exception in ProductPageProcessor id="+item.id+" url="+item.permalink;
            Logger.log(msg);
            Logger.log(e);
            System.out.println(msg);
            e.printStackTrace();
        }
    }

    public static int getFetchStock(String runnerID, String htmlString, String url, boolean DEBUG) {
        int stock=HTMLParseUtils.getStock(htmlString,url);
        HashMap<String, ArrayList<String>> variationsMap = HTMLParseUtils.getVariationsMap(htmlString,url);
        ArrayList<String> selectedVariationTypeURLs
                = HTMLParseUtils.getSelectedVariationTypeURLs(htmlString,url,variationsMap);
        boolean manyVariationsSelected=false;
        ArrayList<String> selectedVariationList = HTMLParseUtils.getSelectedVariation(htmlString, url);
        if (selectedVariationList.size()>1){
            manyVariationsSelected=true;
        }
        String selectedVariation="";
        if (!selectedVariationList.isEmpty()){
            selectedVariation=selectedVariationList.get(0);
        }
        if (selectedVariationTypeURLs.size()>1|| manyVariationsSelected) {
            if (!selectedVariation.equals("")){
                if (!manyVariationsSelected) {
                    boolean defaultVariationChecked = false;
                    for (String variation : selectedVariationTypeURLs) {
                        int pos = variation.indexOf("|");
                        String variationName = variation.substring(0, pos);
                        if (variationName.equals(selectedVariation)) {
                            defaultVariationChecked = true;
                        }
                    }
                    if (!defaultVariationChecked) {
                        stock = 0;
                    }
                }
                for (String variation: selectedVariationTypeURLs){
                    int pos=variation.indexOf("|");
                    String variationName=variation.substring(0,pos);
                    if (variationName.equals(selectedVariation)){
                        continue;
                    }
                    String variationUrl=variation.substring(pos+1);
                    String variationPage=getProductPage(variationUrl,DEBUG, runnerID);
                    stock+=HTMLParseUtils.getStock(variationPage,variationUrl);
                }
            }
        }
        return stock;
    }

    private static String getProductPage(String url, boolean DEBUG, String runnerID){
        String result=null;
        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
        String htmlString = HttpUtils.getHTMLStringFromPage(url, httpClient, DEBUG, true, null);
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
            htmlString = HttpUtils.getHTMLStringFromPage(url, httpClient, DEBUG, true, null);
        }
        boolean paused=HTMLParseUtils.isPaused(htmlString);
        if (paused) {
            String msg = "El producto esta pausado " + url;
            System.out.println(msg);
            Logger.log(msg);
            return null;
        }


        boolean disable = false;
        int totalSold = 0;
        int stock=0;
        if (!HttpUtils.isOK(htmlString)) {
            Logger.log(htmlString); // todo sacar
            disable = true;
        } else {
            totalSold = HTMLParseUtils.getTotalSold(htmlString, url);
            stock=HTMLParseUtils.getStock(htmlString,url);
            if (totalSold <= 0 || stock <=0) {
                String msg = "TOTAL SOLD = " + totalSold + "STOCK = " + stock +" on " + url + " vamos a recargar la pagina carajo";
                Logger.log(msg);
                System.out.println(msg);

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
                htmlString = HttpUtils.getHTMLStringFromPage(url, httpClient, DEBUG, true, null);


                if (!HttpUtils.isOK(htmlString)) {
                    Logger.log(htmlString); // todo sacar
                    disable = true;
                } else {
                    paused=HTMLParseUtils.isPaused(htmlString);
                    if (paused) {
                        msg = "El producto esta pausado " + url;
                        System.out.println(msg);
                        Logger.log(msg);
                        return null;
                    }
                    totalSold=HTMLParseUtils.getTotalSold(htmlString, url);
                }

                if (totalSold <= 0 && !url.startsWith(HTMLParseUtils.SERVICIO_URL)) {
                    msg = "TOTAL SOLD = " + totalSold + " on " + url;
                    Logger.log(msg);
                    System.out.println(msg);
                    Logger.log(htmlString);
                    disable = true;
                }
            }
        }
        result = disable+"|"+totalSold+"|"+htmlString;
        return result;
    }


    public static boolean getDisable(String productPage){
        int pos1=productPage.indexOf("|");
        String disableStr=productPage.substring(0,pos1);
        boolean disable = false;
        if (disableStr.equals("true")){
            disable=true;
        }
        return disable;
    }

    public static int getTotalSold(String productPage,String url){
        int pos1=productPage.indexOf("|");
        pos1++;
        int pos2=productPage.indexOf("|",pos1);
        String totalSoldSrt=productPage.substring(pos1,pos2);
        int totalSold=0;
        try {
            totalSold = Integer.parseInt(totalSoldSrt);
        }catch (Exception e){
            String msg="Error getting totalsold "+url;
            System.out.println(msg);
            e.printStackTrace();
            Logger.log(msg);
            Logger.log(e);
        }
        return totalSold;
    }

    public String getHtmlString(String productPage){
        int pos=productPage.indexOf("|")+1;
        pos=productPage.indexOf("|",pos)+1;
        return productPage.substring(pos);
    }


    public static void main(String[] args){


    }


}
