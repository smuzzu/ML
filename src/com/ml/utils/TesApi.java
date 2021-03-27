package com.ml.utils;

import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class TesApi {

    private static String ACACIA="ACACIAYLENGA";
    private static String SOMOS="SOMOS_MAS";
    private static String QUEFRESQUETE="QUEFRESQUETE";
    private static String sampleCustomerUrl="https://api.mercadolibre.com/sites/MLA/search?nickname=modular+drawers";


    public static void main(String[] args){

        String url = sampleCustomerUrl;
        String apiUser=QUEFRESQUETE;
        boolean arrayResponse=false;


        CloseableHttpClient client = HttpUtils.buildHttpClient();
        JSONObject  object = HttpUtils.getJsonObjectUsingToken(url,client,apiUser,arrayResponse);

        boolean b=false;
    }

}
