package com.ml;


import com.ml.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;



public class Mercadolibre02b {

    static final int RESULTS_WITHOUT_TOKEN =1000;
    static final int RESULTS_LIMIT =10000;
    static final String DATABASE="ML6";
    static final int MINIMUM_SALES = 1;
    static final boolean FOLLOWING_DAY = false;
    static final boolean PRERVIOUS_DAY = false;
    static final boolean ONLY_RELEVANT = false;



    static  int requestCount=0;
    static Date globalDate = null;

    static class Item {
        String id;
        String title;
        String permalink;
        double price;
        int discount;
        boolean advertised;
        boolean officialStore;
        int shipping;
        boolean premium;
        int ranking;
        int page;
        int totalSold;
        String sellerName;
        int sellerId;

        Item () {
            this.advertised=false;
            this.ranking=-1;
            this.totalSold=-1;
            this.price=-1.0;
            this.discount=-1;
            this.sellerId=-1;
        }

        public boolean equals(Object obj){
            if (this.id== null || this.id.isEmpty()){
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Item)){
                return false;
            }
            Item otherItem = (Item)obj;
            if (otherItem.id== null || otherItem.id.isEmpty()){
                return false;
            }
            return this.id.equals(otherItem.id);
        }

    }


    public static void main(String[] args) {
/*
        String apiBaseUrl = "https://api.mercadolibre.com/sites/MLA/search?category=MLA2513";
        String webBaseUrl = "https://hogar.mercadolibre.com.ar/textiles-decoracion-alfombras-carpetas/";
        int[] intervals = new int[]{0,350,500,700,900,1000,1300,1600,1900,2200,2500,3000,3500,4000,4500,5000,6000,7000
                ,8000,9000,10000,12000,14000,16000,18000,20000,25000,35000,50000,2147483647};
                */

        //todo agregar una validacion de intervalos
/*

        String apiBaseUrl = "https://api.mercadolibre.com/sites/MLA/search?category=MLA1182";
        String webBaseUrl = "https://instrumentos.mercadolibre.com.ar/";
        int[] intervals = new int[]{0,4000,25000,2147483647};
*/

        String apiBaseUrl = "https://api.mercadolibre.com/sites/MLA/search?category=MLA1631";
        String webBaseUrl = "https://hogar.mercadolibre.com.ar/adornos-decoracion-del/";
        int[] intervals = new int[]{0,22,25,29,36,49,50,59,65,69,74,79,80,89,94,99,100,110,119,120,129,130,139,140,149,
                150,159,160,169,170,179,180,189,190,198,199,200,210,219,220,224,225,230,239,240,249,250,259,260,269,270,
                279,280,289,290,298,299,300,315,320,329,333,340,349,350,359,360,369,370,379,380,389,390,398,399,400,410,
                419,420,429,430,439,440,449,450,459,460,469,470,479,480,481,489,490,498,499,500,509,510,519,520,525,529,
                535,545,549,550,560,570,579,580,584,585,589,590,598,599,600,610,619,620,629,630,639,640,649,650,659,660,
                669,670,679,680,689,690,698,699,700,710,720,729,730,739,740,748,749,750,760,770,779,780,789,790,798,799,
                800,819,820,829,830,840,849,850,860,870,879,880,889,890,898,899,900,909,910,919,920,927,930,939,
                940,949,950,960,970,979,980,989,990,998,999,1000,1030,1049,1050,1079,1080,1090,1099,1100,1140,1149,1150,
                1170,1180,1190,1198,1199,1200,1240,1249,1250,1280,1298,1299,1300,1330,1349,1350,1370,1380,1390,1398,1399,
                1400,1440,1449,1450,1480,1498,1499,1530,1540,1550,1560,1569,1570,1580,1588,1590,1600,1640,1649,1650,147483647};

        for (int j=1; j<intervals.length; j++) {
            if (intervals[j - 1] >= intervals[j]) {
                System.out.println("Error en intervalo #" + j + " // " + intervals[j - 1] + "-" + intervals[j]);
                System.exit(0);
            }
        }


        CloseableHttpClient client = HttpUtils.buildHttpClient();
        HashMap<String, Item> itemHashMap = new HashMap<String, Item>();
        String usuario = "SOMOS_MAS";

        processItemsOnUrl(webBaseUrl, client, itemHashMap);

        if (!ONLY_RELEVANT) {
            processItemsWithApi(apiBaseUrl, -1, -1, client, itemHashMap, usuario);
        }

        for (int i=1; i<intervals.length; i++) {
            int since = intervals[i - 1] + 1;
            int upto = intervals[i];
            processItemsWithApi(apiBaseUrl, since, upto, client, itemHashMap, usuario);
        }

        //removemos lo que no nos interesa o ya fue procesado
        ArrayList<String> incompleteList=purgeItemHashMap(itemHashMap);
        completeWebItems(client, itemHashMap, incompleteList);
        incompleteList=purgeItemHashMap(itemHashMap);
        if (incompleteList.size()>0){
            System.out.println("algo salio mal aca");
        }

        int needsNewRequest=0;
        ArrayList<String> removeList= new ArrayList<String>();
        for (Item item: itemHashMap.values()) {
            String formatedId = getFormatedId(item);
            int totalSold=DatabaseHelper.fetchTotalSold(formatedId,DATABASE);
            if (item.totalSold<5){
                if (totalSold==item.totalSold){
                    removeList.add(item.id);
                }
            }else {
                int topOfRange=getTopOfRange(item.totalSold);
                if (totalSold==topOfRange){
                    removeList.add(item.id);
                }else {
                    needsNewRequest++;
                }
            }
        }
        for (String itemId: removeList){
            itemHashMap.remove(itemId);
        }


        for (Item item : itemHashMap.values()){
            System.out.println(
            item.id+","+item.title+","+item.permalink+","+item.price+","+item.discount+","+item.advertised+","+item.officialStore+","+
            item.shipping+","+item.premium+","+item.ranking+","+item.page+","+item.totalSold+","+item.sellerName+","+item.sellerId);
        }

        boolean b=false;

    }


    private static int getTopOfRange(int totalSold){
        int result =-1;
        if (totalSold==5){
            result=25;
        } else {
            if (totalSold==25){
                result=50;
            } else {
                if (totalSold==50){
                    result=100;
                } else {
                    if (totalSold==100){
                        result=150;
                    } else {
                        if (totalSold==150){
                            result=200;
                        } else {
                            if (totalSold==200){
                                result=250;
                            } else {
                                if (totalSold==250){
                                    result=500;
                                } else {
                                    if (totalSold==500){
                                        result=5000;
                                    } else {
                                        if (totalSold==5000){
                                            result=50000;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private static void completeWebItems(CloseableHttpClient client, HashMap<String, Item> itemHashMap, ArrayList<String> incompleteList) {
        boolean processFinished=false;
        int i=-1;
        while (!processFinished){
            String itemsIds="";
            for (int j=0; j<20; j++) {
                i++;
                if (i== incompleteList.size()){
                    processFinished=true;
                    break;
                }
                itemsIds+= incompleteList.get(i)+",";
            }
            if (itemsIds.contains(",")) {
                itemsIds=itemsIds.substring(0,itemsIds.length()-1);
                String itemsUrl = "https://api.mercadolibre.com/items?ids="+itemsIds;
                JSONObject jsonObject = HttpUtils.getJsonObjectWithoutToken(itemsUrl, client, true);
                JSONArray jsonArray = jsonObject.getJSONArray("elArray");
                for (int j=0; j<jsonArray.length(); j++){
                    JSONObject itemObject2 = jsonArray.getJSONObject(j);
                    JSONObject productObj = itemObject2.getJSONObject("body");
                    String id  = productObj.getString("id");
                    Item item= itemHashMap.get(id);
                    completeItem(productObj,item);
                    boolean b= false;
                }
            }
        }
    }

    private static ArrayList<String>  purgeItemHashMap(HashMap<String, Item> itemHashMap) {
        ArrayList<String> removeList= new ArrayList<String>();
        for (Item item: itemHashMap.values()){
            if (item.totalSold>-1 && item.totalSold<MINIMUM_SALES){
                removeList.add(item.id);
                continue;
            }
            String formatedId = getFormatedId(item);
            Date lastUpdate = DatabaseHelper.fetchLastUpdate(formatedId,DATABASE);
            if (lastUpdate != null) {//producto existente
                boolean sameDate = Counters.isSameDate(lastUpdate, getGlobalDate());
                if (sameDate){
                    removeList.add(item.id);
                }
            }
        }
        for (String itemId: removeList){
            itemHashMap.remove(itemId);
        }
        ArrayList<String> incompleteList= new ArrayList<String>();
        for (Item item: itemHashMap.values()) {
            if (item.page > 0 && item.sellerId == -1) {
                incompleteList.add(item.id);
            }
        }
        return incompleteList;
    }

    private static String getFormatedId(Item item) {
        String formatedId= item.id.substring(0,3)+"-"+ item.id.substring(3);
        return formatedId;
    }


    private synchronized static Date getGlobalDate(){
        if (globalDate==null){
            Calendar cal = Calendar.getInstance();
            long milliseconds = cal.getTimeInMillis();
            long oneDayinMiliseconds=0;
            if (FOLLOWING_DAY){
                oneDayinMiliseconds=86400000; //this will add a complete day on milliseconds
            }
            if (PRERVIOUS_DAY){
                oneDayinMiliseconds=-86400000; //this will add a complete day on milliseconds
            }
            Date date = new Date(milliseconds+oneDayinMiliseconds);
            globalDate=date;
        }
        return globalDate;
    }


    private static void processItemsOnUrl(String webBaseUrl, CloseableHttpClient client, HashMap<String, Item> itemHashMap) {
        int page = 0;
        int ITEMS_PER_PAGE = 48;
        boolean DEBUG = false;
        boolean processFinished = false;


        while (!processFinished){
            page++;
            if (page==43){
                processFinished=true;
                continue;
            }

            Counters.incrementGlobalPageCount();
            int since = (page - 1) * ITEMS_PER_PAGE + 1;
            String sinceStr = "_Desde_" + since;
            String uRL = webBaseUrl + sinceStr;
            uRL += "_DisplayType_G";

            String htmlStringFromPage = HttpUtils.getHTMLStringFromPage(uRL, client, DEBUG, false);
            requestCount++;
            if (!HttpUtils.isOK(htmlStringFromPage)) { //suponemos que se terminó
                // pero tambien hacemos pausa por si es problema de red
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Logger.log(e);
                }
                Logger.log("AA hmlstring from page is null " + uRL);
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                client = null;
                client = HttpUtils.buildHttpClient();
                continue;
            }

            htmlStringFromPage = htmlStringFromPage.toString();
            int resultSectionPos = htmlStringFromPage.indexOf("search-results");
            String resultListHMTLData = null;
            if (resultSectionPos == -1) {
                if (htmlStringFromPage.indexOf("Escrib") > 0
                        && htmlStringFromPage.indexOf("en el buscador lo que quer") > 0
                        && htmlStringFromPage.indexOf("s encontrar") > 0) {
                    String msg = "No se pudo obtener ningun resultado en este intervalo " + uRL;
                    Logger.log(msg);
                    continue;
                }
                Logger.log("Error getting search-results tag on page " + page + " " + uRL);
                Logger.log(htmlStringFromPage);
                resultListHMTLData = htmlStringFromPage;
            } else {
                resultListHMTLData = htmlStringFromPage.substring(resultSectionPos);
            }

            String[] allHrefsOnPage = StringUtils.substringsBetween(resultListHMTLData, "<a href", "</a>");
            if (allHrefsOnPage == null) { //todo check
                System.out.println("this page has no Hrefs !!! " + allHrefsOnPage);
                Logger.log("this page has no Hrefs !!!" + allHrefsOnPage);
                continue;
            }

            ArrayList<String> productsURLArrayList = new ArrayList();
            for (String href : allHrefsOnPage) {
                if (href.indexOf(HTMLParseUtils.ARTICLE_PREFIX) > 0 && href.indexOf("-_JM") > 0) {
                    href = href.substring(href.indexOf("http"), href.indexOf("-_JM")) + "-_JM";
                    if (!productsURLArrayList.contains(href)) {
                        productsURLArrayList.add(href);
                    }
                }
            }
            int productsOnPage = productsURLArrayList.size();
            if (productsOnPage < 47) {//EL NUMERO ES 47 TODO VER QUE PASA CON LOS ITEMS QUE TIENEN URL DISTINTA
                processFinished=true;
            }

            for (String productUrl : productsURLArrayList) {
                Counters.incrementGlobalProductCount();
                Item item=null;
                String productId = HTMLParseUtils.getProductIdFromURL(productUrl);
                productId=productId.substring(0,3)+productId.substring(4);
                if (itemHashMap.containsKey(productId)){
                    item= itemHashMap.get(productId);
                }else {
                    item=new Item();
                    item.id=productId;
                    itemHashMap.put(productId,item);
                }

                item.page=page;
                item.permalink=productUrl;

                int initPoint = resultListHMTLData.indexOf(productUrl);
                int nextPoint = resultListHMTLData.length();//just for the last item #48 o #50 depending on the page layout

                String productHTMLdata = null;
                int nextItem = productsURLArrayList.indexOf(productUrl) + 1;
                if (nextItem < productsURLArrayList.size()) {
                    String nextURL = productsURLArrayList.get(nextItem);
                    nextPoint = resultListHMTLData.indexOf(nextURL);
                }

                productHTMLdata = resultListHMTLData.substring(initPoint, nextPoint);
                if (productHTMLdata != null) {
                    productHTMLdata = productHTMLdata.toString(); //aca le sacamos los caracteres de control que impiden hacer los search dentro del string
                }

                item.title = HTMLParseUtils.getTitle2(productHTMLdata);
                if (item.title != null) {
                    item.title = item.title.trim();
                }
                if (item.title==null || item.title.length()==0) {
                    Logger.log("AA invalid title on page " + page + " url " + uRL);
                }

                item.discount = HTMLParseUtils.getDiscount2(productHTMLdata);
                if (item.discount<0){
                    Logger.log("AA I couldn't get the discount on " + productUrl);
                }

                item.shipping=HTMLParseUtils.getShipping(productHTMLdata);

                item.premium=HTMLParseUtils.getPremium(productHTMLdata);

                item.price = HTMLParseUtils.getPrice2(productHTMLdata);
                if (item.price==0){
                    Logger.log("AA I couldn't get the price on " + productUrl);
                }

                item.advertised = productHTMLdata.contains("Promocionado"); //todo check

            }

        }
    }

    private static void processItemsWithApi(String apiBaseUrl, int since, int upto, CloseableHttpClient client, HashMap<String, Item> itemHashMap, String usuario) {
        if (since>=0){
            apiBaseUrl+="&price="+since+"-"+upto;
        }

        String apiSearchUrl= apiBaseUrl;

        int offset=0;
        int ranking=0;
        int totalResults=-1; //valor inicial que se reemplazará

        JSONObject jsonObject=null;
        boolean rankingOnly=since==-1;

        while (offset<totalResults || totalResults==-1){
            if (offset< RESULTS_WITHOUT_TOKEN) {
                jsonObject = HttpUtils.getJsonObjectWithoutToken(apiSearchUrl, client, false);
                if (totalResults==-1){
                    JSONObject pagingObject = jsonObject.getJSONObject("paging");
                    totalResults=pagingObject.getInt("total");
                    if (totalResults> RESULTS_LIMIT){
                        totalResults= RESULTS_LIMIT;
                    }
                }
            }else {
                if (ONLY_RELEVANT){
                    return;
                }
                String msg="No se pudo recorrer el intervalo por completo sin token "+apiSearchUrl;
                Logger.log(msg);
                jsonObject = HttpUtils.getJsonObjectUsingToken(apiSearchUrl, client,usuario);
            }
            requestCount++;

            JSONArray resultsArray = jsonObject.getJSONArray("results");
            for (int i=0; i<resultsArray.length(); i++){
                JSONObject productObj=resultsArray.getJSONObject(i);
                Item item=null;
                String id  = productObj.getString("id");
                if (itemHashMap.containsKey(id)){
                    item= itemHashMap.get(id);
                }else {
                    item=new Item();
                    item.id=id;
                    itemHashMap.put(id,item);
                }


                ranking++;
                if (item.ranking==-1) {
                    item.ranking = ranking;
                }
                    if (rankingOnly) {
                    continue;
                }
                completeItem(productObj, item);

            }

            offset+=50;
            apiSearchUrl= apiBaseUrl +"&offset="+offset;
        }
    }

    private static void completeItem(JSONObject productObj, Item item) {
        item.totalSold= productObj.getInt("sold_quantity");

        if (item.permalink==null || item.permalink.isEmpty()){
            item.permalink= productObj.getString("permalink");
        }
        if (item.title==null || item.title.isEmpty()){
            item.title= productObj.getString("title");
        }
        if (item.price<=0){
            item.price= productObj.getDouble("price");
        }
        if (item.discount<0){
            item.discount=0;
            if (productObj.has("original_price") && !productObj.isNull("original_price")){
                double originalPrice= productObj.getDouble("original_price");
                if (item.price>0 && originalPrice>0 && originalPrice> item.price){
                    item.discount=(int)((originalPrice- item.price)/originalPrice*100.0);
                }

            }
        }

        item.shipping=0;
        if (productObj.has("shipping") && !productObj.isNull("shipping")){
            JSONObject shippingObject = productObj.getJSONObject("shipping");
            String shippingMode = shippingObject.getString("mode");
            if (shippingMode.equals("me2")){
                item.shipping=100;
            }
            if (shippingObject.has("free_shipping") && !shippingObject.isNull("free_shipping")) {
                boolean freeShipping = shippingObject.has("free_shipping");
                if (freeShipping){
                    item.shipping=200;
                }
            }

        }

        String listingTypeId = productObj.getString("listing_type_id");
        if (listingTypeId.equals("gold_pro")){
            item.premium=true;
        }else {
            item.premium=false;
        }

        item.officialStore=false;
        if (productObj.has("official_store_id") && !productObj.isNull("official_store_id")){
            item.officialStore=true;
        }

        if (productObj.has("seller") && !productObj.isNull("seller")) {
            JSONObject sellerObject = productObj.getJSONObject("seller");
            if (sellerObject.has("permalink") && !sellerObject.isNull("permalink")) {
                String sellerPermalink = sellerObject.getString("permalink");
                item.sellerName = sellerPermalink.substring(sellerPermalink.lastIndexOf("/") + 1);
            }
            item.sellerId = sellerObject.getInt("id");
        }else {
            if (productObj.has("seller_id") && !productObj.isNull("seller_id")) {
                item.sellerId = productObj.getInt("seller_id");
            }
        }
    }

}
