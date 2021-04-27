package com.ml;

import com.ml.utils.CompressionUtil;
import com.ml.utils.DatabaseHelper;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.Message;
import com.ml.utils.MessagesAndSalesHelper;
import com.ml.utils.SData;
import com.ml.utils.TokenUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuestionsChecker1 {

    static String user = SData.getSomosMas();

    public static void main(String args[]){

        LocalTime now = LocalTime.now();
        if (now.isAfter(SData.NON_WORKING_HOURS_FROM) && now.isBefore(SData.NON_WORKING_HOURS_TO)) {
            String msg = "Zzzzzzzz "+now.toString();
            System.out.println(msg);
            Logger.log(msg);
            System.exit(0);
        }

        if (!DatabaseHelper.isServiceEnabledOnCloud()) {
            String msg = "QuestionsChecker1 deshabilitado en cloud";
            System.out.println(msg);
            Logger.log(msg);
            System.exit(0);
        }

        if (args!=null && args.length>0){
            String usuarioArg = args[0];
            if (usuarioArg!=null && usuarioArg.length()>0){
                user=usuarioArg;
            }
        }


        String msg="*********** Procesando preguntas de usuario: "+user;
        Logger.log(msg);
        System.out.println(msg);

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        long oneDayinMiliseconds = 86400000L;
        Timestamp last3Days = new Timestamp(System.currentTimeMillis()-oneDayinMiliseconds*3);

        Map<Long,String> questionsOnCloud= DatabaseHelper.fetchQuestions();
        ArrayList<JSONObject> unansweredQuestions = fetchUnansweredQuestions(httpClient,user,last3Days);

        for (JSONObject questionObj:unansweredQuestions){
            Long id=-1L;
            if (questionObj.has("id") && !questionObj.isNull("id")) {
                id = questionObj.getLong("id");
            }
            if (id==null || id<=0){
                msg = "No se pudo recuperar el id de la pregunta ";
                System.out.println(msg);
                Logger.log(msg);
                continue;
            }

            long fromId=0L;
            if (questionObj.has("from") && !questionObj.isNull("from")) {
                JSONObject fromObj = questionObj.getJSONObject("from");
                if (fromObj.has("id") && !fromObj.isNull("id")) {
                    fromId = fromObj.getLong("id");
                }
            }
            if (fromId<=0){
                msg = "No se pudo recuperar el id del usuario de la pregunta "+id;
                System.out.println(msg);
                Logger.log(msg);
                continue;
            }

            if (questionsOnCloud.containsKey(id)) {
                continue;
            }

            String questionText="";
            if (questionObj.has("text") && !questionObj.isNull("text")) {
                questionText = questionObj.getString("text");
            }

            String line="BASICO\nP: "+questionText+"\n";
            System.out.print(line);
            String text=line;

            text = processBasicInfo(httpClient, fromId,text);
            int pos1=text.indexOf("|");
            String nickName=text.substring(0,pos1);
            text=text.substring(pos1+1);
            text = processQuestionFiles(httpClient, fromId, nickName, text);
            storeText(user, id, text);
        }

        removeAnsweredQuestionsOnCloud(httpClient,questionsOnCloud,last3Days);

    }

    private static void removeAnsweredQuestionsOnCloud(CloseableHttpClient httpClient, Map<Long, String> questionsOnCloud, Timestamp last3Days) {
        for (Long questionId: questionsOnCloud.keySet()){
            String questionUrl = "https://api.mercadolibre.com/questions/"+questionId;
            JSONObject questionObject = HttpUtils.getJsonObjectUsingToken(questionUrl,httpClient,user,false);
            boolean delete=false;
            if (questionObject==null){
                delete=true;
            }else {
                if (questionObject.has("answer") && !questionObject.isNull("answer")) {
                    delete = true;
                } else {
                    if (questionObject.has("date_created") && !questionObject.isNull("date_created")) {
                        String dateCreatedStr = questionObject.getString("date_created");
                        Timestamp creationTimestamp = MessagesAndSalesHelper.getTimestamp(dateCreatedStr);
                        if (creationTimestamp.before(last3Days)) {
                            delete = true;
                        }
                    }
                }
            }
            if (delete){
                DatabaseHelper.removeQuestion(questionId);
            }
        }
    }

    private static String processQuestionFiles(CloseableHttpClient httpClient, long fromId, String nickName, String text) {
        String text2= processQuestionFiles(httpClient,SData.getAcaciaYLenga(),fromId,nickName,text);
        String text3= processQuestionFiles(httpClient,SData.getSomosMas(),fromId,nickName,text2);
        return text3;
    }

    private static String processQuestionFiles(CloseableHttpClient httpClient, String user, long fromId, String nickName, String text) {
        String newText=text;
        String line="";
        String questionFileName=MessagesAndSalesHelper.getQuestionFileName(user);
        ArrayList<Message> allQuestionsArrayList=MessagesAndSalesHelper.getQuestionsFromFile(questionFileName);
        for (Message question: allQuestionsArrayList){
            if (question!=null){
                if (question.customerId== fromId && question.direction=='R'){
                    String itemUrl="https://api.mercadolibre.com/items/"+question.productId;
                    JSONObject itemObj = HttpUtils.getJsonObjectWithoutToken(itemUrl, httpClient,false);
                    if (itemObj.has("permalink") && !itemObj.isNull("permalink")){
                        String permalink=itemObj.getString("permalink");
                        line=permalink+"\n";
                        System.out.print(line);
                        newText +=line;
                    }
                    line=nickName+": "+question.text+"\n";
                    System.out.print(line);
                    newText +=line;
                    String answerId=question.id.substring(0,question.id.length()-1)+"R";
                    Message answer = new Message();
                    answer.id=answerId;
                    answer.direction='E';
                    if (allQuestionsArrayList.contains(answer)) {
                        answer = allQuestionsArrayList.get(allQuestionsArrayList.indexOf(answer));
                    }
                    line=user+": "+answer.text+"\n\n";
                    System.out.print(line);
                    newText +=line;
                }
            }
        }
        return newText;
    }

    private static void storeText(String user, Long id, String text) {
        String compressedText=null;
        try {
            compressedText = CompressionUtil.compressAndReturnB64(text);
        }catch (Exception e){
            String msg="Exception comprimiendo texto en QuestionsChecker1";
            System.out.println(msg);
            Logger.log(msg);
            e.printStackTrace();
            Logger.log(e);
        }
        DatabaseHelper.insertQuestion(id,TokenUtils.getUserNumber(user),compressedText);
    }

    private static String processBasicInfo(CloseableHttpClient httpClient, long fromId, String text) {
        String customerUrl="https://api.mercadolibre.com/users/"+ fromId;
        JSONObject customerObj = HttpUtils.getJsonObjectWithoutToken(customerUrl, httpClient,false);


        String newText=text;
        String nickname = customerObj.getString("nickname");
        String userPermalink = customerObj.getString("permalink");

        String sellerLevel="N/A";
        String sellerColor="N/A";
        String sellerSales="N/A";
        String sellerSalesInPeriod="N/A";
        String sellerRating="N/A";

        String tags="tags: ";
        ArrayList<String> tagsArrayList=new ArrayList<String>();

        if (customerObj.has("seller_reputation") && !customerObj.isNull("seller_reputation")){
            JSONObject sellerReputation = customerObj.getJSONObject("seller_reputation");

            if (sellerReputation.has("power_seller_status") && !sellerReputation.isNull("power_seller_status")) {
                sellerLevel = sellerReputation.getString("power_seller_status");
            }

            if (sellerReputation.has("level_id") && !sellerReputation.isNull("level_id")) {
                sellerColor = sellerReputation.getString("level_id");
            }

            if (sellerReputation.has("metrics") && !sellerReputation.isNull("metrics")) {
                JSONObject metricsObj = sellerReputation.getJSONObject("metrics");
                JSONObject salesObj = metricsObj.getJSONObject("sales");
                String period = salesObj.getString("period");
                int completed = salesObj.getInt("completed");
                sellerSales = completed + " sales in " + period.trim();
            }

            JSONObject transactionsObj = sellerReputation.getJSONObject("transactions");
            String period2 = transactionsObj.getString("period");
            int canceled = transactionsObj.getInt("canceled");
            int total = transactionsObj.getInt("total");
            int completed2 =  transactionsObj.getInt("completed");

            String canceledPercentageStr=""+(canceled*1.0/total)*100L;
            sellerSalesInPeriod=period2.trim()+" total="+total+" completed="+completed2+" canceled="+canceled+"->"+canceledPercentageStr.substring(0,3);


            JSONObject ratings = transactionsObj.getJSONObject("ratings");
            Double positive = ratings.getDouble("positive");
            Double neutral = ratings.getDouble("neutral");
            Double negative = ratings.getDouble("negative");

            sellerRating="positive="+positive+" / neutral="+neutral+" / negative="+negative;

        }

        if (customerObj.has("tags") && !customerObj.isNull("tags")) {
            JSONArray tagsArray = customerObj.getJSONArray("tags");
            for (int j = 0; j < tagsArray.length(); j++) {
                String tag = (String) tagsArray.get(j);
                tagsArrayList.add(tag);
            }
        }

        String registrationDate = "N/A";
        if (customerObj.has("registration_date") && !customerObj.isNull("registration_date")) {
            registrationDate = customerObj.getString("registration_date");
        }

        int points = customerObj.getInt("points");
        String userType = customerObj.getString("user_type");

        JSONObject addressObj = customerObj.getJSONObject("address");
        String city = "N/A";
        if (addressObj.has("city") && !addressObj.isNull("city")) {
            city = (String) addressObj.get("city");
        }
        String stateDesc = "N/A";
        if (addressObj.has("state") && !addressObj.isNull("state")) {
            String state = (String) addressObj.get("state");
            HashMap<String, String> stateHashMap = getStateHashMap();
            stateDesc = stateHashMap.get(state);
        }
        String address=city+", "+stateDesc;


        if (customerObj.has("tags") && !customerObj.isNull("tags")) {
            JSONArray tagsArray = customerObj.getJSONArray("tags");
            for (int j = 0; j < tagsArray.length(); j++) {
                String tag = (String) tagsArray.get(j);
                if (!tagsArrayList.contains(tag)) {
                    tagsArrayList.add(tag);
                }
            }
        }
        if (tagsArrayList.size()>0){
            for (String tag: tagsArrayList){
                tags+=tag+" / ";
            }
        }else {
            tags+="N/A";
        }

        String line="---------------------------------------------------------------------------------------------------------\n";
        System.out.print(line);
        newText+=line;

        line=nickname+" "+userPermalink+"\n";
        System.out.print(line);
        newText+=line;

        line="UserType: "+userType+" Registered: "+registrationDate+" Points: "+points+"\n";
        System.out.print(line);
        newText+=line;

        line="Location: "+address+"\n";
        System.out.print(line);
        newText+=line;

        line=tags+"\n\n";
        System.out.print(line);
        newText+=line;

        if (!sellerSales.equals("N/A")) {
            line="Seller Info: " + sellerSales+"\n";
            System.out.print(line);
            newText+=line;

            line=sellerSalesInPeriod + "\n\n";
            System.out.print(line);
            newText+=line;
        }

        if (!sellerColor.equals("N/A")) {
            line="Color: "+sellerColor+" Seller level: "+sellerLevel+"\n";
            System.out.print(line);
            newText+=line;

            line=sellerRating+ "\n\n";
            System.out.print(line);
            newText+=line;

        }

        line="---------------------------------------------------------------------------------------------------------\n";
        System.out.print(line);
        newText+=line;

        String customerItemsUrl="https://api.mercadolibre.com/sites/MLA/search?seller_id="+ fromId;
        JSONObject itemsObj=HttpUtils.getJsonObjectWithoutToken(customerItemsUrl, httpClient,false);
        JSONArray itemsArray = itemsObj.getJSONArray("results");
        line="Items on Sale: "+itemsArray.length()+"\n";
        System.out.print(line);
        newText+=line;
        for (int j=0; j<itemsArray.length() && j<10; j++){
            JSONObject itemObject = itemsArray.getJSONObject(j);
            String permalink = itemObject.getString("permalink");
            line=permalink+"\n";
            System.out.print(line);
            newText+=line;
        }

        line="---------------------------------------------------------------------------------------------------------\n";
        System.out.print(line);
        newText+=line;
        return nickname+"|"+newText;

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

    private static ArrayList<JSONObject> fetchUnansweredQuestions(CloseableHttpClient httpClient, String user,Timestamp last3Days){
        ArrayList<JSONObject> unansweredQuestions = new ArrayList<>();
        String questionsUrl = "https://api.mercadolibre.com/my/received_questions/search?sort_fields=date_created&sort_types=DESC";
        JSONObject questionObject = HttpUtils.getJsonObjectUsingToken(questionsUrl,httpClient,user,false);
        if (questionObject!=null){
            if (questionObject.has("questions")){
                JSONArray questionArray=questionObject.getJSONArray("questions");
                if (questionArray!=null){
                    for (int i=0; i<questionArray.length(); i++){
                        JSONObject questionObj=questionArray.getJSONObject(i);
                        if (questionObj.has("status") && !questionObj.isNull("status")) {
                            String status = questionObj.getString("status");
                            if (!status.equals("UNANSWERED")) {
                                continue;
                            }
                            if (questionObj.has("hold") && !questionObj.isNull("hold")) {
                                boolean hold = questionObj.getBoolean("hold");
                                if (hold) {
                                    continue;
                                }
                            }
                            if (questionObj.has("deleted_from_listing") && !questionObj.isNull("deleted_from_listing")) {
                                boolean deleted = questionObj.getBoolean("deleted_from_listing");
                                if (deleted) {
                                    continue;
                                }
                            }
                            if (questionObj.has("date_created") && !questionObj.isNull("date_created")) {
                                String dateCreatedStr = questionObj.getString("date_created");
                                Timestamp creationTimestamp = MessagesAndSalesHelper.getTimestamp(dateCreatedStr);
                                if (creationTimestamp.before(last3Days)) {
                                    continue;
                                }
                            }

                            long id = 0L;
                            if (questionObj.has("id") && !questionObj.isNull("id")) {
                                id = questionObj.getLong("id");
                            }
                            if (id <= 0) {
                                String msg = "No se pudo recuperar el id de pregunta";
                                System.out.println(msg);
                                Logger.log(msg);
                                continue;
                            }
                            unansweredQuestions.add(questionObj);
                        }
                    }
                }
            }
        }
        return unansweredQuestions;
    }

}
