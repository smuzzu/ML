package com.ml;

import com.ml.utils.HTMLParseUtils;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;

/**
 * Created by Muzzu on 6/20/2018.
 */
public class MLSellerStatistics extends Thread {

    static Connection globalSelectConnection = null;
    static Connection globalUpadteConnection = null;
    static PreparedStatement selectPreparedStatement2 = null;
    static PreparedStatement custIdPreparedStatement=null;
    static DateFormat globalDateformat=null;
    static Timestamp globalDate;

    static ResultSet globalSelectSellerResultSet = null;
    static String PROFILE_BASE_URL ="https://www.mercadolibre.com.ar/perfil/";
    static String PROFILE_BASE_URL2 ="https://perfil.mercadolibre.com.ar/";
    static String PRODUCT_LIST_BASE_URL = "https://listado.mercadolibre.com.ar/_CustId_";
    static String OFFICIAL_STORE_BASE_URL = "https://listado.mercadolibre.com.ar/_DisplayType_LF_Tienda_";
    static String VISITS_URL = "https://api.mercadolibre.com/users/CUSTID/items_visits?date_from=DATE1T00:00:00.000-00:00&date_to=DATE2T00:00:00.000-00:00";
    static int globalRunnerCount;
    static int globalProdutCount=0;
    static int globalInsertCount=0;
    static int globalDisableCount=0;

    static int MAX_THREADS = 30; //TODO CAMBIAR 30
    static boolean SAVE = true; //TODO CAMBIAR
    static String DATABASE = "ML2";
    static boolean DEBUG = false;
    static String FECHA="2021/01/01";
    static String START_FROM="";
    static String ARTICLE_PREFIX="MLA";
    static int DAYS_WITHOUT_MOVEMENTS=180;

    static String SEE_SELLER_DETAILS_LABEL = "Ver más datos de";
    //static String SEE_SELLER_DETAILS_LABEL = "Ver mais dados";

    private static String formatSeller(String seller) {
        try {//encode seller to url
            seller = URLEncoder.encode(seller, StandardCharsets.UTF_8.name());

        } catch (UnsupportedEncodingException e) {
            Logger.log("something went wrong trying to decode the seller " + seller);
            Logger.log(e);
        }
        return seller;
    }

