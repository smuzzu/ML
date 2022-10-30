package com.ml;

import com.ml.utils.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionsChecker2 {

    static int RESULTS_WITHOUT_TOKEN=1000;
    static final int RESULTS_LIMIT = 10000;


    public static void main(String args[]){

        String msg="*********** Procesando preguntas en categoras ";
        Logger.log(msg);
        System.out.println(msg);

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
        Map<Long,String> questionsOnCloud= DatabaseHelper.fetchQuestions();
        for (Long questionId: questionsOnCloud.keySet()){
            String text=null;
            String compressedText=questionsOnCloud.get(questionId);
            try {
                text=CompressionUtil.decompressB64(compressedText);
            }catch (Exception e){
                msg="Error descomprimiendo info de pregunta";
                System.out.println(msg);
                Logger.log(msg);
                e.printStackTrace();
                Logger.log(e);
            }
            if (text!=null && text.startsWith("BASICO")){
                text=text.substring(7);//removemos basico
                text=processAllQuestionsInCategory(httpClient,questionId,text);
                String compressedTest=null;
                try {
                    compressedTest = CompressionUtil.compressAndReturnB64(text);
                }catch (Exception e){
                    msg="Error comprimiendo información de preguntas";
                    System.out.println(msg);
                    Logger.log(msg);
                    System.out.println(e);
                    e.printStackTrace();
                }
                DatabaseHelper.updateQuestion(questionId,compressedTest);
            }
        }


        LocalTime now = LocalTime.now();
        if (now.isAfter(SData.NON_WORKING_HOURS_FROM) && now.isBefore(SData.NON_WORKING_HOURS_TO2)) {
            System.exit(0);
        }// evitamos mailear a la noche

        List<String> questions = fetchQuestions(true);
        if (questions.isEmpty()) {//no hay preguntas no notificamos
            System.exit(0);
        }
        String body="<table border=1>";
        for (String question:questions){
            body+="<tr><td>"+question+"</td></tr>";
        }
        body+="</table>";
        String destinationAddress3=SData.getMailAddressList3();
        GoogleMailSenderUtil.sendMail("Preguntas sin responder",body,destinationAddress3);

    }


    public static List<String> fetchQuestions(boolean web) {
        List<String> result = new ArrayList<String>();
        Map<Long, String> questionsMap = DatabaseHelper.fetchQuestions();
        for (String compressedQuestion : questionsMap.values()) {
            String text = null;
            try {
                text = CompressionUtil.decompressB64(compressedQuestion);
            } catch (Exception e) {
                String msg = "Error descomprimiendo pregunta";
                System.out.println(msg);
                Logger.log(msg);
                e.printStackTrace();
                Logger.log(e);
            }
            if (text != null) {
                if (web) {
                    text=text.replaceAll("\n", "<br/>");
                }
                result.add(text);
            }
        }
        return result;
    }


    private static String processAllQuestionsInCategory(CloseableHttpClient httpClient, long questionId, String text){
        String newText=text;
        String user=DatabaseHelper.fetchQuestionSeller(questionId);
        String questionUrl = "https://api.mercadolibre.com/questions/"+questionId;
        JSONObject questionObject = HttpUtils.getJsonObjectUsingToken(questionUrl,httpClient,user,false);

        String itemId=null;
        if (questionObject!=null && questionObject.has("item_id") && !questionObject.isNull("item_id")){
            itemId=questionObject.getString("item_id");
        }
        if (itemId==null || itemId.isEmpty()){
            String msg="No se pudo recuperar el item id, no es posible obtener los detalles de la pregunta";
            System.out.println(msg);
            Logger.log(msg);
            return newText;
        }
        String itemUrl="https://api.mercadolibre.com/items/"+itemId;
        JSONObject itemObject = HttpUtils.getJsonObjectUsingToken(itemUrl,httpClient,user,false);

        String categoryId=null;
        if (itemObject.has("category_id") && !itemObject.isNull("category_id")){
            categoryId=itemObject.getString("category_id");
        }
        if (categoryId==null || categoryId.isEmpty()){
            String msg="No se pudo recuperar la categoria, no es posible obtener los detalles de la pregunta";
            System.out.println(msg);
            Logger.log(msg);
            return newText;
        }


        long fromId=-1;
        if (questionObject.has("from") && !questionObject.isNull("from")){
            JSONObject fromObj=questionObject.getJSONObject("from");
            if (fromObj.has("id") && !fromObj.isNull("id")){
                fromId=fromObj.getLong("id");
            }
        }
        if (fromId<1){
            String msg="No se pudo recuperar el item from id, no es posible obtener los detalles de la pregunta";
            System.out.println(msg);
            Logger.log(msg);
            return newText;
        }
        String customerUrl="https://api.mercadolibre.com/users/"+ fromId;
        String customerNickname = "P";
        JSONObject customerObj = HttpUtils.getJsonObjectWithoutToken(customerUrl, httpClient,false);
        if (customerObj!=null && customerObj.has("nickname") && !customerObj.isNull("nickname")) {
            customerNickname = customerObj.getString("nickname");
        }

        String line="";

        ArrayList<String> itemsInCategory = getAllRelevantItems(categoryId,httpClient,user);

        System.out.println("Analizando "+itemsInCategory.size()+" publicaciones");

        int count=0;
        for (String productId: itemsInCategory) {
            count++;
            if (count>50) {
                System.out.print(".");
                count=0;
                httpClient=HttpUtils.buildHttpClient();
            }
            String questionsUrl2 = "https://api.mercadolibre.com/questions/search?item="+productId+"&from="+fromId;
            JSONObject questionsObj = HttpUtils.getJsonObjectUsingToken(questionsUrl2, httpClient, user,false);
            if (questionsObj == null) {
                continue;
            }
            JSONArray questionsArray = questionsObj.getJSONArray("questions");
            for (Object questionObject2 : questionsArray) {
                JSONObject questionJSONObject = (JSONObject) questionObject2;

                String dateStr = questionJSONObject.getString("date_created");
                String questionText=questionJSONObject.getString("text");
                String answerText="";
                if (questionJSONObject.has("answer") && !questionJSONObject.isNull("answer")) {
                    JSONObject answerObj = questionJSONObject.getJSONObject("answer");
                    if (answerObj.has("text") && !answerObj.isNull("text")) {
                        answerText=answerObj.getString("text");
                    }
                }

                String itemUrl2="https://api.mercadolibre.com/items/"+productId;
                JSONObject itemObj2 = HttpUtils.getJsonObjectWithoutToken(itemUrl2, httpClient, false);
                String itemPermalink = itemObj2.getString("permalink");

                String sellerNickname = "R";
                if (itemObj2.has("seller_id") && !itemObj2.isNull("seller_id")){
                    long sellerId=itemObj2.getLong("seller_id");
                    String customerUrl2="https://api.mercadolibre.com/users/"+ sellerId;

                    JSONObject customerObj2 = HttpUtils.getJsonObjectWithoutToken(customerUrl2, httpClient,false);
                    if (customerObj2!=null && customerObj2.has("nickname") && !customerObj2.isNull("nickname")) {
                        sellerNickname = customerObj2.getString("nickname");
                    }
                }

                System.out.println(" ");
                line="\n"+dateStr+" "+itemPermalink+"\n"
                        +customerNickname+": "+questionText+"\n"
                        +sellerNickname+": "+answerText+"\n"
                        +"------------------------------\n";
                System.out.print(line);
                newText+=line;

            }
        }


        return newText;
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

    private static ArrayList<String> getAllRelevantItems(String categoryId, CloseableHttpClient httpClient, String user) {
        ArrayList<String> itemsArrayList = new ArrayList<>();

        /*
        //acacia  TODO meter todos los items, usando tokens
        String acaciaylenga = SData.getAcaciaYLenga();
        boolean moreItems=true;
        String productListURL =null;
        JSONObject jsonResponse=null;
        JSONArray jsonProductArray=null;
        String scrollId=null;
        while (moreItems){
            productListURL = "https://api.mercadolibre.com/users/" + TokenUtils.getIdCliente(acaciaylenga) + "/items/search?search_type=scan";
            if (scrollId!=null){
                productListURL+="&scroll_id="+scrollId;
            }
            jsonResponse = HttpUtils.getJsonObjectUsingToken(productListURL, httpClient,acaciaylenga,false);
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

        //somos
        String somos = SData.getSomosMas();
        moreItems=true;
        scrollId=null;
        while (moreItems){
            productListURL = "https://api.mercadolibre.com/users/" + TokenUtils.getIdCliente(somos) + "/items/search?search_type=scan";
            if (scrollId!=null){
                productListURL+="&scroll_id="+scrollId;
            }
            jsonResponse = HttpUtils.getJsonObjectUsingToken(productListURL, httpClient,somos,false);
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
        */

        boolean finished=false;
        int offset=0;
        JSONObject itemsObj;
        while (!finished) {
            String itemsUrl = "https://api.mercadolibre.com/sites/MLA/search?category="+categoryId+"&offset="+offset;
            if (offset > RESULTS_LIMIT) {
                finished=true;
                continue;
            }
            if (offset < RESULTS_WITHOUT_TOKEN) {
                itemsObj = HttpUtils.getJsonObjectWithoutToken(itemsUrl,httpClient,false);
            }else {
                itemsObj = HttpUtils.getJsonObjectUsingToken(itemsUrl,httpClient,user,false);
            }
            if (itemsObj==null){
                finished=true;
                continue;
            }
            JSONArray itemsObjJSONArray = itemsObj.getJSONArray("results");
            for (Object itemObject : itemsObjJSONArray){
                JSONObject itemJSONObject = (JSONObject)itemObject;
                String itemId=itemJSONObject.getString("id");

                if (itemJSONObject.has("seller") && !itemJSONObject.isNull("seller")){
                    JSONObject sellerObject=itemJSONObject.getJSONObject("seller");
                    if (sellerObject.has("id") && !sellerObject.isNull("id")){
                        String sellerId=""+sellerObject.getInt("id");
                        if (sellerId.equals(SData.getIdClienteAcaciaYLenga()) ||
                                sellerId.equals(SData.getIdClienteSomosMas())){
                            continue;//estas preguntas a nuestro usuario se buscaron antes
                        }
                    }
                }

                if (!itemsArrayList.contains(itemId)){
                    itemsArrayList.add(itemId);
                }
            }
            offset+=50;
        }

        return itemsArrayList;
    }


}
