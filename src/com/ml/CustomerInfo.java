package com.ml;

import com.ml.utils.HttpUtils;
import com.ml.utils.TokenUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class CustomerInfo {


    static int RESULTS_WITHOUT_TOKEN=1000;
    static final int RESULTS_LIMIT = 10000;
    static final char ALFOMBRAS='0';
    static final char CESTOS_COCINA ='1';
    static final char CORREDERAS_TELESCOPICAS='2';
    static final char ORGANIZADDOR_ROPA='3'; //perchero elevador y pantalonero
    static final char ORDENADORES_FILA='4';
    static final char COCINA_ALMACENAMIENTO='5'; //portacopas
    static final char CESTOS_PAPELERO ='6';
    static final char INSUMOS_OFICINA ='7';
    static final char MUEBLES_Y_SILLAS ='8';
    static final char CAJONES_DE_MADERA ='9'; //galleta
    static final char ESCRITORIOS='A';
    static final char VAJILLA='B'; //cubiertero
    static final char PICAPORTES='C';
    static final char PERCHEROS='D';
    static final char RUEDAS_SILLA='E';
    static final char MESAS_DE_LUZ='F';
    static final char ORGANIZADORES_DE_ESCOBAS='G';
    static final char MANIJAS_Y_PICAPORTES='H';
    static final char SILLONES_GERENCIALES='I'; //de oficina
    static final char ORGANIZADOR_ESCRITORIO='J'; //mampara anti covid
    static final char ORGANIZADOR_ESCRITORIO2='K'; //mampara anti covid
    static final char ORGANIZADOR_ESCRITORIO3='L'; //mampara anti covid
    static final char HERRAMIENTAS_OTROS='M';   //minifix
    static final char BISAGRAS='N';
    static final char EQUIPAMIENTO_OFICINA_OTROS='Ñ'; //porta CPU
    static final char MESAS_RATONAS='O'; //ratonas y mesasa
    static final char BURLETES='P';

    public static void main (String[] args) {

        //para buscar un item y su categoria
        //https://api.mercadolibre.com/items/MLA678401085

        String nickname="LEONARDOCHABUR";
        char categoria=ORGANIZADDOR_ROPA;

        String categoryId=null;
        if (categoria==ALFOMBRAS){
            categoryId="MLA2513";
        }
        if (categoria== CESTOS_COCINA){
            categoryId="MLA121951";
        }
        if (categoria==CORREDERAS_TELESCOPICAS){
            categoryId="MLA432104";
        }
        if (categoria==ORGANIZADDOR_ROPA){
            categoryId="MLA431869";
        }
        if (categoria==ORDENADORES_FILA){
            categoryId="MLA412739";
        }
        if (categoria==COCINA_ALMACENAMIENTO){
            categoryId="MLA436298";
        }
        if (categoria==CESTOS_PAPELERO){
            categoryId="MLA105430";
        }
        if (categoria==INSUMOS_OFICINA){
            categoryId="MLA417280";
        }
        if (categoria==MUEBLES_Y_SILLAS){
            categoryId="MLA5978";
        }
        if (categoria==CAJONES_DE_MADERA){
            categoryId="MLA378056";
        }
        if (categoria==ESCRITORIOS){
            categoryId="MLA30991";
        }
        if (categoria==VAJILLA){
            categoryId="MLA436289";
        }
        if (categoria==PICAPORTES) {
            categoryId="MLA357155";
        }
        if (categoria==PERCHEROS) {
            categoryId="MLA31325";
        }
        if (categoria==RUEDAS_SILLA) {
            categoryId="MLA432821";
        }
        if (categoria==MESAS_DE_LUZ) {
            categoryId="MLA370415";
        }
        if (categoria==ORGANIZADORES_DE_ESCOBAS) {
            categoryId="MLA414040";
        }
        if (categoria==MANIJAS_Y_PICAPORTES) {
            categoryId="MLA357155";
        }
        if (categoria==SILLONES_GERENCIALES) {
            categoryId="MLA6847";
        }
        if (categoria==ORGANIZADOR_ESCRITORIO) {
            categoryId="MLA430962";
        }
        if (categoria==HERRAMIENTAS_OTROS) {
            categoryId="MLA430962";
        }
        if (categoria==BISAGRAS) {
            categoryId="MLA432055";
        }
        if (categoria==EQUIPAMIENTO_OFICINA_OTROS) {
            categoryId="MLA2105";
        }
        if (categoria==MESAS_RATONAS) {
            categoryId="MLA436384";
        }
        if (categoria==BURLETES) {
            categoryId="MLA416677";
        }


        CloseableHttpClient client = HttpUtils.buildHttpClient();

        //String user = "SOMOS_MAS";
        String user = "ACACIAYLENGA";

        nickname=nickname.replace(" ","+");

        String customerUrl="https://api.mercadolibre.com/sites/MLA/search?nickname="+nickname;

        JSONObject customerObj = HttpUtils.getJsonObjectWithoutToken(customerUrl,client,false);
        JSONObject sellerObj = customerObj.getJSONObject("seller");
        int custId=sellerObj.getInt("id");

        String userPermalink = "https://www.mercadolibre.com.ar/perfil/"+nickname;

        String sellerLevel="N/A";
        String sellerColor="N/A";
        String sellerSales="N/A";
        String sellerSalesInPeriod="N/A";
        String sellerRating="N/A";

        String tags="tags: ";
        ArrayList<String> tagsArrayList=new ArrayList<String>();

        if (sellerObj.has("seller_reputation") && !sellerObj.isNull("seller_reputation")){
            JSONObject sellerReputation = sellerObj.getJSONObject("seller_reputation");

            if (sellerReputation.has("power_seller_status") && !sellerReputation.isNull("power_seller_status")) {
                sellerLevel = sellerReputation.getString("power_seller_status");
            }

            if (sellerReputation.has("level_id") && !sellerReputation.isNull("level_id")) {
                sellerColor = sellerReputation.getString("level_id");
            }

            JSONObject metricsObj = sellerReputation.getJSONObject("metrics");
            JSONObject salesObj = metricsObj.getJSONObject("sales");
            String period = salesObj.getString("period");
            int completed = salesObj.getInt("completed");
            sellerSales=completed+" sales in "+period.trim();

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

            boolean b= false;
        }

        if (sellerObj.has("tags") && !sellerObj.isNull("tags")) {
            JSONArray tagsArray = sellerObj.getJSONArray("tags");
            for (int i = 0; i < tagsArray.length(); i++) {
                String tag = (String) tagsArray.get(i);
                tagsArrayList.add(tag);
            }
        }

        String customerUrl2="https://api.mercadolibre.com/users/"+custId;
        JSONObject sellerObj2 = HttpUtils.getJsonObjectWithoutToken(customerUrl2,client,false);

        String registrationDate = "N/A";
        if (sellerObj2.has("registration_date") && !sellerObj2.isNull("registration_date")) {
            registrationDate = sellerObj2.getString("registration_date");
        }

        int points = sellerObj2.getInt("points");
        String userType = sellerObj2.getString("user_type");

        JSONObject addressObj = sellerObj2.getJSONObject("address");
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


        if (sellerObj2.has("tags") && !sellerObj2.isNull("tags")) {
            JSONArray tagsArray = sellerObj2.getJSONArray("tags");
            for (int i = 0; i < tagsArray.length(); i++) {
                String tag = (String) tagsArray.get(i);
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


        System.out.println("---------------------------------------------------------------------------------------------------------");
        System.out.println(nickname+" "+userPermalink);
        System.out.println("UserType: "+userType+" Registered: "+registrationDate+" Points: "+points);
        System.out.println("Location: "+address);
        System.out.println(tags+"\n");

        if (!sellerSales.equals("N/A")) {
            System.out.println("Seller Info: " + sellerSales);
            System.out.println(sellerSalesInPeriod + "\n");
        }

        if (!sellerColor.equals("N/A")) {
            System.out.println("Color: "+sellerColor+" Seller level: "+sellerLevel);
            System.out.println(sellerRating+ "\n    ");
        }

        System.out.println("---------------------------------------------------------------------------------------------------------");

        String customerItemsUrl="https://api.mercadolibre.com/sites/MLA/search?seller_id="+custId;
        JSONObject itemsObj=HttpUtils.getJsonObjectWithoutToken(customerItemsUrl,client,false);
        JSONArray itemsArray = itemsObj.getJSONArray("results");
        System.out.println("Items on Sale: "+itemsArray.length());
        for (int i=0; i<itemsArray.length() && i<10; i++){
            JSONObject itemObject = itemsArray.getJSONObject(i);
            String permalink = itemObject.getString("permalink");
            System.out.println(permalink);
        }

        System.out.println("---------------------------------------------------------------------------------------------------------");

        ArrayList<String> itemsInCategory = getAllRelevantItems(categoryId,client,user);

        System.out.println("Analizando "+itemsInCategory.size()+" publicaciones");

        int count=0;
        for (String productId: itemsInCategory) {
            count++;
            if (count>50) {
                System.out.print(".");
                count=0;
                client=HttpUtils.buildHttpClient();
            }
            String questionsUrl = "https://api.mercadolibre.com/questions/search?item="+productId+"&from="+custId;
            JSONObject questionsObj = HttpUtils.getJsonObjectWithoutToken(questionsUrl, client, false);
            if (questionsObj == null) {
                continue;
            }
            JSONArray questionsArray = questionsObj.getJSONArray("questions");
            for (Object questionObject : questionsArray) {
                JSONObject questionJSONObject = (JSONObject) questionObject;

                String dateStr = questionJSONObject.getString("date_created");
                String questionText=questionJSONObject.getString("text");
                String answerText="";
                if (questionJSONObject.has("answer") && !questionJSONObject.isNull("answer")) {
                    JSONObject answerObj = questionJSONObject.getJSONObject("answer");
                    if (answerObj.has("text") && !answerObj.isNull("text")) {
                        answerText=answerObj.getString("text");
                    }
                }

                String itemUrl="https://api.mercadolibre.com/items/"+productId;
                JSONObject itemObj = HttpUtils.getJsonObjectWithoutToken(itemUrl, client, false);
                String itemPermalink = itemObj.getString("permalink");
                System.out.println(" ");
                System.out.println(dateStr+" "+itemPermalink);
                System.out.println("P: "+questionText);
                System.out.println("R: "+answerText);
                System.out.println("------------------------------");
            }
        }


        boolean b= false;

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

        //acacia  TODO meter todos los items, usando tokens
        String acaciaylenga = "ACACIAYLENGA";
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
            jsonResponse = HttpUtils.getJsonObjectUsingToken(productListURL, httpClient,acaciaylenga);
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
        String somos = "SOMOS_MAS";
        moreItems=true;
        scrollId=null;
        while (moreItems){
            productListURL = "https://api.mercadolibre.com/users/" + TokenUtils.getIdCliente(somos) + "/items/search?search_type=scan";
            if (scrollId!=null){
                productListURL+="&scroll_id="+scrollId;
            }
            jsonResponse = HttpUtils.getJsonObjectUsingToken(productListURL, httpClient,somos);
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
                itemsObj = HttpUtils.getJsonObjectUsingToken(itemsUrl,httpClient,user);
            }
            if (itemsObj==null){
                finished=true;
                continue;
            }
            JSONArray itemsObjJSONArray = itemsObj.getJSONArray("results");
            for (Object itemObject : itemsObjJSONArray){
                JSONObject itemJSONObject = (JSONObject)itemObject;
                String itemId=itemJSONObject.getString("id");
                if (!itemsArrayList.contains(itemId)){
                    itemsArrayList.add(itemId);
                }
            }
            offset+=50;
            /*
            if (itemsObjJSONArray.length()<50){
                System.out.println("itemsObjJSONArray.length()<50");
                finished=true;
            }*/
        }

        return itemsArrayList;
    }


}
