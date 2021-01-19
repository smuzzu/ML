package com.ml.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Stream;


public class MessagesAndSalesHelper {

    static ArrayList<Message> allQuestionsArrayList=new ArrayList<Message>();
    static HashMap<Long,String> reviewsHashMap = new HashMap<Long,String>();
    static ArrayList<String> allMyItems=new ArrayList<String>();

    //static final String usuario="ACACIAYLENGA";
    //static final String usuario="QUEFRESQUETE";
    static long tooMuchTime=10000l;
    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSX");

    public static boolean isDelivered(JSONObject orderShippingObj, JSONObject shippingObject){
        boolean result = false;
        String shippingStatus=getStringValue(orderShippingObj,shippingObject,"status");
        if (shippingStatus != null && (shippingStatus.equals("shipped") || shippingStatus.equals("delivered"))) {
            result = true;
        }
        return result;
    }


    public static boolean isReadyForSending(JSONObject orderShippingObj, JSONObject shippingObject){
        boolean result = false;
        String shippingStatus=getStringValue(orderShippingObj,shippingObject,"substatus");
        if (shippingStatus != null && (shippingStatus.equals("ready_to_print") || shippingStatus.equals("printed"))) {
            result = true;
        }
        return result;
    }


    public static boolean isFulfilled(JSONObject jsonOrder){
        boolean result = false;
        if (jsonOrder.has("fulfilled")) {
            if (!jsonOrder.isNull("fulfilled")) {
                result = jsonOrder.getBoolean("fulfilled");
            }
        }
        return result;
    }

    public static JSONObject getShippingObjectIfNeeded(JSONObject orderShippingObj, CloseableHttpClient httpClient, String user){
        JSONObject shippingObj=null;
        if (orderShippingObj!=null){
            if (orderShippingObj.has("id") && !orderShippingObj.isNull("id")){
                long shippingId = orderShippingObj.getLong("id");
                if (shippingId>0) {
                    if (!orderShippingObj.has("status") || orderShippingObj.isNull("status")) {
                        String shippingUrl = "https://api.mercadolibre.com/shipments/" + shippingId + "?";
                        shippingObj = HttpUtils.getJsonObjectUsingToken(shippingUrl, httpClient, user, false);
                    }
                }
            }
        }
        return shippingObj;
    }

    public static boolean isCancelled(JSONObject jsonOrder){
        boolean result = false;
        if (jsonOrder.has("status")) {
            if (!jsonOrder.isNull("status")) {
                String status = jsonOrder.getString("status");
                if (status!=null && status.equals("cancelled") ) {
                    result = true;
                }
            }
        }
        return result;
    }

    public static boolean isReturned(JSONObject jsonOrder){
        boolean result = false;
        JSONArray mediationsJSONArray = jsonOrder.getJSONArray("mediations");
        if (mediationsJSONArray.length()>0){
            for (int i=0; i<mediationsJSONArray.length(); i++){
                JSONObject mediationObject = mediationsJSONArray.getJSONObject(i);
                String mediationStatus=mediationObject.getString("status");
                //todo profundizar tema reclamos recien abiertos aca aca
                if (mediationStatus.equals("return_closed")){
                    result=true;
                }
            }
        }
        if (!result){
            if (jsonOrder.has("shipping") && !jsonOrder.isNull("shipping")) {
                JSONObject shippingObject = jsonOrder.getJSONObject("shipping");
                if (shippingObject.has("substatus") && !shippingObject.isNull("substatus")) {
                    String shippingSubstatus = shippingObject.getString("substatus");
                    //habrá otros substatus de returned_to_warehouse?
                    if (shippingSubstatus.startsWith("returned")){
                        result=true;
                    }
                }
            }
        }
        return result;
    }



    private static HashMap<String,String> getStateHashMap(){
        HashMap<String,String> stateHashMap = new HashMap<String, String>();
        stateHashMap.put("AR-A","Salta");
        stateHashMap.put("AR-B","Buenos Aires");
        stateHashMap.put("AR-C","Capital Federal");
        stateHashMap.put("AR-D","San Luis");
        stateHashMap.put("AR-E","Entre Ríos");
        stateHashMap.put("AR-F","La Rioja");
        stateHashMap.put("AR-G","Santiago del Estero");
        stateHashMap.put("AR-H","Chaco");
        stateHashMap.put("AR-J","San Juan");
        stateHashMap.put("AR-K","Catamarca");
        stateHashMap.put("AR-L","La Pampa");
        stateHashMap.put("AR-M","Mendoza");
        stateHashMap.put("AR-N","Misiones");
        stateHashMap.put("AR-P","Formosa");
        stateHashMap.put("AR-Q","Neuquén");
        stateHashMap.put("AR-R","Río Negro");
        stateHashMap.put("AR-S","Santa Fe");
        stateHashMap.put("AR-T","Tucuman");
        stateHashMap.put("AR-U","Chubut");
        stateHashMap.put("AR-V","Tierra del Fuego");
        stateHashMap.put("AR-W","Corrientes");
        stateHashMap.put("AR-X","Córdoba");
        stateHashMap.put("AR-Y","Jujuy");
        stateHashMap.put("AR-Z","Santa Cruz");
        return stateHashMap;
    }



