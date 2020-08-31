package com.ml.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;

public class HttpUtils {

    public static final String URLChanged="urlChanged|";
    public static final String EXPIRED_TOKEN = "401|";

    private static int currentProxyNumber=0;

    private static boolean PROXY_ENABLED=false;
    private static String[] proxyList= new String[]{
        "83.97.23.90:18080",
        "178.35.230.10:8080",
        "179.228.138.152:3128",
        "191.233.198.18:80",
        "199.247.9.182:443"
    };
    static long requestCount=0;
    static long timeRequestCount=System.nanoTime();


    synchronized private static String getProxy() {
        int totalproxies=proxyList.length;
        currentProxyNumber++;
        if (currentProxyNumber>=totalproxies){
            currentProxyNumber=0;
        }
        return proxyList[currentProxyNumber];
    }


    public static RequestConfig buildRequestConfig(String proxyHostStr){
        RequestConfig requestConfig = null;
        if (proxyHostStr==null || proxyHostStr.equals("NADA")) {
            requestConfig = RequestConfig.custom()
                    .setConnectTimeout(40000) //40 seconds in milliseconds
                    .setConnectionRequestTimeout(40000)
                    .setSocketTimeout(40000)
                    .setCookieSpec(CookieSpecs.STANDARD)
                    .build();
        }else {
            HttpHost proxyHost = HttpHost.create(proxyHostStr);
            requestConfig = RequestConfig.custom()
                    .setConnectTimeout(40000) //40 seconds in milliseconds
                    .setConnectionRequestTimeout(40000)
                    .setSocketTimeout(40000)
                    .setCookieSpec(CookieSpecs.STANDARD)
                    .setProxy(proxyHost)
                    .build();
        }
        return requestConfig;
    }

