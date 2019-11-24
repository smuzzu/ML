package com.ml.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class HttpUtils {

    public static final String URLChanged="urlChanged|";
    public static final String EXPIRED_TOKEN = "401|";

    public static CloseableHttpClient buildHttpClient() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("random", "random"));

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(40000) //40 seconds in milliseconds
                .setConnectionRequestTimeout(40000)
                .setSocketTimeout(40000)
                .setCookieSpec(CookieSpecs.STANDARD)
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


    public static JSONObject getJsonObjectWithoutToken(String uRL, CloseableHttpClient httpClient) {

        JSONObject jsonResponse=null;

        String jsonStringFromRequest = HttpUtils.getHTMLStringFromPage(uRL, httpClient, false);
        if (isOK(jsonStringFromRequest)) {
            jsonStringFromRequest = jsonStringFromRequest.substring(3);
            if (jsonStringFromRequest.startsWith("[")){
                jsonStringFromRequest=jsonStringFromRequest.substring(1,jsonStringFromRequest.length()-1);
            }
            try {
                jsonResponse = new JSONObject(jsonStringFromRequest);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
        return jsonResponse;
    }

    public static JSONObject getJsonObjectUsingToken(String uRL, CloseableHttpClient httpClient, String usuario) {

        JSONObject jsonResponse=null;
        String token= TokenUtils.getToken(usuario);
        String urlWithToken = uRL + "&access_token=" + token;
        String jsonStringFromRequest = HttpUtils.getHTMLStringFromPage(urlWithToken, httpClient, false);
        if (jsonStringFromRequest.equals(EXPIRED_TOKEN)) {
            TokenUtils.refreshToken(httpClient,usuario);
            token= TokenUtils.getToken(usuario);
            urlWithToken = uRL + "&access_token=" + token;
            jsonStringFromRequest = HttpUtils.getHTMLStringFromPage(urlWithToken, httpClient, false);
        }
        if (!isOK(jsonStringFromRequest)){ //1 solo reintento
            try {
                Thread.sleep(5000);//aguantamos los trapos 5 segundos antes de reintentar
            } catch (InterruptedException e) {
                Logger.log(e);
            }

            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            httpClient = null;
            httpClient = HttpUtils.buildHttpClient();
            jsonStringFromRequest = HttpUtils.getHTMLStringFromPage(urlWithToken, httpClient, false);
        }
        if (isOK(jsonStringFromRequest)) {
            jsonStringFromRequest = jsonStringFromRequest.substring(3);
            if (jsonStringFromRequest.startsWith("[")){
                jsonStringFromRequest=jsonStringFromRequest.substring(1,jsonStringFromRequest.length()-1);
            }
            try {
                jsonResponse = new JSONObject(jsonStringFromRequest);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
        return jsonResponse;
    }


    public static String getHTMLStringFromPage(String uRL, CloseableHttpClient client, boolean DEBUG) {

        HttpGet httpGet = new HttpGet(uRL);

        CloseableHttpResponse response = null;
        HttpContext context = new BasicHttpContext();

        int retries = 0;
        boolean retry = true;
        int statusCode = 0;

        while (retry && retries < 5) {
            retries++;
            try {
                response = client.execute(httpGet, context);
            } catch (IOException e) {
                response = null;
                Logger.log("Error en getHTMLStringFromPage intento #" + retries + " " + uRL);
                Logger.log(e);
            }

            if (response != null) {
                StatusLine statusline = response.getStatusLine();
                if (statusline != null) {
                    statusCode = statusline.getStatusCode();
                    retry = false;
                }
            }

            if (retry) {
                try {
                    Thread.sleep(2000 * retries * retries);//aguantamos los trapos 5 segundos antes de reintentar
                    } catch (InterruptedException e) {
                        Logger.log(e);
                    }
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    client = null;
                    client = HttpUtils.buildHttpClient();
            }
        }

        if (statusCode != 200) {
            if ((DEBUG) || (statusCode != 404 && statusCode != 403)) {  //403 y 404 se loguea solo con debug
                Logger.log("new status code " + statusCode + " " + uRL);
            }
            return ""+statusCode+"|";
        }

        if (isUrlChanged(context, uRL)) {
            if (DEBUG) {
                Logger.log("url changed " + uRL);
            }
            return URLChanged;
        }

        InputStream inputStream = null;

        if (response!=null) {
            HttpEntity httpEntity = response.getEntity();
            try {
                inputStream = httpEntity.getContent();
            } catch (IOException e) {
                Logger.log(e);
                e.printStackTrace();
            }
        }

        if (inputStream!=null) {
            String responseStr = getStringFromInputStream(inputStream);
            if (responseStr != null && responseStr.length() > 0) {
                return "ok|" + responseStr;
            }
        }

        return "nullORempty|";
    }

    public static boolean isOK(String hmlString){
        if (hmlString!=null && hmlString.length()>1){
            String result=hmlString.substring(0,2);
            return result.equals("ok");
        }
        return false;
    }

    public static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    private static synchronized boolean isUrlChanged(HttpContext context, String url) {
        HttpRequestWrapper httpRequestWrapper = (HttpRequestWrapper) context.getAttribute("http.request");
        HttpRequest newRequest = httpRequestWrapper.getOriginal();
        RequestLine requestLine = newRequest.getRequestLine();
        String newURL = requestLine.getUri();
        if (newURL == null || newURL.indexOf("NoIndex_True") > 0 || (newURL.indexOf("mercadolibre.com.ar") == -1 && newURL.indexOf("api.mercadolibre.com") == -1)) {
            return true;
        }
        if (url.indexOf("MLA-")>0){
            String productId1=HTMLParseUtils.getProductIdFromURL(url);
            String productId2=HTMLParseUtils.getProductIdFromURL(newURL);
            if (!productId1.equals(productId2)){
                return true;
            }
        }

        return false;
    }

    public static ArrayList<String> getNewQuestionsFromPreviousLastQuestion(String uRL, CloseableHttpClient httpClient, String runnerID, boolean DEBUG, String previousLastQuestion) {

        ArrayList<String> newQuestions = new ArrayList<String>();
        String questionsURL = HTMLParseUtils.getQuestionsURL(uRL);
        String htmlStringFromQuestionsPage = getHTMLStringFromPage(questionsURL, httpClient, DEBUG);
        if (!HttpUtils.isOK(htmlStringFromQuestionsPage)) {
            // hacemos pausa por si es problema de red
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Logger.log(e);
            }
            Logger.log(runnerID + " hmlstring from page 2 is null " + uRL);
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            httpClient = null;
            httpClient = HttpUtils.buildHttpClient();
        } else {//procesamos las preguntas
            String[] allquestionsOnPage = StringUtils.substringsBetween(htmlStringFromQuestionsPage, "questions__item--question", "</p>");
            if (allquestionsOnPage != null) { //todo check
                for (String question : allquestionsOnPage) {
                    int pos1 = question.indexOf("<p>") + 3;
                    question = question.substring(pos1);
                    if (question.equals(previousLastQuestion)) {
                        break;
                    }
                    newQuestions.add(question);
                }
            }
        }
        return newQuestions;
    }


}
