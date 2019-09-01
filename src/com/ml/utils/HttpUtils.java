package com.ml.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class HttpUtils {

    public static CloseableHttpClient buildHttpClient() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("random", "random"));

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(40000) //40 seconds in milliseconds
                .setConnectionRequestTimeout(40000)
                .setSocketTimeout(40000)
                .build();

        CloseableHttpClient httpclient =
                HttpClientBuilder.create()
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .setDefaultRequestConfig(requestConfig)
                        .setConnectionManagerShared(true)
                        .build();
        return httpclient;
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
                    Thread.sleep(5000);//aguantamos los trapos 5 segundos antes de reintentar
                } catch (InterruptedException e) {
                    Logger.log(e);
                }

                if (1 == 1) { //rebuild client on retry?
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    client = null;
                    client = HttpUtils.buildHttpClient();
                }
            }/// todo fin
        }

        if (statusCode != 200) {
            if ((DEBUG) || (statusCode != 404 && statusCode != 403)) {  //403 y 404 se loguea solo con debug
                Logger.log("new status code " + statusCode + " " + uRL);
            }
            return null;
        }

        if (isUrlChanged(context, uRL)) {
            if (DEBUG) {
                Logger.log("url changed " + uRL);
            }
            return null;
        }


        HttpEntity httpEntity = response.getEntity();
        InputStream inputStream = null;
        try {
            inputStream = httpEntity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return getStringFromInputStream(inputStream);
    }

    private static String getStringFromInputStream(InputStream is) {

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

        return false;
    }

    public static ArrayList<String> getNewQuestionsFromPreviousLastQuestion(String htmlString, String uRL, CloseableHttpClient httpClient, String runnerID, boolean DEBUG, String previousLastQuestion) {

        ArrayList<String> newQuestions = new ArrayList<String>();
        String questionsURL = HTMLParseUtils.getQuestionsURL(uRL);
        String htmlStringFromQuestionsPage = HttpUtils.getHTMLStringFromPage(questionsURL, httpClient, DEBUG);
        if (htmlStringFromQuestionsPage == null) {
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