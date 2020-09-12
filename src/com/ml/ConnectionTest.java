package com.ml;

import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ConnectionTest {

    public static CloseableHttpClient buildHttpClient2() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("random", "random"));

        HttpHost proxyHost = HttpHost.create("62.148.67.110:81");

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(40000) //40 seconds in milliseconds
                .setConnectionRequestTimeout(40000)
                .setSocketTimeout(40000)
                .setCookieSpec(CookieSpecs.STANDARD)
                .setProxy(proxyHost)
                .build();


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


    private static int isProxyAlive(CloseableHttpClient client ,String proxyStr){
        HttpGet httpGet = new HttpGet("https://www.mercadolibre.com.ar/");
        //HttpGet httpGet = new HttpGet("https://www.lanacion.com.ar/");

        HttpHost proxyHost = HttpHost.create(proxyStr);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(20000) //40 seconds in milliseconds
                .setConnectionRequestTimeout(20000)
                .setSocketTimeout(20000)
                .setCookieSpec(CookieSpecs.STANDARD)
                .setProxy(proxyHost)
                .build();
        httpGet.setConfig(requestConfig);


        CloseableHttpResponse response = null;
        HttpContext context = new BasicHttpContext();

        long t1 = System.nanoTime();

        try {
            response = client.execute(httpGet, context);
        } catch (IOException e) {
            String msg = "Proxy "+proxyStr+" request throws exepcion ";
            System.out.println(msg);
            e.printStackTrace();
            Logger.log(msg);
            Logger.log(e);
            return -1;
        }

        long t2 = System.nanoTime();

        int decimasDeSegundo = (int) ((t2-t1)/100000000);

        int statusCode=-1;
        StatusLine statusline = response.getStatusLine();
        if (statusline != null) {
            statusCode = statusline.getStatusCode();
        }
        if (statusCode!=200){
            String msg = "Proxy "+proxyStr+" statuscode ="+statusCode+" statusline="+statusline;
            System.out.println(msg);
            Logger.log(msg);
            return -1;
        }

        InputStream inputStream = null;
        HttpEntity httpEntity = response.getEntity();
        try {
            inputStream = httpEntity.getContent();
        } catch (IOException e) {
            String msg = "Proxy "+proxyStr+" cannot get response content";
            System.out.println(msg);
            e.printStackTrace();
            Logger.log(msg);
            Logger.log(e);
            return -1;
        }


        String responseStr = null;
        if (inputStream!=null) {
            responseStr = HttpUtils.getStringFromInputStream(inputStream);
        }
        if (responseStr==null || responseStr.trim().isEmpty()){
            String msg = "Proxy "+proxyStr+" response is empty";
            System.out.println(msg);
            Logger.log(msg);
            return -1;
        }

        return decimasDeSegundo;
    }


    private static  ArrayList<String> getProxies(){
        ArrayList<String> proxies = new ArrayList<String>();


/*
        proxies.add("18.141.216.9:80");     //transparent
        proxies.add("52.179.18.244:8080");
        proxies.add("54.169.225.153:80");   //transparent
        proxies.add("54.255.19.255:80");    //transparent
        proxies.add("83.97.23.90:18080");   //high anonymous
        proxies.add("95.174.67.50:18080");  //high anonymous
        proxies.add("179.228.138.152:3128");
        proxies.add("199.247.9.182:443");
        proxies.add("213.174.89.7:3128");
*/


        //proxies.add("186.211.177.161:8082");
        //proxies.add("125.177.124.73:80");
        //proxies.add("191.241.39.42:3128");
        proxies.add("199.247.9.182:443");
        proxies.add("200.225.198.170:3128");
        proxies.add("213.137.240.243:81");
        //proxies.add("213.174.89.7:3128");
        //proxies.add("34.80.14.167:3128");
        proxies.add("45.79.225.183:3128");
        //proxies.add("54.37.130.205:3128");
        proxies.add("71.174.241.163:3128");
        //proxies.add("77.94.245.73:8080");
        proxies.add("78.96.125.24:3128");
        proxies.add("85.214.244.174:3128");
        proxies.add("91.214.179.24:8080");
        proxies.add("94.251.95.94:81");

        return proxies;
    }


    public static void main (String[] args) {

        ArrayList<String> proxies = getProxies();
        CloseableHttpClient client = HttpUtils.buildHttpClient();

        for (String proxy: proxies){
            int time = isProxyAlive(client,proxy);
            System.out.println(proxy+" "+time);
        }

    }
}
