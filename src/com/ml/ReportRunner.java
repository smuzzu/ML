package com.ml;

import com.ml.utils.Counters;
import com.ml.utils.DatabaseHelper;
import com.ml.utils.HTMLParseUtils;
import com.ml.utils.HttpUtils;
import com.ml.utils.Item;
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
import java.util.Collections;
import java.util.HashMap;

public class ReportRunner {

    static final int RESULTS_WITHOUT_TOKEN = 1000;
    static final int RESULTS_LIMIT = 10000;

    static int MAX_THREADS = 50;//14
    static final boolean SAVE = true;
    static final boolean DEBUG = false;
    static final int MINIMUM_SALES = 1;
    static final boolean FOLLOWING_DAY = false;
    static final boolean PREVIOUS_DAY = true;
    static final boolean IGNORE_VISITS = false;

    static final boolean REBUILD_INTERVALS = false;
    static final int MAX_INTERVAL_SIZE = 800;


    static int requestCount = 0;
    static Date globalDate = null;

    protected static int[] buildIntervals(String url1, int maxItemsInRange, CloseableHttpClient client) {
        ArrayList<Integer> intervals = new ArrayList<Integer>();
        rebuildInterval(url1,0,Integer.MAX_VALUE,intervals,maxItemsInRange, client);
        Collections.sort(intervals);

        ArrayList<Integer> removelist=new ArrayList<Integer>();
        for (int i = 1; i < (intervals.size() - 1); i++) {
            int since = intervals.get(i - 1);
            int to = intervals.get(i + 1);
            String newurl = url1 + "&price=" + since + "-" + to;
            JSONObject itemListObject = HttpUtils.getJsonObjectWithoutToken(newurl, client, false);
            JSONObject pagingObject = itemListObject.getJSONObject("paging");
            int total = pagingObject.getInt("total");
            if (total == 0 ) {
                removelist.add(intervals.get(i));
            }
        }
        for (Integer itemToRemove: removelist){
            intervals.remove(itemToRemove);
        }
        removelist.clear();


        boolean itemRemoved=true;
        while (itemRemoved) {
            for (int i = 1; i < (intervals.size() - 1); i++) {
                int since = intervals.get(i - 1) + 1;
                int to = intervals.get(i + 1);
                String newurl = url1 + "&price=" + since + "-" + to;
                JSONObject itemListObject = HttpUtils.getJsonObjectWithoutToken(newurl, client, false);
                JSONObject pagingObject = itemListObject.getJSONObject("paging");
                int total = pagingObject.getInt("total");
                if (total < maxItemsInRange) {
                    removelist.add(intervals.get(i));
                    i++;
                }
            }
            itemRemoved=removelist.size()>0;
            for (Integer itemToRemove: removelist){
                intervals.remove(itemToRemove);
            }
            removelist.clear();
        }

        if (!intervals.contains(0)){
            intervals.add(0);
        }
        Collections.sort(intervals);
        if (!intervals.contains(Integer.MAX_VALUE)){
            if (intervals.size()>1) {
                intervals.remove(intervals.size()-1);//volamos el ultimo
            }
            intervals.add(Integer.MAX_VALUE);
        }
        Collections.sort(intervals);

        int[] intArray = new int[intervals.size()];
        for (int j=0; j < intArray.length; j++)
        {
            intArray[j] = intervals.get(j).intValue();
        }



        String intervalStr=url1+" -> {";
        for (int i = 0; i < intervals.size(); i++) {
            intervalStr+=intervals.get(i)+",";
        }
        intervalStr=intervalStr.substring(0,intervalStr.length()-1)+"}";
        System.out.println(intervalStr);
        Logger.log(intervalStr);


        for (int i = 1; i < intervals.size(); i++) {
            int since = intervals.get(i - 1) + 1;
            int to = intervals.get(i);
            String newurl = url1 + "&price=" + since + "-" + to;
            JSONObject itemListObject = HttpUtils.getJsonObjectWithoutToken(newurl, client, false);
            JSONObject pagingObject = itemListObject.getJSONObject("paging");
            int total = pagingObject.getInt("total");
            if (total >maxItemsInRange) {
                Logger.log("XXXXXXXXXXXX "+since+"-"+to+" = " + total);
            }else {
                Logger.log(""+since+"-"+to+" = " + total);
            }

        }
        return intArray;
    }

