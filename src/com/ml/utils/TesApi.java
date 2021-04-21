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


    public static void main(String[] args){

        String productId="MLA901398938";
        getCategory(productId,SOMOS);

        boolean b=false;
    }

}
