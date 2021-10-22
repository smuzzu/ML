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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class HttpUtils {

    public static final String URLChanged="urlChanged|";
    public static final String EXPIRED_TOKEN = "401|";

    private static int currentProxyNumber=0;
    private static long proxyStopSeconds=0;

    private static int requestCount=0;
    private static long timeRequestCount=System.nanoTime();
    private static boolean proxyOn=false;



    private static boolean PROXY_ENABLED=false;

    //habilita informacion de debug en tiempo real system.out
    private static boolean PROXY_DEBUG=false;

    //es el minimo y agrega una pausa para cada request para completar ese minimo si hace falta
    private static long MIN_REQUEST_NANOS=6000000000L;

    //los dos siguientes establecen un limite de tiempo para ejecutar una cantidad de requests sin proxy
    //si e alcanza la cantidad de requests y todavia queda tiempo se encienden los proxies por el tiempo remantente
    //hasta alcanzar los PROXY_STOP_SECONDS, luego de eso vuelve a funcionar sin proxy
    //si e alcanza la cantidad de requests y el tiempo vencio entonces no es necesario recurrir a los proxies
    private static long PROXY_REQUESTS_NUMBER =200L;
    private static long PROXY_STOP_SECONDS =60L;

    private static ArrayList proxyList=null;
    private static HashMap<String,Long> proxyStatisticsTime =null;
    private static HashMap<String,Long> proxyStatisticsCount =null;
    private static int proxyCycleCount=0;


    protected static synchronized ArrayList<String> loadProxiesFromFile() {

        proxyCycleCount++;
        HashMap<String,Long> myProxyStatisticsTime=new HashMap<String,Long>();
        if (proxyStatisticsTime!=null){
            myProxyStatisticsTime=new HashMap<String,Long>(proxyStatisticsTime);
        }
        int proxyListSize=myProxyStatisticsTime.size();
        HashMap<String,Long> myProxyStatisticsCount=new HashMap<String,Long>();
        if (proxyStatisticsCount!=null){
            myProxyStatisticsCount=new HashMap<String,Long>(proxyStatisticsCount);
        }


        ArrayList<String> myProxyList=null;
        if (proxyList==null){
            myProxyList=new ArrayList<String>();
        }else {
            myProxyList=proxyList;
        }

        if (proxyCycleCount==4 || (proxyCycleCount==15 && proxyListSize>5)
             || (proxyCycleCount==30 && proxyListSize>5)){
            if (proxyListSize>5) {
                HashMap<String, Long> averageTime = new HashMap<String, Long>();
                for (String proxy : myProxyStatisticsTime.keySet()) {
                    long time = myProxyStatisticsTime.get(proxy);
                    long count = myProxyStatisticsCount.get(proxy);
                    long averageSeconds = time / count / 1000000000L;
                    averageTime.put(proxy, averageSeconds);
                }
                int eightyPercent = averageTime.size() * 80 / 100;
                Map<String, Long> proxyStatisticsTimeSorted = sortByComparator(myProxyStatisticsTime);
                myProxyList = new ArrayList<String>(proxyStatisticsTimeSorted.keySet());
                for (int i = proxyListSize-1; i >= eightyPercent; i--) {
                    myProxyList.remove(i);
                }
                proxyStatisticsCount=new HashMap<String, Long>();
                proxyStatisticsTime=new HashMap<String, Long>();
            }
        }

        if (proxyList == null || proxyCycleCount == 100) {
            proxyCycleCount=0;
            proxyStatisticsCount=new HashMap<String,Long>();
            proxyStatisticsTime=new HashMap<String,Long>();
            String proxiesFile = "C:\\centro\\proxyList.txt";
            StringBuilder contentBuilder = new StringBuilder();
            try (Stream<String> stream = Files.lines(Paths.get(proxiesFile), StandardCharsets.UTF_8)) {
                stream.forEach(s -> contentBuilder.append(s).append("\n"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String proxiesListStr = contentBuilder.toString();
            String[] proxies = proxiesListStr.split("\n");
            for (String proxy : proxies) {
                if (proxy.contains("PROXY_DEBUG")) {
                    int pos1 = proxy.indexOf("=") + 1;
                    PROXY_DEBUG = Boolean.parseBoolean(proxy.substring(pos1));
                }
                if (proxy.contains("PROXY_REQUESTS_NUMBER")) {
                    int pos1 = proxy.indexOf("=") + 1;
                    PROXY_REQUESTS_NUMBER = Long.parseLong(proxy.substring(pos1));
                }
                if (proxy.contains("PROXY_STOP_SECONDS")) {
                    int pos1 = proxy.indexOf("=") + 1;
                    PROXY_STOP_SECONDS = Long.parseLong(proxy.substring(pos1));
                }
                if (proxy.contains("MIN_REQUEST_NANOS")) {
                    int pos1 = proxy.indexOf("=") + 1;
                    MIN_REQUEST_NANOS = Long.parseLong(proxy.substring(pos1));
                }
                if (proxy.startsWith("//")){
                    continue;
                }
                myProxyList.add(proxy);
            }
        }
        return myProxyList;
    }

    private static Map<String, Long> sortByComparator(Map<String, Long> unsortMap)
    {

        List<Map.Entry<String, Long>> list = new LinkedList<Map.Entry<String, Long>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, Long>>()
        {
            public int compare(Map.Entry<String, Long> o1,
                               Map.Entry<String, Long> o2)
            {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Long> sortedMap = new LinkedHashMap<String, Long>();
        for (Map.Entry<String, Long> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    synchronized private static String getProxy() {
        return (String) proxyList.get(currentProxyNumber);
    }

    synchronized private static String getNewProxy() {
        String result;
        int totalproxies=proxyList.size();
        if (currentProxyNumber>=totalproxies){
            currentProxyNumber=0;
        }
        result= (String) proxyList.get(currentProxyNumber);
        currentProxyNumber++;
        return result;
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


    public static JSONObject getJsonObjectWithoutToken(String uRL, CloseableHttpClient httpClient, boolean giveMeArray) {

        return getJsonObjectUsingToken(uRL,httpClient,SData.getQuefresquete(),giveMeArray);
    }

    public static boolean delete(String url, CloseableHttpClient httpClient, String user) {
        boolean ok=false;

        String token = TokenUtils.getToken(user);

        HttpDelete httpDelete = new HttpDelete(url);
        //httpDelete.setHeader("Accept", "application/json");
        //httpDelete.setHeader("Content-type", "application/json");
        httpDelete.addHeader("Authorization","Bearer "+token);

        CloseableHttpResponse response=null;
        try {
            response = httpClient.execute(httpDelete);
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
                if (statusCode == 200 || statusCode == 201) { //este cambio dejarlo
                    ok=true;
                }else {
                    Logger.log("Error posteando mensaje post venta. status code =  "+statusCode);
                }
            }
        }
        return ok;
    }



     public static boolean postMessage(String text, CloseableHttpClient httpClient, long packId, String user, long customerId, char shippingType) {
        boolean ok=false;
        String myUserId=TokenUtils.getIdCliente(user);
        String token = TokenUtils.getToken(user);
        String url = "https://api.mercadolibre.com/messages/packs/"+packId+"/sellers/"+myUserId;

        //todo nota importante:  se supone que el nuevo formato aplica para coreo y cross docking
        if (shippingType==Order.CORREO || shippingType==Order.FLEX) {
            url = "https://api.mercadolibre.com/messages/action_guide/packs/" + packId + "/option";
            if (text.length()>=350){ //todo restringir a shippingType = correo
                Logger.log("Mensaje superior a 350 caracteres");
                Logger.log(text);
                text=text.substring(0,349); //todo descomentar
            }
        }

        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.addHeader("Authorization","Bearer "+token);

        String json = "{\"from\" : { \"user_id\": \""+myUserId
                +"\",\"email\" : \"abcdfg@nospam.com\"},\"to\": { \"user_id\" : \""+customerId+"\"}, \"text\": \""+
                text+"\"}";


        if (shippingType==Order.CORREO || shippingType==Order.FLEX) {
            json = "{\"option_id\": \"OTHER\",\"text\": \"" + text + "\"}";
        }


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
                if (statusCode == 200 || statusCode == 201) { //este cambio dejarlo
                    ok=true;
                }else {
                    Logger.log("Error posteando mensaje post venta. status code =  "+statusCode);
                }
            }
        }
        return ok;
    }

    public static boolean updatePublication(String itemId, CloseableHttpClient httpClient, String user, String status, Double price) {
        boolean ok=false;
        //String myUserId=TokenUtils.getIdCliente(user);
        String token = TokenUtils.getToken(user);
        String url="https://api.mercadolibre.com/items/"+itemId;

        HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader("Authorization","Bearer "+token);
        httpPut.setHeader("Content-type", "application/json");
        httpPut.setHeader("Accept", "application/json");

        String json = "{";
        if (price!=null){
            json += "\"price\": "+price;
        }
        if (status!=null){
            if (!json.equals("{")){//si ya seteo otro parametro metemos coma
                json +=",";
            }
            json += "\"status\":\""+status+"\"";
        }
        json+="}";

        StringEntity entity = new StringEntity(json, StandardCharsets.US_ASCII);
        httpPut.setEntity(entity);

        CloseableHttpResponse response=null;
        try {
            response = httpClient.execute(httpPut);

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
                if (statusCode == 200 || statusCode == 201) { //este cambio dejarlo
                    ok=true;
                }else {
                    Logger.log("Error actualizando publicacion. "+itemId+" status code =  "+statusCode);
                }
            }
        }
        return ok;
    }


   public static JSONObject getJsonObjectUsingToken(String uRL, CloseableHttpClient httpClient, String usuario, boolean giveMeArray) {

        JSONObject jsonResponse=null;
        String token= TokenUtils.getToken(usuario);
        String jsonStringFromRequest = getHTMLStringFromPage(uRL, httpClient, false, false,token );
        if (jsonStringFromRequest.equals(EXPIRED_TOKEN)) {
            TokenUtils.refreshToken(httpClient,usuario);
            token= TokenUtils.getToken(usuario);
            jsonStringFromRequest = getHTMLStringFromPage(uRL, httpClient, false, false,token );
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
            jsonStringFromRequest = getHTMLStringFromPage(uRL, httpClient, false, false,null );
        }
        if (isOK(jsonStringFromRequest)) {
            jsonStringFromRequest = jsonStringFromRequest.substring(3);
            if (jsonStringFromRequest.startsWith("[")){
                if (giveMeArray) {
                    jsonStringFromRequest = "{\"elArray\":"+jsonStringFromRequest+"}";
                }else {
                    jsonStringFromRequest = jsonStringFromRequest.substring(1, jsonStringFromRequest.length() - 1);
                }
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


    public static String getHTMLStringFromPage(String uRL, CloseableHttpClient client, boolean DEBUG, boolean useProxy, String token) {

        HttpGet httpGet = null;

        try {
            httpGet = new HttpGet(uRL);
        } catch (Exception e){
            String msg = "Error en getHTMLStringFromPage parseando url "+uRL;
            System.out.println(msg);
            Logger.log(msg);
            e.printStackTrace();
            Logger.log(e);
            return "nullORempty|";
        }

        if (token!=null){
            httpGet.addHeader("Authorization","Bearer "+token);
        }

        CloseableHttpResponse response = null;
        HttpContext context = new BasicHttpContext();

        int retries = 0;
        boolean retry = true;
        int statusCode = 0;

        String proxy = null;

       long requestNanos1=System.nanoTime();
        while (retry && retries < 5) {
            retries++;
            proxy=proxyAndPauseManagement(useProxy,httpGet);


            try {
                response = client.execute(httpGet, context);
            } catch (IOException e) {
                response = null;
                String msg="Error en getHTMLStringFromPage intento #" + retries + " " + uRL+ " con proxy "+proxy;
                if (proxy != null) {
                    if (proxyStatisticsTime!=null && proxyStatisticsTime.containsKey(proxy)){
                        long time = proxyStatisticsTime.get(proxy);
                        time+=40000000000L;//penalizamos con 40 segundos en la estadistica
                        proxyStatisticsTime.replace(proxy,time);
                    }
                    if (PROXY_DEBUG) {
                        System.out.println(msg);
                    }
                }
                Logger.log(msg);
                if (proxy==null || PROXY_DEBUG) {
                    Logger.log(e);
                }
            }

            if (useProxy && PROXY_ENABLED) {
                long requestNanos2 = System.nanoTime();
                long requestNanos = requestNanos2 - requestNanos1;
                if (requestNanos < MIN_REQUEST_NANOS) {
                    try {
                        long pause = (MIN_REQUEST_NANOS - requestNanos) / 10000000;
                        if (PROXY_DEBUG) {
                            System.out.println("pause " + pause + " miliseconds");
                        }
                        Thread.sleep(pause);
                    } catch (InterruptedException e) {
                        Logger.log(e);
                    }
                }

                //statistics
                if (proxy != null) {

                    if (!proxyStatisticsTime.containsKey(proxy)) {
                        proxyStatisticsTime.put(proxy, 0L);
                        proxyStatisticsCount.put(proxy, 0L);
                    }
                    long elapsed = proxyStatisticsTime.get(proxy);
                    long count=proxyStatisticsCount.get(proxy);
                    elapsed += requestNanos;
                    count++;
                    proxyStatisticsTime.replace(proxy, elapsed);
                    proxyStatisticsCount.replace(proxy, count);
                }
            }

            Counters.incrementGlobalRequestCount();

            if (response != null) {
                StatusLine statusline = response.getStatusLine();
                if (statusline != null) {
                    statusCode = statusline.getStatusCode();
                    if (statusCode!=420 && statusCode!=403) { //429=too many requests
                        retry = false;
                    } else {
                        Logger.log("Http "+statusCode+" en getHTMLStringFromPage intento #" + retries + " " + uRL+" whith proxy"+proxy);
                        //todo en el 403 hacemos algo?
                    }
                }
            }

            if (retry) {
                try {
                    long pause=2000 * retries * retries;
                    if (statusCode==403 && proxy==null){
                        pause=2L*pause;
                    }
                    Thread.sleep(pause);//aguantamos los trapos 5 segundos antes de reintentar
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
                Logger.log("new status code " + statusCode + " " + uRL+" con proxy "+proxy);
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


   public static int getStatusCode(String uRL, CloseableHttpClient client, String token) {

        HttpGet httpGet = null;

        try {
            httpGet = new HttpGet(uRL);
        } catch (Exception e){
            String msg = "Error en getStatusCode parseando url "+uRL;
            System.out.println(msg);
            Logger.log(msg);
            e.printStackTrace();
            Logger.log(e);
            return -1;
        }

        if (token!=null){
            httpGet.addHeader("Authorization","Bearer "+token);
        }

        CloseableHttpResponse response = null;
        HttpContext context = new BasicHttpContext();

        int statusCode = 0;


        try {
            response = client.execute(httpGet, context);
        } catch (IOException e) {
            response = null;
            Logger.log("Error en getStatusCode intento #" + uRL);
            Logger.log(e);
        }
        Counters.incrementGlobalRequestCount();

        if (response != null) {
            StatusLine statusline = response.getStatusLine();
            if (statusline != null) {
                statusCode = statusline.getStatusCode();
            }
        }
        return statusCode;
   }


   private static String proxyAndPauseManagement(boolean useProxy,HttpGet httpGet) {
        String result=null;
        if (useProxy) {

            long elapsedSeconds = getElapsedSeconds();
            increaseOrResetRequestCountersAndProxy(false);
            boolean isProxyOn=isProxyOn();
            boolean counterLimitReached=getRequestCount() >= PROXY_REQUESTS_NUMBER;

            boolean timeout=elapsedSeconds > PROXY_STOP_SECONDS;
            if (isProxyOn){
                timeout=elapsedSeconds > proxyStopSeconds;
            }

            if (!PROXY_ENABLED) {
                checkCountNoProxy(counterLimitReached, timeout);
            } else {//proxy enabled
                if (counterLimitReached || timeout){
                    increaseOrResetRequestCountersAndProxy(true);
                    if (isProxyOn){
                        setProxyOff();
                    }else {
                        if (counterLimitReached && !timeout){
                            proxyStopSeconds= PROXY_STOP_SECONDS -elapsedSeconds;
                            proxyList=loadProxiesFromFile();
                            setProxyOn();
                        }
                    }
                }

                if (isProxyOn()) {
                    String proxy = getNewProxy();
                    result=proxy;
                    RequestConfig requestConfig = buildRequestConfig(proxy);
                    httpGet.setConfig(requestConfig);
                    if (PROXY_DEBUG) {
                        System.out.println(getRequestCount() + " " + getProxy() + " " + getElapsedSeconds());
                    }
                } else {
                    //nada - request sin proxy
                    if (PROXY_DEBUG) {
                        System.out.println(getRequestCount() + " NADA " + getElapsedSeconds());
                    }
                }
            }
        }else {
            //nada - request sin proxy
            if (PROXY_DEBUG) {
                System.out.println(" request sin proxy ");
            }
        }
        return result;
   }

    private static synchronized void checkCountNoProxy(boolean counterLimitReached, boolean timeoutReached) {
        if (counterLimitReached || timeoutReached){
            increaseOrResetRequestCountersAndProxy(true);
        }
        if (PROXY_DEBUG) {
            System.out.println(getRequestCount() + " NADA " + getElapsedSeconds());
        }
        if (counterLimitReached && !timeoutReached){
            long pauseMilliseconds = (PROXY_STOP_SECONDS - getElapsedSeconds()) * 1000L;
            System.out.println(PROXY_REQUESTS_NUMBER + " transacciones en menos de " + PROXY_STOP_SECONDS
                    + " segundos. Aguantamos los trapos " + pauseMilliseconds / 1000 + " segundos ");
            try {
                Thread.sleep(pauseMilliseconds);
            } catch (InterruptedException e) {
                Logger.log(e);
            }
        }
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


   public static boolean downloadFile(CloseableHttpClient httpClient, String fileUrl, String filePath, String usuario){
        HttpGet httpGet = new HttpGet(fileUrl);
       String token = TokenUtils.getToken(usuario);
       if (token!=null){
           httpGet.addHeader("Authorization","Bearer "+token);
       }

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
            Counters.incrementGlobalRequestCount();

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
        if (newURL == null || newURL.indexOf("NoIndex_True") > 0 || newURL.indexOf("redirectedFromVip") > 0 || (newURL.indexOf("mercadolibre.com.ar") == -1 && newURL.indexOf("api.mercadolibre.com") == -1) && newURL.indexOf("api.mercadopago.com") == -1) {
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

   public static ArrayList<String> getNewQuestionsFromPreviousLastQuestion(String uRL, String productId, CloseableHttpClient httpClient, String runnerID, boolean DEBUG, String previousLastQuestion) {

        ArrayList<String> newQuestions = new ArrayList<String>();
        String questionsURL = HTMLParseUtils.getQuestionsURL(productId);
        String htmlStringFromQuestionsPage = getHTMLStringFromPage(questionsURL, httpClient, DEBUG, true,null );
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

    static boolean isProxyOn(){
        return proxyOn;
    }

    static void setProxyOn(){
        proxyOn=true;
    }

    static void setProxyOff(){
        proxyOn=false;
    }

    static int getRequestCount(){
        return requestCount;
    }

    static long getElapsedSeconds(){
        return (System.nanoTime()-timeRequestCount)/1000000000L;
    }


    ///proxy
    static void increaseOrResetRequestCountersAndProxy(boolean reset){
        requestCount++;
        if (reset){
            requestCount=0;
            timeRequestCount=System.nanoTime();
        }
    }



    public static void main(String[] args){
        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
        JSONObject object = getJsonObjectUsingToken("https://api.mercadolibre.com/users/me",httpClient,SData.getQuefresquete(),false);
        String text="Muchas gracias por tu compra";
        long packId=4336466554l;
        long buyerCustId=95978482l;
        String user=SData.getAcaciaYLenga();
        postMessage(text,httpClient,packId,user,buyerCustId,Order.ACORDAR);

   }

    static class ValueComparator implements Comparator<String> {
        Map<String, Long> base;

        public ValueComparator(Map<String, Long> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with
        // equals.
        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }

}