    private static void rebuildInterval(String url, int since, int to, ArrayList<Integer> valuesArrayList,
                                        int maxItemsInRange, CloseableHttpClient client){
        String newurl=url+"&price="+since+"-"+to;
        JSONObject itemListObject = HttpUtils.getJsonObjectWithoutToken(newurl,client,false);
        JSONObject pagingObject = itemListObject.getJSONObject("paging");
        int total = pagingObject.getInt("total");
        if (total>800 && since<to){
            int intermediateValue=((to-since)/2)+since;
            if (!valuesArrayList.contains(intermediateValue)){
                valuesArrayList.add(intermediateValue);
                rebuildInterval(url,since,intermediateValue,valuesArrayList,maxItemsInRange,client);
                rebuildInterval(url,intermediateValue,to,valuesArrayList,maxItemsInRange,client);
            }
        }
        boolean b=false;
    }

    private static void completeSellerName(HashMap<String, Item> itemHashMap, String DATABASE) {
        for (Item item : itemHashMap.values()) {
            if (item.sellerName == null || item.sellerName.isEmpty()) {
                item.sellerName = DatabaseHelper.fetchSellerName(HTMLParseUtils.getFormatedId(item.id), DATABASE);
            }
        }
    }

    protected static boolean addPossiblePaused(HashMap<String, Item> itemHashMap, String DATABASE) {
        //XXXXXXXXXXXXXXXXXXXXXXXXXXXX posibles pausados
        ArrayList<String> proccessedItemsArrayList = new ArrayList<>();
        for (String itemId : itemHashMap.keySet()) {
            String formatedItem = HTMLParseUtils.getFormatedId(itemId);
            proccessedItemsArrayList.add(formatedItem);
        }

        ArrayList<String> possiblyPausedProductList = DatabaseHelper.getPossiblePausedProducts(DATABASE, proccessedItemsArrayList, getGlobalDate());

        int totalItemsSofar = 0;
        if (proccessedItemsArrayList != null) {
            totalItemsSofar = proccessedItemsArrayList.size();
        }
        String msg = "posibles pausados: " + possiblyPausedProductList.size() + " de " + totalItemsSofar;
        System.out.println(msg);
        Logger.log(msg);

        for (String itemId : possiblyPausedProductList) {
            Item item = new Item();
            item.id = HTMLParseUtils.getUnformattedId(itemId);
            itemHashMap.put(item.id, item);
        }
        return false;
    }