    public static ArrayList<Order> requestOrdersAndMessages(boolean includeDetails, boolean onlyPending,  String user, CloseableHttpClient httpClient){


        int totalOrders=Integer.MAX_VALUE;

        ArrayList<Order> orderArrayList = new ArrayList<Order>();
        long startTime=0;
        long elapsedTime=0;

        HashMap<String,String> stateHashMap = getStateHashMap();

        HashMap<String,Integer> usersThatLeftMessage = new HashMap<String,Integer>();
        HashMap<String,String> userFeedbackMessages = new HashMap<String,String>();
        HashMap<String,Integer> usersInOrders = new HashMap<String,Integer>();


        if (includeDetails){
            boolean finished=false;
            int offset=0;
            while (!finished) {
                String feedbackUrl = "https://www.mercadolibre.com.ar/perfil/api/feedback/askForFeedback?userIdentifier=nickname%3D" + user + "&rating=all&limit=200&offset=" + offset + "&role=seller";
                JSONObject feedbacksObj = HttpUtils.getJsonObjectWithoutToken(feedbackUrl, httpClient, false);
                JSONArray feedbacksArray = feedbacksObj.getJSONArray("feedbacks");
                for (Object feedbackObject : feedbacksArray){
                    JSONObject feedbackJSONObject = (JSONObject)feedbackObject;
                    String message = feedbackJSONObject.getString("message");
                    if (message.length()>0){
                        String nickName = feedbackJSONObject.getJSONObject("user").getString("nickname");
                        if (usersThatLeftMessage.containsKey(nickName)){
                            int numberOfMessages = usersThatLeftMessage.get(nickName);
                            numberOfMessages++;
                            usersThatLeftMessage.replace(nickName,numberOfMessages);
                        } else {
                            usersThatLeftMessage.put(nickName,1);
                            userFeedbackMessages.put(nickName,message);
                        }
                    }
                }
                offset+=200;
                if (!feedbacksObj.has("subtitle")){
                    finished=true;
                }
            }
            for (String nickname: usersThatLeftMessage.keySet()){
                if (usersThatLeftMessage.get(nickname)>1){
                    userFeedbackMessages.remove(nickname);
                }
            }
            usersThatLeftMessage.clear();
            usersThatLeftMessage=null;
        }


        for (int ordersOffset=0; ordersOffset<=totalOrders; ordersOffset+=50) {

            String ordersUrl = "https://api.mercadolibre.com/orders/search?seller=" + TokenUtils.getIdCliente(user) + "&sort=date_desc&offset="+ordersOffset;

            JSONObject jsonOrders = HttpUtils.getJsonObjectUsingToken(ordersUrl, httpClient, user, false);
            if (ordersOffset==0){
                JSONObject pagingObj = jsonOrders.getJSONObject("paging");
                totalOrders=pagingObj.getInt("total");
                String msg="total ventas registradas: "+totalOrders;
                System.out.println(totalOrders);
                Logger.log(msg);
            }
            JSONArray jsonOrdersArray = jsonOrders.getJSONArray("results");
            for (Object orderObject : jsonOrdersArray) {
                Order order = new Order();
                JSONObject jsonOrder = (JSONObject) orderObject;
                order.id=jsonOrder.getLong("id");
                if (orderArrayList.contains(order)){
                    continue;
                }

                JSONObject orderShippingObj = jsonOrder.getJSONObject("shipping");

                JSONObject shippingObj = getShippingObjectIfNeeded(orderShippingObj,httpClient,user);
                order.delivered=isDelivered(orderShippingObj,shippingObj);
                order.readyForSending=isReadyForSending(orderShippingObj,shippingObj);

                order.fulfilled=isFulfilled(jsonOrder);
                order.cancelled=isCancelled(jsonOrder);
                order.returned=isReturned(jsonOrder);

                order.pending=true;
                if (order.delivered || order.fulfilled || order.cancelled || order.returned){
                    order.pending=false;
                    if (onlyPending){
                        continue;
                    }
                }

                order=processOrder(order,jsonOrder,shippingObj,stateHashMap,usersInOrders,includeDetails,user,httpClient);
                orderArrayList.add(order);

                System.out.println(order.creationTimestamp +" "+ order.orderStatus+" "+ order.userNickName + " "+order.buyerAddressState+","+order.buyerAddressCity);
                elapsedTime=System.currentTimeMillis()-startTime;
                if (elapsedTime>=tooMuchTime){
                    httpClient=HttpUtils.buildHttpClient();
                }
            }
        }

        //lo mismo para las archived orders no se si vale la pena
        if (!onlyPending) {
            for (int ordersOffset = 0; ordersOffset <= totalOrders; ordersOffset += 50) {
                String ordersUrl = "https://api.mercadolibre.com/orders/search/archived?seller=" + TokenUtils.getIdCliente(user) + "&sort=date_desc&offset=" + ordersOffset;

                JSONObject jsonOrders = HttpUtils.getJsonObjectUsingToken(ordersUrl, httpClient, user, false);
                if (ordersOffset == 0) {
                    JSONObject pagingObj = jsonOrders.getJSONObject("paging");
                    totalOrders = pagingObj.getInt("total");
                    String msg = "total ventas registradas: " + totalOrders;
                    System.out.println(totalOrders);
                    Logger.log(msg);
                }
                JSONArray jsonOrdersArray = jsonOrders.getJSONArray("results");
                for (Object orderObject : jsonOrdersArray) {
                    Order order = new Order();
                    JSONObject jsonOrder = (JSONObject) orderObject;
                    order.id = jsonOrder.getLong("id");
                    if (orderArrayList.contains(order)) {
                        continue;
                    }

                    JSONObject orderShippingObj = jsonOrder.getJSONObject("shipping");
                    JSONObject shippingObj = getShippingObjectIfNeeded(jsonOrder,httpClient,user);
                    order.delivered=isDelivered(orderShippingObj,shippingObj);
                    order.readyForSending=isReadyForSending(orderShippingObj,shippingObj);

                    order.fulfilled=isFulfilled(jsonOrder);
                    order.cancelled=isCancelled(jsonOrder);
                    order.returned=isReturned(jsonOrder);
                    order.pending=true;
                    if (order.delivered || order.fulfilled || order.cancelled || order.returned){
                        order.pending=false;
                    }

                    order = processOrder(order, jsonOrder, shippingObj, stateHashMap, usersInOrders, includeDetails, user, httpClient);

                    orderArrayList.add(order);

                    System.out.println(order.creationTimestamp +" "+ order.orderStatus+" "+ order.userNickName + " "+order.buyerAddressState+","+order.buyerAddressCity);
                    elapsedTime=System.currentTimeMillis()-startTime;
                    if (elapsedTime>=tooMuchTime){
                        httpClient=HttpUtils.buildHttpClient();
                    }

                }
            }
        }


            //adding feedback messages + checking multi items
        for (Order order:orderArrayList) {
            int numberOfOrders = usersInOrders.get(order.userNickName);
            if (numberOfOrders == 1 && userFeedbackMessages.containsKey(order.userNickName)) {
                order.receivedFeedbackComment = userFeedbackMessages.get(order.userNickName);
            }
            for (Order order2: orderArrayList){
                if (order.shippingId==order2.shippingId && order.id!=order2.id){
                    order.multiItem=true;
                    order2.multiItem=true;
                }
            }
        }

        Collections.sort(orderArrayList);
        return orderArrayList;
    }

