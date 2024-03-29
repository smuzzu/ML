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

    static String [][] tokenMatrix = {
            {SData.getQuefresquete(), null, "0"},
            {SData.getAcaciaYLenga(), null, "0"},
            {SData.getSomosMas(), null, "0"},
            {SData.getMarianaTest(), null, "0"},
            {"test01","TEST-1076243182886342-092414-8824995665685dc48dee330dd9c79359-20451879","0"},
            {"test02","TEST-2255961064013319-103123-25d1eb69586c9cda457446875fef476a-290295101","0"},
            {"test03","TEST-1311235808921113-080316-a4dbc3d0a4c7db074768f3b8d38c6e6c-267138009","0"},
            {"test04","APP_USR-1311235808921113-080316-2d2b380e360b66b5f5bf16a75b3ba96e-267138009","0"},
            {"test05","APP_USR-6453962663304739-041217-a5c0aac952acf6db4c482411fe212bee-314350611","0"},
            {"test06","APP_USR-6637655320562965-033110-3f688757fc570ebb8aa273bd614da586-95095923","0"},
            {"test07","APP_USR-2080473907061160-092412-dbf5e910b023e44f194553d0d90a7b15-293324417","0"},
            {"test08","APP_USR-4558-061211-f94e98afa1edbb6aee17c1001fd86221-101777428","0"}
        };
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


    private static boolean needsToFetchTokenOnCloud(String user){
        if (user.startsWith("test")){
            return false;
        }
        if (tokenMatrix[getUserNumber(user)-1][1]==null){
            return true;
        }
        long previousTimestamp=0;
        try{
            previousTimestamp=Long.parseLong(tokenMatrix[getUserNumber(user)-1][2]);
        }catch (Exception e){
            String errorMsg="Error obteniendo needsToFetchTokenOnCloud "+user;
            System.out.println(errorMsg);
            Logger.log(errorMsg);
            e.printStackTrace();
            Logger.log(e);
            return true;
        }
        long currentTimestamp=System.currentTimeMillis();
        long elapsedTime=currentTimestamp-previousTimestamp;
        long anHourInMilliseconds=3600000L;
        if (elapsedTime>anHourInMilliseconds){
            return true;
        }
        return false;
    }

    public static synchronized String getToken(String user){
        String token=null;
        if (needsToFetchTokenOnCloud(user)){
            String encodedToken=DatabaseHelper.fetchTokenOnCloud(user);
            if (encodedToken==null){
                String fileStr = getFileStr(user);
                int eolPos1 = fileStr.indexOf("\n");
                int eolPos2 = fileStr.length()-1;
                token = fileStr.substring(0, eolPos1);
                tokenMatrix[getUserNumber(user)-1][1]=token;
                tokenMatrix[getUserNumber(user)-1][2]=""+System.currentTimeMillis();
                String theRefreshToken = fileStr.substring(eolPos1+1,eolPos2);
                encodedToken= SData.encode(token);
                String encodedRefreshToken= SData.encode(theRefreshToken);
                DatabaseHelper.addTokenOnCloud(user,encodedToken,encodedRefreshToken);
            }else {
                token= SData.decode(encodedToken);
                tokenMatrix[getUserNumber(user)-1][1]=token;
                tokenMatrix[getUserNumber(user)-1][2]=""+System.currentTimeMillis();
            }
        }else {
            token=tokenMatrix[getUserNumber(user)-1][1];
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
                }else {
                    if (user.equals(SData.getMarianaTest())) {
                        userNumber = 4;
                    }
                    else {
                        if (user.equals("test01")){
                            userNumber = 5;
                        }
                        else {
                            if (user.equals("test02")){
                                userNumber = 6;
                            }
                            else {
                                if (user.equals("test03")) {
                                    userNumber = 7;
                                } else {
                                    if (user.equals("test04")) {
                                        userNumber = 8;
                                    } else {
                                        if (user.equals("test05")) {
                                            userNumber = 9;
                                        } else {
                                            if (user.equals("test06")) {
                                                userNumber = 10;
                                            } else {
                                                if (user.equals("test07")) {
                                                    userNumber = 11;
                                                }else {
                                                    if (user.equals("test08")) {
                                                        userNumber = 12;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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

        String url = "https://api.mercadolibre.com/oauth/token";
        HttpPost httppost = new HttpPost(url);
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

        String msg="XXXXXXXXXXXX refreshing token "+url+"?grant_type=refresh_token&client_id="+appId+"&client_secret="+appSecret+"&refresh_token="+refresh_token;
        System.out.println(msg);
        Logger.log(msg);

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

        String token="";
        if (responseStr != null && responseStr.length() > 0) {
            JSONObject jsonResponse = new JSONObject(responseStr);
            Object tokenObject = jsonResponse.get("access_token");
            if (tokenObject != null) {
                token = (String) tokenObject;
                tokenMatrix[getUserNumber(user)-1][1]=token;
                tokenMatrix[getUserNumber(user)-1][2]=""+System.currentTimeMillis();
            }
            Object refreshTokenObject = jsonResponse.get("refresh_token");
            if (refreshTokenObject != null) {
                refresh_token = (String) refreshTokenObject;
            }
        }
        msg = "******************************* new token";
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

    private static void updateTokenOnCloud(String user, String token, String refreshToken){
        String encodedToken= SData.encode(token);
        String encodedRefreshToken= SData.encode(refreshToken);
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
        //updateTokenOnCloud(SData.getMarianaTest(),"token","");
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