    private static void purgeItemHashMap2(HashMap<String, Item> itemHashMap, String DATABASE) {
        ArrayList<String> removeList = new ArrayList<String>();
        for (Item item : itemHashMap.values()) {
            String formatedId = getFormatedId(item);
            int totalSold = DatabaseHelper.fetchTotalSold(formatedId, DATABASE);
            if (totalSold > 0) {
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
        for (String itemId : removeList) {
            itemHashMap.remove(itemId);
        }
    }


    private static int getTopOfRange(int totalSold) {
        int result = -1;
        if (totalSold == 5) {
            result = 25;
        } else {
            if (totalSold == 25) {
                result = 50;
            } else {
                if (totalSold == 50) {
                    result = 100;
                } else {
                    if (totalSold == 100) {
                        result = 150;
                    } else {
                        if (totalSold == 150) {
                            result = 200;
                        } else {
                            if (totalSold == 200) {
                                result = 250;
                            } else {
                                if (totalSold == 250) {
                                    result = 500;
                                } else {
                                    if (totalSold == 500) {
                                        result = 5000;
                                    } else {
                                        if (totalSold == 5000) {
                                            result = 50000;
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
        boolean processFinished = false;
        int i = -1;
        while (!processFinished) {
            String itemsIds = "";
            for (int j = 0; j < 20; j++) {
                i++;
                if (i == incompleteList.size()) {
                    processFinished = true;
                    break;
                }
                itemsIds += incompleteList.get(i) + ",";
            }
            if (itemsIds.contains(",")) {
                itemsIds = itemsIds.substring(0, itemsIds.length() - 1);
                String itemsUrl = "https://api.mercadolibre.com/items?ids=" + itemsIds;
                JSONObject jsonObject = HttpUtils.getJsonObjectWithoutToken(itemsUrl, client, true);
                JSONArray jsonArray = jsonObject.getJSONArray("elArray");
                for (int j = 0; j < jsonArray.length(); j++) {
                    JSONObject itemObject2 = jsonArray.getJSONObject(j);
                    int code = itemObject2.getInt("code");
                    if (code != 200) {
                        continue; //nunca paso
                    }
                    JSONObject productObj = itemObject2.getJSONObject("body");
                    String id = productObj.getString("id");
                    Item item = itemHashMap.get(id);
                    if (item==null){
                        Logger.log("Item is nul.  Why? "+id);
                        continue;
                    }
                    boolean completed = completeItem(productObj, item);
                    if (!completed) {
                        itemHashMap.remove(id);
                    }
                }
            }
        }
    }

    private static ArrayList<String> purgeItemHashMap(HashMap<String, Item> itemHashMap, String DATABASE) {
        ArrayList<String> removeList = new ArrayList<String>();
        ArrayList<String> incompleteList = new ArrayList<String>();
        for (Item item : itemHashMap.values()) {
            if (item.totalSold > -1 && item.totalSold < MINIMUM_SALES) {
                removeList.add(item.id);
                continue;
            }
            String formatedId = getFormatedId(item);
            Date lastUpdate = DatabaseHelper.fetchLastUpdate(formatedId, DATABASE);
            if (lastUpdate != null) {//producto existente
                boolean sameDate = Counters.isSameDate(lastUpdate, getGlobalDate());
                if (sameDate) {
                    removeList.add(item.id);
                }
            }
            if (item.totalSold == -1) {
                incompleteList.add(item.id);
            }
        }
        for (String itemId : removeList) {
            itemHashMap.remove(itemId);
        }
        return incompleteList;
    }

    private static String getFormatedId(Item item) {
        return HTMLParseUtils.getFormatedId(item.id);
    }


    private synchronized static Date getGlobalDate() {
        if (globalDate == null) {
            Calendar cal = Calendar.getInstance();
            long milliseconds = cal.getTimeInMillis();
            long oneDayinMiliseconds = 0;
            if (FOLLOWING_DAY) {
                oneDayinMiliseconds = 86400000; //this will add a complete day on milliseconds
            }
            if (PREVIOUS_DAY) {
                oneDayinMiliseconds = -86400000; //this will add a complete day on milliseconds
            }
            Date date = new Date(milliseconds + oneDayinMiliseconds);
            globalDate = date;
        }
        return globalDate;
    }


    private static void processItemsOnUrl(String webBaseUrl, CloseableHttpClient client, HashMap<String, Item> itemHashMap) {
        int page = 0;
        int ITEMS_PER_PAGE = 48;
        boolean DEBUG = false;
        boolean processFinished = false;


        while (!processFinished) {
            page++;
            if (page == 43) {
                processFinished = true;
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
                if (href.indexOf(HTMLParseUtils.ARTICLE_PREFIX) > 0 ){
                    if (href.indexOf("-_JM") > 0) {
                        href = href.substring(href.indexOf("http"), href.indexOf("-_JM")) + "-_JM";
                        if (!productsURLArrayList.contains(href)) {
                            productsURLArrayList.add(href);
                        }
                    }else {
                        if (href.indexOf("/p/"+HTMLParseUtils.ARTICLE_PREFIX)>0){
                            href = href.substring(href.indexOf("http"), href.indexOf("?"));
                            if (!productsURLArrayList.contains(href)) {
                                productsURLArrayList.add(href);
                            }
                        }
                    }
                }
            }
            int productsOnPage = productsURLArrayList.size();
            if (productsOnPage < ITEMS_PER_PAGE) {
                processFinished = true;
            }

            for (String productUrl : productsURLArrayList) {
                Counters.incrementGlobalProductCount();

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

                Item item = null;
                String productId = HTMLParseUtils.getProductIdFromHtmldata(productHTMLdata);
                if (productId==null){
                    String msg="No se pudo recuperar item ID \n"+uRL+"\n"+productUrl;
                    continue;
                }
                if (itemHashMap.containsKey(productId)) {
                    item = itemHashMap.get(productId);
                } else {
                    item = new Item();
                    item.id = productId;
                    itemHashMap.put(productId, item);
                }

                item.page = page;
                item.permalink = productUrl;


                item.title = HTMLParseUtils.getTitle2(productHTMLdata);
                if (item.title != null) {
                    item.title = item.title.trim();
                }
                if (item.title == null || item.title.length() == 0) {
                    Logger.log("AA invalid title on page " + page + " url " + uRL);
                }

                item.discount = HTMLParseUtils.getDiscount2(productHTMLdata);
                if (item.discount < 0) {
                    Logger.log("AA I couldn't get the discount on " + productUrl);
                }

                item.shipping = HTMLParseUtils.getShipping(productHTMLdata);

                item.premium = HTMLParseUtils.getPremium(productHTMLdata);

                item.price = HTMLParseUtils.getPrice2(productHTMLdata);
                if (item.price == 0) {
                    Logger.log("AA I couldn't get the price on " + productUrl);
                }

                item.advertised = productHTMLdata.contains("Promocionado"); //todo check

            }

        }
    }

    private static boolean completeItem(JSONObject productObj, Item item) {

        if (!productObj.has("sold_quantity")) {
            return false;//under review y otros casos raros
        }

        item.totalSold = productObj.getInt("sold_quantity");

        if (item.permalink == null || item.permalink.isEmpty()) {
            item.permalink = productObj.getString("permalink");
        }
        if (item.title == null || item.title.isEmpty()) {
            item.title = productObj.getString("title");
        }
        if (item.price <= 0) {
            if (productObj.has("price") && !productObj.isNull("price")) {
                item.price = productObj.getDouble("price");
            }else {
                item.price=0.01;
            }
        }
        if (item.discount < 0) {
            item.discount = 0;
            if (productObj.has("original_price") && !productObj.isNull("original_price")) {
                double originalPrice = productObj.getDouble("original_price");
                if (item.price > 0 && originalPrice > 0 && originalPrice > item.price) {
                    item.discount = (int) ((originalPrice - item.price) / originalPrice * 100.0);
                }

            }
        }

        item.shipping = 0;
        if (productObj.has("shipping") && !productObj.isNull("shipping")) {
            JSONObject shippingObject = productObj.getJSONObject("shipping");
            String shippingMode = shippingObject.getString("mode");
            if (shippingMode.equals("me2")) {
                item.shipping = 100;
            }
            if (shippingObject.has("free_shipping") && !shippingObject.isNull("free_shipping")) {
                boolean freeShipping = shippingObject.has("free_shipping");
                if (freeShipping) {
                    item.shipping = 200;
                }
            }

        }

        String listingTypeId = productObj.getString("listing_type_id");
        if (listingTypeId.equals("gold_pro")) {
            item.premium = true;
        } else {
            item.premium = false;
        }

        item.officialStore = false;
        if (productObj.has("official_store_id") && !productObj.isNull("official_store_id")) {
            item.officialStore = true;
        }

        if (productObj.has("seller") && !productObj.isNull("seller")) {
            JSONObject sellerObject = productObj.getJSONObject("seller");
            if (sellerObject.has("permalink") && !sellerObject.isNull("permalink")) {
                String sellerPermalink = sellerObject.getString("permalink");
                item.sellerName = sellerPermalink.substring(sellerPermalink.lastIndexOf("/") + 1);
            }
            item.sellerId = sellerObject.getInt("id");
        } else {
            if (productObj.has("seller_id") && !productObj.isNull("seller_id")) {
                item.sellerId = productObj.getInt("seller_id");
            }
        }
        return true;
    }

    protected static void runWeeklyReport(String[] webBaseUrls, String[] apiBaseUrls, int[][] intervals, CloseableHttpClient client, String usuario, String DATABASE, boolean ONLY_RELEVANT) {
        getGlobalDate(); //seteamos al principio de la corrida

        if (REBUILD_INTERVALS){
            System.out.println("Reconstruyendo intervalos...");
            for (int i=0; i<apiBaseUrls.length; i++) {
                intervals[i]=buildIntervals(apiBaseUrls[i],MAX_INTERVAL_SIZE, client);
            }
        }


        HashMap<String, Item> itemHashMap = new HashMap<String, Item> ();
        for (String webBaseUrl : webBaseUrls) {
            Logger.log("XXXXXXXXXXXXX Procesando nueava web url " + webBaseUrl);
            processItemsOnUrl(webBaseUrl, client, itemHashMap);
        }

        if (!ONLY_RELEVANT) {
            for (String apiBaseUrl : apiBaseUrls) {
                Logger.log("XXXXXXXXXX Procesando nueava api url " + apiBaseUrl);
                processItemsWithApi(apiBaseUrl, -1, -1, client, itemHashMap, usuario, ONLY_RELEVANT);
            }
        }

        for (int i = 0; i < intervals.length; i++) {
            int[] interval= intervals[i];
            String url= apiBaseUrls[i];
            for (int j = 1; j < interval.length; j++) {
                int since = interval[j - 1] + 1;
                int upto = interval[j];
                Logger.log("XXXXXXXXXX Procesando intervalo " + since + "-" + upto + " " + url);
                processItemsWithApi(url, since, upto, client, itemHashMap, usuario, ONLY_RELEVANT);
            }
        }

        if (!ONLY_RELEVANT) {
            Logger.log("XXXXXXXXXX Agregando posibles pausados.  itemHashMap=" + itemHashMap.size());
            addPossiblePaused(itemHashMap, DATABASE);
        }

        //removemos lo que no nos interesa o ya fue procesado
        Logger.log("XXXXXXXXXX Purgando items 1. itemHashMap=" + itemHashMap.size());
        ArrayList<String> incompleteList = purgeItemHashMap(itemHashMap, DATABASE);
        Logger.log("XXXXXXXXXX Completando items 1.  itemHashMap=" + itemHashMap.size());
        completeWebItems(client, itemHashMap, incompleteList);
        Logger.log("XXXXXXXXXX Purgando items 2. itemHashMap=" + itemHashMap.size());
        incompleteList = purgeItemHashMap(itemHashMap, DATABASE);
        if (incompleteList.size() > 0) {
            System.out.println("algo salio mal aca");
        }

        Logger.log("XXXXXXXXXX Purgando items 3. itemHashMap=" + itemHashMap.size());
        purgeItemHashMap2(itemHashMap, DATABASE);

        Logger.log("XXXXXXXXXX Completando items 2. itemHashMap=" + itemHashMap.size());
        completeSellerName(itemHashMap, DATABASE);

        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
        ArrayList<Thread> removeList = new ArrayList<Thread>();
        Logger.log("XXXXXXXXXX Procesando productos. itemHashMap=" + itemHashMap.size());
        for (Item item : itemHashMap.values()) {


            ProductPageProcessor ppp = new ProductPageProcessor(item.permalink, item.id, item.sellerId, item.page, item.ranking, SAVE, DEBUG, DATABASE, getGlobalDate(), false);
            threadArrayList.add(ppp);
            ppp.start();

            while (threadArrayList.size() >= MAX_THREADS) {
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

        if (!IGNORE_VISITS) {
            VisitCounter.updateVisits(DATABASE,SAVE,DEBUG);
        }
    }

    protected static void processItemsWithApi(String apiBaseUrl, int since, int upto, CloseableHttpClient client, HashMap<String, Item> itemHashMap, String usuario, boolean ONLY_RELEVANT) {
        if (since >= 0) {
            apiBaseUrl += "&price=" + since + "-" + upto;
        }

        String apiSearchUrl = apiBaseUrl;

        int offset = 0;
        int ranking = 0;
        int totalResults = -1; //valor inicial que se reemplazará

        JSONObject jsonObject = null;
        boolean rankingOnly = since == -1;

        while (offset < totalResults || totalResults == -1) {
            if (offset < RESULTS_WITHOUT_TOKEN) {
                jsonObject = HttpUtils.getJsonObjectWithoutToken(apiSearchUrl, client, false);
                if (totalResults == -1) {
                    JSONObject pagingObject = jsonObject.getJSONObject("paging");
                    totalResults = pagingObject.getInt("total");
                    if (totalResults > RESULTS_LIMIT) {
                        totalResults = RESULTS_LIMIT;
                    }
                }
            } else {
                if (ONLY_RELEVANT) {
                    return;
                }
                String msg = "No se pudo recorrer el intervalo por completo sin token " + apiSearchUrl;
                Logger.log(msg);
                jsonObject = HttpUtils.getJsonObjectUsingToken(apiSearchUrl, client, usuario);
            }
            requestCount++;
            if (jsonObject == null) {
                String msg = "No se pudo recuperar el item " + apiSearchUrl;
                Logger.log(msg);
                return;
            }
            JSONArray resultsArray = jsonObject.getJSONArray("results");
            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject productObj = resultsArray.getJSONObject(i);
                Item item = null;
                String id = productObj.getString("id");
                if (itemHashMap.containsKey(id)) {
                    item = itemHashMap.get(id);
                } else {
                    item = new Item();
                    item.id = id;
                    itemHashMap.put(id, item);
                }


                ranking++;
                if (item.ranking == 10000) {
                    item.ranking = ranking;
                }
                if (rankingOnly) {
                    continue;
                }


                boolean completed = completeItem(productObj, item);
                if (!completed) {
                    itemHashMap.remove(id);
                }
            }

            offset += 50;
            apiSearchUrl = apiBaseUrl + "&offset=" + offset;
        }
    }


}