    private static ArrayList<Message> getAllQuestions(String user, CloseableHttpClient httpClient) {
        System.out.println("Cargando todas las preguntas de "+user);
        ArrayList<Message> previousQuestionsArrayList = new ArrayList<>();
        boolean finished=false;
        int offset=0;

        ArrayList<String> itemsIdArrayList = new ArrayList<String>();
        offset=0;
        finished=false;
        while (!finished) {
            String publicationsUrl = "https://api.mercadolibre.com/users/"+TokenUtils.getIdCliente(user)
                    +"/items/search?offset=" + offset;
            JSONObject publicationsObj = HttpUtils.getJsonObjectUsingToken(publicationsUrl,httpClient,user,false);
            if (publicationsObj==null){
                finished=true;
                continue;
            }
            JSONArray itemsArray = publicationsObj.getJSONArray("results");
            if (itemsArray.length()==0){
                finished=true;
                continue;
            }
            for (Object itemObject : itemsArray) {
                String itemId=(String) itemObject;
                itemsIdArrayList.add(itemId);
            }
            offset+=50;
        }

        for (String itemId: itemsIdArrayList){
            offset=0;
            finished=false;
            while (!finished) {
                String questionsUrl = "https://api.mercadolibre.com/questions/search?item="+itemId+"&offset="+ offset;
                JSONObject questionsObj = HttpUtils.getJsonObjectUsingToken(questionsUrl,httpClient,user,false);
                if (questionsObj==null){
                    finished=true;
                    continue;
                }
                JSONArray questionsArray = questionsObj.getJSONArray("questions");
                if (questionsArray.length()==0){
                    finished=true;
                    continue;
                }
                for (Object questionObject : questionsArray){
                    JSONObject questionJSONObject = (JSONObject)questionObject;
                    addQuestionToArray(previousQuestionsArrayList, questionJSONObject);
                }
                offset+=50;
            }
        }

        Collections.sort(previousQuestionsArrayList);

        return previousQuestionsArrayList;
    }

    private static String getQuestionFileName(String user){
        return "C:\\centro\\questions_"+ user +".txt";
    }


    private static ArrayList<Message> updateQuestionsFile(String user, CloseableHttpClient httpClient) {
        System.out.println("Buscando las nuevas preguntas de "+user);

        String fileName = getQuestionFileName(user);

        ArrayList<Message> previousQuestionsArrayList  = new ArrayList<Message>();
        ArrayList<Message> newQuestionsArrayList = new ArrayList<>();
        Timestamp weekBefore=getTimestamp("1970-01-01 03:10:00.001-03");

        File f = new File(fileName);
        if (f.exists() && !f.isDirectory()) {
            previousQuestionsArrayList  = getQuestionsFromFile(fileName);

            int lastItem=previousQuestionsArrayList.size()-1;
            Message lastQuestion = previousQuestionsArrayList.get(lastItem);
            weekBefore=new Timestamp(lastQuestion.creationTimestamp.getTime()-604800000L);

            boolean finished=false;
            int offset=0;
            while (!finished) {
                String questionsUrl = "https://api.mercadolibre.com/my/received_questions/search?sort_fields=date_created&sort_types=DESC&offset="+ offset;
                JSONObject questionsObj = HttpUtils.getJsonObjectUsingToken(questionsUrl,httpClient,user,false);
                if (questionsObj==null){
                    finished=true;
                    continue;
                }
                JSONArray questionsArray = questionsObj.getJSONArray("questions");
                if (questionsArray.length()==0){
                    finished=true;
                    continue;
                }
                for (Object questionObject : questionsArray){
                    JSONObject questionJSONObject = (JSONObject)questionObject;
                    addQuestionToArray(newQuestionsArrayList, questionJSONObject);
                }
                lastItem=newQuestionsArrayList.size()-1;
                lastQuestion = newQuestionsArrayList.get(lastItem);
                if (lastQuestion.creationTimestamp.before(weekBefore)){
                    finished=true;
                    continue;
                }
                offset+=50;
            }
        }else {
            newQuestionsArrayList=getAllQuestions(user,httpClient);
        }

        ArrayList<Message> questionsToSaveArrayList = new ArrayList<>();
        for (Message question: newQuestionsArrayList){
            if (!previousQuestionsArrayList.contains(question)){
                questionsToSaveArrayList.add(question);
            }else {
                int index = previousQuestionsArrayList.indexOf(question);
                Message previousQuestion = previousQuestionsArrayList.get(index);
                if (previousQuestion.direction=='E'){
                    if (previousQuestion.text==null || previousQuestion.text.isEmpty()){
                        if (question.text!=null && !question.text.isEmpty()){
                            questionsToSaveArrayList.add(question);
                        }
                    }
                }
            }
        }

        for (Message previousQuestion: previousQuestionsArrayList){
            if (previousQuestion.creationTimestamp!=null &&
                    previousQuestion.creationTimestamp.after(weekBefore)){
                if (previousQuestion.direction=='R' && !previousQuestion.deleted){
                    if (!newQuestionsArrayList.contains(previousQuestion)){ //los borrados
                        previousQuestion.deleted=true;
                        questionsToSaveArrayList.add(previousQuestion);
                    }
                }
            }
        }

        Collections.sort(questionsToSaveArrayList);

        for (Message questionToSave: questionsToSaveArrayList){
            String dateStr="";
            if (questionToSave.creationTimestamp!=null){
                dateStr=dateFormat.format(questionToSave.creationTimestamp);
            }
            questionToSave.text=questionToSave.text.replace('|',' ');
            String questionRecord=questionToSave.id+"|"+questionToSave.direction+"|"+dateStr+"|"+
                    questionToSave.productId+"|"+questionToSave.text+"|"+questionToSave.deleted
                    +"|"+questionToSave.customerId;
            Logger.writeOnFile(fileName,questionRecord);
        }

        return newQuestionsArrayList;
    }


