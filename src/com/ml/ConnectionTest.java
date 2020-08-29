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
        //HttpGet httpGet = new HttpGet("https://www.mercadolibre.com.ar/");
        HttpGet httpGet = new HttpGet("https://www.lanacion.com.ar/");

        HttpHost proxyHost = HttpHost.create(proxyStr);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(40000) //40 seconds in milliseconds
                .setConnectionRequestTimeout(40000)
                .setSocketTimeout(40000)
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

        proxies.add("178.35.230.10:8080");
        proxies.add("179.228.138.152:3128");
        proxies.add("199.247.9.182:443");





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
