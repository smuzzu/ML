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
        String productId;
        long visits;
        long questions;
        long orders;
        boolean active;
        double price;
        String title;
        Date creationDate;
        String categoryId;
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

    static final String usuario="ACACIAYLENGA";


    static long oneDayinMiliseconds = 86400000;
    static String DATABASE="ML3";
    static Date globalDate = null;


    private static JSONArray getUnAnsweredQuestions(CloseableHttpClient httpClient) {
        JSONArray unansweredQuestions = new JSONArray();

        String productListURL = "https://api.mercadolibre.com/users/" + TokenUtils.getIdCliente(usuario) + "/items/search?search_type=scan";
        JSONObject jsonResponse = HttpUtils.getJsonObjectUsingToken(productListURL, httpClient,usuario);

        JSONArray jSONArray = jsonResponse.getJSONArray("results");
        ArrayList<String> productsArrayList = new ArrayList<>();
        for (Object productIdÒbj : jSONArray) {
            String productId = (String) productIdÒbj;
            productsArrayList.add(productId);
        }

        for (String productId : productsArrayList) {
            String permalinkStr = null;
            String questionsUrl = "https://api.mercadolibre.com/questions/search?search_type=scan&item=" + productId;
            JSONObject jsonQuestions = HttpUtils.getJsonObjectUsingToken(questionsUrl, httpClient,usuario);
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
                            JSONObject jsonProduct = HttpUtils.getJsonObjectUsingToken(productDetailsUrl, httpClient,usuario);
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
            productInfo.productId=productId;
            String productDetailsUrl="https://api.mercadolibre.com/items/"+productId;
            JSONObject jsonProductDetail = HttpUtils.getJsonObjectWithoutToken(productDetailsUrl, httpClient);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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
                JSONObject jsonQuestions = HttpUtils.getJsonObjectUsingToken(questionsUrl, httpClient,usuario);
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
                    java.util.Date parsedDate = null;
                    try {
                        parsedDate = dateFormat.parse(dateCreatedStr);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    java.sql.Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
                    question.dateCreated = new java.sql.Date(timestamp.getTime());
                    question.status = jsonQuestion.getString("status");
                    JSONObject fromJsonObject = jsonQuestion.getJSONObject("from");
                    question.fromUserId=""+fromJsonObject.getInt("id");
                    if (jsonQuestion.get("answer") instanceof JSONObject) {
                        JSONObject answerJsonObject = answerJsonObject = jsonQuestion.getJSONObject("answer");
                        dateCreatedStr=answerJsonObject.getString("date_created");
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
        JSONObject jsonOrders = HttpUtils.getJsonObjectUsingToken(ordersUrl, httpClient,usuario);
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


    private static ArrayList<Info> getVisitsAndQuestions(CloseableHttpClient httpClient, Date date, ArrayList<String> allProductIDsArrayList, HashMap<String,Info> allProductFixedDetailsHashMap,HashMap<String,ArrayList<Question>> allTimesQuestionsHashMap) {

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

            productId="MLA-"+productId.substring(3);

            Info info = new Info();
            info.date=date;
            info.productId=productId;
            info.visits=visits;
            info.questions=totalQuestions;
            info.orders=salesTotal;
            info.active=fixedProductDetails.active;
            info.price=fixedProductDetails.price;
            info.title=fixedProductDetails.title;

            results.add(info);
            String msg = date.toString()+" "+productId+" visitas:"+visits+" preguntas:"+totalQuestions+" ventas:"+salesTotal+" activo:"+info.active+" precio:"+info.price+" descripcion:"+info.title;
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
            jsonResponse = HttpUtils.getJsonObjectUsingToken(productListURL, httpClient,usuario);
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

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        Date registrationDate=getRegistrationDate(httpClient);
        Date todaysDate = getGlobalDate();
        Date aYearAgo = new Date(todaysDate.getTime()-oneDayinMiliseconds*360);
        Date lastDairyUpdate = DatabaseHelper.fetchLasLastDailyUpdate(DATABASE);
        if (lastDairyUpdate==null){ //tabla vacia
            if (registrationDate.after(aYearAgo)) {
                lastDairyUpdate = registrationDate;
            }else {
                lastDairyUpdate=aYearAgo;
            }
        }

        ArrayList<String> allProductIDsArrayList = getAllProductIDs(httpClient);
        HashMap<String,Info> allProductFixedDetailsHashMap = getProductDetails(httpClient,allProductIDsArrayList);

        HashMap<String,ArrayList<Question>> allTimesQuestionsHashMap= getAllTimesQuestions(httpClient,allProductIDsArrayList);

        processDaily(httpClient, todaysDate, lastDairyUpdate,allProductIDsArrayList,allProductFixedDetailsHashMap,allTimesQuestionsHashMap);

        processWeekly(registrationDate, lastDairyUpdate);

        processMonthly(registrationDate, lastDairyUpdate);

    }

    private static void processDaily(CloseableHttpClient httpClient, Date todaysDate, Date lastDairyUpdate, ArrayList<String> allProductIDsArrayList,HashMap<String,Info> allProductFixedDetailsHashMap, HashMap<String,ArrayList<Question>> allTimesQuestionsHashMap) {
        String msg="*********** Proceso diario";
        System.out.println(msg);
        Logger.log(msg);


        Date dailyStartDate=new Date(lastDairyUpdate.getTime()+oneDayinMiliseconds);
        Date dailyEndDate=new Date(todaysDate.getTime()-oneDayinMiliseconds);
        ArrayList<Info> infoArrayList = new ArrayList<Info>();
        for (long iterator=dailyStartDate.getTime(); iterator<=dailyEndDate.getTime(); iterator+=oneDayinMiliseconds) {
            Date date = new Date(iterator);
            System.out.println("Processing date: "+date);
            infoArrayList.addAll(getVisitsAndQuestions(httpClient, date, allProductIDsArrayList, allProductFixedDetailsHashMap,allTimesQuestionsHashMap));
        }
        for (Info info:infoArrayList){
            DatabaseHelper.insertDaily(DATABASE, info.date,info.productId, info.orders, info.visits, info.questions, info.active, info.price, info.title);
        }
    }

    private static void processWeekly(Date registrationDate, Date lastDairyUpdate) {
        String msg="*********** Proceso semanal";
        System.out.println(msg);
        Logger.log(msg);

        Date lastWeeklyUpdate = DatabaseHelper.fetchLasLastWeeklyUpdate(DATABASE);
        if (lastWeeklyUpdate==null){
            lastWeeklyUpdate=registrationDate;
        }

        Date weeklyStartDate=followingDayOfWeek(lastWeeklyUpdate,Calendar.MONDAY);
        Date weeklyEndDate=previousOrSameDayOfWeek(lastDairyUpdate, Calendar.SUNDAY);
        ResultSet rs = DatabaseHelper.fetchAllDailyUpdatesBetweenDates(DATABASE,weeklyStartDate,weeklyEndDate);
        boolean finished=false;
        Info weekInfo = new Info();
        try {
            finished=!rs.next();
            if (!finished){
                weekInfo.date = rs.getDate(1);
                weekInfo.productId = rs.getString(2);
                weekInfo.visits = rs.getLong(3);
                weekInfo.questions = rs.getLong(4);
                weekInfo.orders = rs.getLong(5);
                weekInfo.active = rs.getBoolean(6);
                weekInfo.price = rs.getDouble(7);
                weekInfo.title = rs.getString(8);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

        Date startDate=null;
        Date followingSunday=null;
        Date followingMonday=null;
        String productId=null;

        while (!finished) {
            startDate=weekInfo.date;
            followingSunday=followingDayOfWeek(startDate,Calendar.SUNDAY);
            followingMonday=followingDayOfWeek(startDate,Calendar.MONDAY);
            productId=weekInfo.productId;

            long visits=0;
            long questions=0;
            long orders=0;
            long pausedDays=0;
            String title=null;
            double price=0.0;

            while (weekInfo.productId.equals(productId) && weekInfo.date.before(followingMonday) && !finished) {
                visits+=weekInfo.visits;
                questions+=weekInfo.questions;
                orders+=weekInfo.orders;
                if (!weekInfo.active){
                    pausedDays++;
                }
                title=weekInfo.title;
                price=weekInfo.price;

                try {
                    if (!rs.next()) {
                        finished = true;
                    } else {
                        weekInfo.date = rs.getDate(1);
                        weekInfo.productId = rs.getString(2);
                        weekInfo.visits = rs.getLong(3);
                        weekInfo.questions = rs.getLong(4);
                        weekInfo.orders = rs.getLong(5);
                        weekInfo.active = rs.getBoolean(6);
                        weekInfo.price = rs.getDouble(7);
                        weekInfo.title = rs.getString(8);

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            /*
            ArrayList<String> arrayListWithOneProductId= new ArrayList<String>();
            String unformatedProductId=productId.substring(0,3)+productId.substring(4);
            arrayListWithOneProductId.add(unformatedProductId);
            HashMap<String,Integer> visitsHashMapWithOneProductId = VisitCounter.processVisits(startDate,followingSunday,arrayListWithOneProductId,false);
            long visits2=visitsHashMapWithOneProductId.get(unformatedProductId);
            if (visits<visits2){
                boolean si=false;
            } else {
                boolean no=false;
            }*/
            msg = productId+" "+startDate.toString()+"/"+followingSunday.toString()+" visitas:"+visits+" preguntas:"+questions+" ventas:"+orders+" dias en pausa:"+pausedDays+" precio:"+price+" titulo:"+title;
            System.out.println(msg);
            Logger.log(msg);
            DatabaseHelper.insertWeekly(DATABASE,startDate,followingSunday,productId,orders,visits,questions,pausedDays,price,title);
        }

    }

    private static void processMonthly(Date registrationDate, Date lastDairyUpdate) {
        String msg="*********** Proceso mensual";
        System.out.println(msg);
        Logger.log(msg);


        Date lastMonthlyUpdate = DatabaseHelper.fetchLasLastMonthlyUpdate(DATABASE);
        if (lastMonthlyUpdate==null){
            lastMonthlyUpdate=registrationDate;
        }
        Date monthlyStartDate = firstDayNextMonth(lastMonthlyUpdate);
        Date monthlyEndDate = lastDayPreviousMonth(lastDairyUpdate);

        ResultSet rs = DatabaseHelper.fetchAllDailyUpdatesBetweenDates(DATABASE,monthlyStartDate,monthlyEndDate);
        boolean finished=false;
        Info monthInfo = new Info();
        try {
            finished=!rs.next();
            if (!finished){
                monthInfo.date = rs.getDate(1);
                monthInfo.productId = rs.getString(2);
                monthInfo.visits = rs.getLong(3);
                monthInfo.questions = rs.getLong(4);
                monthInfo.orders = rs.getLong(5);
                monthInfo.active = rs.getBoolean(6);
                monthInfo.price = rs.getDouble(7);
                monthInfo.title = rs.getString(8);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

        while (!finished) {
            Date startDate=monthInfo.date;
            Date firstDayNextMonth=firstDayNextMonth(startDate);
            Date followingEndOfMonth=new Date(firstDayNextMonth.getTime()-oneDayinMiliseconds);
            String productId=monthInfo.productId;

            long visits=0;
            long questions=0;
            long orders=0;
            long pausedDays=0;
            String title=null;
            double price=0.0;

            while (monthInfo.productId.equals(productId) && monthInfo.date.before(firstDayNextMonth) && !finished) {
                visits+=monthInfo.visits;
                questions+=monthInfo.questions;
                orders+=monthInfo.orders;
                if (!monthInfo.active){
                    pausedDays++;
                }
                title=monthInfo.title;
                price=monthInfo.price;

                try {
                    if (!rs.next()) {
                        finished = true;
                    } else {
                        monthInfo.date = rs.getDate(1);
                        monthInfo.productId = rs.getString(2);
                        monthInfo.visits = rs.getLong(3);
                        monthInfo.questions = rs.getLong(4);
                        monthInfo.orders = rs.getLong(5);
                        monthInfo.active = rs.getBoolean(6);
                        monthInfo.price = rs.getDouble(7);
                        monthInfo.title = rs.getString(8);

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            msg = productId+" "+startDate.toString()+"/"+followingEndOfMonth.toString()+" visitas:"+visits+" preguntas:"+questions+" ventas:"+orders+" dias en pausa:"+pausedDays+" precio:"+price+" titulo:"+title;
            System.out.println(msg);
            Logger.log(msg);
            DatabaseHelper.insertMonthly(DATABASE,startDate,followingEndOfMonth,productId,orders,visits,questions,pausedDays,price,title);
        }
    }

    private static Date getRegistrationDate(CloseableHttpClient httpClient){
        Date result=null;
        String userUrl="https://api.mercadolibre.com/users/"+TokenUtils.getIdCliente(usuario);
        JSONObject jsonCustomer = HttpUtils.getJsonObjectWithoutToken(userUrl, httpClient);
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

    private static Date lastDayPreviousMonth(Date date){
        Date lastDayPreviousMonth = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        lastDayPreviousMonth = new Date(calendar.getTime().getTime()-oneDayinMiliseconds);
        return lastDayPreviousMonth;
    }
}
