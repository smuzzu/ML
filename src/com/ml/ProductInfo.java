package com.ml;


import com.ml.utils.DatabaseHelper;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.TokenUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ProductInfo {

    private static class Info {
        Date date;
        String user;
        String productId;
        long visits;
        long questions;
        long orders;
        boolean active;
        double price;
        String title;
        Date creationDate;
        String categoryId;
        int ranking;
    }

    private static class Question {
        String questionId;
        String productId;
        Date dateCreated;
        String fromUserId;
        String status;
        Date answerDate;
        String answerText;
    }

    static String usuario="SOMOS_MAS";


    static long oneDayinMiliseconds = 86400000;
    static String DATABASE="ML3";
    static Date globalDate = null;
    static boolean IGNORE_CATETORIES=false;


    private static JSONArray getUnAnsweredQuestions(CloseableHttpClient httpClient) {
        JSONArray unansweredQuestions = new JSONArray();

        String productListURL = "https://api.mercadolibre.com/users/" + TokenUtils.getIdCliente(usuario) + "/items/search?search_type=scan";
        JSONObject jsonResponse = HttpUtils.getJsonObjectUsingToken(productListURL, httpClient,usuario,false);

        JSONArray jSONArray = jsonResponse.getJSONArray("results");
        ArrayList<String> productsArrayList = new ArrayList<>();
        for (Object productIdÒbj : jSONArray) {
            String productId = (String) productIdÒbj;
            productsArrayList.add(productId);
        }

        for (String productId : productsArrayList) {
            String permalinkStr = null;
            String questionsUrl = "https://api.mercadolibre.com/questions/search?search_type=scan&item=" + productId;
            JSONObject jsonQuestions = HttpUtils.getJsonObjectUsingToken(questionsUrl, httpClient,usuario,false);
            JSONArray jsonQuestionsArray = (JSONArray) jsonQuestions.get("questions");
            for (int i = 0; i < jsonQuestionsArray.length(); i++) {
                Object objectQuestion = jsonQuestionsArray.get(i);
                JSONObject jsonQuestion = (JSONObject) objectQuestion;
                Object statuObj = jsonQuestion.get("status");
                if (statuObj != null) {
                    String statusStr = (String) statuObj;
                    if (statusStr.equals("UNANSWERED")) {
                        if (permalinkStr == null) {
                            String productDetailsUrl = "https://api.mercadolibre.com/items/" + productId;
                            JSONObject jsonProduct = HttpUtils.getJsonObjectUsingToken(productDetailsUrl, httpClient,usuario,false);
                            Object permalinkObj = jsonProduct.get("permalink");
                            permalinkStr = (String) permalinkObj;
                        }
                        jsonQuestion.put("permalink", permalinkStr);
                        unansweredQuestions.put(jsonQuestion);
                    }
                }
            }
        }
        return unansweredQuestions;
    }


    private static HashMap<String,Info> getProductDetails(CloseableHttpClient httpClient, ArrayList<String> allProductIDsArrayList) {
        String msg="*********** Cargando detalles de productos";
        System.out.println(msg);
        Logger.log(msg);

        HashMap<String,Info> result = new HashMap<String,Info>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (Object productIdÒbj : allProductIDsArrayList) {
            String productId = (String) productIdÒbj;
            Info productInfo = new Info();
            productInfo.user=usuario;
            productInfo.productId=productId;
            String productDetailsUrl="https://api.mercadolibre.com/items/"+productId;
            JSONObject jsonProductDetail = HttpUtils.getJsonObjectWithoutToken(productDetailsUrl, httpClient,false );
            String dateCreatedStr= (String) jsonProductDetail.get("date_created");
            String priceStr="0.0";
            String title=null;
            try {
                if (jsonProductDetail.has("price")) {
                    priceStr = (String) ((jsonProductDetail.get("price")).toString());
                }
                productInfo.title = (String) jsonProductDetail.get("title");
            }catch (Exception e){
                msg = "Exception processing price/title "+productDetailsUrl;
                System.out.println(msg);
                e.getStackTrace();
                Logger.log(msg);
                Logger.log(e);
            }
            productInfo.price = Double.parseDouble(priceStr);
            String statusStr=(String) jsonProductDetail.get("status");
            productInfo.active=statusStr.equals("active");
            dateCreatedStr=dateCreatedStr.substring(0,10);
            java.util.Date creationDateUtil =null;
            try {
                creationDateUtil = dateFormat.parse(dateCreatedStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (creationDateUtil!=null) {
                productInfo.creationDate = new Date(creationDateUtil.getTime());
            }else {
                productInfo.creationDate = null;
            }
            productInfo.categoryId=jsonProductDetail.getString("category_id");
            result.put(productId,productInfo);
        }
        return result;
    }

    private static HashMap<String,ArrayList<Question>> getAllTimesQuestions(CloseableHttpClient httpClient, ArrayList<String> allProductIDsArrayList) {
        String msg="*********** Cargando preguntas";
        System.out.println(msg);
        Logger.log(msg);

        HashMap<String,ArrayList<Question>> allTimeQuestionsHashMap = new HashMap<String,ArrayList<Question>>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSX");

        for (String productId : allProductIDsArrayList) {

            String scrollId=null;
            String questionsUrl =null;
            boolean moreItems=true;
            ArrayList<Question> questionArrayList = new ArrayList<Question>();

            while (moreItems){
                questionsUrl = "https://api.mercadolibre.com/questions/search?search_type=scan&item=" + productId;
                if (scrollId!=null){
                    questionsUrl+="&scroll_id="+scrollId;
                }
                JSONObject jsonQuestions = HttpUtils.getJsonObjectUsingToken(questionsUrl, httpClient,usuario,false);
                scrollId=jsonQuestions.getString("scroll_id");
                JSONArray jsonQuestionsArray = (JSONArray) jsonQuestions.get("questions");
                if (jsonQuestionsArray.length()<50){
                    moreItems=false;
                }
                for (int i = 0; i < jsonQuestionsArray.length(); i++) {
                    JSONObject jsonQuestion = jsonQuestionsArray.getJSONObject(i);
                    Question question = new Question();
                    question.productId=productId;
                    question.questionId=""+jsonQuestion.getInt("id");
                    String dateCreatedStr = jsonQuestion.getString("date_created");
                    dateCreatedStr=dateCreatedStr.replace('T',' ');
                    java.util.Date parsedDate = null;
                    try {
                        parsedDate = dateFormat.parse(dateCreatedStr);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    java.sql.Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());

                    if (dateCreatedStr.substring(11,13).equals("12") && dateCreatedStr.substring(24).equals("04:00")){ //fix para problema de las 13 hs
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(parsedDate.getTime());
                        if (calendar.get(Calendar.HOUR_OF_DAY)==1){
                            calendar.set(Calendar.HOUR_OF_DAY,13);
                            timestamp.setTime(calendar.getTimeInMillis());
                        }
                    }

                    question.dateCreated = new java.sql.Date(timestamp.getTime());
                    question.status = jsonQuestion.getString("status");
                    JSONObject fromJsonObject = jsonQuestion.getJSONObject("from");
                    question.fromUserId=""+fromJsonObject.getInt("id");
                    if (jsonQuestion.get("answer") instanceof JSONObject) {
                        JSONObject answerJsonObject = answerJsonObject = jsonQuestion.getJSONObject("answer");
                        dateCreatedStr=answerJsonObject.getString("date_created");
                        dateCreatedStr=dateCreatedStr.replace('T',' ');
                        try {
                            parsedDate = dateFormat.parse(dateCreatedStr);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        timestamp = new java.sql.Timestamp(parsedDate.getTime());
                        question.answerDate = new java.sql.Date(timestamp.getTime());
                        question.answerText=answerJsonObject.getString("text");
                    }
                    questionArrayList.add(question);
                }
                allTimeQuestionsHashMap.put(productId,questionArrayList);
                boolean b=false;

            }
        }
        return allTimeQuestionsHashMap;
    }



    private static ArrayList<String> getItemsInOrdersBetweenDates(CloseableHttpClient httpClient, Date date1, Date date2){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr1 = dateFormat.format(date1);
        String dateStr2 = dateFormat.format(date2);

        String dateOnQuery2Str = "&order.date_created.from=" + dateStr1 + "T00:00:00.000-03:00&order.date_created.to=" + dateStr2 + "T23:59:59.999-03:00";

        ArrayList<String> itemsInOrdersArrayList = new ArrayList<String>();
        String ordersUrl="https://api.mercadolibre.com/orders/search?seller="+TokenUtils.getIdCliente(usuario)+dateOnQuery2Str+"&order.status%20ne%20cancelled&order.status%20ne%20invalid";
        JSONObject jsonOrders = HttpUtils.getJsonObjectUsingToken(ordersUrl, httpClient,usuario,false);
        JSONArray jsonOrdersArray = (JSONArray) jsonOrders.get("results");
        for (Object orderObjectArray: jsonOrdersArray){
            JSONObject jsonOrder= (JSONObject) orderObjectArray;
            JSONArray jsonOrdersItemsArray = jsonOrder.getJSONArray("order_items");
            for (Object itemObject : jsonOrdersItemsArray){
                JSONObject jsonItem= (JSONObject)itemObject;
                JSONObject item = (JSONObject)jsonItem.get("item");
                String itemStr=(String)item.get("id");
                itemsInOrdersArrayList.add(itemStr); //agrega articulos comprados en el dia con posible repeticion
            }
        }
        return itemsInOrdersArrayList;
    }

    private static HashMap<Integer,Integer> getOrdersStatistics(CloseableHttpClient httpClient){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSX");
                                                                   // 2018-06-08T20:21:27.000-04:00
/*
        HashMap<Integer,Integer> statistics = new HashMap<Integer, Integer>();
        for (int i=0;i<24;i++){
            statistics.put(i,0);
        }*/
        HashMap<Integer,Integer> statistics = new HashMap<Integer, Integer>();
        for (int i=0;i<32;i++){
            statistics.put(i,0);
        }
        //String dateOnQuery2Str = "&order.date_created.from=2019-06-24T15:53:04.000-03:00&order.date_created.to=2019-06-24T15:53:04.000-03:00";

        //ArrayList<String> itemsInOrdersArrayList = new ArrayList<String>();
        int totalOrders=999999;
        int count=0;


        for (int offset=0; offset<=totalOrders; offset+=50) {
            String ordersUrl = "https://api.mercadolibre.com/orders/search?seller=" + TokenUtils.getIdCliente(usuario) + "&order.status%20ne%20cancelled&order.status%20ne%20invalid&offset="+offset;
            JSONObject jsonOrders = HttpUtils.getJsonObjectUsingToken(ordersUrl, httpClient, usuario,false);
            if (offset==0){
                JSONObject pagingObj = jsonOrders.getJSONObject("paging");
                totalOrders=pagingObj.getInt("total");
                String msg="total ventas registradas: "+totalOrders;
                System.out.println(totalOrders);
                Logger.log(msg);
            }
            JSONArray jsonOrdersArray = (JSONArray) jsonOrders.get("results");
            for (Object orderObjectArray : jsonOrdersArray) {
                JSONObject jsonOrder = (JSONObject) orderObjectArray;
                String dateCreatedStr=jsonOrder.getString("date_created");
                dateCreatedStr=dateCreatedStr.replace('T',' ');
                try {
                    java.util.Date dateCreated = dateFormat.parse(dateCreatedStr);
                    int hour = dateCreated.getHours();
                    if (dateCreatedStr.substring(11,13).equals("12") && dateCreatedStr.substring(24).equals("04:00")){
                        hour=13;
                    }
                    int day = dateCreated.getDay();
                    int dayOfMonth=dateCreated.getDate();

                    int sales = statistics.get(dayOfMonth);
                    sales++;
                    statistics.put(dayOfMonth, sales);

/*                    if (day>0 && day<6) {//S a D
                        count++;
                        int sales = statistics.get(hour);
                        sales++;
                        statistics.put(hour, sales);
                    }*/
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("count: "+count);
        return statistics;
    }

    private static ArrayList<Info> getVisitsAndQuestions(CloseableHttpClient httpClient, Date date, ArrayList<String> allProductIDsArrayList, HashMap<String,Info> allProductFixedDetailsHashMap,HashMap<String,ArrayList<Question>> allTimesQuestionsHashMap, HashMap<String,ArrayList> productsInCatetoriesHashMap) {

        ArrayList<Info> results=new ArrayList<Info>();

        Date beginingOfTheDay = getBeginingOfTheDay(date);
        Date endOfTheDay = getEndOfTheDay(date);


        ArrayList<String> itemsInOrdersArrayList =getItemsInOrdersBetweenDates(httpClient,date,date);

        HashMap<String,Integer> oneDayVisitsHashMap = VisitCounter.processVisits(date, date, allProductIDsArrayList,false);

        for (String productId : allProductIDsArrayList) {
            Info fixedProductDetails=allProductFixedDetailsHashMap.get(productId);
            ArrayList<Question> questionArrayList = allTimesQuestionsHashMap.get(productId);
            if (date.before(fixedProductDetails.creationDate)){
                continue; //ignoro esta fecha, que es anterior a la creacion de este item
            }

            int totalQuestions = 0;
            for (Question question:questionArrayList) {
                if (!question.dateCreated.before(beginingOfTheDay) && !question.dateCreated.after(endOfTheDay)) {
                    totalQuestions++;
                }
            }
            Integer visits = oneDayVisitsHashMap.get(productId);

            int salesTotal=0;
            for (String id: itemsInOrdersArrayList){
                if (id.equals(productId)){
                    salesTotal++;
                }
            }

            if (salesTotal>visits){//la cocina de ML
                visits=salesTotal;
            }
            if (totalQuestions>visits){
                visits=totalQuestions;
            }

            int ranking=2000;
            if (productsInCatetoriesHashMap.containsKey(fixedProductDetails.categoryId)){
                ArrayList<String> productsOnCategory = productsInCatetoriesHashMap.get(fixedProductDetails.categoryId);
                if (productsOnCategory.contains(productId)){
                    ranking=productsOnCategory.indexOf(productId);
                }
            }

            productId="MLA-"+productId.substring(3);

            Info info = new Info();
            info.date=date;
            info.user=usuario;
            info.productId=productId;
            info.visits=visits;
            info.questions=totalQuestions;
            info.orders=salesTotal;
            info.active=fixedProductDetails.active;
            info.price=fixedProductDetails.price;
            info.title=fixedProductDetails.title;
            info.ranking=ranking;

            results.add(info);
            String msg = date.toString()+" "+productId+" visitas:"+visits+" preguntas:"+totalQuestions+" ventas:"+salesTotal+" activo:"+info.active+" precio:"+info.price+" descripcion:"+info.title+" ranking:"+info.ranking;
            System.out.println(msg);
            Logger.log(msg);
        }

        return results;
    }

    private static ArrayList<String> getCategories(ArrayList<String> allProductIDsArrayList, HashMap<String,Info> allProductFixedDetailsHashMap){
        ArrayList<String> categoriesArrayList=new ArrayList<String>();
        for (String productId:allProductIDsArrayList){
            Info productInfo = allProductFixedDetailsHashMap.get(productId);
            if (!categoriesArrayList.contains(productInfo.categoryId)){
                categoriesArrayList.add(productInfo.categoryId);
            }
        }

        return categoriesArrayList;
    }

    private static ArrayList<String> getAllProductIDs(CloseableHttpClient httpClient) {
        ArrayList<String> allproductIds=new ArrayList<String>();
        boolean moreItems=true;
        String productListURL =null;
        JSONObject jsonResponse=null;
        JSONArray jsonProductArray=null;
        String scrollId=null;
        while (moreItems){
            productListURL = "https://api.mercadolibre.com/users/" + TokenUtils.getIdCliente(usuario) + "/items/search?search_type=scan";
            if (scrollId!=null){
                productListURL+="&scroll_id="+scrollId;
            }
            jsonResponse = HttpUtils.getJsonObjectUsingToken(productListURL, httpClient,usuario,false);
            scrollId=(String) jsonResponse.get("scroll_id");
            jsonProductArray = jsonResponse.getJSONArray("results");
            if (jsonProductArray.length()<50){
                moreItems=false;//end
            }
            for (Object productIdÒbj : jsonProductArray) {
                String productId = (String) productIdÒbj;
                allproductIds.add(productId);
            }
        }
        return allproductIds;
    }

    public static void main(String[] args) {

        if (args!=null && args.length>0){
            String usuarioArg = args[0];
            if (usuarioArg!=null && usuarioArg.length()>0){
                usuario=usuarioArg;
            }
        }
        String msg="*********** Procesando usuario: "+usuario;
        Logger.log(msg);
        System.out.println(msg);

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        Date registrationDate=getRegistrationDate(httpClient);
        Date todaysDate = getGlobalDate();
        Date aYearAgo = new Date(todaysDate.getTime()-oneDayinMiliseconds*360);
        Date lastDairyUpdate = DatabaseHelper.fetchLasLastDailyUpdate(DATABASE,usuario);
        if (lastDairyUpdate==null){ //tabla vacia
            if (registrationDate.after(aYearAgo)) {
                lastDairyUpdate = registrationDate;
            }else {
                lastDairyUpdate=aYearAgo;
            }
        }

        ArrayList<String> allProductIDsArrayList = getAllProductIDs(httpClient);
        HashMap<String,Info> allProductFixedDetailsHashMap = getProductDetails(httpClient,allProductIDsArrayList);


        ArrayList<String> categoriesArrayList = new ArrayList<String>();
        if (!IGNORE_CATETORIES) {
            categoriesArrayList = getCategories(allProductIDsArrayList, allProductFixedDetailsHashMap);
        }

        HashMap<String,ArrayList> productsInCatetoriesHashMap = getProductsInCategories(httpClient, categoriesArrayList);

        HashMap<String,ArrayList<Question>> allTimesQuestionsHashMap= getAllTimesQuestions(httpClient,allProductIDsArrayList);

        processDaily(httpClient, todaysDate, lastDairyUpdate,allProductIDsArrayList,allProductFixedDetailsHashMap,allTimesQuestionsHashMap,productsInCatetoriesHashMap);

        processWeekly(registrationDate, lastDairyUpdate);

        processMonthly(registrationDate, lastDairyUpdate);

    }

    private static HashMap<String,ArrayList> getProductsInCategories(CloseableHttpClient httpClient, ArrayList<String> categoriesArrayList) {
        String msg="*********** Recorriendo Categorias:";
        Logger.log(msg);
        System.out.println(msg);
        HashMap<String,ArrayList> productsInCatetoriesHashMap = new HashMap<String,ArrayList>();
        for (String categoryID: categoriesArrayList){
            String categoryUrl = "https://api.mercadolibre.com/categories/"+categoryID;
            JSONObject categoryObject = HttpUtils.getJsonObjectWithoutToken(categoryUrl,httpClient,false );
            JSONArray categoryPathArray = categoryObject.getJSONArray("path_from_root");
            String categoryFullPath="";
            for (int j=0; j<categoryPathArray.length(); j++){
                JSONObject pathObject = categoryPathArray.getJSONObject(j);
                String name = pathObject.getString("name");
                categoryFullPath+=name+" / ";
            }
            categoryFullPath=categoryFullPath.substring(0,categoryFullPath.length()-3);

            Logger.log(categoryFullPath);
            System.out.println(categoryFullPath);

            ArrayList<String> productsInCategoryArrayList = new ArrayList<String>();
            Logger.log(categoryID);
            int maxProductsInCategory=2000;
            JSONObject jsonResponse = null;
            for (int offset=0; offset<=maxProductsInCategory; offset+=50) {
                String itemsInCategoryURL = "https://api.mercadolibre.com/sites/MLA/search?search_type=scan&category=" + categoryID + "&offset="+offset;
                if (offset<=1000) {
                    jsonResponse = HttpUtils.getJsonObjectWithoutToken(itemsInCategoryURL, httpClient, false);
                }else {
                    jsonResponse = HttpUtils.getJsonObjectUsingToken(itemsInCategoryURL, httpClient,usuario,false);
                }
                if (offset==0){
                    JSONObject pagingObj = jsonResponse.getJSONObject("paging");
                    int total=pagingObj.getInt("total");
                    if (total<maxProductsInCategory){
                        maxProductsInCategory=total;
                    }
                }
                JSONArray resultsArray = jsonResponse.getJSONArray("results");
                for (int i=0; i<resultsArray.length(); i++){
                    JSONObject articleObject = resultsArray.getJSONObject(i);
                    String productId = articleObject.getString("id");
                    productsInCategoryArrayList.add(productId);
                }
            }
            productsInCatetoriesHashMap.put(categoryID,productsInCategoryArrayList);
        }
        return productsInCatetoriesHashMap;
    }

    private static void processDaily(CloseableHttpClient httpClient, Date todaysDate, Date lastDairyUpdate, ArrayList<String> allProductIDsArrayList,HashMap<String,Info> allProductFixedDetailsHashMap, HashMap<String,ArrayList<Question>> allTimesQuestionsHashMap, HashMap<String,ArrayList> productsInCatetoriesHashMap) {
        String msg="*********** Proceso diario";
        System.out.println(msg);
        Logger.log(msg);


        Date dailyStartDate=new Date(lastDairyUpdate.getTime()+oneDayinMiliseconds);
        Date dailyEndDate=new Date(todaysDate.getTime()-oneDayinMiliseconds);
        ArrayList<Info> infoArrayList = new ArrayList<Info>();
        for (long iterator=dailyStartDate.getTime(); iterator<=dailyEndDate.getTime(); iterator+=oneDayinMiliseconds) {
            Date date = new Date(iterator);
            System.out.println("Processing date: "+date);
            infoArrayList.addAll(getVisitsAndQuestions(httpClient, date, allProductIDsArrayList, allProductFixedDetailsHashMap,allTimesQuestionsHashMap, productsInCatetoriesHashMap));
        }
        for (Info info:infoArrayList){
            DatabaseHelper.insertDaily(DATABASE, info.date,info.productId, info.orders, info.visits, info.questions, info.active, info.price, info.title, info.ranking, info.user);
        }
    }

    private static void processWeekly(Date registrationDate, Date lastDairyUpdate) {
        String msg="*********** Proceso semanal";
        System.out.println(msg);
        Logger.log(msg);

        Date lastWeeklyUpdate = DatabaseHelper.fetchLasLastWeeklyUpdate(DATABASE,usuario);
        if (lastWeeklyUpdate==null){
            lastWeeklyUpdate=registrationDate;
        }

        Date weeklyStartDate=followingDayOfWeek(lastWeeklyUpdate,Calendar.MONDAY);
        Date weeklyEndDate=followingDayOfWeek(weeklyStartDate, Calendar.SUNDAY);


        while (weeklyEndDate.before(lastDairyUpdate)) {
            String msg2="Procesando semana "+weeklyStartDate+" "+weeklyEndDate;
            System.out.println(msg2);
            Logger.log(msg2);
            try {
                ArrayList<String> productsArrayList = DatabaseHelper.fetchValidProductsBetweenDates(DATABASE, weeklyStartDate, weeklyEndDate,5, usuario);
                for (String productId : productsArrayList) {
                    ResultSet rs1 = DatabaseHelper.fetchAllDailyUpdatesBetweenDates(DATABASE, weeklyStartDate, weeklyEndDate, productId, usuario);
                    long sumVisits=0;
                    long sumQuestions=0;
                    long sumSales=0;
                    long sumPaused=0;
                    int sumRanking=0;
                    String title="";
                    double price=0.0;
                    int count=0;
                    while (rs1.next()) {
                        count++;
                        Long visits = rs1.getLong(2);
                        if (visits!=null) {
                            sumVisits +=visits;
                        }
                        Long questions =rs1.getLong(3);
                        if (questions!=null) {
                            sumQuestions += questions;
                        }
                        Long sales = rs1.getLong(4);
                        if (sales!=null) {
                            sumSales += sales;
                        }
                        Integer ranking = rs1.getInt(5);
                        if (ranking!=null) {
                            sumRanking += ranking;
                        }
                        boolean active = rs1.getBoolean(6);
                        if (!active){
                            sumPaused++;
                        }
                        price=rs1.getDouble(7);
                        title=rs1.getString(8);

                    }
                    int ranking=sumRanking/count;
                    if (ranking==0){
                        ranking=-1;
                    }
                    DatabaseHelper.insertWeekly(DATABASE,weeklyStartDate,weeklyEndDate,productId,sumSales,sumVisits,sumQuestions,ranking,sumPaused,price,title,usuario);

                }
            }catch (SQLException e){
                String msg1 = "Exception in processWeekly";
                Logger.log(msg1);
                Logger.log(e);
                System.out.println(msg1);
                e.printStackTrace();
            }
            weeklyStartDate=followingDayOfWeek(weeklyEndDate,Calendar.MONDAY);
            weeklyEndDate=followingDayOfWeek(weeklyStartDate, Calendar.SUNDAY);
        }
    }

    private static void processMonthly(Date registrationDate, Date lastDairyUpdate) {
        String msg="*********** Proceso mensual";
        System.out.println(msg);
        Logger.log(msg);

        Date lastMonthlyUpdate = DatabaseHelper.fetchLasLastMonthlyUpdate(DATABASE, usuario);
        if (lastMonthlyUpdate==null){
            lastMonthlyUpdate=registrationDate;
        }

        Date monthlyStartDate=firstDayNextMonth(lastMonthlyUpdate);
        Date monthlyEndDate=lastDayThisMonth(monthlyStartDate);

        while (monthlyEndDate.before(lastDairyUpdate)) {
            String msg2="Procesando mes "+monthlyStartDate+" "+monthlyEndDate;
            System.out.println(msg2);
            Logger.log(msg2);
            try {
                ArrayList<String> productsArrayList = DatabaseHelper.fetchValidProductsBetweenDates(DATABASE, monthlyStartDate, monthlyEndDate,20, usuario);
                for (String productId : productsArrayList) {
                    ResultSet rs1 = DatabaseHelper.fetchAllDailyUpdatesBetweenDates(DATABASE, monthlyStartDate, monthlyEndDate, productId,usuario);
                    long sumVisits=0;
                    long sumQuestions=0;
                    long sumSales=0;
                    long sumPaused=0;
                    int sumRanking=0;
                    String title="";
                    double price=0.0;
                    int count=0;
                    while (rs1.next()) {
                        count++;

                        Long visits = rs1.getLong(2);
                        if (visits!=null) {
                            sumVisits +=visits;
                        }
                        Long questions =rs1.getLong(3);
                        if (questions!=null) {
                            sumQuestions += questions;
                        }
                        Long sales = rs1.getLong(4);
                        if (sales!=null) {
                            sumSales += sales;
                        }
                        Integer ranking = rs1.getInt(5);
                        if (ranking!=null) {
                            sumRanking += ranking;
                        }
                        boolean active = rs1.getBoolean(6);
                        if (!active){
                            sumPaused++;
                        }
                        price=rs1.getDouble(7);
                        title=rs1.getString(8);

                    }
                    int ranking=sumRanking/count;
                    if (ranking==0){
                        ranking=-1;
                    }
                    DatabaseHelper.insertMonthly(DATABASE,monthlyStartDate,monthlyEndDate,productId,sumSales,sumVisits,sumQuestions,ranking,sumPaused,price,title,usuario);

                }
            }catch (SQLException e){
                String msg1 = "Exception in processMonthly";
                Logger.log(msg1);
                Logger.log(e);
                System.out.println(msg1);
                e.printStackTrace();
            }
            monthlyStartDate=firstDayNextMonth(monthlyEndDate);
            monthlyEndDate=lastDayThisMonth(monthlyStartDate);
        }

    }

    private static Date getRegistrationDate(CloseableHttpClient httpClient){
        Date result=null;
        String userUrl="https://api.mercadolibre.com/users/"+TokenUtils.getIdCliente(usuario);
        JSONObject jsonCustomer = HttpUtils.getJsonObjectWithoutToken(userUrl, httpClient, false);
        String registrationDateStr= (String) jsonCustomer.get("registration_date");
        registrationDateStr=registrationDateStr.substring(0,10);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date registrationDateUtil =null;
        try {
            registrationDateUtil = dateFormat.parse(registrationDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (registrationDateUtil!=null) {
            result = new Date(registrationDateUtil.getTime());
        }
        return result;
    }


    private synchronized static Date getGlobalDate() {
        if (globalDate == null) {
            Calendar cal = Calendar.getInstance();
            long milliseconds = cal.getTimeInMillis();
            Date date = new Date(milliseconds);
            String dateStr = date.toString();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            java.util.Date parsedDate = null;
            try {
                parsedDate = dateFormat.parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            java.sql.Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
            Date dateCreated = new java.sql.Date(timestamp.getTime());
            globalDate = dateCreated;
        }
        return globalDate;
    }

    public static Date getBeginingOfTheDay(Date date) {
        Calendar beginingOfTheDayCalendar = Calendar.getInstance();
        beginingOfTheDayCalendar.setTime(date);
        beginingOfTheDayCalendar.set(Calendar.HOUR_OF_DAY,0);
        beginingOfTheDayCalendar.set(Calendar.MINUTE,0);
        beginingOfTheDayCalendar.set(Calendar.SECOND,0);
        beginingOfTheDayCalendar.set(Calendar.MILLISECOND,0);
        return new Date(beginingOfTheDayCalendar.getTimeInMillis());
    }

    public static Date getEndOfTheDay(Date date){
        Calendar endOfTheDayCalendar = Calendar.getInstance();
        endOfTheDayCalendar.setTime(date);
        endOfTheDayCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endOfTheDayCalendar.set(Calendar.MINUTE, 59);
        endOfTheDayCalendar.set(Calendar.SECOND, 59);
        endOfTheDayCalendar.set(Calendar.MILLISECOND, 999);
        return new Date(endOfTheDayCalendar.getTimeInMillis());
    }


    private static Date followingDayOfWeek(Date date, int dayOfWeek){
        Date resultDate=new Date(date.getTime()+oneDayinMiliseconds);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(resultDate.getTime());
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        while (weekday!=dayOfWeek){
            resultDate=new Date(resultDate.getTime()+oneDayinMiliseconds);
            calendar.setTimeInMillis(resultDate.getTime());
            weekday = calendar.get(Calendar.DAY_OF_WEEK);
        }
        return resultDate;
    }

    private static Date previousOrSameDayOfWeek(Date date, int dayOfWeek){
        Date resultDate=new Date(date.getTime());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(resultDate.getTime());
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        while (weekday!=dayOfWeek){
            resultDate=new Date(resultDate.getTime()-oneDayinMiliseconds);
            calendar.setTimeInMillis(resultDate.getTime());
            weekday = calendar.get(Calendar.DAY_OF_WEEK);
        }
        return resultDate;
    }

    private static Date firstDayNextMonth(Date date){
        Date firstDayNextMonth = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        firstDayNextMonth = new Date(calendar.getTime().getTime());
        return firstDayNextMonth;
    }

    private static Date lastDayThisMonth(Date date){
        Date lastDayThisMonth = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        lastDayThisMonth = new Date(calendar.getTime().getTime());
        return lastDayThisMonth;
    }
}
