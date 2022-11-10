package com.ml.utils;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class BlockUsers {

    private static String userByNickname="https://api.mercadolibre.com/sites/MLA/search?nickname=";

    private static void lockUser(String nicknameToLock, String myNickname, boolean questions, boolean orders){
        if (questions){
            lockQuestions(nicknameToLock,myNickname);
        }
        if (orders){
            lockOrders(nicknameToLock,myNickname);
        }
    }


    private static void lockQuestions(String nicknameToLock, String myNickname){
        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
        String token = TokenUtils.getToken(myNickname);
        long userIdToLock=getUserIDByNickname(nicknameToLock,myNickname);
        String url1 = "https://api.mercadolibre.com/users/"+TokenUtils.getIdCliente(myNickname)+"/questions_blacklist";
        CloseableHttpResponse response = null;


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

    private static void lockOrders(String nicknameToLock, String myNickname){

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
        String token = TokenUtils.getToken(myNickname);
        long userIdToLock=getUserIDByNickname(nicknameToLock,myNickname);
        CloseableHttpResponse response = null;

        String url2 = "https://api.mercadolibre.com/users/" + TokenUtils.getIdCliente(myNickname) + "/order_blacklist";

        HttpPost httpPost2 = new HttpPost(url2);
        httpPost2.setHeader("Accept", "application/json");
        httpPost2.setHeader("Content-type", "application/json");
        httpPost2.addHeader("Authorization", "Bearer " + token);

        String json2 = "{ \"user_id\": " + userIdToLock + " }";
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

    private static boolean isLocked(String nicknameToCheck, String myNickname){
        boolean questionLock=isQuestionsLocked(nicknameToCheck,myNickname);
        boolean ordersLock=isOrderLocked(nicknameToCheck,myNickname);
        return questionLock && ordersLock;
    }

    private static boolean isOrderLocked(String nicknameToCheck, String myNickname){
        boolean orderLock=false;
        long userIdToCheck=getUserIDByNickname(nicknameToCheck,myNickname);
        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        boolean finished=false;
        int offset=0;
        while (!finished) {
            String url2 = "https://api.mercadolibre.com/users/"+TokenUtils.getIdCliente(myNickname)+"/order_blacklist?limit=50&offset=" + offset ;
            JSONObject object = HttpUtils.getJsonObjectUsingToken(url2,httpClient,myNickname,true);
            if (object!=null && object.has("elArray")){
                JSONArray userArray = object.getJSONArray("elArray");
                for (int i=0; i<userArray.length(); i++){
                    JSONObject user = userArray.getJSONObject(i);
                    if (user!=null && user.has("user")) {
                        user=user.getJSONObject("user");
                        if (user != null && user.has("id")) {
                            long userId = user.getLong("id");
                            if (userId == userIdToCheck) {
                                orderLock = true;
                                break;
                            }
                        }
                    }
                }
            }
            offset+=50;
            if (!object.has("users") || object.getJSONArray("users").length()==0){
                finished=true;
            }
        }
        return orderLock;
    }


    private static boolean isQuestionsLocked(String nicknameToCheck, String myNickname){
        boolean questionsLock=false;
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

        return questionsLock;
    }



    private static void unlockUser(String nickname, String myUserName){
        unlockOrders(nickname,myUserName);
        unlockQuestions(nickname,myUserName);
    }

    private static void unlockOrders(String nickname, String myUserName){
        long lockedUserId=getUserIDByNickname(nickname,myUserName);
        String unlockUrl="https://api.mercadolibre.com/users/"+TokenUtils.getIdCliente(myUserName)+"/order_blacklist/"+lockedUserId;
        HttpUtils.delete(unlockUrl,HttpUtils.buildHttpClient(),myUserName);
    }

    private static void unlockQuestions(String nickname, String myUserName){
        long lockedUserId=getUserIDByNickname(nickname,myUserName);
        String unlockUrl="https://api.mercadolibre.com/users/"+TokenUtils.getIdCliente(myUserName)+"/questions_blacklist/"+lockedUserId;
        HttpUtils.delete(unlockUrl,HttpUtils.buildHttpClient(),myUserName);
    }


    private static long getUserIDByNickname(String nickname,String apiUser){
        long result=0;
        String formattedNickName=nickname.replace(" ","%20");
        String userUrl = userByNickname+formattedNickName;
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

    private static void pausa(int seconds){
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    public static void main(String[] args){

        //todo correr 2 veces o ver si hay que meter una pausa porque no refleja los cambios al toque
        String nickNameToLock="TIENDA CN";

        ////ACACIA
        String myUserName=SData.getAcaciaYLenga();
        boolean isOrderLocked=isOrderLocked(nickNameToLock,myUserName);
        boolean isQuestionsLocked =isQuestionsLocked(nickNameToLock,myUserName);
        System.out.println("Chequeando en "+myUserName);
        System.out.println(nickNameToLock+" bloqueaPreguntas="+isQuestionsLocked+" bloqueaCompras="+isOrderLocked);
        System.out.println("bloqueando...");
        lockUser(nickNameToLock,myUserName,true,true);
        pausa(30);
        System.out.println("ok!");
        System.out.println(nickNameToLock+" bloqueaPreguntas="+isQuestionsLocked+" bloqueaCompras="+isOrderLocked);


        ////SOMOS
        myUserName=SData.getSomosMas();
        isOrderLocked=isOrderLocked(nickNameToLock,myUserName);
        isQuestionsLocked =isQuestionsLocked(nickNameToLock,myUserName);
        System.out.println("\n\nChequeando en "+myUserName);
        System.out.println(nickNameToLock+" bloqueaPreguntas="+isQuestionsLocked+" bloqueaCompras="+isOrderLocked);
        System.out.println("bloqueando...");
        lockUser(nickNameToLock,myUserName,true,true);
        pausa(30);
        System.out.println("ok!");
        System.out.println(nickNameToLock+" bloqueaPreguntas="+isQuestionsLocked+" bloqueaCompras="+isOrderLocked);

    }

}
