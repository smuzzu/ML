package com.ml.utils;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;


public class TesApi {

    private static String ACACIA=SData.getAcaciaYLenga();
    private static String SOMOS=SData.getSomosMas();
    private static String QUEFRESQUETE=SData.getQuefresquete();
    private static String sampleCustomerUrl="https://api.mercadolibre.com/sites/MLA/search?nickname=modular+drawers";
    private static String baseProductUrl="https://api.mercadolibre.com/items/";
    private static String baseOrderUrl="https://api.mercadolibre.com/orders/";
    private static String baseShippingUrl="https://api.mercadolibre.com/shipments/";
    private static String userByNickname="https://api.mercadolibre.com/sites/MLA/search?nickname=";
    private static String mercadopagoPayment="https://api.mercadopago.com/v1/payments/search?sort=date_created&criteria=desc&external_reference=";
    private static String mercadopagoPayment2="https://api.mercadopago.com/v1/payments/";
    private static String mercadopagoOrder="https://api.mercadopago.com/merchant_orders/search?payer_id=";
    private static String mercadopagoCustomer="https://api.mercadopago.com/v1/customers/";


    private static String getCategory(String productId,String apiUser){
        String result="N/A";
        String productUrl=baseProductUrl+productId;
        JSONObject  itemObject = HttpUtils.getJsonObjectUsingToken(productUrl,HttpUtils.buildHttpClient(),apiUser,false);
        if (itemObject!=null && itemObject.has("category_id") && !itemObject.isNull("category_id")){
            result=itemObject.getString("category_id");
        }
        System.out.println(result);
        return result;
    }


    private static JSONObject getShipping(String orderId,String apiUser){
        JSONObject orderObject = getOrder(orderId,QUEFRESQUETE);
        JSONObject shippingObject = orderObject.getJSONObject("shipping");
        String shippingId=""+shippingObject.getLong("id");
        String shippingtUrl=baseShippingUrl+shippingId;
        shippingObject = HttpUtils.getJsonObjectUsingToken(shippingtUrl,HttpUtils.buildHttpClient(),apiUser,false);
        return shippingObject;
    }

    private static JSONObject getOrder(String orderId,String apiUser){
        String result="N/A";
        String productUrl=baseOrderUrl+orderId;
        JSONObject  orderObject = HttpUtils.getJsonObjectUsingToken(productUrl,HttpUtils.buildHttpClient(),apiUser,false);
        return orderObject;
    }

    private static long getUserIDByNickname(String nickname,String apiUser){
        long result=0;
        String userUrl = userByNickname+nickname;
        JSONObject userObject = HttpUtils.getJsonObjectUsingToken(userUrl,HttpUtils.buildHttpClient(),apiUser,false);
        if (userObject!=null && userObject.has("seller")){
            JSONObject sellerObject=userObject.getJSONObject("seller");
            if (sellerObject!=null && sellerObject.has("id")){
                result=sellerObject.getLong("id");
            }
        }
        if (result==0){
            System.out.println("No se pudo obtener el id de "+nickname);
        }
        return result;
    }

    private static void unlockUser(String nickname, String user){
        long lockedUserId=getUserIDByNickname(nickname,user);
        String unlockUrl="https://api.mercadolibre.com/users/"+TokenUtils.getIdCliente(user)+"/order_blacklist/"+lockedUserId;
        HttpUtils.delete(unlockUrl,HttpUtils.buildHttpClient(),user);
    }