    private static ArrayList<Message>  getQuestionsFromFile(String fileName) {

        ArrayList<Message> previousQuestionsArrayList = new ArrayList<Message>();

        ArrayList<String> questionsFromFileArrayList=new ArrayList<String>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName), StandardCharsets.UTF_8)) {
            stream.forEach(s -> questionsFromFileArrayList.add(s));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        for (String questionFromFileStr : questionsFromFileArrayList){
            if (questionFromFileStr!=null && !questionFromFileStr.isEmpty()){
                Message question = new Message();
                int pos1=questionFromFileStr.indexOf("|");
                question.id=questionFromFileStr.substring(0,pos1);
                pos1++;
                int pos2=questionFromFileStr.indexOf("|",pos1);
                question.direction=questionFromFileStr.substring(pos1,pos2).charAt(0);
                pos1=pos2+1;
                pos2=questionFromFileStr.indexOf("|",pos1);
                String creationTimestampStr=questionFromFileStr.substring(pos1,pos2);
                if (creationTimestampStr!=null && !creationTimestampStr.isEmpty()){
                    question.creationTimestamp=getTimestamp(creationTimestampStr);
                }
                pos1=pos2+1;
                pos2=questionFromFileStr.indexOf("|",pos1);
                question.productId=questionFromFileStr.substring(pos1,pos2);
                pos1=pos2+1;
                pos2=questionFromFileStr.indexOf("|",pos1);
                question.text=questionFromFileStr.substring(pos1,pos2);
                pos1=pos2+1;
                pos2=questionFromFileStr.indexOf("|",pos1);
                String deletedStr = questionFromFileStr.substring(pos1,pos2);
                question.deleted=Boolean.parseBoolean(deletedStr);
                pos1=pos2+1;
                String custIdStr=questionFromFileStr.substring(pos1);
                try {
                    question.customerId = Long.parseLong(custIdStr);
                }catch (Exception e){
                    String errorMsg="Exception parsing "+custIdStr+" for question id="+question.id;
                    Logger.log(errorMsg);
                    Logger.log(e);
                    System.out.println(errorMsg);
                    e.printStackTrace();
                }

                if (previousQuestionsArrayList.contains(question)){
                    previousQuestionsArrayList.remove(question);
                }
                previousQuestionsArrayList.add(question);
            }
        }

        Collections.sort(previousQuestionsArrayList);

        return previousQuestionsArrayList;
    }


    private static ArrayList<Message> getQuestionsFromUser(String user, long custId, CloseableHttpClient httpClient) {
        ArrayList<Message> previousQuestionsArrayList = new ArrayList<>();

        if (allMyItems.size()==0) {
            allMyItems = getAllMyItems(user, httpClient);
        }

        for (String productId: allMyItems) {
            String questionsUrl = "https://api.mercadolibre.com/questions/search?item=" + productId + "&from=" + custId;
            JSONObject questionsObj = HttpUtils.getJsonObjectUsingToken(questionsUrl, httpClient, user,false);
            if (questionsObj == null) {
                continue;
            }
            JSONArray questionsArray = questionsObj.getJSONArray("questions");
            for (Object questionObject : questionsArray) {
                JSONObject questionJSONObject = (JSONObject) questionObject;
                addQuestionToArray(previousQuestionsArrayList, questionJSONObject);
            }
        }
        return previousQuestionsArrayList;
    }


    private static HashMap<Long,String> getAllReviews(String user, CloseableHttpClient httpClient) {
        HashMap<Long,String> reviewsHashMap = new HashMap<Long,String>();

        if (allMyItems.size()==0) {
            allMyItems = getAllMyItems(user, httpClient);
        }

        for (String productId: allMyItems) {
            String feedbacksUrl = "https://api.mercadolibre.com/reviews/item/" + productId;
            JSONObject feedbacksObject = HttpUtils.getJsonObjectWithoutToken(feedbacksUrl,httpClient,false);
            if (feedbacksObject == null) {
                continue;
            }
            JSONArray reviewsArray = feedbacksObject.getJSONArray("reviews");
            for (Object reviewObject : reviewsArray) {
                JSONObject reviewJSONObject = (JSONObject) reviewObject;
                long reviewerId=reviewJSONObject.getLong("reviewer_id");
                int rate=reviewJSONObject.getInt("rate");
                String title = reviewJSONObject.getString("title");
                String content = reviewJSONObject.getString("content");
                JSONObject reviewableObject= reviewJSONObject.getJSONObject("reviewable_object");
                String itemId=reviewableObject.getString("id");
                String reviewStr = itemId+"|"+rate+"* "+title.trim()+": "+content.trim();
                if (!reviewsHashMap.containsKey(reviewerId)){
                    reviewsHashMap.put(reviewerId,reviewStr);
                }
            }
        }
        return reviewsHashMap;
    }



    private static ArrayList<String> getAllMyItems(String user, CloseableHttpClient closeableHttpClient){
        ArrayList<String> itemsArrayList = new ArrayList<String>();
        boolean moreItems=true;
        String productListURL =null;
        JSONObject jsonResponse=null;
        JSONArray jsonProductArray=null;
        String scrollId=null;
        while (moreItems){
            productListURL = "https://api.mercadolibre.com/users/" + TokenUtils.getIdCliente(user) + "/items/search?search_type=scan";
            if (scrollId!=null){
                productListURL+="&scroll_id="+scrollId;
            }
            jsonResponse = HttpUtils.getJsonObjectUsingToken(productListURL, closeableHttpClient,user, false);
            scrollId=(String) jsonResponse.get("scroll_id");
            jsonProductArray = jsonResponse.getJSONArray("results");
            if (jsonProductArray.length()<50){
                moreItems=false;//end
            }
            for (Object productIdÒbj : jsonProductArray) {
                String productId = (String) productIdÒbj;
                itemsArrayList.add(productId);
            }
        }
        return itemsArrayList;
    }


    private static Timestamp getTimestamp(String dateStr){
        Timestamp result=null;
        dateStr=dateStr.replace('T',' ');
        try {
            java.util.Date date = dateFormat.parse(dateStr);
            result =new Timestamp(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void addQuestionToArray(ArrayList<Message> previousQuestionsArrayList, JSONObject questionJSONObject) {
        Message question = new Message();
        Message answer = new Message();
        long id=questionJSONObject.getLong("id");
        String formattedId=String.format("%016d", id);
        question.id=formattedId+"P";
        answer.id=formattedId+"R";

        question.deleted=false;
        answer.deleted=false;

        String dateCreatedStr=questionJSONObject.getString("date_created");
        question.creationTimestamp=getTimestamp(dateCreatedStr);

        JSONObject from = questionJSONObject.getJSONObject("from");
        question.customerId=from.getLong("id");
        answer.customerId=question.customerId;
        question.text= questionJSONObject.getString("text");
        question.direction='R';
        answer.direction='E';
        if (questionJSONObject.has("answer") && !questionJSONObject.isNull("answer")) {
            JSONObject answerJSONObject = questionJSONObject.getJSONObject("answer");
            answer.text=answerJSONObject.getString("text");
            dateCreatedStr=answerJSONObject.getString("date_created");
            answer.creationTimestamp=getTimestamp(dateCreatedStr);
        }else {
            answer.text="";
            answer.creationTimestamp=question.creationTimestamp;
        }
        question.productId= questionJSONObject.getString("item_id");
        answer.productId=question.productId;
        if  (previousQuestionsArrayList.contains(question)) {
            previousQuestionsArrayList.remove(question);
        }
        previousQuestionsArrayList.add(question);
        if (previousQuestionsArrayList.contains(answer)) {
            previousQuestionsArrayList.remove(answer);
        }
        previousQuestionsArrayList.add(answer);
    }


    public static Order getOrderDetails(CloseableHttpClient httpClient, String user, long orderId){
        String orderUrlListForOneURL="https://api.mercadolibre.com/orders/search?seller="
                +TokenUtils.getIdCliente(user) +"&q="+orderId;

        JSONObject jsonOrders = HttpUtils.getJsonObjectUsingToken(orderUrlListForOneURL, httpClient, user, false);
        JSONArray jsonOrdersArray = jsonOrders.getJSONArray("results");

        if (jsonOrdersArray.length()!=1){
            String msg = "error buscando "+orderUrlListForOneURL;
            Logger.log(msg);
            System.out.println(msg);
        }

        JSONObject jsonOrder = jsonOrdersArray.getJSONObject(0);

        Order order = new Order();
        order.id = orderId;

        JSONObject orderShippingObj = jsonOrder.getJSONObject("shipping");
        JSONObject shippingObj = getShippingObjectIfNeeded(orderShippingObj,httpClient,user);
        order.delivered=isDelivered(orderShippingObj,shippingObj);
        order.readyForSending=isReadyForSending(orderShippingObj,shippingObj);

        order.fulfilled=isFulfilled(jsonOrder);
        order.cancelled=isCancelled(jsonOrder);
        order.returned=isReturned(jsonOrder);
        order.pending=true;
        if (order.delivered || order.fulfilled || order.cancelled || order.returned){
            order.pending=false;
        }

        HashMap<String,String> stateHashMap = getStateHashMap();

        order = processOrder(order, jsonOrder, shippingObj, stateHashMap, new HashMap<String,Integer>(), true, user, httpClient);
        return order;
    }

    private static Order processOrder(Order order,JSONObject jsonOrder,JSONObject shippingObj, HashMap<String,String> stateHashMap, HashMap<String,Integer> usersInOrders, boolean includeDetails, String user, CloseableHttpClient httpClient){
        order.sellerName =user;
        order.delivered=false;
        order.waitingForWithdrawal=false;
        order.cancelled=false;
        order.refunded=false;
        order.finished=false;
        order.timeoutFulfilled =false;
        order.multiItem=false;
        order.messageArrayList=new ArrayList<Message>();
        order.paymentMethod ="";
        order.paymentInstallments=false;

        order.productVariationText ="N/A";

        order.buyerEmail="N/A";
        order.buyerAddressState="N/A";
        order.buyerAddressCity="N/A";
        order.buyerAddressZip="N/A";
        order.buyerAddressStreet="N/A";

        order.receivedFeedbackRating="";
        order.receivedFeedbackComment="";

        String dateCreatedStr=jsonOrder.getString("date_created");
        order.creationTimestamp=getTimestamp(dateCreatedStr);

        String dateUpdatedStr=jsonOrder.getString("last_updated");
        order.updateTimestamp=getTimestamp(dateUpdatedStr);

        order.paymentAmount=""+jsonOrder.getDouble("total_amount");

        order.paymentStatus = jsonOrder.getString("status");

        if (jsonOrder.has("buyer")) {
            JSONObject buyerObject = jsonOrder.getJSONObject("buyer");
            order.buyerCustId=buyerObject.getLong("id");
            if (buyerObject.has("nickname")) {
                order.userNickName = buyerObject.getString("nickname");
                order.buyerFirstName=humanNameFormater(buyerObject.getString("first_name"));
                order.buyerLastName=humanNameFormater(buyerObject.getString("last_name"));
            }
            if (buyerObject.has("phone")) {
                JSONObject phoneObj = buyerObject.getJSONObject("phone");
                if (!phoneObj.isNull("number")) {
                    order.buyerPhone = phoneObj.getString("number");
                }
            }
            if (buyerObject.has("billing_info")) {
                JSONObject billingInfoObj = buyerObject.getJSONObject("billing_info");
                if (!billingInfoObj.isNull("doc_number")) {
                    order.buyerDocNumber = "" + billingInfoObj.getLong("doc_number");
                }
            }
        }
        if (includeDetails) {
            String buyerUrl = "https://api.mercadolibre.com/users/" + order.buyerCustId;
            JSONObject buyerObj = HttpUtils.getJsonObjectWithoutToken(buyerUrl, httpClient, false);

            if (buyerObj!=null && !buyerObj.isEmpty()) {

                if (order.userNickName==null || order.userNickName.isEmpty()) {
                    order.userNickName = buyerObj.getString("nickname");
                }

                JSONObject addressObj = buyerObj.getJSONObject("address");

                String state = null;
                if (!addressObj.isNull("state")) {
                    state = addressObj.getString("state");//aca puede venir el id o la descripcion del estado
                    if (stateHashMap.containsKey(state)) {
                        order.userState = stateHashMap.get(state);
                    } else {
                        order.userState = state;
                    }
                }
                if (!addressObj.isNull("city")) {
                    order.userCity = addressObj.getString("city");
                }
            }
        }
        if (usersInOrders.containsKey(order.userNickName)){
            int orderCount = usersInOrders.get(order.userNickName);
            orderCount++;
            usersInOrders.replace(order.userNickName,orderCount);
        }else {
            usersInOrders.put(order.userNickName,1);
        }



        order.finished=order.fulfilled;

        //messages
        order.packId = order.id;
        if (!jsonOrder.isNull("pack_id")) {
            order.packId = jsonOrder.getLong("pack_id");
        }

        JSONArray itemsArray = jsonOrder.getJSONArray("order_items");
        JSONObject itemObject = itemsArray.getJSONObject(0);
        order.productQuantity=itemObject.getInt("quantity");
        JSONObject itemObject2 = itemObject.getJSONObject("item");
        order.productId=itemObject2.getString("id");
        order.productCategoryId=itemObject2.getString("category_id");
        order.productTitle=itemObject2.getString("title");
        order.productVariationId=0;//no variation
        if (!itemObject2.isNull("variation_id")) {
            order.productVariationId = itemObject2.getLong("variation_id");
        }
        order.productManufacturingDays=0;
        if (!itemObject.isNull("manufacturing_days")) {
            order.productManufacturingDays = itemObject.getInt("manufacturing_days");
        }


        JSONArray variationsArray = itemObject2.getJSONArray("variation_attributes");
        if (variationsArray.length()>0){
            JSONObject variationObject = variationsArray.getJSONObject(0);
            variationObject = variationsArray.getJSONObject(0);
            order.productVariationName1 =variationObject.getString("name");
            order.productVariationValue1 =variationObject.getString("value_name");
            if (order.productVariationValue1!=null && order.productVariationValue1.length()>1 &&
                    order.productVariationValue1.charAt(1)=='.'){
                //cocinamos esto de 1.Negro 2.Gris etc
                order.productVariationValue1 =order.productVariationValue1.substring(2);
            }

            if (variationsArray.length()==2){
                variationObject = variationsArray.getJSONObject(1);
                order.productVariationName2 =variationObject.getString("name");
                order.productVariationValue2 =variationObject.getString("value_name");
                if (order.productVariationValue2.charAt(1)=='.'){
                    //cocinamos esto de 1.Negro 2.Gris etc
                    order.productVariationValue2 =order.productVariationValue2.substring(2);
                }
            }

            order.productVariationText =order.productVariationName1 +" "+order.productVariationValue1;
            if (order.productVariationName2!=null && !order.productVariationValue2.isEmpty()) {
                order.productVariationText +=" " + order.productVariationName2 + " " + order.productVariationValue2;
            }
        }

        String url = "https://api.mercadolibre.com/items/"+order.productId;
        JSONObject publicationJsonObject = HttpUtils.getJsonObjectWithoutToken(url,httpClient, false);
        order.productPictureURL= getOrderPictureUrl(publicationJsonObject,order.productVariationId,false);
        order.productPictureThumbnailURL= getOrderPictureUrl(publicationJsonObject,order.productVariationId,true);
        if (publicationJsonObject.has("permalink")) {
            order.publicationURL = publicationJsonObject.getString("permalink");
        }

        JSONArray publicationAttributesJsonArray=null;
        if (publicationJsonObject!=null && publicationJsonObject.has("attributes") &&
                !publicationJsonObject.isNull("attributes")){
            publicationAttributesJsonArray=publicationJsonObject.getJSONArray("attributes");
            if (publicationAttributesJsonArray!=null){
                String publicationKeyAttributes="";
                for (int i=0; i<publicationAttributesJsonArray.length(); i++){
                    JSONObject attributeObject = publicationAttributesJsonArray.getJSONObject(i);
                    if (attributeObject!=null && attributeObject.has("name") &&
                            !attributeObject.isNull("name") && attributeObject.has("value_name") &&
                            !attributeObject.isNull("value_name") && attributeObject.has("id") &&
                            !attributeObject.isNull("id")){
                        String id = attributeObject.getString("id");
                        String name = attributeObject.getString("name");
                        String value = attributeObject.getString("value_name");
                        //if (Order.isProductKeyAttribute(id) && !valueId.equals("-1")){ //-1= invalido
                        if (Order.isProductKeyAttribute(id) && value!=null && !value.isEmpty()){ //-1= invalido
                            publicationKeyAttributes+=name+" "+value+" // ";
                        }

                    }
                }
                if (!publicationKeyAttributes.isEmpty()){
                    order.productKeyAttributes=publicationKeyAttributes;
                }
            }
        }



        JSONArray paymentsArray = jsonOrder.getJSONArray("payments");
        if (paymentsArray.length()>0) {
            for (int i = 0; i < paymentsArray.length(); i++) {
                JSONObject paymentObj = paymentsArray.getJSONObject(i);
                String paymentStatus = paymentObj.getString("status");
                if (paymentStatus.equals("refunded")) {
                    order.refunded = true;
                    break;
                }
            }
        }

        JSONObject orderShippingObj = jsonOrder.getJSONObject("shipping");
        if (orderShippingObj.has("id") && !orderShippingObj.isNull("id")) {
            order.shippingId = orderShippingObj.getLong("id");
        }
        if (shippingObj == null) {
            if (order.shippingId != 0) {
                String shippingUrl = "https://api.mercadolibre.com/shipments/" + order.shippingId + "?";
                shippingObj = HttpUtils.getJsonObjectUsingToken(shippingUrl, httpClient, user, false);
            }
        }

        boolean customShipping=false;
        String shippingMode = getStringValue(orderShippingObj,shippingObj,"mode");
        if (shippingMode!=null && shippingMode.equals("custom")){
            customShipping=true;
        }

        order.shippingStatus=getStringValue(orderShippingObj,shippingObj,"status");
        order.shippingSubStatus = getStringValue(orderShippingObj,shippingObj,"substatus");

        order.shippingTrackingNumber = getStringValue(orderShippingObj,shippingObj,"tracking_number");
        order.shippingCurrier = getStringValue(orderShippingObj,shippingObj,"tracking_method");
        order.shippingLogisticType = getStringValue(orderShippingObj,shippingObj,"logistic_type");
        boolean full=false;
        if (order.shippingLogisticType!=null && order.shippingLogisticType.equals("fulfillment")){
            full=true;
        }

        JSONObject shippingOptionObj = getObjectValue(orderShippingObj,shippingObj,"shipping_option");
        if (shippingOptionObj!=null && shippingOptionObj.has("name") && !shippingOptionObj.isNull("name")) {
            order.shippingOptionNameDescription = shippingOptionObj.getString("name");
        }

        JSONObject receiverAddressObj = getObjectValue(orderShippingObj,shippingObj,"receiver_address");
        if (receiverAddressObj!=null){
            order.shippingReceiverName = receiverAddressObj.getString("receiver_name");
            order.buyerAddressState = receiverAddressObj.getJSONObject("state").getString("name");
            order.buyerAddressCity = receiverAddressObj.getJSONObject("city").getString("name");
            order.buyerAddressZip = receiverAddressObj.getString("zip_code");
            order.buyerAddressStreet = receiverAddressObj.getString("address_line");
            if (receiverAddressObj.has("comment") && !receiverAddressObj.isNull("comment")) {
                order.buyerAddressComments = receiverAddressObj.getString("comment");
            }
            if (order.buyerPhone == null) {
                if (receiverAddressObj.has("receiver_phone")) {
                    if (!receiverAddressObj.isNull("receiver_phone")) {
                        order.buyerPhone = receiverAddressObj.getString("receiver_phone");
                    }
                }
            }
            order.shippingAddressLine1 = humanNameFormater(order.buyerAddressStreet);
            order.shippingAddressLine2 = "CP " + order.buyerAddressZip + " - " + humanNameFormater(order.buyerAddressCity + ", " + order.buyerAddressState);
            order.shippingAddressLine3 = order.buyerAddressComments;
        }

        if (order.shippingSubStatus!=null && order.shippingSubStatus.equals("waiting_for_withdrawal")) {
            order.waitingForWithdrawal = true;
        }

        order.shippingType=Order.UNKNOWN;
        //custom shipping
        if (customShipping) {
            order.shippingType = Order.PERSONALIZADO;
        }else {
            if (full) {
                order.shippingType = Order.FULL;
            } else {
                //acordar o mercadoenvios
                if (order.shippingStatus == null || order.shippingStatus.equals("to_be_agreed")) {
                    order.shippingType = Order.ACORDAR;
                    order.shippingOptionNameDescription = "Acordar";
                } else { //mercadoenvios
                    if (order.shippingLogisticType != null) {
                        if (order.shippingLogisticType.equals("self_service")) {//Rapido a domicilio
                            order.shippingType = Order.FLEX;
                        } else {
                            if (order.shippingLogisticType.contains("drop_off")) {//Correo a domicilio o retira en sucursal
                                order.shippingType = Order.CORREO;
                            }
                        }
                    }
                }
            }
        }

        if (includeDetails) {
            String billingInfoUrl = "https://api.mercadolibre.com/orders/" + order.id + "/billing_info?";
            JSONObject billingInfoObject = HttpUtils.getJsonObjectUsingToken(billingInfoUrl, httpClient, user, false);
            if (billingInfoObject != null) {
                JSONObject billingInfoObject2 = billingInfoObject.getJSONObject("billing_info");
                String billingDocType = "";
                String billingDocNUmber = "";
                String billingFirstName = "";
                String billingLastName = "";
                String billingStreetName = "";
                String billingStreetNumber = "";
                String billingZipCode = "";
                String billingCity = "";
                String billingState = "";
                String billingComments = "";
                if (billingInfoObject2 != null) {
                    billingDocType = billingInfoObject2.getString("doc_type");
                    billingDocNUmber = billingInfoObject2.getString("doc_number");
                    JSONArray additionalInfo = billingInfoObject2.getJSONArray("additional_info");
                    if (additionalInfo != null) {
                        for (int i = 0; i < additionalInfo.length(); i++) {
                            JSONObject infoObject = additionalInfo.getJSONObject(i);
                            if (infoObject != null) {
                                String type = infoObject.getString("type");
                                String value = infoObject.getString("value");
                                if (type != null && !type.isEmpty()) {
                                    if (value != null && !value.isEmpty()) {

                                        if (type.equals("FIRST_NAME")) {
                                            billingFirstName = value;
                                        } else if (type.equals("LAST_NAME")) {
                                            billingLastName = value;
                                        } else if (type.equals("STREET_NAME")) {
                                            billingStreetName = value;
                                        } else if (type.equals("STREET_NUMBER")) {
                                            billingStreetNumber = value;
                                        } else if (type.equals("ZIP_CODE")) {
                                            billingZipCode = value;
                                        } else if (type.equals("CITY_NAME")) {
                                            billingCity = value;
                                        } else if (type.equals("STATE_NAME")) {
                                            billingState = value;
                                        } else if (type.equals("COMMENT")) {
                                            billingComments = value;
                                        }

                                    }
                                }
                            }
                        }
                        order.billingDniCuit = billingDocType + " " + billingDocNUmber;
                        order.billingName = humanNameFormater(billingFirstName + " " + billingLastName);
                        order.billingAddressLine1 = humanNameFormater(billingStreetName) + " " + billingStreetNumber;
                        order.billingAddressLine2 = "CP " + billingZipCode + " - " + humanNameFormater(billingCity + ", " + billingState);
                        order.billingAddressLine3 = billingComments;
                    }
                }
            }
        }

        order.orderStatus=Order.VENDIDO;
        if (order.fulfilled || order.delivered){
            order.orderStatus=Order.ENTREGADO;
        }else {
            if (order.cancelled || order.returned){
                order.orderStatus=Order.CANCELADO;
            }
            //todo RECLAMO abierto
        }


        JSONObject feedbackObj = jsonOrder.getJSONObject("feedback");
        if (!feedbackObj.isNull("sale")) {
            JSONObject saleObject = feedbackObj.getJSONObject("sale");
            if (saleObject.getBoolean("fulfilled")) {
                order.delivered = true;
            }
        }
        if (!feedbackObj.isNull("purchase")) {
            JSONObject purchaseObject = feedbackObj.getJSONObject("purchase");
            if (purchaseObject.has("rating")) {
                if (!purchaseObject.isNull("rating")) {
                    order.receivedFeedbackRating = purchaseObject.getString("rating");
                }
            }
            if (purchaseObject.has("message")){
                if (!purchaseObject.isNull("message")) {
                    order.receivedFeedbackComment = purchaseObject.getString("message");
                }
            }
        }

        for (Object paymentObj: paymentsArray) {
            JSONObject paymentJSONObject = (JSONObject)paymentObj;
            order.paymentMethod += paymentJSONObject.getString("payment_method_id")+" / ";
            if (paymentJSONObject.getInt("installments")>1){
                order.paymentInstallments=true;
            }
        }
        if (order.paymentMethod.endsWith(" / ")) {
            order.paymentMethod = order.paymentMethod.substring(0, order.paymentMethod.length() - 3);
        }

        order.buyerEmail="N/A";
        if (includeDetails) {
            order.messageArrayList=getAllMessagesOnOrder(order.packId,user,httpClient);
            if (order.messageArrayList.size()>0){  //todo devolver
                for (int i=0; i<order.messageArrayList.size(); i++) {
                    Message msg =  order.messageArrayList.get(i);
                    String emailAddress = msg.buyerEmail;
                    if (emailAddress!=null && !emailAddress.contains("mercadolibre")) { //tiene que recibir al menos un mensaje para tener un email valido
                        order.buyerEmail = emailAddress;
                        break;
                    }
                }
            }

            //todas las preguntass en el caso del proceso global
            order.previousQuestionsOnItemArrayList = new ArrayList<Message>();
            order.previousQuestionsOtherItemsArrayList = new ArrayList<Message>();
            if (allQuestionsArrayList==null || allQuestionsArrayList.isEmpty()){
                updateQuestionsFile(user,httpClient);
                String questionFileName=getQuestionFileName(user);
                allQuestionsArrayList=getQuestionsFromFile(questionFileName);
            }
            for (Message question : allQuestionsArrayList) {
                if (question.customerId == order.buyerCustId) {
                    if (question.productId.equals(order.productId)) {
                        order.previousQuestionsOnItemArrayList.add(question);
                    } else {
                        order.previousQuestionsOtherItemsArrayList.add(question);
                    }
                }
            }
        }

        if (reviewsHashMap.size()>0){
            if (reviewsHashMap.containsKey(order.buyerCustId)) {
                String reviewRecord = reviewsHashMap.get(order.buyerCustId);
                int pos1 = reviewRecord.indexOf("|");
                String itemId = reviewRecord.substring(0, pos1);
                if (order.productId.equals(itemId)) {
                    pos1++;
                    int pos2 = reviewRecord.indexOf("*");
                    String starStr = reviewRecord.substring(pos1, pos2);
                    int stars=Integer.parseInt(starStr);
                    pos2 += 2;
                    String review = reviewRecord.substring(pos2);
                    order.itemReviewStars =stars;
                    order.itemReviewComment =review;
                    boolean b = false;
                }
            }
        }

        return order;
    }

    public static String getStringValue(JSONObject jsonObject1, JSONObject jsonObject2, String parameter) {
        String result=null;
        String value1=null;
        String value2=null;
        if (jsonObject1!=null) {
            if (jsonObject1.has(parameter) && !jsonObject1.isNull(parameter)){
                value1=jsonObject1.getString(parameter);
            }
        }
        if (jsonObject2!=null) {
            if (jsonObject2.has(parameter) && !jsonObject2.isNull(parameter)){
                value2=jsonObject2.getString(parameter);
            }
        }
        if (value1!=null && !value1.isEmpty()){
            result=value1;
        }else {
            if (value2!=null && !value2.isEmpty()){
                result=value2;
            }
        }
        return result;
    }


    public static JSONObject getObjectValue(JSONObject jsonObject1, JSONObject jsonObject2, String parameter) {
        JSONObject result=null;
        JSONObject value1=null;
        JSONObject value2=null;
        if (jsonObject1!=null) {
            if (jsonObject1.has(parameter) && !jsonObject1.isNull(parameter)){
                value1=jsonObject1.getJSONObject(parameter);
            }
        }
        if (jsonObject2!=null) {
            if (jsonObject2.has(parameter) && !jsonObject2.isNull(parameter)){
                value2=jsonObject2.getJSONObject(parameter);
            }
        }
        if (value1!=null && !value1.isEmpty()){
            result=value1;
        }else {
            if (value2!=null && !value2.isEmpty()){
                result=value2;
            }
        }
        return result;
    }



    public static String getOrderPictureUrl(JSONObject publicationJsonObject, long onlineOrderVariationId, boolean thumbnail){
        String pictureId=null;
        String url=null;
        if (publicationJsonObject!=null) {
            if (onlineOrderVariationId != 0) { //si eligió una variante tenemos que poner esa foto, else foto general
                JSONArray variationsArray = publicationJsonObject.getJSONArray("variations");
                for (int i = 0; i < variationsArray.length(); i++) {
                    JSONObject variation = variationsArray.getJSONObject(i);
                    long id = variation.getLong("id");
                    if (id == onlineOrderVariationId) {
                        JSONArray jsonArrayPictureIds = variation.getJSONArray("picture_ids");
                        pictureId = jsonArrayPictureIds.getString(0);
                        break;
                    }
                }
                JSONArray picturesArray = publicationJsonObject.getJSONArray("pictures");

                for (int i = 0; i < picturesArray.length(); i++) {
                    JSONObject pictureObject = picturesArray.getJSONObject(i);
                    String id = pictureObject.getString("id");
                    if (id.equals(pictureId)) {
                        url = pictureObject.getString("url");
                        break;
                    }
                }
                if (thumbnail && url != null && url.endsWith("-O.jpg")) {//reemplazando por el thumbnail
                    url = url.substring(0, url.indexOf("-O.jpg")) + "-I.jpg";
                }
            } else {
                if (publicationJsonObject.has("thumbnail")) {
                    url = publicationJsonObject.getString("thumbnail");
                    if (!thumbnail && url != null && url.endsWith("-I.jpg")){
                        url = url.substring(0, url.indexOf("-I.jpg")) + "-O.jpg";
                    }
                }
            }
        }
        return url;

    }

    public static ArrayList<Message> getAllMessagesOnOrder(long packId, String user, CloseableHttpClient httpClient){
        ArrayList<Message> result = new ArrayList<Message>();
        int totalMessages = Integer.MAX_VALUE;
        for (int messagesOffset = 0; messagesOffset <= totalMessages; messagesOffset += 10) {
            String messagesUrl = "https://api.mercadolibre.com/messages/packs/" + packId + "/sellers/" + TokenUtils.getIdCliente(user)
                    + "?offset=" + messagesOffset+"&mark_as_read=false";
            long startTime2 = System.currentTimeMillis();
            JSONObject jsonMessages = HttpUtils.getJsonObjectUsingToken(messagesUrl, httpClient, user, false);
            long elapsedTime2 = System.currentTimeMillis() - startTime2;
            if (elapsedTime2 >= tooMuchTime) {
                httpClient = HttpUtils.buildHttpClient();
            }
            if (messagesOffset == 0) {
                JSONObject pagingObj = jsonMessages.getJSONObject("paging");
                totalMessages = pagingObj.getInt("total");
            }
            JSONArray jsonMessagesArray = jsonMessages.getJSONArray("messages");
            for (Object messageObject : jsonMessagesArray) {
                JSONObject messageJsonObject = (JSONObject) messageObject;
                Message message = new Message();
                message.id = messageJsonObject.getString("id");
                message.text = messageJsonObject.getString("text");
                String fromId = messageJsonObject.getJSONObject("from").getString("user_id");
                if (fromId.equals(TokenUtils.getIdCliente(user))) {
                    message.direction = 'E';
                    message.buyerEmail = messageJsonObject.getJSONObject("to").getString("email");
                } else {
                    message.direction = 'R';
                    message.buyerEmail = messageJsonObject.getJSONObject("from").getString("email");
                }
                result.add(message);
            }
        }
        return result;
    }


    public static void main(String args[]){


        Calendar calendar = Calendar.getInstance();

        String user = "SOMOS_MAS";
        //String user = "ACACIAYLENGA";

        String fileName = ("C:\\centro\\reportes\\"+user+ "_"+ calendar.get(Calendar.YEAR) + "-" + String.format("%02d",(calendar.get(Calendar.MONTH)+1) )+ "-" +
                calendar.get(Calendar.DAY_OF_MONTH)+"_"+ calendar.getTime().getTime() / 1000 + ".csv");

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        //todo descomentar proxima linea
        reviewsHashMap=getAllReviews(user,httpClient); //opcional para que busque reviews de items
        ArrayList<Order> orderArrayList = requestOrdersAndMessages(true,false, user,httpClient);
        String headers=new Order().getPrintableCSVHeader();
        Logger.writeOnFile(fileName,headers);
        for (Order order:orderArrayList){
            String record = order.getPrintableCSVValues();
            Logger.writeOnFile(fileName,record);
        }
    }

    private static String humanNameFormater(String inputString)
    {
        String result="";

        try {
            if (StringUtils.isBlank(inputString)) {
                return "";
            }

            if (StringUtils.length(inputString) == 1) {
                return inputString.toUpperCase();
            }


            String[] strings = inputString.split(" ");

            for (String name:strings) {
                name=name.trim();
                if (name.length()==0){
                    continue;
                }
                char[] nameCharArray=name.toCharArray();
                for (int i = 0; i < nameCharArray.length; i++) {
                    if (i==0){
                        nameCharArray[0]=Character.toUpperCase(nameCharArray[0]);
                    }else {
                        nameCharArray[i]=Character.toLowerCase(nameCharArray[i]);
                    }
                }
                name=new String(nameCharArray);
                result+=name+" ";
            }
            result=result.trim();

        }catch (Exception x){
            boolean b=false;
        }

        return result;
    }
}


