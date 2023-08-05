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
import java.util.ArrayList;
import java.util.List;

public class TokenUtils2 {

    public static void main(String[] args){

        String userName=SData.getSomosMas(); //todo elegir el usuario correcto

        System.out.println("##########################################################");
        System.out.println("##########################################################");
        System.out.println("PASO 1");
        System.out.println("ingresar a mis aplicaciones (https://developers.mercadolibre.com.ar/devcenter) y recolectar la siguiente informacion ");

        //todo completar con los datos de configuracion de la app de ML en developers
        String appId="7836320290984619";
        String appSecret="amMS1MMaUAgDZDeESPHpV1gmjlXYamdJ";
        String redirectUrl="https://www.something.com/";

        System.out.println("##########################################################");
        System.out.println("##########################################################");
        System.out.println("PASO 2");
        System.out.println("Pegar la siguiente URL en la sesion developers de mercadolibre para obtener el codigo de autorizacion");
        String url = "https://auth.mercadolibre.com.ar/authorization?response_type=code&client_id="+appId+"&redirect_uri="+redirectUrl;
        System.out.println(url);
        System.out.println("El mismo aparecerá luego en la misma url al final, luego de hacer una redireccion a "+redirectUrl);
        System.out.println("con ese codigo completar la variable refreshToken a continuacion");

        //todo completar el refresh token de la url
        String refreshToken="TG-6490776e055f9b0001d20dd9-67537324";

        System.out.println("##########################################################");
        System.out.println("##########################################################");
        System.out.println("PASO 3");
        System.out.println("actualizar SData para el usuario "+userName+" con la siguiente informacion encodeada");
        String encodedAppId=SData.encode(appId);
        String encodedAppSecret=SData.encode(appSecret);
        System.out.println("appId"+userName+"="+encodedAppId);
        System.out.println("appSecret"+userName+"="+encodedAppSecret);

        //todo completar 2 las variables anteriormente mencionadas

        System.out.println("##########################################################");
        System.out.println("##########################################################");
        System.out.println("PASO 4");
        System.out.println("una vez completados los pasos 1,2 y 3 se puede comentar System.exit(0) para poder continuar");
        System.exit(0);

        String msg="refreshing token "+url+"?grant_type=authorization_code&client_id="+appId
                +"&client_secret="+appSecret+"&code="+refreshToken+"&redirect_uri="+redirectUrl;
        System.out.println("procederemos a obtener un nuevo token y nuevo refresh token con la siguiente info "+msg);


        url = "https://api.mercadolibre.com/oauth/token";
        HttpPost httppost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>(5);
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));
        params.add(new BasicNameValuePair("client_id", appId));
        params.add(new BasicNameValuePair("client_secret", appSecret));
        params.add(new BasicNameValuePair("code", refreshToken));
        params.add(new BasicNameValuePair("redirect_uri", redirectUrl));

        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        HttpResponse response = null;
        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
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
            Object refreshTokenObject = jsonResponse.get("refresh_token");
            if (refreshTokenObject != null) {
                refreshToken = (String) refreshTokenObject;
            }
            if (tokenObject != null) {
                token = (String) tokenObject;
            }
            msg="new token="+token;
            System.out.println(msg);
            Logger.log(msg);
            msg="new refreshToken="+refreshToken;
            System.out.println(msg);
            Logger.log(msg);
        }

        if (token!=null && !token.isEmpty() && token.startsWith("APP") &&
                refreshToken!=null && !refreshToken.isEmpty() && refreshToken.startsWith("TG")) {
            System.out.println("Procedemos a actualizar el token y refresh token en el file local");

            String fileStr = TokenUtils.getFileStr(userName);
            try {
                FileWriter fw = new FileWriter(fileStr);
                fw.write(token + System.lineSeparator());
                fw.write(refreshToken);
                fw.close();
            } catch (Exception e) {
                msg = "exception writing on file " + fileStr;
                System.out.println(msg);
                Logger.log(msg);
                System.out.println(e);
                Logger.log(e);
            }


            System.out.println("Procedemos a actualizar el token y refresh token en cloud");
            String encodedToken = SData.encode(token);
            refreshToken = SData.encode(refreshToken);
            DatabaseHelper.updateTokenOnCloud(userName, encodedToken, refreshToken);
        }else {
            msg="invalid tokens. token="+token+" refreshToken="+refreshToken;
            System.out.println(msg);
            Logger.log(msg);
        }
    }

}