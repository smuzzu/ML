package com.ml.utils;

import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.util.ArrayList;

public class ProxyChecker {

    public static CloseableHttpClient buildHttpClientWithProxy(String proxy) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("random", "random"));

        RequestConfig requestConfig = HttpUtils.buildRequestConfig(proxy);

        CloseableHttpClient httpclient =
                HttpClientBuilder.create()
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .setDefaultRequestConfig(requestConfig)
                        .setConnectionManagerShared(true)
                        .setMaxConnPerRoute(1000)
                        .setMaxConnTotal(1000)
                        .build();
        return httpclient;
    }

    private static void checkProxy(String proxy,String url){
        CloseableHttpClient httpClient = buildHttpClientWithProxy(proxy);
        HttpGet httpGet = null;

        try {
            httpGet = new HttpGet(url);
        } catch (Exception e){
            String msg = "Error parseando url "+url;
            System.out.println(msg);
            e.printStackTrace();
        }

        CloseableHttpResponse response = null;
        HttpContext context = new BasicHttpContext();

        long timestamp1=System.currentTimeMillis();

        try {
            response = httpClient.execute(httpGet, context);
        }catch (Exception e){
            if (e.getMessage()!=null && e.getMessage().contains("timed out")){
                long timestamp3=System.currentTimeMillis();
                long elapsed2=(timestamp3-timestamp1)/1000;
                String msg = "Timeout ejecutando get en " + url + " con proxy " + proxy +" "+elapsed2+" s";
                System.out.println(msg);
                return;
            }else {
                String msg = "Exception ejecutando get en " + url + " con proxy " + proxy;
                System.out.println(msg);
                e.printStackTrace();
                return;
            }
        }
        if (response == null) {
            String msg = "Null response ejecutando get en "+url+" con proxy "+proxy;
            System.out.println(msg);
            return;
        }
        StatusLine statusline = response.getStatusLine();
        if (statusline == null) {
            String msg = "StatusLine is null ejecutando get en "+url+" con proxy "+proxy;
            System.out.println(msg);
            return;
        }
        int statusCode = statusline.getStatusCode();
        if (statusCode!=200){
            String msg = "Http "+statusCode+" ejecutando get en "+url+" con proxy "+proxy;
            System.out.println(msg);
            return;
        }
        long timestamp2=System.currentTimeMillis();
        long elapsed=(timestamp2-timestamp1)/1000;
        System.out.println("OK proxy "+proxy+" en "+elapsed+" s");
    }

    public static void checkAllProxies(String url){
        ArrayList<String> proxyList=HttpUtils.loadProxiesFromFile();
        for (String proxy:proxyList){
            if (proxy.equals("NADA")){
                continue;
            }
            checkProxy(proxy,url);
        }
    }


    private static void speedCheck(String url){
        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
        HttpGet httpGet = null;

        try {
            httpGet = new HttpGet(url);
        } catch (Exception e){
            String msg = "Error parseando url "+url;
            System.out.println(msg);
            e.printStackTrace();
        }

        CloseableHttpResponse response = null;
        HttpContext context = new BasicHttpContext();

        long timestamp0 = System.currentTimeMillis();

        for (int i=0; i<500; i++) {


            long timestamp1 = System.currentTimeMillis();

            try {
                response = httpClient.execute(httpGet, context);
            } catch (Exception e) {
                if (e.getMessage().contains("timed out")) {
                    long timestamp3 = System.currentTimeMillis();
                    long elapsed2 = (timestamp3 - timestamp1) / 1000;
                    String msg = "Timeout ejecutando get en " + url + " sin proxy " + elapsed2 + " s";
                    System.out.println(msg);
                    return;
                } else {
                    String msg = "Exception ejecutando get en " + url + " sin proxy ";
                    System.out.println(msg);
                    e.printStackTrace();
                    return;
                }
            }
            if (response == null) {
                String msg = "Null response ejecutando get en " + url + " sin proxy";
                System.out.println(msg);
                return;
            }
            StatusLine statusline = response.getStatusLine();
            if (statusline == null) {
                String msg = "StatusLine is null ejecutando get en " + url + " sin proxy";
                System.out.println(msg);
                return;
            }
            int statusCode = statusline.getStatusCode();
            if (statusCode != 200) {
                String msg = "Http " + statusCode + " ejecutando get en " + url + " sin proxy";
                System.out.println(msg);
                return;
            }
            long timestamp2 = System.currentTimeMillis();
            long elapsed = (timestamp2 - timestamp1) / 1000;
            long totalElapsed = (timestamp2 - timestamp0) / 1000;
            System.out.println(i+" OK " + elapsed + " s / total "+totalElapsed+" s");
        }

    }

    public static void main (String [] args){
        String url="https://www.mercadolibre.com.ar/";
        checkAllProxies(url);
        //speedCheck(url);

    }
}