    private static String unFormatSeller(String seller) {
        try {//decode seller url
            seller = URLDecoder.decode(seller, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            Logger.log("something went wrong trying to decode the seller " + seller);
            Logger.log(e);
        }
        return seller;
    }



    private static Connection getSelectConnection() {
        if (globalSelectConnection == null) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/" + DATABASE;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "postgres");
            try {
                globalSelectConnection = DriverManager.getConnection(url, props);
            } catch (SQLException e) {
                Logger.log("I couldn't make a select connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalSelectConnection;
    }


    private static Connection getUpdateConnection() {
        if (globalUpadteConnection == null) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/" + DATABASE;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "password");
            try {
                globalUpadteConnection = DriverManager.getConnection(url, props);
                globalUpadteConnection.setAutoCommit(true);
            } catch (SQLException e) {
                Logger.log("I couldn't make an update connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalUpadteConnection;
    }

    private static synchronized String fetchSeller(String threadId){

        String seller=null;
        try {
            if (globalSelectSellerResultSet.next()){
                seller= globalSelectSellerResultSet.getString(1);
            }
        } catch (SQLException e) {
            Logger.log("No se pudo recuperar al vendedor");
            Logger.log(e);
        }
        System.out.println(threadId+" procesando vendedor "+seller);
        if (seller!=null) {
            incrementGlobalProductCount();
        }
        return seller;
    }

    private static synchronized long fetchSellerId(String seller){

        long id=0;
        try {
            custIdPreparedStatement.setString(1,seller);
            ResultSet resultSet = custIdPreparedStatement.executeQuery();
            if (resultSet==null){
                Logger.log("Couldn't get id for seller "+seller);
            }
            if (resultSet.next()){
                id=resultSet.getLong(1);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return id;
    }


    private static synchronized void insertSeller(String seller, long custId, boolean oficialStore, boolean noReputation, boolean hiddenReputation,
                                                  boolean noActivePublications, String lider, int years, boolean kindSeller,
                                                  boolean onTime, String location, double averageSalesPerDay, double estimatedSalePriceAverage,
                                                  int reputation, int totalProducts, int positiveOpinions, int neutralOpinions,
                                                  int negativeOpinions, int totalOpinions, double positiveAverage, double delayAverage,
                                                  double problemsAverage, double problemsDefective, double problemsDifferent, double problemsLate,
                                                  double problemsNotReceived, double problemsNotDelivered, double problemsNoAnswersOnTime,
                                                  double problemsOutOfStock, double problemsOther, int visitsLastMonth, String url){

        boolean found=true;
        try {
            PreparedStatement preparedStatement = globalSelectConnection.prepareStatement("select * from empresas where fecha = '"+FECHA+"' and proveedor = ?");
            preparedStatement.setString(1,seller);
            ResultSet resultSet = preparedStatement.executeQuery();
            found=resultSet.next();

        } catch (SQLException e) {
            Logger.log("Error buscando en tabla empresas");
            Logger.log(e);
        }

        int registrosInsertados=0;
        if (!found) {//el registro no existe, entonces hay que insertar la info
            try {
                PreparedStatement preparedStatement = globalUpadteConnection.prepareStatement("insert into empresas("+
                    "fecha,proveedor,idproveedor,tiendaoficial,sinreputacion,reputacionoculta,sinpublicacionesactivas,lider,anios,"+
                    "atiendebien,entregaatiempo,ubicacion,promedioventadiario,precioestimadoventapromedio,reputacion,"+
                    "totalproductos,opinionesbuenas,opinionesregulares,opinionesmalas,opinionestotales,"+
                    "porcentajepositivas,porcentajedemoras,porcentajeproblemas,problemadefectuoso,problemadiferente,"+
                    "problematarde,problemanorecibioproducto,problemaNoEntregado,problemaNoResponde,problemaSinStock," +
                    "problemaotro,visitasmensuales,url) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                preparedStatement.setTimestamp(1,globalDate);
                preparedStatement.setString(2, seller);
                preparedStatement.setLong(3, custId);
                preparedStatement.setBoolean(4, oficialStore);
                preparedStatement.setBoolean(5, noReputation);
                preparedStatement.setBoolean(6, hiddenReputation);
                preparedStatement.setBoolean(7, noActivePublications);
                preparedStatement.setString(8, lider);
                preparedStatement.setInt(9, years);
                preparedStatement.setBoolean(10, kindSeller);
                preparedStatement.setBoolean(11, onTime);
                preparedStatement.setString(12, location);
                preparedStatement.setDouble(13, averageSalesPerDay);
                preparedStatement.setDouble(14, estimatedSalePriceAverage);
                preparedStatement.setInt(15, reputation);
                preparedStatement.setInt(16, totalProducts);

                preparedStatement.setInt(17, positiveOpinions);
                preparedStatement.setInt(18, neutralOpinions);
                preparedStatement.setInt(19, negativeOpinions);
                preparedStatement.setInt(20, totalOpinions);

                preparedStatement.setDouble(21, positiveAverage);
                preparedStatement.setDouble(22, delayAverage);
                preparedStatement.setDouble(23, problemsAverage);
                preparedStatement.setDouble(24, problemsDefective);
                preparedStatement.setDouble(25, problemsDifferent);
                preparedStatement.setDouble(26, problemsLate);
                preparedStatement.setDouble(27, problemsNotReceived);
                preparedStatement.setDouble(28, problemsNotDelivered);
                preparedStatement.setDouble(29, problemsNoAnswersOnTime);
                preparedStatement.setDouble(30, problemsOutOfStock);
                preparedStatement.setDouble(31, problemsOther);
                preparedStatement.setInt(32,visitsLastMonth);

                preparedStatement.setString(33,url);

                registrosInsertados = preparedStatement.executeUpdate();
                incrementGlobalInsertCount();
            } catch (SQLException e) {
                Logger.log("Error insertando en tabla empresas "+seller);
                Logger.log(e);
            }

            if (registrosInsertados != 1) {
                Logger.log("Couldn't insert empresa " + seller);
            }
        }
    }

    private static synchronized void disableSellerProducts(String seller){
        int registrosModificados=0;

            try {
                PreparedStatement preparedStatement = globalUpadteConnection.prepareStatement("update productos set deshabilitado = true where proveedor = ?");
                preparedStatement.setString(1, seller);
                registrosModificados = preparedStatement.executeUpdate();
                incrementGlobalDisableCount();
            } catch (SQLException e) {
                Logger.log("Error deshabilitando en tabla empresas "+seller);
                Logger.log(e);
            }

            if (registrosModificados < 1) {
                Logger.log("Couldn't no pudo deshabilitar ningun producto de empresa " + seller);
            }

    }


    private static synchronized int getGlobalRunnerCount() {
        return ++globalRunnerCount;
    }


    public void run() {

        String runnerID="R"+getGlobalRunnerCount();

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        boolean finished=false;
        while (!finished){

            String seller=fetchSeller(runnerID);
            if (seller==null){
                finished=true;
                continue;
            }

            String custId=null;
            long sellerId=fetchSellerId(seller);
            if (sellerId!=0){
                custId=""+sellerId;
            }

            boolean oficialStore=false;
            boolean noReputation=false;
            boolean hiddenReputation=false;
            boolean noActivePublications=false;
            String lider="N"; //N=No, L=Lider, P=Platinum
            int years=0;
            boolean kindSeller=true;
            boolean onTime=true;
            String location=null;
            double averageSalesPerDay=0.0;
            int totalProducts =0;
            int visitsLastMonth=0;

            int positiveOpinions=0;
            int neutralOpinions=0;
            int negativeOpinions=0;
            int totalOpinions=0;

            double positiveAverage=0.0;
            double delayAverage=0.0;
            double problemsAverage=0.0;
            double estimatedSalePriceAverage=0.0;

            int reputation=99;/* valid values
            0 = green
            1 = yellowish green
            2 = yellow
            3 = orange
            4 = red
            99= not available*/

            double problemsDefective=0;
            double problemsDifferent=0;
            double problemsLate=0;
            double problemsNotReceived=0;
            double problemsNotDelivered=0;
            double problemsNoAnswersOnTime=0;
            double problemsOutOfStock=0;
            double problemsOther=0;
            String sellerUrl=null;
            String productsUrl=null;

            sellerUrl= PROFILE_BASE_URL +formatSeller(seller);

            long timestamp1 = System.currentTimeMillis();
            String htmlString = HttpUtils.getHTMLStringFromPage(sellerUrl,httpClient,DEBUG, true);
            long timestamp2 = System.currentTimeMillis();
            long secondsElapsed = (timestamp2-timestamp1)/1000;
            if (secondsElapsed>30){
                String msg="Ha tardado mucho con: "+seller;
                System.out.println(msg);
                Logger.log(msg);

                //rebuild httpClient
                try {
                    Thread.sleep(5000);
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
                ////////////////////////////////////////////

            }



            if (!HttpUtils.isOK(htmlString)){
                String msg = "el vendedor no aparecio de entrada "+seller;
                if (DEBUG) {
                    System.out.println(msg);
                    Logger.log(msg);
                }

                //rebuild httpClient
                try {
                    Thread.sleep(5000);
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
                ////////////////////////////////////////////


                if (sellerId!=0) {
                    sellerUrl = fetchNewSellerURL(sellerId, httpClient);
                    if (sellerUrl==null){
                        Logger.log("XXXXXXXXXXXXXXXXXX  ------------- sellerUrl=null "+seller);
                    }
                }else {
                    sellerUrl=fetchNewSellerURL(seller,httpClient);
                }

                if (sellerUrl==null){
                    //rebuild httpClient
                    try {
                        Thread.sleep(5000);
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
                    msg="al final el vendedor no aparecio nunca 1 si no es tienda oficial hay que verificar: "+seller;
                    System.out.println(msg);
                    Logger.log(msg);
                    continue;
                }

                timestamp1 = System.currentTimeMillis();
                htmlString = HttpUtils.getHTMLStringFromPage(sellerUrl,httpClient,DEBUG, true);
                timestamp2 = System.currentTimeMillis();
                secondsElapsed = (timestamp2-timestamp1)/1000;
                if (secondsElapsed>30){
                    msg="Ha tardado mucho 2 con: "+seller;
                    System.out.println(msg);
                    Logger.log(msg);

                    //rebuild httpClient
                    try {
                        Thread.sleep(5000);
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


                if (!HttpUtils.isOK(htmlString)){
                    msg="al final el vendedor no aparecio nunca 2 si no es tienda oficial hay que verificar: "+seller;
                    System.out.println(msg);
                    Logger.log(msg);

                    //rebuild httpClient
                    try {
                        Thread.sleep(5000);
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
                    ////////////////////////////////////////////

                    continue;
                }
            }



            String htmlStringWithoutUserComments=htmlString;
            int commentPos1=htmlString.indexOf("rating__wrapper");
            if (commentPos1>0){
                int comentsPos2=htmlString.indexOf("Ver todas",commentPos1);
                if (comentsPos2>0) {
                    int len = htmlString.length();
                    htmlStringWithoutUserComments = htmlString.substring(0, commentPos1) + htmlString.substring(comentsPos2,len);  //without user comments
                }
            }

            noReputation=htmlStringWithoutUserComments.indexOf("no tiene suficientes ventas para calcular su reputaci")!=-1;
            if (noReputation){
                String msg="el vendedor "+seller+" no tiene reputacion";
                if (DEBUG) {
                    System.out.println(msg);
                    Logger.log(msg);
                }
            }


            if (!noReputation) {

                int experiencePos1=htmlStringWithoutUserComments.indexOf("experience\">");
                if (experiencePos1==-1){
                    Logger.log("errorr !!! buscando experience "+sellerUrl);
                    continue;
                }

                int experiencePos2=htmlStringWithoutUserComments.indexOf("vendiendo en Mercado Libre",experiencePos1);
                if (experiencePos2==-1){
                    Logger.log("errorr !!! buscando experiencePos2");
                    continue;
                }
                experiencePos2--;
                if (experiencePos1>experiencePos2){
                    Logger.log("errorr !!! buscando experience III");
                    continue;
                }

                String experienceStr=htmlStringWithoutUserComments.substring(experiencePos1,experiencePos2);
                if (experienceStr.contains("año")){

                    int experiencePos3=experiencePos1;
                    while (!Character.isDigit(htmlStringWithoutUserComments.charAt(experiencePos3))){
                        experiencePos3++;
                    }
                    int experiencePos4=experiencePos3+1;
                    while (Character.isDigit(htmlStringWithoutUserComments.charAt(experiencePos4))){
                        experiencePos4++;
                    }
                    if (experiencePos3>experiencePos4){
                        Logger.log("errorr !!! buscando experience IV");
                        continue;
                    }
                    experienceStr=htmlStringWithoutUserComments.substring(experiencePos3,experiencePos4);
                    try {
                        years=Integer.parseInt(experienceStr);
                    }catch (Exception e){
                        Logger.log("Error parsing experience "+experienceStr);
                        Logger.log(e);
                    }
                }else {
                    years=0;
                }


                int salesPos1 = htmlStringWithoutUserComments.indexOf("subtitle-sales");
                if (salesPos1 == -1) {
                    Logger.log("errorr !!! buscando salesPos I");
                    continue;
                }
                salesPos1 += 28;
                int salesPos2 = htmlStringWithoutUserComments.indexOf("</p>", salesPos1);
                if (salesPos2 == -1) {
                    Logger.log("errorr !!! buscando salesPos II");
                    continue;
                }
                if (salesPos1 > salesPos2) {
                    Logger.log("errorr !!! buscando salesPos III");
                    continue;
                }
                String salesOnPeriodStr = htmlStringWithoutUserComments.substring(salesPos1, salesPos2);
                if (salesOnPeriodStr == null) {
                    Logger.log("errorr !!! buscando salesPos IV");
                    continue;
                }
                salesOnPeriodStr = salesOnPeriodStr.trim();
                if (salesOnPeriodStr.isEmpty()) {
                    Logger.log("errorr !!! buscando salesPos V");
                    continue;
                }
                int salesPos3 = salesOnPeriodStr.indexOf("</span>");
                if (salesPos1 == -1) {
                    Logger.log("errorr !!! buscando salesPos VI");
                    continue;
                }
                String salesAmount = salesOnPeriodStr.substring(0, salesPos3);
                if (salesAmount == null) {
                    Logger.log("errorr !!! buscando salesPos VII");
                    continue;
                }
                salesAmount = salesAmount.trim();
                if (salesAmount.isEmpty()) {
                    Logger.log("errorr !!! buscando salesPos VIII");
                    continue;
                }
                salesAmount = salesAmount.replace(".", "");
                int totalSales = 0;
                try {
                    totalSales = Integer.parseInt(salesAmount);
                } catch (Exception e) {
                    Logger.log("Error parsing experience " + experienceStr);
                    Logger.log(e);
                }
                int salesPos4 = salesOnPeriodStr.indexOf("ltimo");
                if (salesPos4 == -1) {
                    Logger.log("errorr !!! buscando salesPos IX");
                    continue;
                }


                int periods = 0;

                boolean justOnePeriod = salesOnPeriodStr.indexOf("ltimo año") != -1;
                if (!justOnePeriod) {
                    justOnePeriod = salesOnPeriodStr.indexOf("ltimo mes") != -1;
                }

                if (justOnePeriod) {
                    periods = 1;
                } else {

                    salesPos4 += 7;
                    while (!Character.isDigit(salesOnPeriodStr.charAt(salesPos4))){
                        salesPos4++;
                    }

                    int salesPos5 = salesPos4+1;
                    while (Character.isDigit(salesOnPeriodStr.charAt(salesPos5))){
                        salesPos5++;
                    }
                    if (salesPos5 == -1) {
                        Logger.log("errorr !!! buscando salesPos X");
                        continue;
                    }
                    if (salesPos4 >= salesPos5) {
                        Logger.log("errorr !!! buscando salesPos XI");
                        continue;
                    }
                    String periodsStr = salesOnPeriodStr.substring(salesPos4, salesPos5);
                    if (periodsStr == null || periodsStr.isEmpty()) {
                        Logger.log("errorr !!! buscando salesPos XII");
                        continue;
                    }
                    try {
                        periods = Integer.parseInt(periodsStr);
                    } catch (Exception e) {
                        Logger.log("Error parsing period " + periodsStr);
                        Logger.log(e);
                    }
                }

                int daysInPeriod = 0;
                if (salesOnPeriodStr.indexOf("mes") != -1) {
                    daysInPeriod = 30;
                } else {
                    if (salesOnPeriodStr.indexOf("año") != -1) {
                        daysInPeriod = 360;
                    }else if (salesOnPeriodStr.indexOf("día")!=-1){
                        daysInPeriod=1;
                    }
                }
                averageSalesPerDay = totalSales / (daysInPeriod * periods * 1.0);

                int thermometherPos1=htmlStringWithoutUserComments.indexOf("thermometer");
                if (thermometherPos1 == -1) {
                    Logger.log("errorr !!! buscando thermometherPos1");
                    continue;
                }
                int thermometherPos2=htmlStringWithoutUserComments.indexOf("</div", thermometherPos1);
                if (thermometherPos2 == -1) {
                    Logger.log("errorr !!! buscando thermometherPos2");
                    continue;
                }
                if (thermometherPos1 >= thermometherPos2) {
                    Logger.log("errorr !!! buscando thermomether positions");
                    continue;
                }

                String thermometherStr=htmlStringWithoutUserComments.substring(thermometherPos1,thermometherPos2);
                if (thermometherStr == null || thermometherStr.isEmpty()) {
                    Logger.log("errorr !!! buscando thermometherStr");
                    continue;
                }

                int polygonPos1=0;
                int polygonPos2=0;
                String colorStr=null;
                for (int i=0; i<5; i++){
                    polygonPos1=thermometherStr.indexOf("<polygon",polygonPos2);
                    if (polygonPos1 == -1) {
                        Logger.log("errorr !!! buscando polygonPos1 "+thermometherStr);
                        break;
                    }
                    polygonPos2=thermometherStr.indexOf("</polygon",polygonPos1);
                    if (polygonPos2 == -1) {
                        Logger.log("errorr !!! buscando polygonPos2 "+thermometherStr);
                        break;
                    }
                    if (polygonPos1 >= polygonPos2) {
                        Logger.log("errorr !!! buscando polygon positions "+thermometherStr);
                        break;
                    }
                    String polygonStr = thermometherStr.substring(polygonPos1,polygonPos2);
                    if (polygonStr == null || polygonStr.isEmpty()) {
                        Logger.log("errorr !!! buscando polygonStr "+thermometherStr);
                        break;
                    }

                    if (polygonStr.indexOf("opacity")==-1){
                        //procesar y salir
                        int colorPos1=polygonStr.indexOf("fill=");
                        if (colorPos1 == -1) {
                            Logger.log("errorr !!! buscando colorPos1 "+polygonStr);
                            break;
                        }
                        colorPos1+=6;
                        int colorPos2=polygonStr.indexOf("\"",colorPos1);
                        if (colorPos1 == -1) {
                            Logger.log("errorr !!! buscando colorPos1 "+polygonStr);
                            break;
                        }
                        if (colorPos1 >= colorPos2) {
                            Logger.log("errorr !!! buscando color positions "+thermometherStr);
                            break;
                        }
                        colorStr=polygonStr.substring(colorPos1,colorPos2);
                        break;
                    }
                }
                if (colorStr==null){
                    Logger.log("errorr !!! buscando Color");
                    continue;

                }

                if (colorStr.equals("#39B54A")){ //verde
                    reputation=0;
                }else {
                    if (colorStr.equals("#AEEF1B")) { //verde claro
                        reputation = 1;
                    } else {
                        if (colorStr.equals("#FFF044")){ //amarillo
                            reputation=2;
                        }else {
                            if (colorStr.equals("#FFB657")){ //naranja
                                reputation=3;
                            }else {
                                if (colorStr.equals("#FF605A")){ //rojo
                                    reputation=4;
                                }else {
                                    Logger.log("No es ningun color "+sellerUrl);
                                }
                            }
                        }
                    }
                }

                if (reputation==99){
                    boolean quepasoaca=true;
                }


                if (htmlStringWithoutUserComments.contains("leader-status")) {// es lider o platinum
                    if (htmlStringWithoutUserComments.contains("MercadoLíder Platinum")) {
                        lider = "P";
                    }
                    else {
                            if (htmlStringWithoutUserComments.contains("MercadoLíder Gold")) {
                                lider = "G";
                            } else {
                                lider = "L";
                            }
                        }
                }
            }

            int locationPos1=htmlStringWithoutUserComments.indexOf("location-subtitle");
            if (locationPos1==-1){
                Logger.log("Este vendedor no tiene ubicacion! "+seller);
                continue;
            }
            locationPos1+=19;
            int locationPos2=htmlStringWithoutUserComments.indexOf("<",locationPos1);
            if (locationPos2==-1){
                Logger.log("errorr !!!");
                continue;
            }
            if (locationPos1>locationPos2){
                Logger.log("errorr !!!");
                continue;
            }
            location=htmlStringWithoutUserComments.substring(locationPos1,locationPos2);



            if (!noReputation) {
                kindSeller = htmlStringWithoutUserComments.indexOf("Brinda buena atenci") != -1;
                if (!kindSeller) {
                    Double theProblemsAverage=getPercentage(htmlStringWithoutUserComments,"Tuvo problemas en sus ventas");
                    if (theProblemsAverage==null){
                        Logger.log("errorr !!! buscando problemas");
                        continue;
                    }
                    problemsAverage=theProblemsAverage;


                    int defectuosoPos1=htmlStringWithoutUserComments.indexOf("El producto era defectuoso");
                    if (defectuosoPos1>-1){
                        Double theProblemsDefective=getPercentage(htmlStringWithoutUserComments,"El producto era defectuoso");
                        if (theProblemsDefective==null){
                            Logger.log("errorr !!! buscando theProblemsDefective");
                            continue;
                        }
                        problemsDefective=theProblemsDefective;
                    }
                    defectuosoPos1=htmlStringWithoutUserComments.indexOf("El producto es defectuoso");
                    if (defectuosoPos1>-1){
                        Double theProblemsDefective=getPercentage(htmlStringWithoutUserComments,"El producto es defectuoso");
                        if (theProblemsDefective==null){
                            Logger.log("errorr !!! buscando theProblemsDefective II");
                            continue;
                        }
                        problemsDefective+=theProblemsDefective;
                    }

                    int ningunProductoPos1=htmlStringWithoutUserComments.indexOf("El comprador no recibió el producto");
                    if (ningunProductoPos1>-1){
                        Double thePproblemsNotReceived=getPercentage(htmlStringWithoutUserComments,"El comprador no recibió el producto");
                        if (thePproblemsNotReceived==null){
                            Logger.log("errorr !!! buscando thePproblemsNotReceived");
                            continue;
                        }
                        problemsNotReceived=thePproblemsNotReceived;
                    }

                    int noAcordadoPos1=htmlStringWithoutUserComments.indexOf("El comprador recibió un producto diferente al acordado");
                    if (noAcordadoPos1>-1){
                        Double theProblemsDifferent=getPercentage(htmlStringWithoutUserComments,"El comprador recibió un producto diferente al acordado");
                        if (theProblemsDifferent==null){
                            Logger.log("errorr !!! buscando theProblemsDifferent");
                            continue;
                        }
                        problemsDifferent=theProblemsDifferent;
                    }
                    noAcordadoPos1=htmlStringWithoutUserComments.indexOf("El producto es distinto");
                    if (noAcordadoPos1>-1){
                        Double theProblemsDifferent=getPercentage(htmlStringWithoutUserComments,"El producto es distinto");
                        if (theProblemsDifferent==null){
                            Logger.log("errorr !!! buscando theProblemsDifferent");
                            continue;
                        }
                        problemsDifferent+=theProblemsDifferent;
                    }

                    int tardePos1=htmlStringWithoutUserComments.indexOf("El producto llegó tarde");
                    if (tardePos1>-1){

                        Double theProblemsLate=getPercentage(htmlStringWithoutUserComments,"El producto llegó tarde");
                        if (theProblemsLate==null){
                            Logger.log("errorr !!! buscando theProblemsLate");
                            continue;
                        }
                        problemsLate=theProblemsLate;
                    }
                    tardePos1=htmlStringWithoutUserComments.indexOf("Entregado fuera de tiempo");
                    if (tardePos1>-1){

                        Double theProblemsLate=getPercentage(htmlStringWithoutUserComments,"Entregado fuera de tiempo");
                        if (theProblemsLate==null){
                            Logger.log("errorr !!! buscando theProblemsLate 2");
                            continue;
                        }
                        problemsLate+=theProblemsLate;
                    }


                    int otrosPos1=htmlStringWithoutUserComments.indexOf("Otro tipo de reclamos");
                    if (otrosPos1>-1){
                        Double theProblemsOther=getPercentage(htmlStringWithoutUserComments,"Otro tipo de reclamos");
                        if (theProblemsOther==null){
                            Logger.log("errorr !!! geting theProblemsOther");
                            continue;
                        }
                        problemsOther=theProblemsOther;
                    }


                    int notDeliveredPos1=htmlStringWithoutUserComments.indexOf("El producto no fue entregado");
                    if (notDeliveredPos1>-1){
                        Double theProblemsNotDelivered=getPercentage(htmlStringWithoutUserComments,"El producto no fue entregado");
                        if (theProblemsNotDelivered==null){
                            Logger.log("errorr !!! geting theProblemsNotDelivered");
                            continue;
                        }
                        problemsNotDelivered=theProblemsNotDelivered;
                    }

                    int noAnswersOnTimePos1=htmlStringWithoutUserComments.indexOf("No respondes a tiempo al comprador");
                    if (noAnswersOnTimePos1>-1){
                        Double theProblemsNoAnswersOnTime=getPercentage(htmlStringWithoutUserComments,"No respondes a tiempo al comprador");
                        if (theProblemsNoAnswersOnTime==null){
                            Logger.log("errorr !!! geting theProblemsNoAnswersOnTime");
                            continue;
                        }
                        problemsNoAnswersOnTime=theProblemsNoAnswersOnTime;
                    }
                    noAnswersOnTimePos1=htmlStringWithoutUserComments.indexOf("Demora en responder al comprador");
                    if (noAnswersOnTimePos1>-1){
                        Double theProblemsNoAnswersOnTime=getPercentage(htmlStringWithoutUserComments,"Demora en responder al comprador");
                        if (theProblemsNoAnswersOnTime==null){
                            Logger.log("errorr !!! geting theProblemsNoAnswersOnTime");
                            continue;
                        }
                        problemsNoAnswersOnTime+=theProblemsNoAnswersOnTime;
                    }


                    int OutOfStockPos1=htmlStringWithoutUserComments.indexOf("Te quedaste sin stock");
                    if (OutOfStockPos1>-1){
                        Double theProblemsOutOfStock=getPercentage(htmlStringWithoutUserComments,"Te quedaste sin stock");
                        if (theProblemsOutOfStock==null){
                            Logger.log("errorr !!! geting theProblemsOutOfStock");
                            continue;
                        }
                        problemsOutOfStock=theProblemsOutOfStock;
                    }
                    OutOfStockPos1=htmlStringWithoutUserComments.indexOf("El producto está fuera de stock");
                    if (OutOfStockPos1>-1){
                        Double theProblemsOutOfStock=getPercentage(htmlStringWithoutUserComments,"El producto está fuera de stock");
                        if (theProblemsOutOfStock==null){
                            Logger.log("errorr !!! geting theProblemsOutOfStock");
                            continue;
                        }
                        problemsOutOfStock+=theProblemsOutOfStock;
                    }

                    double allTheProblems=problemsDefective+problemsDifferent+problemsLate+problemsNotReceived+
                            problemsNotDelivered+problemsNoAnswersOnTime+problemsOutOfStock+problemsOther;
                    if (allTheProblems<99 && allTheProblems!=0){
                        Logger.log("errorr !!! Houston we have a unknown problem "+sellerUrl);
                    }

                }

                //




                onTime = htmlStringWithoutUserComments.indexOf("Despacha sus productos a tiempo") != -1;
                if (!onTime) {
                    Double theDelayAverage=getPercentage(htmlStringWithoutUserComments,"demora");
                    if (theDelayAverage==null){
                        Logger.log("errorr !!! geting delay average");
                        continue;
                    }
                    delayAverage=theDelayAverage;
                }
            }


            int buyersFeedbackPos1=htmlString.indexOf("buyers-feedback-bar");
            if (buyersFeedbackPos1 == -1) {
                Logger.log("errorr !!! buyersFeedbackPos1 "+sellerUrl);
                continue;
            }
            int buyersFeedbackPos2=htmlString.indexOf("</div",buyersFeedbackPos1);
            if (buyersFeedbackPos2 == -1) {
                Logger.log("errorr !!! buyersFeedbackPos2");
                continue;
            }
            if (buyersFeedbackPos1>=buyersFeedbackPos2){
                Logger.log("errorr !!! buyersFeedback Position");
                continue;
            }
            String buyersFeedbackStr=htmlString.substring(buyersFeedbackPos1,buyersFeedbackPos2);
            if (buyersFeedbackStr == null || buyersFeedbackStr.isEmpty()) {
                Logger.log("errorr !!! buscando buyersFeedbackStr");
                continue;
            }

            int positiveFeedbackPos1=buyersFeedbackStr.indexOf("Buena");
            if (positiveFeedbackPos1 == -1) {
                Logger.log("errorr !!! goodFeedbackPos1");
                continue;
            }
            for (int i=positiveFeedbackPos1; i<buyersFeedbackStr.length(); i++){
                if (Character.isDigit(buyersFeedbackStr.charAt(i))){
                    positiveFeedbackPos1=i;
                    break;
                }
            }
            if (positiveFeedbackPos1 == buyersFeedbackStr.length()) {
                Logger.log("errorr !!! goodFeedbackPos1 = buyersFeedbackStr.length()");
                continue;
            }
            int positiveFeedbackPos2=positiveFeedbackPos1+1;
            for (int i=positiveFeedbackPos2; i<buyersFeedbackStr.length(); i++){
                if (!Character.isDigit(buyersFeedbackStr.charAt(i))){
                    positiveFeedbackPos2=i;
                    break;
                }
            }
            if (positiveFeedbackPos2 == buyersFeedbackStr.length()) {
                Logger.log("errorr !!! goodFeedbackPos2 = buyersFeedbackStr.length()");
                continue;
            }
            if (positiveFeedbackPos1>=positiveFeedbackPos2){
                Logger.log("errorr !!! goodFeedbackPos1 and goodFeedbackPos2");
                continue;
            }
            String positiveFeedbackStr=buyersFeedbackStr.substring(positiveFeedbackPos1,positiveFeedbackPos2);
            if (positiveFeedbackStr==null){
                Logger.log("errorr !!! goodFeedbackStr is null");
                continue;
            }
            positiveFeedbackStr=positiveFeedbackStr.trim();
            if (positiveFeedbackStr.isEmpty()){
                Logger.log("errorr !!! goodFeedbackStr is empty");
                continue;
            }
            positiveOpinions=Integer.parseInt(positiveFeedbackStr);


            int neutralFeedbackPos1=buyersFeedbackStr.indexOf("Regular");
            if (neutralFeedbackPos1 == -1) {
                Logger.log("errorr !!! neutralFeedbackPos1");
                continue;
            }
            for (int i=neutralFeedbackPos1; i<buyersFeedbackStr.length(); i++){
                if (Character.isDigit(buyersFeedbackStr.charAt(i))){
                    neutralFeedbackPos1=i;
                    break;
                }
            }
            if (neutralFeedbackPos1 == buyersFeedbackStr.length()) {
                Logger.log("errorr !!! regularFeedbackPos1 = buyersFeedbackStr.length()");
                continue;
            }
            int neutralFeedbackPos2=neutralFeedbackPos1+1;
            for (int i=neutralFeedbackPos2; i<buyersFeedbackStr.length(); i++){
                if (!Character.isDigit(buyersFeedbackStr.charAt(i))){
                    neutralFeedbackPos2=i;
                    break;
                }
            }
            if (neutralFeedbackPos2 == buyersFeedbackStr.length()) {
                Logger.log("errorr !!! regularFeedbackPos2 = buyersFeedbackStr.length()");
                continue;
            }
            if (neutralFeedbackPos1>=neutralFeedbackPos2){
                Logger.log("errorr !!! regularFeedbackPos1 and regularFeedbackPos2");
                continue;
            }
            String neutralFeedbackStr=buyersFeedbackStr.substring(neutralFeedbackPos1,neutralFeedbackPos2);
            if (neutralFeedbackStr==null){
                Logger.log("errorr !!! regularFeedbackStr is null");
                continue;
            }
            neutralFeedbackStr=neutralFeedbackStr.trim();
            if (neutralFeedbackStr.isEmpty()){
                Logger.log("errorr !!! regularFeedbackStr is empty");
                continue;
            }
            neutralOpinions=Integer.parseInt(neutralFeedbackStr);


            int negativeFeedbackPos1=buyersFeedbackStr.indexOf("Mala");
            if (negativeFeedbackPos1 == -1) {
                Logger.log("errorr !!! negativeFeedbackPos1");
                continue;
            }
            for (int i=negativeFeedbackPos1; i<buyersFeedbackStr.length(); i++){
                if (Character.isDigit(buyersFeedbackStr.charAt(i))){
                    negativeFeedbackPos1=i;
                    break;
                }
            }
            if (negativeFeedbackPos1 == buyersFeedbackStr.length()) {
                Logger.log("errorr !!! negativeFeedbackPos1 = buyersFeedbackStr.length()");
                continue;
            }
            int negativeFeedbackPos2=negativeFeedbackPos1+1;
            for (int i=negativeFeedbackPos2; i<buyersFeedbackStr.length(); i++){
                if (!Character.isDigit(buyersFeedbackStr.charAt(i))){
                    negativeFeedbackPos2=i;
                    break;
                }
            }
            if (negativeFeedbackPos2 == buyersFeedbackStr.length()) {
                Logger.log("errorr !!! negativeFeedbackPos2 = buyersFeedbackStr.length()");
                continue;
            }
            if (negativeFeedbackPos1>=negativeFeedbackPos2){
                Logger.log("errorr !!! negativeFeedbackPos1 and negativeFeedbackPos2");
                continue;
            }
            String negativeFeedbackStr=buyersFeedbackStr.substring(negativeFeedbackPos1,negativeFeedbackPos2);
            if (negativeFeedbackStr==null){
                Logger.log("errorr !!! negativeFeedbackStr is null");
                continue;
            }
            negativeFeedbackStr=negativeFeedbackStr.trim();
            if (negativeFeedbackStr.isEmpty()){
                Logger.log("errorr !!! negativeFeedbackStr is empty");
                continue;
            }
            negativeOpinions=Integer.parseInt(negativeFeedbackStr);

            totalOpinions=positiveOpinions+neutralOpinions+negativeOpinions;

            if (totalOpinions>0) {
                positiveAverage = 100.0 * positiveOpinions / totalOpinions;
            }

            if (custId==null) {
                String userId = null;
                int userIdPos1 = htmlString.indexOf("user_id");
                if (userIdPos1 > 0) {
                    userIdPos1 += 9;
                    int userIdPos2 = htmlString.indexOf(",", userIdPos1);
                    if (userIdPos2 > 0) {
                        userId = htmlString.substring(userIdPos1, userIdPos2);
                        if (userId != null && userId.length() > 0) {
                            custId = userId;
                        }
                    }
                }
            }

            if (!seller.equals(seller.toUpperCase())){ //tiene minuscula / posible tienda oficial
                String officialStoreUrl = OFFICIAL_STORE_BASE_URL + formatSeller(seller);
                String officialStoreHtmlString = HttpUtils.getHTMLStringFromPage(officialStoreUrl, httpClient,DEBUG, true);

                if (officialStoreHtmlString==null) {
                    String msg = "la tienda oficial no aparecio de entrada " + officialStoreUrl;
                    System.out.println(msg);
                    Logger.log(msg);

                    //rebuild httpClient
                    try {
                        Thread.sleep(5000);
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
                    ////////////////////////////////////////////

                    officialStoreHtmlString = HttpUtils.getHTMLStringFromPage(officialStoreUrl, httpClient,DEBUG, true);
                    if (officialStoreHtmlString!=null && !officialStoreHtmlString.contains("Escribí en el buscador lo que querés encontrar")) {
                        oficialStore=true;
                    }

                }else {
                    if (!officialStoreHtmlString.contains("Escribí en el buscador lo que querés encontrar")) {
                        oficialStore = true;
                    }
                }
            }

            productsUrl = PRODUCT_LIST_BASE_URL + custId;
            if (oficialStore){
                productsUrl=OFFICIAL_STORE_BASE_URL+formatSeller(seller);
            }

            htmlString = HttpUtils.getHTMLStringFromPage(productsUrl, httpClient, DEBUG, true);

            if (!HttpUtils.isOK(htmlString)) {
                String msg = "el listado no aparecio de entrada " + productsUrl;
                if(DEBUG) {
                    System.out.println(msg);
                    Logger.log(msg);
                }

                //rebuild httpClient
                try {
                    Thread.sleep(5000);
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
                ////////////////////////////////////////////

                htmlString = HttpUtils.getHTMLStringFromPage(productsUrl, httpClient,DEBUG, true);
                if (!HttpUtils.isOK(htmlString)) {
                    if(DEBUG) {
                        msg = "al final el listado no aparecio nunca 2: " + productsUrl;
                        System.out.println(msg);
                        Logger.log(msg);
                    }
                                //rebuild httpClient
                    try {
                        Thread.sleep(5000);
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
                    ////////////////////////////////////////////
                    //seller = fetchSeller(runnerID);
                    //continue;

                }
            }

            if (!HttpUtils.isOK(htmlString) || htmlString.contains("Escribí en el buscador lo que querés encontrar")){//vendedor inactivo
                noActivePublications=true;
                String msg = "Vendedor sin publicaciones activas " + sellerUrl;
                System.out.println(msg);
                Logger.log(msg);
            }


            if (!noActivePublications) {
                int totalProductsPos1 = htmlString.indexOf("quantity-results");
                if (totalProductsPos1 < 0) {
                    Logger.log("errorr en totalProductsPos1 !!!");
                    continue;
                }
                totalProductsPos1 += 18;
                int totalProductsPos2 = htmlString.indexOf("resultado", totalProductsPos1);
                if (totalProductsPos2 < 0) {
                    Logger.log("errorr en totalProductsPos2 !!!");
                    continue;
                }
                totalProductsPos2--;
                String totalProductsStr = htmlString.substring(totalProductsPos1, totalProductsPos2);
                if (totalProductsStr == null) {
                    Logger.log("errorr en totalProductsStr !!!");
                    continue;
                }
                totalProductsStr=totalProductsStr.replace(".", "");
                totalProductsStr=totalProductsStr.replace(",", "");
                totalProductsStr=totalProductsStr.trim();
                try {
                    totalProducts = Integer.parseInt(totalProductsStr);
                }catch (NumberFormatException e){
                    Logger.log("Total productos cannot be found on "+productsUrl);
                    e.printStackTrace();
                    Logger.log(e);
                    continue;
                }

                String[] allHrefsOnPage = StringUtils.substringsBetween(htmlString, "<a href", "</a>");
                if (allHrefsOnPage == null) { //todo check
                    Logger.log("errorr en allHrefsOnPage !!!");
                    continue;
                }

                ArrayList<String> productsURLArrayList = new ArrayList();
                for (String href : allHrefsOnPage) {
                    if (href.indexOf(ARTICLE_PREFIX) > 0 && href.indexOf("-_JM") > 0) {
                        href = href.substring(href.indexOf("http"), href.indexOf("-_JM")) + "-_JM";
                        if (!productsURLArrayList.contains(href)) {
                            productsURLArrayList.add(href);
                        }
                    }
                }

                int amount = 0;
                int countDown = 50;
                int ponderation=0;
                int totalPonderations = 0;

                for (String productUrl : productsURLArrayList) {

                    int initPoint = htmlString.indexOf(productUrl);
                    int nextPoint = htmlString.length();//just for the last item #48 o #50 depending on the page layout

                    String productHTMLdata = null;
                    int nextItem = productsURLArrayList.indexOf(productUrl) + 1;
                    if (nextItem < productsURLArrayList.size()) {
                        String nextURL = productsURLArrayList.get(nextItem);
                        nextPoint = htmlString.indexOf(nextURL);
                    }

                    productHTMLdata = htmlString.substring(initPoint, nextPoint);
                    if (productHTMLdata != null) {
                        productHTMLdata = productHTMLdata.toString(); //aca le sacamos los caracteres de control que impiden hacer los search dentro del string
                    }

                    int pricePos1 = productHTMLdata.indexOf("price__fraction\">");
                    if (pricePos1>0){
                        pricePos1+=17;
                    }else { //hacemos esto porque hay productos con tags distintos
                        pricePos1 = productHTMLdata.indexOf("price-tag-fraction\">") + 20;
                    }

                    int pricePos2 = productHTMLdata.indexOf("<", pricePos1);
                    String priceStr = productHTMLdata.substring(pricePos1, pricePos2);
                    if (priceStr != null) {
                        //sacamos los puntos de miles para que no confunda con decimales
                        priceStr = priceStr.replace(".", "");
                        priceStr = priceStr.trim();
                    }

                    String priceDecimalsStr = null;
                    int priceDecimalPos1 = productHTMLdata.indexOf("price__decimals\">");
                    if (priceDecimalPos1 >= 0) { //el tag de decimales puede no estar
                        priceDecimalPos1 += 17; //le sumo los caracteres de posicion de "price__decimals">
                        int priceDecimalPos2 = productHTMLdata.indexOf("<", priceDecimalPos1);
                        priceDecimalsStr = productHTMLdata.substring(priceDecimalPos1, priceDecimalPos2);
                        if (priceDecimalsStr != null) {
                            priceDecimalsStr = priceDecimalsStr.trim();
                        }
                    }

                    Double price = null;
                    try {
                        price = Double.parseDouble(priceStr);
                        if (priceDecimalsStr != null) {
                            price += Double.parseDouble(priceDecimalsStr) / 100;
                        }
                    } catch (NumberFormatException e) {
                        Logger.log(" I couldn't get the price on product.  Ignoring " + productUrl);
                        Logger.log(e);
                        continue;
                    }

/*
                    double totalSold = 0;
                    int soldPos1 = productHTMLdata.indexOf("item__condition\">") + 17;
                    if (soldPos1 > 0) {//puede ser que no vendió o es usado
                        int soldPos2 = productHTMLdata.indexOf("</div>", soldPos1);
                        if (soldPos2 > 0) {
                            String soldStr = productHTMLdata.substring(soldPos1, soldPos2);
                            //parseo doble, primero ubicamos el div y despue vemos que su contenido tenga la palabra vendidos
                            if (soldStr != null && soldStr.lastIndexOf("vendido") > 0) {
                                soldPos2 = productHTMLdata.indexOf("vendido", soldPos1);
                                if (soldPos2 > 0) {
                                    soldStr = productHTMLdata.substring(soldPos1, soldPos2);
                                    if (soldStr != null) {
                                        soldStr = soldStr.trim();
                                        try {
                                            totalSold = Integer.parseInt(soldStr);
                                        } catch (NumberFormatException e) {
                                            Logger.log("I couldn't get total sold on " + productUrl);
                                        }
                                    }
                                }
                            }
                        }
                    }*/

                    countDown--;
                    ponderation=countDown^3;
                    totalPonderations+=ponderation;
                    amount += price * ponderation;
                }

                if (totalPonderations > 0) {
                    estimatedSalePriceAverage = amount / totalPonderations;
                }
            }

            if (custId!=null){
                String visitUrl = VISITS_URL.replaceAll("CUSTID",custId);

                boolean retry=true;
                int retries=0;
                while (retry && retries<5) {
                    retry=false;
                    retries++;
                    String htmlString3 = HttpUtils.getHTMLStringFromPage(visitUrl, httpClient,DEBUG, true);
                    if (HttpUtils.isOK(htmlString3)) {
                        int pos1 = htmlString3.indexOf("total_visits\":");
                        if (pos1 > 0) {
                            pos1 += 14;
                            int pos2 = htmlString3.indexOf(",", pos1);
                            if (pos2 > 0) {
                                String visitsStr = htmlString3.substring(pos1, pos2);
                                if (visitsStr != null && visitUrl.length() > 0) {
                                    visitsLastMonth = Integer.parseInt(visitsStr);
                                }
                            }
                        }
                    }
                    if (visitsLastMonth == 0) {
                        retry=true;
                        try {
                            Thread.sleep(5000);//aguantamos los trapos 5 segundos antes de reintentar
                        } catch (InterruptedException e) {
                            Logger.log(e);
                        }
                        if (DEBUG) {
                            String msg = "Algo salio mal buscando visitas " + retries + " " + sellerUrl;
                            System.out.println(msg);
                            Logger.log(msg);
                            Logger.log(visitUrl);
                            Logger.log(htmlString3);
                        }
                    }
                }
                if (visitsLastMonth == 0 && !noActivePublications) {
                    String msg = "No se pudieron recuperar visitas " + retries + " " + sellerUrl;
                    System.out.println(msg);
                    Logger.log(msg);
                }

            }

            if (SAVE) {
                insertSeller(seller, sellerId, oficialStore, noReputation, hiddenReputation,
                        noActivePublications, lider, years, kindSeller,
                        onTime, location, averageSalesPerDay, estimatedSalePriceAverage,
                        reputation, totalProducts, positiveOpinions, neutralOpinions, negativeOpinions,
                        totalOpinions, positiveAverage, delayAverage, problemsAverage,
                        problemsDefective, problemsDifferent, problemsLate,
                        problemsNotReceived, problemsNotDelivered, problemsNoAnswersOnTime, problemsOutOfStock,
                        problemsOther, visitsLastMonth, sellerUrl);
            }
        }
    }

    private int getStatusCode(String uRL, CloseableHttpClient client) {

        HttpGet httpGet = null;

        try {
            httpGet = new HttpGet(uRL);
        }catch (Exception e){
            int pos = uRL.indexOf("<");
            if (pos>0){
                uRL=uRL.substring(pos);
            }
            pos = uRL.indexOf("&");
            if (pos>0){
                uRL=uRL.substring(pos);
            }
            uRL=uRL.trim();
            try {
                httpGet = new HttpGet(uRL);
            }catch (Exception e1) {
                Logger.log("Error wrong URL "+uRL);
                Logger.log(e1);
                return 404;
            }
        }


        CloseableHttpResponse response= null;
        HttpContext context = new BasicHttpContext();

        int retries=0;
        boolean retry=true;
        int statusCode=0;

        while (retry && retries<5) {
            retries++;
            try {
                response = client.execute(httpGet,context);
            } catch (Exception e) {
                response=null;
                Logger.log("Error en getHTMLStringFromPage"+uRL);
                Logger.log(e);
            }
            if (response != null) {
                StatusLine statusline = response.getStatusLine();
                if (statusline!=null) {
                    statusCode=statusline.getStatusCode();
                    retry = false;
                }
            }

            if (retry){
                try {
                    client = HttpUtils.buildHttpClient();
                    Thread.sleep(5000);//aguantamos los trapos 5 segundos antes de reintentar
                } catch (InterruptedException e) {
                    Logger.log(e);
                }
            }/// todo fin
        }

        if (isUrlChanged(context,uRL)){
            if (DEBUG) {
                Logger.log("url changed " + uRL);
            }
            return 404;
        }

        return statusCode;
    }


    private String fetchNewSellerURL(long sellerId, CloseableHttpClient client) {

        String permalink=null;
        String url="https://api.mercadolibre.com/users/"+sellerId;
        JSONObject sellerObject = HttpUtils.getJsonObjectWithoutToken(url,client,false);
        if (sellerObject!=null){
            if (sellerObject.has("permalink") && !sellerObject.isNull("permalink")){
                permalink = sellerObject.getString("permalink");
            }
        }
        return permalink;
    }


    private String fetchNewSellerURL(String seller, CloseableHttpClient client) {
        String sellerUrl=null;
        long selerId=0;
        ArrayList<String>ids=getSellerProductsIDs(seller);
        String itemsIds="";
        for (String id: ids) {
            itemsIds += id + ",";
        }
        if (itemsIds.contains(",")) {
            itemsIds = itemsIds.substring(0, itemsIds.length() - 1);
            String itemsUrl = "https://api.mercadolibre.com/items?ids=" + itemsIds;
            JSONObject jsonObject = HttpUtils.getJsonObjectWithoutToken(itemsUrl, client, true);
            if (jsonObject!=null) {
                JSONArray jsonArray = jsonObject.getJSONArray("elArray");
                for (int j = 0; j < jsonArray.length(); j++) {
                    JSONObject itemObject2 = jsonArray.getJSONObject(j);
                    int code = itemObject2.getInt("code");
                    if (code==200) {
                        JSONObject productObj = itemObject2.getJSONObject("body");
                        if (productObj != null && productObj.has("seller_id") && !productObj.isNull("seller_id")) {
                            selerId = productObj.getLong("seller_id");
                            break;
                        }
                    }
                }
            }
        }
        if (selerId!=0){
            sellerUrl=fetchNewSellerURL(selerId,client);
        }
        return sellerUrl;
    }

    private static synchronized ArrayList<String> getSellerProductsIDs(String seller) {
        String id=null;
        ArrayList<String> idList=new ArrayList<String>();
        if (selectPreparedStatement2==null){
            Connection connection = getSelectConnection();
            try {
                selectPreparedStatement2=connection.prepareStatement("select id from productos where deshabilitado=false and proveedor = ? order by lastupdate desc, id desc limit 20");
            } catch (SQLException e) {
                Logger.log(e);
                e.printStackTrace();
            }
        }
        try {
            selectPreparedStatement2.setString(1,seller);
            ResultSet resultSet = selectPreparedStatement2.executeQuery();
            if (resultSet==null){
                Logger.log("Couldn't get URL for seller "+seller);
            }
            while (resultSet.next()){
                id=resultSet.getString(1);
                String formatedId=HTMLParseUtils.getUnformattedId(id);
                if (!idList.contains(formatedId)){
                    idList.add(formatedId);
                }
            }
        } catch (SQLException e) {
            Logger.log(e);
            e.printStackTrace();
        }
        return idList;
    }


    private static synchronized boolean isUrlChanged(HttpContext context,String url) {
        HttpRequestWrapper httpRequestWrapper = (HttpRequestWrapper)context.getAttribute("http.request");
        HttpRequest newRequest = httpRequestWrapper.getOriginal();
        RequestLine requestLine = newRequest.getRequestLine();
        int len = url.length();
        String newURL = requestLine.getUri();
        if (newURL==null || newURL.indexOf("NoIndex_True")>0 || (newURL.indexOf("mercadolibre.com.ar")==-1 && newURL.indexOf("api.mercadolibre.com")==-1) ){
            return true;
        }

        return false;
    }


    Double getPercentage(String htmlString,String literal){
        Double result=null;

        int percentagePos1 = htmlString.indexOf(literal);
        if (percentagePos1 == -1) {
            Logger.log("errorr literal position 1 not found !!! " + literal);
            return result;
        }

        int percentagePos2=htmlString.indexOf("%",percentagePos1);
        if (percentagePos2 == -1) {
            Logger.log("errorr literal position 2 not found !!! "+literal);
            return result;
        }
        percentagePos1=percentagePos2-1;
        while (Character.isDigit(htmlString.charAt(percentagePos1)) || htmlString.charAt(percentagePos1) == '.' || htmlString.charAt(percentagePos1)==','){
            percentagePos1--;
        }
        percentagePos1++;

        if (percentagePos1>percentagePos2){
            Logger.log("errorr literal position !!! "+literal);
            return result;
        }
        String percentageStr=htmlString.substring(percentagePos1,percentagePos2);
        if (percentageStr != null && !percentageStr.isEmpty()) {
            percentageStr=percentageStr.replace(",",".");
            result = Double.parseDouble(percentageStr);
        } else {
            Logger.log("errorr empty string looking for !!! "+literal);
            return result;
        }
        return result;
    }

     private static synchronized void incrementGlobalProductCount(){
        globalProdutCount++;
    }

    private static synchronized void incrementGlobalInsertCount(){
        globalInsertCount++;
    }

    private static synchronized void incrementGlobalDisableCount(){
        globalDisableCount++;
    }

    public static void main (String []args){
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Muzzu\\IdeaProjects\\selenium\\chromedriver_win32_2.4\\chromedriver.exe");
        System.setProperty("webdriver.gecko.driver", "C:\\Users\\Muzzu\\IdeaProjects\\selenium\\geckodriver-v0.20.0-win64\\geckodriver.exe");
        globalDateformat = new SimpleDateFormat("HH:mm:ss.SSS");


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        java.util.Date date = null;
        try {
            date = sdf.parse(FECHA);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        globalDate = new Timestamp(date.getTime());

        ///setting visits url time interval
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(globalDate);
        calendar1.add(Calendar.MONTH, -1);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(globalDate);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date1 = dateFormat.format(calendar1.getTime());
        String date2 = dateFormat.format(calendar2.getTime());
        VISITS_URL = VISITS_URL.replaceAll("DATE1",date1).replaceAll("DATE2",date2);



        getSelectConnection();
        getUpdateConnection();

        PreparedStatement selectPreparedStatement = null;
        try {
            Timestamp oldDateTimestamp = new Timestamp(System.currentTimeMillis()-DAYS_WITHOUT_MOVEMENTS*24*60*60*1000l);
            Date oldDate = new Date(oldDateTimestamp.getTime());
            String oldDateStr = new SimpleDateFormat("yyyy-MM-dd").format(oldDate);

            String query="select proveedor from productos where deshabilitado=false and proveedor in " +
                    "(select proveedor from movimientos where proveedor >='"+START_FROM+"' and fecha >'"+oldDateStr+"' group by proveedor) " +
                    "group by proveedor order by proveedor asc";
            selectPreparedStatement = globalSelectConnection.prepareStatement(query);
            globalSelectSellerResultSet = selectPreparedStatement.executeQuery();

            String query2="select idproveedor from productos where proveedor = ? and idproveedor >0 group by idproveedor";
            custIdPreparedStatement = globalSelectConnection.prepareStatement(query2);

        } catch (SQLException e) {
            Logger.log ("SQLException tratando de recuperar vendedores");
            Logger.log(e);
        }

        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
        for (int i=0; i< MAX_THREADS; i++) {
            MLSellerStatistics thread1 = new MLSellerStatistics();
            threadArrayList.add(thread1);
            thread1.start();
        }

        for (Thread thread : threadArrayList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String msg = "TERMINO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        System.out.println(msg);
        Logger.log(msg);
        msg = "Se procesaron " +globalProdutCount+" vendedores. \nSe guardo la reputacion de "+globalInsertCount+" vendedores\n" +
                "Se deshabilitaron "+globalDisableCount+" vendedores";
        System.out.println(msg);
        Logger.log(msg);

    }



}