    public static CloseableHttpClient buildHttpClient() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("random", "random"));

        RequestConfig requestConfig = buildRequestConfig(null);

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

        String jsonStringFromRequest = getHTMLStringFromPage(uRL, httpClient, false, false);
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

    public static boolean postMessage(String text, CloseableHttpClient httpClient, long packId, String user, long customerId) {
        boolean ok=false;
        String myUserId=TokenUtils.getIdCliente(user);
        String token = TokenUtils.getToken(user);
        String url = "https://api.mercadolibre.com/messages/packs/"+packId+"/sellers/"+myUserId+"?access_token="+token;

        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        String json = "{\"from\" : { \"user_id\": \""+myUserId
                +"\",\"email\" : \"abcdfg@nospam.com\"},\"to\": { \"user_id\" : \""+customerId+"\"}, \"text\": \""+
                text+"\"}";

        StringEntity entity = new StringEntity(json,"UTF-8");
        httpPost.setEntity(entity);

        CloseableHttpResponse response=null;
        try {
            response = httpClient.execute(httpPost);

        }
        catch (Exception e){
            Logger.log("Error executing put "+e.getMessage());
            Logger.log(e);
            e.printStackTrace();
        }

        if (response!=null) {
            StatusLine statusline = response.getStatusLine();
            if (statusline != null) {
                int statusCode = statusline.getStatusCode();
                if (statusCode == 201) {
                    ok=true;
                }
            }
        }
        return ok;
    }


    public static JSONObject getJsonObjectUsingToken(String uRL, CloseableHttpClient httpClient, String usuario) {

        JSONObject jsonResponse=null;
        String token= TokenUtils.getToken(usuario);
        String urlWithToken = uRL + "&access_token=" + token;
        String jsonStringFromRequest = getHTMLStringFromPage(urlWithToken, httpClient, false, false);
        if (jsonStringFromRequest.equals(EXPIRED_TOKEN)) {
            TokenUtils.refreshToken(httpClient,usuario);
            token= TokenUtils.getToken(usuario);
            urlWithToken = uRL + "&access_token=" + token;
            jsonStringFromRequest = getHTMLStringFromPage(urlWithToken, httpClient, false, false);
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
            jsonStringFromRequest = getHTMLStringFromPage(urlWithToken, httpClient, false, false);
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

    synchronized static long increaseRequestCount(boolean reset){
        requestCount++;
        if (reset){
            requestCount=0;
            timeRequestCount=System.nanoTime();
        }
        return requestCount;
    }


    public static String getHTMLStringFromPage(String uRL, CloseableHttpClient client, boolean DEBUG, boolean useProxy) {

        HttpGet httpGet = new HttpGet(uRL);

        CloseableHttpResponse response = null;
        HttpContext context = new BasicHttpContext();

        int retries = 0;
        boolean retry = true;
        int statusCode = 0;

        while (retry && retries < 5) {
            retries++;

            String proxy = "NO_PROXY";
            if (PROXY_ENABLED) {
                proxy = getProxy();
                RequestConfig requestConfig = buildRequestConfig(proxy);
                httpGet.setConfig(requestConfig);
            }

            try {
                response = client.execute(httpGet, context);
            } catch (IOException e) {
                response = null;
                Logger.log("Error en getHTMLStringFromPage intento #" + retries + " " + uRL+ " con proxy "+proxy);
                Logger.log(e);
            }

            long requestCount=increaseRequestCount(false);
            if (requestCount>=8500){
                int eplapsedSeconds = (int) ((System.nanoTime()-timeRequestCount)/1000000000L);
                int minSeconds=420;
                if (eplapsedSeconds<minSeconds) {
                    if (PROXY_ENABLED && useProxy) {

                    } else {
                        long pasuseMilliseconds = (minSeconds - eplapsedSeconds) * 1000L;
                        System.out.println("Aguantamos los trapos " + pasuseMilliseconds + " milisegundos ");
                        try {
                            Thread.sleep(pasuseMilliseconds);
                        } catch (InterruptedException e) {
                            Logger.log(e);
                        }
                        requestCount=increaseRequestCount(false);
                        if (requestCount>=9000){
                            increaseRequestCount(true);
                        }
                    }
                }else {
                    increaseRequestCount(true);
                }
            }







            if (response != null) {
                StatusLine statusline = response.getStatusLine();
                if (statusline != null) {
                    statusCode = statusline.getStatusCode();
                    if (statusCode!=420 && statusCode!=403) { //429=too many requests
                        retry = false;
                    } else {
                        Logger.log("Http "+statusCode+" en getHTMLStringFromPage intento #" + retries + " " + uRL);
                        //todo en el 403 hacemos algo?
                        int segundosQuePasaron = (int) ((System.nanoTime()-timeRequestCount)/1000000000L);
                        boolean b=false;
                    }
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

    public static boolean putJsonOnURL(CloseableHttpClient client, String uRL, JSONObject jsonObject, String usuario){
        String token = TokenUtils.getToken(usuario);
        String urlWithToken = uRL + "&access_token=" + token;
        HttpPost httpPut = new HttpPost(urlWithToken);
        String strContent=jsonObject.toString();
        //strContent=strContent.substring(1,strContent.length()-1);//volamos las llaves {}
        StringEntity requestEntity = new StringEntity(
                strContent,
                ContentType.APPLICATION_JSON);
        httpPut.setEntity(requestEntity);

        CloseableHttpResponse response = null;
        HttpContext context = new BasicHttpContext();

        int retries = 0;
        boolean retry = true;
        int statusCode = 0;

        while (retry && retries < 3) {
            retries++;
            try {
                response = client.execute(httpPut, context);
            } catch (IOException e) {
                response = null;
                Logger.log("Error en putJsonOnURL intento #" + retries + " " + uRL);
                Logger.log(e);
            }

            if (response != null) {
                StatusLine statusline = response.getStatusLine();
                if (statusline != null) {
                    statusCode = statusline.getStatusCode();
                    if (statusCode!=420) { //429=too many requests
                        retry = false;
                    } else {
                        Logger.log("Http 420 en putJsonOnURL intento #" + retries + " " + uRL);
                    }
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
            String msgStr = "Error in putJsonOnURL http="+statusCode;
            System.out.println(msgStr);
            Logger.log(msgStr);
            return false; //error on put
        }

        return true; //put successfull
    }


    public static boolean downloadFile(CloseableHttpClient httpClient, String fileUrl, String filePath){
        HttpGet httpGet = new HttpGet(fileUrl);

        CloseableHttpResponse response = null;
        HttpContext context = new BasicHttpContext();

        int retries = 0;
        boolean retry = true;
        int statusCode = 0;

        while (retry && retries < 5) {
            retries++;
            try {
                response = httpClient.execute(httpGet, context);
            } catch (IOException e) {
                response = null;
                Logger.log("Error downloading binary file intento #" + retries + " " + fileUrl);
                Logger.log(e);
            }

            if (response != null) {
                StatusLine statusline = response.getStatusLine();
                if (statusline != null) {
                    statusCode = statusline.getStatusCode();
                    if (statusCode!=420) { //429=too many requests
                        retry = false;
                    } else {
                        Logger.log("Http 420 downloading pdf intento #" + retries + " " + fileUrl);
                    }
                }
            }

            if (retry) {
                try {
                    Thread.sleep(2000 * retries * retries);//aguantamos los trapos 5 segundos antes de reintentar
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
            }
        }

        if (statusCode != 200) {
            String errorMsg="error downloading label file http=" + statusCode + " " + fileUrl;
            System.out.println(errorMsg);
            Logger.log(errorMsg);
            return false;
        }

        InputStream is = null;
        try {
            is = response.getEntity().getContent();
            FileOutputStream fos = new FileOutputStream(new File(filePath));
            int inByte;
            while((inByte = is.read()) != -1)
                fos.write(inByte);
            is.close();
            fos.close();
        } catch (IOException e) {
            String errorMsg="error downloading label file 2 "+ filePath;
            System.out.println(errorMsg);
            Logger.log(errorMsg);
            e.printStackTrace();
            Logger.log(e);
            return false;
        }
        return true;
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
        String newURL = null;
        if (context.getAttribute("http.request") instanceof HttpRequestWrapper) {
            HttpRequestWrapper httpRequestWrapper = (HttpRequestWrapper) context.getAttribute("http.request");
            HttpRequest newRequest = httpRequestWrapper.getOriginal();
            RequestLine requestLine = newRequest.getRequestLine();
            newURL = requestLine.getUri();
        }else {
            CookieOrigin cookieOrigin = (CookieOrigin)context.getAttribute("http.cookie-origin");
            HttpHost httpHost = (HttpHost)context.getAttribute("http.target_host");
            newURL=httpHost.toURI()+cookieOrigin.getPath();
        }
        if (newURL == null || newURL.indexOf("NoIndex_True") > 0 || newURL.indexOf("redirectedFromVip") > 0 || (newURL.indexOf("mercadolibre.com.ar") == -1 && newURL.indexOf("api.mercadolibre.com") == -1)) {
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
        String htmlStringFromQuestionsPage = getHTMLStringFromPage(questionsURL, httpClient, DEBUG, false);
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

    public static void main(String[] args){
        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
        String text="Buenas noches Patricia. Mañana por la tarde te estaremos despachando por Mercadoenvíos 3 unidades de Soporte Porta Copas Cromado marca Häfele. Muchas gracias por tu compra!";
        long packId=2000000993640247l;
        long buyerCustId=21818340l;
        String user="SOMOS_MAS";
        postMessage(text,httpClient,packId,user,buyerCustId);

    }


}