    private static void lockUser(String nicknameToLock, String myNickname, boolean questions, boolean orders){

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
        String token = TokenUtils.getToken(myNickname);
        long userIdToLock=getUserIDByNickname(nicknameToLock,myNickname);
        String url1 = "https://api.mercadolibre.com/users/"+TokenUtils.getIdCliente(myNickname)+"/questions_blacklist";
        CloseableHttpResponse response = null;

        if (questions) {
            HttpPost httpPost = new HttpPost(url1);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.addHeader("Authorization", "Bearer " + token);

            String json = "{ \"user_id\": " + userIdToLock + " }";
            StringEntity entity = new StringEntity(json, StandardCharsets.US_ASCII);
            httpPost.setEntity(entity);

            try {
                response = httpClient.execute(httpPost);
            } catch (Exception e) {
                Logger.log("Error executing post " + e.getMessage());
                Logger.log(e);
                e.printStackTrace();
            }

            if (response != null) {
                StatusLine statusline = response.getStatusLine();
                if (statusline != null) {
                    int statusCode = statusline.getStatusCode();
                    if (statusCode == 200 || statusCode == 201) { //este cambio dejarlo
                    } else {
                        Logger.log("Error actualizando bloqueando usuario en questions_blacklist. " + nicknameToLock + " status code =  " + statusCode);
                    }
                }
            }
        }

        if (orders) {
            String url2 = "https://api.mercadolibre.com/users/" + TokenUtils.getIdCliente(myNickname) + "/order_blacklist";

            HttpPost httpPost2 = new HttpPost(url2);
            httpPost2.setHeader("Accept", "application/json");
            httpPost2.setHeader("Content-type", "application/json");
            httpPost2.addHeader("Authorization", "Bearer " + token);

            String json2 = "{ user_id: {" + userIdToLock + "} }";
            StringEntity entity2 = new StringEntity(json2, StandardCharsets.US_ASCII);
            httpPost2.setEntity(entity2);

            try {
                response = httpClient.execute(httpPost2);
            } catch (Exception e) {
                Logger.log("Error executing post " + e.getMessage());
                Logger.log(e);
                e.printStackTrace();
            }

            if (response != null) {
                StatusLine statusline = response.getStatusLine();
                if (statusline != null) {
                    int statusCode = statusline.getStatusCode();
                    if (statusCode == 200 || statusCode == 201) { //este cambio dejarlo
                    } else {
                        Logger.log("Error actualizando bloqueando usuario en order_blacklist. " + nicknameToLock + " status code =  " + statusCode);
                    }
                }
            }
        }
    }

    static boolean isLocked(String nicknameToCheck, String myNickname){
        boolean questionsLock=false;
        boolean orderLock=false;
        long userIdToCheck=getUserIDByNickname(nicknameToCheck,myNickname);
        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        boolean finished=false;
        int offset=0;
        while (!finished) {
            String url1 = "https://api.mercadolibre.com/users/" + TokenUtils.getIdCliente(myNickname) + "/questions_blacklist?limit=50&offset=" + offset ;
            JSONObject object = HttpUtils.getJsonObjectUsingToken(url1, httpClient, myNickname, false);
            if (object != null && object.has("users")) {
                JSONArray userArray = object.getJSONArray("users");
                for (int i = 0; i < userArray.length(); i++) {
                    JSONObject user = userArray.getJSONObject(i);
                    if (user != null && user.has("id")) {
                        long userId = user.getLong("id");
                        if (userId == userIdToCheck) {
                            questionsLock = true;
                            break;
                        }
                    }
                }
            }
            offset+=50;
            if (!object.has("users") || object.getJSONArray("users").length()==0){
                finished=true;
            }
        }

        finished=false;
        offset=0;
        while (!finished) {
            String url2 = "https://api.mercadolibre.com/users/"+TokenUtils.getIdCliente(myNickname)+"/order_blacklist?limit=50&offset=" + offset ;
            JSONObject object = HttpUtils.getJsonObjectUsingToken(url2,httpClient,myNickname,false);
            if (object!=null && object.has("users")){
                JSONArray userArray = object.getJSONArray("users");
                for (int i=0; i<userArray.length(); i++){
                    JSONObject user = userArray.getJSONObject(i);
                    if (user!=null && user.has("id")){
                        long userId=user.getLong("id");
                        if (userId==userIdToCheck){
                            orderLock=true;
                            break;
                        }
                    }
                }
            }
            offset+=50;
            if (!object.has("users") || object.getJSONArray("users").length()==0){
                finished=true;
            }
        }

        return questionsLock && orderLock;
    }


    public static void main(String[] args){

        //isLocked("EDI_RE2010","SOMOS_MAS");
        //unlockUser("LAMESITAELEVABLE","SOMOS_MAS");
        long orderId=4753621661L;
        long customerId=221535448L;
        JSONObject object = HttpUtils.getJsonObjectUsingToken("https://api.mercadopago.com/v1/customers/"+customerId,HttpUtils.buildHttpClient(),ACACIA,false);
        lockUser("EDI_RE2010","SOMOS_MAS",true,true);
        String oderId="4485479390";
        //JSONObject orderObject = getOrder(oderId,QUEFRESQUETE);
        JSONObject shippingObject = getShipping(oderId,QUEFRESQUETE);

        String productId="MLA901398938";
        getCategory(productId,SOMOS);

        boolean b=false;
    }

}
