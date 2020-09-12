package com.ml;


import com.ml.utils.Counters;
import com.ml.utils.DatabaseHelper;
import com.ml.utils.HTMLParseUtils;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.ProductPageProcessor;

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
    static final String DATABASE="ML2";
    static int MAX_THREADS = 20;//14
    static final boolean SAVE=true;
    static final boolean DEBUG=false;
    static final int MINIMUM_SALES = 1;
    static final boolean FOLLOWING_DAY = false;
    static final boolean PRERVIOUS_DAY = true;
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
            this.ranking=10000;
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

        String[] webBaseUrls = {
                "https://hogar.mercadolibre.com.ar/adornos-decoracion-del/", //decoracion
                "https://hogar.mercadolibre.com.ar/adornos-decoracion-del-cuadros-carteles-espejos/",//espejos (no esta abajo)
                "https://hogar.mercadolibre.com.ar/textiles-decoracion-alfombras-carpetas/", //alfombras

                "https://hogar.mercadolibre.com.ar/articulos-limpieza-productos-limpiadores-alfombras/", //limpiadores alfombra
                "https://listado.mercadolibre.com.ar/lustramuebles_DisplayType_G", //lustramuebles
                "https://listado.mercadolibre.com.ar/rugbee_DisplayType_G",
                "https://listado.mercadolibre.com.ar/stp_DisplayType_G",
                "https://listado.mercadolibre.com.ar/blem_DisplayType_G",

                "https://listado.mercadolibre.com.ar/hafele_DisplayType_G",
                "https://listado.mercadolibre.com.ar/genoud_DisplayType_G",
                "https://hogar.mercadolibre.com.ar/fark_DisplayType_G",

        };


        String[] apiBaseUrls = {
                "https://api.mercadolibre.com/sites/MLA/search?category=MLA1631",   //decoracion
                "https://api.mercadolibre.com/sites/MLA/search?category=MLA2513",   //alfombras

                "https://api.mercadolibre.com/sites/MLA/search?category=MLA417311", //limpiadores de alfombras
                "https://api.mercadolibre.com/sites/MLA/search?q=lustramuebles",
                "https://api.mercadolibre.com/sites/MLA/search?q=rugbee",
                "https://api.mercadolibre.com/sites/MLA/search?q=stp",
                "https://api.mercadolibre.com/sites/MLA/search?q=blem",

                "https://api.mercadolibre.com/sites/MLA/search?q=hafele",
                "https://api.mercadolibre.com/sites/MLA/search?q=genoud",
                "https://api.mercadolibre.com/sites/MLA/search?q=fark"


        };

        int [][] intervals = {
                {0,22,25,29,36,49,50,59,65,69,74,79,80,89,94,99,100,110,119,120,129,130,139,140,149,
                150,159,160,169,170,179,180,189,190,198,199,200,210,219,220,224,225,230,239,240,249,250,259,260,269,270,
                279,280,289,290,298,299,300,315,320,329,333,340,349,350,359,360,369,370,379,380,389,390,398,399,400,410,
                419,420,429,430,439,440,449,450,459,460,469,470,479,480,481,489,490,498,499,500,509,510,519,520,525,529,
                535,545,549,550,560,570,579,580,584,585,589,590,598,599,600,610,619,620,629,630,639,640,649,650,659,660,
                669,670,679,680,689,690,698,699,700,710,720,729,730,739,740,748,749,750,760,770,779,780,789,790,798,799,
                800,819,820,829,830,840,849,850,860,870,879,880,889,890,898,899,900,909,910,919,920,927,930,939,
                940,949,950,960,970,979,980,989,990,998,999,1000,1030,1049,1050,1079,1080,1090,1099,1100,1140,1149,1150,
                1170,1180,1190,1198,1199,1200,1240,1249,1250,1280,1298,1299,1300,1330,1349,1350,1370,1380,1390,1398,1399,
                1400,1440,1449,1450,1480,1498,1499,1530,1540,1550,1560,1569,1570,1580,1588,1590,1600,1640,1649,1650,1659,
                1670,1680,1689,1690,1698,1699,1700,1720,1729,1730,1740,1749,1750,1759,1760,1769,1770,1779,1780,1789,1790,
                1798,1799,1800,1830,1849,1850,1889,1890,1899,1900,1949,1950,1989,1990,1998,1999,2000,2050,2070,2080,2090,
                2099,2100,2150,2190,2199,2200,2210,2250,2260,2270,2280,2290,2299,2300,2350,2398,2399,2400,2450,2480,2490,
                2499,2500,2550,2590,2599,2600,2650,2690,2699,2700,2749,2750,2790,2799,2800,2809,2810,2850,2890,2899,3000,
                3049,3050,3099,3100,3150,3190,3199,3200,3250,3290,3299,4000,4050,4090,4099,4100,4198,4199,4200,4220,4250,
                4290,4299,4300,4350,4399,4400,4450,4490,4499,4500,4550,4599,4600,4650,4699,4700,4750,4799,4800,4850,4899,
                4900,4950,4990,4999,5000,5099,5100,5199,5200,5299,5300,5399,5400,5499,5500,5599,5600,5699,5700,5799,5800,
                5900,5998,5999,6000,6100,6200,6400,6498,6499,6500,6600,6700,6800,6900,6998,6999,7000,7200,7400,7498,7499,
                7500,7700,7900,7998,7999,8000,8400,8498,8499,8500,8700,8900,8998,8999,9000,9400,9500,9600,9700,9800,9990,
                9999,10000,10500,10900,10999,11000,11500,11900,11999,12000,12500,12900,12999,13000,13500,13900,13999,14000,
                14500,14900,14990,14999,15000,15500,16000,16500,17000,17500,18000,18500,19000,19900,20000,21000,22000,
                23000,24000,25000,27000,29000,30000,35000,40000,40500,45000,49000,53000,70000,147483647},
                {0,350,500,700,900,1000,1300,1600,1900,2200,2500,3000,3500,4000,4500,5000,6000,7000
                ,8000,9000,10000,12000,14000,16000,18000,20000,25000,35000,50000,2147483647}, //alfombras

                {0,2147483647}, //limpieza de alfombras
                {0,2147483647}, //lustra muebles
                {0,2147483647}, //rugbee
                {0,499,600,900,1300,2000,2147483647}, //stp
                {0,2147483647}, //blem

                {0,300,650,1300,2500,7000,2147483647}, //hafele
                {0,150,200,300,400,500,700,900,1000,1300,1600,1900,2000,2500,5000,15000,2147483647}, //genoud
                {0,2147483647}, //fark
        };

        for (int i = 0; i < intervals.length; i++) { //validacion de intervalos
            for (int j = 1; j < intervals[i].length; j++) {
                if (intervals[i][j - 1] >= intervals[i][j]) {
                    System.out.println("Error en intervalo #" + i + " // " + intervals[i][j - 1] + "-" + intervals[i][j]);
                    System.exit(0);
                }
            }
        }

        CloseableHttpClient client = HttpUtils.buildHttpClient();
        HashMap<String, Item> itemHashMap = new HashMap<String, Item>();
        String usuario = "SOMOS_MAS";


        for (String webBaseUrl: webBaseUrls) {
            Logger.log("XXXXXXXXXXXXX Procesando nueava web url "+webBaseUrl);
            processItemsOnUrl(webBaseUrl, client, itemHashMap);
        }

        if (!ONLY_RELEVANT) {
            for (String apiBaseUrl: apiBaseUrls) {
                Logger.log("XXXXXXXXXX Procesando nueava api url "+apiBaseUrl);
                processItemsWithApi(apiBaseUrl, -1, -1, client, itemHashMap, usuario);
            }
        }

        for (int i=0; i<intervals.length; i++) {
            for (int j = 1; j < intervals[i].length; j++) {
                int since = intervals[i][j - 1] + 1;
                int upto = intervals[i][j];
                Logger.log("XXXXXXXXXX Procesando intervalo "+since+"-"+upto+" "+apiBaseUrls[i]);
                processItemsWithApi(apiBaseUrls[i], since, upto, client, itemHashMap, usuario);
            }
        }

        Logger.log("XXXXXXXXXX Agregando posibles pausados.  itemHashMap="+itemHashMap.size());
        addPossiblePaused(itemHashMap);

        //removemos lo que no nos interesa o ya fue procesado
        Logger.log("XXXXXXXXXX Purgando items 1. itemHashMap="+itemHashMap.size());
        ArrayList<String> incompleteList=purgeItemHashMap(itemHashMap);
        Logger.log("XXXXXXXXXX Completando items 1.  itemHashMap="+itemHashMap.size());
        completeWebItems(client, itemHashMap, incompleteList);
        Logger.log("XXXXXXXXXX Purgando items 2. itemHashMap="+itemHashMap.size());
        incompleteList=purgeItemHashMap(itemHashMap);
        if (incompleteList.size()>0){
            System.out.println("algo salio mal aca");
        }

        Logger.log("XXXXXXXXXX Purgando items 3. itemHashMap="+itemHashMap.size());
        purgeItemHashMap2(itemHashMap);

        Logger.log("XXXXXXXXXX Completando items 2. itemHashMap="+itemHashMap.size());
        completeSellerName(itemHashMap);

        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
        ArrayList<Thread> removeList=new ArrayList<Thread>();
        Logger.log("XXXXXXXXXX Procesando productos. itemHashMap="+itemHashMap.size());
        for (Item item : itemHashMap.values()){


            ProductPageProcessor ppp = new ProductPageProcessor(item.permalink,item.sellerId,item.page,item.ranking,SAVE,DEBUG,DATABASE,getGlobalDate(),false);
            threadArrayList.add(ppp);
            ppp.start();

            while (threadArrayList.size()>=MAX_THREADS){
                removeList.clear();
                for (Thread t : threadArrayList) {
                    if (!t.isAlive()) {
                        removeList.add(t);
                    }
                }
                for (Thread t : removeList) {
                    threadArrayList.remove(t);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Logger.log(e);
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

        String msg = "******************************************************\r\n"
                + Counters.getGlobalPageCount() + " paginas procesadas\r\n "
                + Counters.getGlobalProductCount() + " productos procesados\r\n "
                + Counters.getGlobalNewsCount() + " productos con novedades";
        System.out.println(msg);
        Logger.log(msg);

        boolean b=false;

    }

    private static void completeSellerName(HashMap<String, Item> itemHashMap) {
        for (Item item : itemHashMap.values()) {
            if (item.sellerName == null || item.sellerName.isEmpty()) {
                item.sellerName=DatabaseHelper.fetchSellerName(HTMLParseUtils.getFormatedId(item.id),DATABASE);
            }
        }
    }

    private static boolean addPossiblePaused(HashMap<String, Item> itemHashMap) {
        //XXXXXXXXXXXXXXXXXXXXXXXXXXXX posibles pausados
        ArrayList<String> proccessedItemsArrayList=new ArrayList<>();
        for (String itemId: itemHashMap.keySet()){
            String formatedItem=HTMLParseUtils.getFormatedId(itemId);
            proccessedItemsArrayList.add(formatedItem);
        }

        ArrayList<String> possiblyPausedProductList = DatabaseHelper.getPossiblePausedProducts(DATABASE,proccessedItemsArrayList,getGlobalDate());

        int totalItemsSofar=0;
        if (proccessedItemsArrayList!=null){
            totalItemsSofar=proccessedItemsArrayList.size();
        }
        String msg="posibles pausados: "+possiblyPausedProductList.size()+" de "+totalItemsSofar;
        System.out.println(msg);
        Logger.log(msg);

        for (String itemId: possiblyPausedProductList){
            Item item = new Item();
            item.id=HTMLParseUtils.getUnformattedId(itemId);
            itemHashMap.put(item.id,item);
        }
        return false;
    }


    private static void purgeItemHashMap2(HashMap<String, Item> itemHashMap) {
        ArrayList<String> removeList= new ArrayList<String>();
        for (Item item: itemHashMap.values()) {
            String formatedId = getFormatedId(item);
            int totalSold=DatabaseHelper.fetchTotalSold(formatedId,DATABASE);
            if (totalSold>0) {
                if (item.totalSold < 5) {
                    if (totalSold == item.totalSold) {
                        removeList.add(item.id);
                    }
                } else {
                    int topOfRange = getTopOfRange(item.totalSold);
                    if (totalSold == topOfRange) {
                        removeList.add(item.id);
                    }
                }
            }
        }
        for (String itemId: removeList){
            itemHashMap.remove(itemId);
        }
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
                    int code = itemObject2.getInt("code");
                    if (code!=200){
                        continue; //nunca paso
                    }
                    JSONObject productObj = itemObject2.getJSONObject("body");
                    String id  = productObj.getString("id");
                    Item item= itemHashMap.get(id);
                    boolean completed = completeItem(productObj,item);
                    if (!completed){
                        itemHashMap.remove(id);
                    }
                }
            }
        }
    }

    private static ArrayList<String>  purgeItemHashMap(HashMap<String, Item> itemHashMap) {
        ArrayList<String> removeList= new ArrayList<String>();
        ArrayList<String> incompleteList= new ArrayList<String>();
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
            if (item.totalSold==-1) {
                incompleteList.add(item.id);
            }
        }
        for (String itemId: removeList){
            itemHashMap.remove(itemId);
        }
        return incompleteList;
    }

    private static String getFormatedId(Item item) {
        return HTMLParseUtils.getFormatedId(item.id);
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
                if (item.ranking==10000) {
                    item.ranking = ranking;
                }
                if (rankingOnly) {
                    continue;
                }


                boolean completed = completeItem(productObj,item);
                if (!completed){
                    itemHashMap.remove(id);
                }
            }

            offset+=50;
            apiSearchUrl= apiBaseUrl +"&offset="+offset;
        }
    }

    private static boolean completeItem(JSONObject productObj, Item item) {

        if (!productObj.has("sold_quantity")){
            return false;//under review y otros casos raros
        }

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
        return true;
    }

}
