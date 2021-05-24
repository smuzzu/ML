package com.ml.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TokenUtils {

    static String token = null;
    static String tokenUser = null;
    static String refresh_token = null;

    public static String getFileStr(String user){
        String tokenFile=null;
        if (user.equals(SData.getQuefresquete())){
            tokenFile=SData.getTokenFileQuefresquete();
        }else {
            if (user.equals(SData.getAcaciaYLenga())){
                tokenFile=SData.getTokenFileAcaciaYLenga();
            }else{
                if (user.equals(SData.getSomosMas())){
                    tokenFile=SData.getTokenFileSomosMas();
                }
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
        if (token==null || !tokenUser.equals(user)){
            String encodedToken=DatabaseHelper.fetchTokenOnCloud(user);
            if (encodedToken==null){
                String fileStr = getFileStr(user);
                int eolPos1 = fileStr.indexOf("\n");
                int eolPos2 = fileStr.length()-1;
                token = fileStr.substring(0, eolPos1);
                tokenUser= user;
                String theRefreshToken = fileStr.substring(eolPos1+1,eolPos2);
                encodedToken= SData.encode(token);
                String encodedRefreshToken= SData.encode(theRefreshToken);
                DatabaseHelper.addTokenOnCloud(user,encodedToken,encodedRefreshToken);
            }else {
                token= SData.decode(encodedToken);
                tokenUser=user;
            }
        }
        return token;
    }

    public static int getUserNumber(String user) {
        int userNumber = 0;
        if (user.equals(SData.getQuefresquete())) {
            userNumber = 1;
        } else {
            if (user.equals(SData.getAcaciaYLenga())) {
                userNumber = 2;
            } else {
                if (user.equals(SData.getSomosMas())) {
                    userNumber = 3;
                }
            }
        }
        return userNumber;
    }

    public static String getUserName(int userID) {
        String userName="N/A";

        if (userID==1) {
            userName=SData.getQuefresquete();
        } else {
            if (userID==2) {
                userName=SData.getAcaciaYLenga();
            } else {
                if (userID==3) {
                    userName=SData.getSomosMas();
                }
            }
        }
        return userName;
    }


    public static synchronized void refreshToken(CloseableHttpClient httpClient,String user) {
        String appId=null;
        String appSecret=null;
        String tokenFile=null;
        if (user.equals(SData.getQuefresquete())){
            appId=SData.getAppIdQuefresquete();
            appSecret=SData.getAppSecretQuefresquete();
            tokenFile=SData.getTokenFileQuefresquete();
        }else {
            if (user.equals(SData.getAcaciaYLenga())){
                appId=SData.getAppIdAcaciaYLenga();
                appSecret=SData.getAppSecretAcaciaYLenga();
                tokenFile=SData.getTokenFileAcaciaYLenga();
            }else{
                if (user.equals(SData.getSomosMas())){
                    appId=SData.getAppIdSomosMas();
                    appSecret=SData.getAppSecretSomosMas();
                    tokenFile=SData.getTokenFileSomosMas();
                }
            }

        }

        String encodedRefreshToken=DatabaseHelper.fetchRefreshTokenOnCloud(user);
        if (encodedRefreshToken==null) {
            String fileStr = getFileStr(user);
            int eolPos1 = fileStr.indexOf("\n") + 1;
            int eolPos2 = fileStr.length() - 1;
            refresh_token = fileStr.substring(eolPos1, eolPos2);
        }else {
            refresh_token= SData.decode(encodedRefreshToken);
        }

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
                tokenUser=user;
            }
            Object refreshTokenObject = jsonResponse.get("refresh_token");
            if (refreshTokenObject != null) {
                refresh_token = (String) refreshTokenObject;
            }
        }
        String msg = "******************************* new token";
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

        String encodedToken= SData.encode(token);
        encodedRefreshToken= SData.encode(refresh_token);
        DatabaseHelper.updateTokenOnCloud(user,encodedToken,encodedRefreshToken);
    }

    public static String getIdCliente(String user){
        String idCliente=null;
        if (user.equals(SData.getQuefresquete())){
            return SData.getIdClienteQuefresquete();
        } else {
            if (user.equals(SData.getAcaciaYLenga())){
                return SData.getIdClienteAcaciaYLenga();
            } else {
                if (user.equals(SData.getSomosMas())){
                    return  SData.getIdClienteSomosMas();
                }
            }
        }
        return idCliente;
    }



    public static void main(String[] args){
        String userName=SData.getAcaciaYLenga();

/*
        String token = DatabaseHelper.fetchTokenOnCloud(userName);
        String refrehsToken=DatabaseHelper.fetchRefreshTokenOnCloud(userName);
        DatabaseHelper.addTokenOnCloud(userName,"t1","t2");
        token=DatabaseHelper.fetchTokenOnCloud(userName);

 */
        getToken(userName);
    }
    public static String getHostname() {
        String hostname = "Unknown";
        try
        {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        }
        catch (UnknownHostException ex)
        {
            System.out.println("Hostname can not be resolved");
            return null;
        }
        return hostname;
    }

}
