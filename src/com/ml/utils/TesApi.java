package com.ml.utils;

import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class TesApi {

    private static String ACACIA=SData.getAcaciaYLenga();
    private static String SOMOS=SData.getSomosMas();
    private static String QUEFRESQUETE=SData.getQuefresquete();
    private static String sampleCustomerUrl="https://api.mercadolibre.com/sites/MLA/search?nickname=modular+drawers";
    private static String baseProductUrl="https://api.mercadolibre.com/items/";
    private static String baseOrderUrl="https://api.mercadolibre.com/orders/";
    private static String baseShippingUrl="https://api.mercadolibre.com/shipments/";
    private static String userByNickname="https://api.mercadolibre.com/sites/MLA/search?nickname=";


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

    public static void main(String[] args){

        unlockUser("LAMESITAELEVABLE","SOMOS_MAS");

        String oderId="4485479390";
        //JSONObject orderObject = getOrder(oderId,QUEFRESQUETE);
        JSONObject shippingObject = getShipping(oderId,QUEFRESQUETE);

        String productId="MLA901398938";
        getCategory(productId,SOMOS);

        boolean b=false;
    }

}
