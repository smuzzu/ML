package com.ml.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TokenUtils {

    static String QUEFRESQUETE="QUEFRESQUETE";
    static String idClienteQUEFRESQUETE = "75607661";
    static String tokenFileQUEFRESQUETE="C:"+File.separator+"centro"+File.separator+"tokenSeba.txt";
    static String appIdQUEFRESQUETE = "1292869017866771";
    static String appSecretQUEFRESQUETE = "lR09aH34VYvPyWsOPT6MY8rSWgizShg5";


    static String ACACIAYLENGA="ACACIAYLENGA";
    static String idClienteACACIAYLENGA = "241751796";
    static String tokenFileACACIAYLENGA="C:"+File.separator+"centro"+File.separator+"tokenAcacia.txt";
    static String appIdACACIAYLENGA = "4747672705070272";
    static String appSecretACACIAYLENGA = "uiG40hwEV02Yto8edjCh8sv0kb4BIGz5";

    static String token = null;
    static String refresh_token = null;

    private static String getFileStr(String user){
        String tokenFile=null;
        if (user.equals(QUEFRESQUETE)){
            tokenFile=tokenFileQUEFRESQUETE;
        }else {
            if (user.equals(ACACIAYLENGA)){
                tokenFile=tokenFileACACIAYLENGA;
            }
        }
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(tokenFile), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return  contentBuilder.toString();
    }


    public static synchronized String getToken(String user){
        if (token==null){
            String fileStr = getFileStr(user);
            int eolPos=fileStr.indexOf("\n");
            token=fileStr.substring(0,eolPos);
        }
        return token;
    }

    public static synchronized void refreshToken(CloseableHttpClient httpClient,String user) {
        String appId=null;
        String appSecret=null;
        String tokenFile=null;
        if (user.equals(QUEFRESQUETE)){
            appId=appIdQUEFRESQUETE;
            appSecret=appSecretQUEFRESQUETE;
            tokenFile=tokenFileQUEFRESQUETE;
        }else {
            if (user.equals(ACACIAYLENGA)){
                appId=appIdACACIAYLENGA;
                appSecret=appSecretACACIAYLENGA;
                tokenFile=tokenFileACACIAYLENGA;
            }
        }

        String fileStr = getFileStr(user);
        int eolPos1 = fileStr.indexOf("\n")+1;
        int eolPos2 = fileStr.length()-1;
        refresh_token = fileStr.substring(eolPos1,eolPos2);

        String tokenURL = "https://api.mercadolibre.com/oauth/token?grant_type=refresh_token"
                + "&client_id=" + appId
                + "&client_secret=" + appSecret
                + "&refresh_token=" + refresh_token;
        System.out.println(tokenURL);
        Logger.log(tokenURL);

        HttpPost httppost = new HttpPost("https://api.mercadolibre.com/oauth/token");
        List<NameValuePair> params = new ArrayList<NameValuePair>(4);
        params.add(new BasicNameValuePair("grant_type", "refresh_token"));
        params.add(new BasicNameValuePair("client_id", appId));
        params.add(new BasicNameValuePair("client_secret", appSecret));
        params.add(new BasicNameValuePair("refresh_token", refresh_token));

        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpResponse response = null;
        try {
            response = httpClient.execute(httppost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream inputStream = null;

        if (response != null) {
            HttpEntity httpEntity = response.getEntity();
            try {
                inputStream = httpEntity.getContent();
            } catch (IOException e) {
                Logger.log(e);
                e.printStackTrace();
            }
        }

        String responseStr = null;
        if (inputStream != null) {
            responseStr = HttpUtils.getStringFromInputStream(inputStream);
        }

        if (responseStr != null && responseStr.length() > 0) {
            JSONObject jsonResponse = new JSONObject(responseStr);
            Object tokenObject = jsonResponse.get("access_token");
            if (tokenObject != null) {
                token = (String) tokenObject;
            }
            Object refreshTokenObject = jsonResponse.get("refresh_token");
            if (refreshTokenObject != null) {
                refresh_token = (String) refreshTokenObject;
            }
        }
        String msg = "******************************* new token\n"+token+"\n"+refresh_token;
        System.out.println(msg);
        Logger.log(msg);

        try {
            FileWriter fw = new FileWriter(tokenFile);
            fw.write(token+System.lineSeparator());
            fw.write(refresh_token);
            fw.close();
        } catch (Exception e) {
            msg="exception writing on file "+tokenFile;
            System.out.println(msg);
            Logger.log(msg);
            System.out.println(e);
            Logger.log(e);
        }
    }

    public static String getIdCliente(String user){
        String idCliente=null;
        if (user.equals(QUEFRESQUETE)){
            return idClienteQUEFRESQUETE;
        } else {
            if (user.equals(ACACIAYLENGA)){
                return idClienteACACIAYLENGA;
            }
        }
        return idCliente;
    }



    public static void main(String[] args){
        getToken(QUEFRESQUETE);
    }

}
