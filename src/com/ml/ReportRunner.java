package com.ml;

import com.ml.utils.Counters;
import com.ml.utils.DatabaseHelper;
import com.ml.utils.HTMLParseUtils;
import com.ml.utils.HttpUtils;
import com.ml.utils.Item;
import com.ml.utils.Logger;
import com.ml.utils.ProductPageProcessor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class ReportRunner {

    static final int RESULTS_WITHOUT_TOKEN = 1000;
    static final int RESULTS_LIMIT = 10000;

    static int MAX_THREADS = 20;//todo corregir 35
    static final boolean DEBUG = false;

    static final char COMPLETE = 'C';
    static final char INCOMPLETE = 'I';
    static final char UNDER_REVIEW = 'R';

    static int globalMinimumSales = 1;
    static Date globalRunDate = null;


    protected static void addBeyondRadarItemsFromDB(HashMap<String, Item> itemHashMap, String DATABASE, CloseableHttpClient httpClient, boolean SAVE) {

        HashMap<String,Item> beyondRadarProductList = DatabaseHelper.fetchItemsBeyondRadar(DATABASE, itemHashMap, getGlobalRunDate());

        String msg = "fuera del radar: " + beyondRadarProductList.size();
        System.out.println(msg);
        Logger.log(msg);

        String itemsIds="";
        int count=0;
        for (Item databaseItem : beyondRadarProductList.values()) {
            itemsIds+=databaseItem.id+",";
            count++;

            if (count == 20) {
                itemsIds = itemsIds.substring(0, itemsIds.length() - 1);
                String itemsUrl = "https://api.mercadolibre.com/items?ids=" + itemsIds;
                JSONObject jsonObject = HttpUtils.getJsonObjectWithoutToken(itemsUrl, httpClient, true);
                if (jsonObject != null) {
                    JSONArray jsonArray = jsonObject.getJSONArray("elArray");
                    for (int j = 0; j < jsonArray.length(); j++) {
                        JSONObject itemObject2 = jsonArray.getJSONObject(j);
                        int code = itemObject2.getInt("code");
                        JSONObject productObj = itemObject2.getJSONObject("body");

                        if (code == 200 && productObj != null) {

                            Item apiItem = null;
                            if (productObj.has("id") && !productObj.isNull("id")) {
                                String id = productObj.getString("id");
                                apiItem = beyondRadarProductList.get(id);

                            }else {
                                String msg1="FATAL !!!!!!!!!!!!! no se recuperó ID "+databaseItem.id;
                                System.out.println(msg1);
                                Logger.log(msg1);
                                continue;
                            }

                            char status = completeOrDisableItem(productObj, apiItem, DATABASE, SAVE);

                            if (status==COMPLETE) {
                                itemHashMap.put(apiItem.id, apiItem);
                            }
                        }
                    }
                }
                count = 0;
                itemsIds = "";
            }
        }
    }



    private synchronized static Date getGlobalRunDate() {
        return globalRunDate;
    }



    private static char completeOrDisableItem(JSONObject productObj, Item item, String database, boolean SAVE) {


        if (productObj.has("status") && !productObj.isNull("status")){

            String status = productObj.getString("status");

            if (status.equals("under_review")){//no se procesa
                if (productObj.has("last_updated") && !productObj.isNull("last_updated")) {
                    String lastUpdatedStr=productObj.getString("last_updated");
                    if (lastUpdatedStr!=null && lastUpdatedStr.length()>9) {
                        lastUpdatedStr=lastUpdatedStr.substring(0,10);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date lastUpdate=null;
                        try {
                            java.util.Date dt =sdf.parse(lastUpdatedStr);
                            lastUpdate=new Date(dt.getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                            System.out.println("Error parsing date "+lastUpdatedStr);
                        }
                        if (lastUpdate!=null) {
                            long oneDayinMiliseconds=86400000;
                            Date allowedDate=new Date(getGlobalRunDate().getTime()-oneDayinMiliseconds*20);
                            if (lastUpdate.after(allowedDate)) {
                                String msg = "No se procesara item " + item.id + " " + status;
                                System.out.println(msg);
                                Logger.log(msg);
                                return UNDER_REVIEW;
                            }
                        }
                    }
                }
            }
            if (status.equals("inactive") || status.equals("closed")){
                String msg = "Deshabilitando item "+item.id+" "+status;
                System.out.println(msg);
                Logger.log(msg);
                Counters.incrementGlobalDisableCount();
                if (SAVE) {
                    String formattedId=HTMLParseUtils.getFormatedId(item.id);
                    DatabaseHelper.disableProduct(formattedId, database);
                }
                return INCOMPLETE;
            }
            if (status.equals("paused")){
                return INCOMPLETE;//no lo procesamos
            }

        }

        if (productObj.has("sold_quantity") && !productObj.isNull("sold_quantity")) {
            item.totalSold = productObj.getInt("sold_quantity");
        }else{
            String msg1="FATAL !!!!!!!!!!!!! no se recuperó sold_quantity "+item.id;
            System.out.println(msg1);
            Logger.log(msg1);
            return INCOMPLETE;//no lo procesamos
        }

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

        item.catalog = false;
        if (productObj.has("catalog_listing") && !productObj.isNull("catalog_listing")) {
            item.catalog = productObj.getBoolean("catalog_listing");
        }

        if (productObj.has("seller") && !productObj.isNull("seller")) {
            JSONObject sellerObject = productObj.getJSONObject("seller");
            if (sellerObject.has("nickname") && !sellerObject.isNull("nickname")) {
                item.sellerName = sellerObject.getString("nickname");
            }
            item.sellerId = sellerObject.getInt("id");
        } else {
            if (productObj.has("seller_id") && !productObj.isNull("seller_id")) {
                item.sellerId = productObj.getInt("seller_id");
            }
        }

        item.variations = getProductVariations(productObj);

        return COMPLETE;
    }

    protected static void runWeeklyReport(String[] apiBaseUrls,
                                          CloseableHttpClient client, String usuario, String DATABASE,
                                          boolean ONLY_RELEVANT, boolean IGNORE_VISITS, Date golbalDate,
                                          int minimumSales, boolean SAVE) {
        globalRunDate = golbalDate;
        globalMinimumSales=minimumSales;

        HashMap<String, Item> itemHashMap = new HashMap<String, Item>();

        long remainingItemsFromPreviousRun=DatabaseHelper.getRemainingItems(DATABASE);

        if (remainingItemsFromPreviousRun==0) {

            for (String apiBaseUrl : apiBaseUrls) {
                Logger.log("XXXXXXXXXX Procesando nueava api url " + apiBaseUrl);
                processItemsWithApi(apiBaseUrl, client, itemHashMap, usuario, ONLY_RELEVANT, DATABASE, SAVE);
            }

            Logger.log("XXXXXXXXXX Agregando publicaciones fuera del radar.  itemHashMap=" + itemHashMap.size());
            addBeyondRadarItemsFromDB(itemHashMap, DATABASE,client, SAVE);

            Logger.log("XXXXXXXXXX guardando items en database");
            DatabaseHelper.saveItemsToProcess(itemHashMap.values(),DATABASE);
        }else {
            Logger.log("XXXXXXXXXX recuperando items de la corrida anterior en database");
            itemHashMap=DatabaseHelper.getRemainingItemsToProcess(DATABASE);
        }

        // purgando servicios (no contabilizan unidades vendidas)
        ArrayList<String> itemsToRemove=new ArrayList<String>();
        for (Item item: itemHashMap.values()){
            if (item.permalink.startsWith(HTMLParseUtils.SERVICIO_URL)){
                itemsToRemove.add(item.id);
            }
        }
        for (String id: itemsToRemove){
            itemHashMap.remove(id);
        }


        int totalItemsToProcess = itemHashMap.size();
        Counters.setGlobalProductCount(totalItemsToProcess);

        ArrayList<String> itemsToRemoveFromDatabase=new ArrayList<String>();
        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
        ArrayList<Thread> removeList = new ArrayList<Thread>();
        Logger.log("\n\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        Logger.log("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        Logger.log("XXXXXXXXXX Procesando productos. itemHashMap=" + itemHashMap.size()+"\n\n");

        for (Item item : itemHashMap.values()) {

            itemsToRemoveFromDatabase.add(item.id);
            if (itemsToRemoveFromDatabase.size()==2000){
                DatabaseHelper.deleteItemsToProcess(itemsToRemoveFromDatabase,DATABASE);
                itemsToRemoveFromDatabase.clear();
            }
            ProductPageProcessor ppp = new ProductPageProcessor(item, SAVE, DEBUG, DATABASE, getGlobalRunDate(), minimumSales);
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

        //terminamos de limpiar los pendientes
        DatabaseHelper.deleteItemsToProcess(itemsToRemoveFromDatabase,DATABASE);

        if (!IGNORE_VISITS) {
            VisitCounter.updateVisits(DATABASE,SAVE);
            VisitCounter.updateVisits(DATABASE,SAVE);
            VisitCounter.updateVisits(DATABASE,SAVE);
        }

    }

    protected static void processItemsWithApi(String apiBaseUrl, CloseableHttpClient client, HashMap<String, Item> itemHashMap, String usuario, boolean ONLY_RELEVANT, String database, boolean SAVE) {

        String apiSearchUrl = apiBaseUrl;

        int offset = 0;
        int ranking = 0;
        int totalResults = -1; //valor inicial que se reemplazará

        JSONObject jsonObject = null;

        while (offset < totalResults || totalResults == -1) {
            if (offset < RESULTS_WITHOUT_TOKEN) {
                jsonObject = HttpUtils.getJsonObjectWithoutToken(apiSearchUrl, client, false);
                if (totalResults == -1 && jsonObject != null) {
                    JSONObject pagingObject = jsonObject.getJSONObject("paging");
                    totalResults = pagingObject.getInt("total");
                    if (totalResults > RESULTS_LIMIT) {
                        totalResults = RESULTS_LIMIT;
                    }
                }
            } else {
                String msg = "No se pudo recorrer el intervalo por completo sin token " + apiSearchUrl;
                Logger.log(msg);
                jsonObject = HttpUtils.getJsonObjectUsingToken(apiSearchUrl, client, usuario,false);
            }
            if (jsonObject == null) {
                String msg = "No se pudo recuperar el item " + apiSearchUrl;
                Logger.log(msg);
                return;
            }
            JSONArray resultsArray = jsonObject.getJSONArray("results");
            for (int i = 0; i < resultsArray.length(); i++) {

                if (ONLY_RELEVANT && i>2000){
                    break;
                }

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

                char status = completeOrDisableItem(productObj, item, database,SAVE);
                if (status!=COMPLETE || item.totalSold==0) {
                    itemHashMap.remove(id);
                }
            }

            offset += 50;
            apiSearchUrl = apiBaseUrl + "&offset=" + offset;
        }
    }

    private static ArrayList<String> getProductVariations(JSONObject productObj) {
        ArrayList<String> variationsArray = new ArrayList<String>();
        if (productObj!=null) {
            if (productObj.has("variations") && !productObj.isNull("variations")) {
                JSONArray variationsObj= productObj.getJSONArray("variations");
                for (int j=0; j<variationsObj.length(); j++) {
                    JSONObject variationObj = variationsObj.getJSONObject(j);
                    if (variationObj.has("attribute_combinations") && !variationObj.isNull("attribute_combinations")){
                        JSONArray attributeCombinationArray = variationObj.getJSONArray("attribute_combinations");
                        if (attributeCombinationArray.length()>0) {
                            for (int i = 0; i < attributeCombinationArray.length(); i++) {
                                JSONObject attributeCombinationObj = attributeCombinationArray.getJSONObject(i);
                                String attributeName=null;
                                String valueName=null;
                                if (attributeCombinationObj.has("id") && !attributeCombinationObj.isNull("id")){
                                    attributeName=attributeCombinationObj.getString("id");
                                }
                                if (attributeCombinationObj.has("value_name") && !attributeCombinationObj.isNull("value_name")){
                                    valueName=attributeCombinationObj.getString("value_name");
                                }
                                if (attributeName!=null && valueName!=null){
                                    String data=attributeName+"|"+valueName;
                                    if (!variationsArray.contains(data)) {
                                        variationsArray.add(data);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Collections.sort(variationsArray);
        return variationsArray;
    }

    public static void main(String[] args){
        String database="ML1";
        String itemId="MLA753971534";
        String currentDateStr="29-01-2023";
        int minimumSales=1;

        String itemUrl = "https://api.mercadolibre.com/items/"+itemId;
        JSONObject itemObject=HttpUtils.getJsonObjectWithoutToken(itemUrl,HttpUtils.buildHttpClient(),false);
        Item item=new Item();
        item.id=itemId;
        char status = completeOrDisableItem(itemObject,item,database,false);
        Date runDate=Counters.parseDate(currentDateStr);
        ProductPageProcessor ppp = new ProductPageProcessor(item, false, DEBUG, database, runDate, minimumSales);
        ppp.run();


        boolean b=false;

    }

}
