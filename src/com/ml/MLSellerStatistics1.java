package com.ml;

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import com.ml.utils.Logger;
import com.ml.utils.HttpUtils;

/**
 * Created by Muzzu on 6/20/2018.
 */
public class MLSellerStatistics1 extends Thread {

    static Connection globalSelectConnection = null;
    static Connection globalUpadteConnection = null;
    static PreparedStatement selectPreparedStatement2 = null;
    static BufferedWriter globalLogger = null;
    static DateFormat globalDateformat=null;
    static Timestamp globalDate;

    static ResultSet globalSelectResultSet = null;
    static String PROFILE_BASE_URL ="https://www.mercadolibre.com.ar/perfil/";
    static String PROFILE_BASE_URL2 ="https://perfil.mercadolibre.com.ar/";
    static String PRODUCT_LIST_BASE_URL = "https://listado.mercadolibre.com.ar/_CustId_";
    static String OFFICIAL_STORE_BASE_URL = "https://listado.mercadolibre.com.ar/_DisplayType_LF_Tienda_";
    static String VISITS_URL = "https://api.mercadolibre.com/users/CUSTID/items_visits?date_from=DATE1T00:00:00.000-00:00&date_to=DATE2T00:00:00.000-00:00";
    static int globalRunnerCount;
    static int globalProdutCount=0;
    static int globalInsertCount=0;
    static int globalDisableCount=0;

    static int MAX_THREADS = 40; //TODO CAMBIAR 40
    static boolean SAVE = true; //TODO CAMBIAR
    static String DATABASE = "ML2";
    static boolean DEBUG = false;
    static String FECHA="2019/08/01";
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
            props.setProperty("password", "postgres");
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
            if (globalSelectResultSet.next()){
                seller= globalSelectResultSet.getString(1);
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

    private static synchronized void insertSeller(String seller, boolean oficialStore, boolean noReputation, boolean hiddenReputation,
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
                    "fecha,proveedor,tiendaoficial,sinreputacion,reputacionoculta,sinpublicacionesactivas,lider,anios,"+
                    "atiendebien,entregaatiempo,ubicacion,promedioventadiario,precioestimadoventapromedio,reputacion,"+
                    "totalproductos,opinionesbuenas,opinionesregulares,opinionesmalas,opinionestotales,"+
                    "porcentajepositivas,porcentajedemoras,porcentajeproblemas,problemadefectuoso,problemadiferente,"+
                    "problematarde,problemanorecibioproducto,problemaNoEntregado,problemaNoResponde,problemaSinStock," +
                    "problemaotro,visitasmensuales,url) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                preparedStatement.setTimestamp(1,globalDate);
                preparedStatement.setString(2, seller);
                preparedStatement.setBoolean(3, oficialStore);
                preparedStatement.setBoolean(4, noReputation);
                preparedStatement.setBoolean(5, hiddenReputation);
                preparedStatement.setBoolean(6, noActivePublications);
                preparedStatement.setString(7, lider);
                preparedStatement.setInt(8, years);
                preparedStatement.setBoolean(9, kindSeller);
                preparedStatement.setBoolean(10, onTime);
                preparedStatement.setString(11, location);
                preparedStatement.setDouble(12, averageSalesPerDay);
                preparedStatement.setDouble(13, estimatedSalePriceAverage);
                preparedStatement.setInt(14, reputation);
                preparedStatement.setInt(15, totalProducts);

                preparedStatement.setInt(16, positiveOpinions);
                preparedStatement.setInt(17, neutralOpinions);
                preparedStatement.setInt(18, negativeOpinions);
                preparedStatement.setInt(19, totalOpinions);

                preparedStatement.setDouble(20, positiveAverage);
                preparedStatement.setDouble(21, delayAverage);
                preparedStatement.setDouble(22, problemsAverage);
                preparedStatement.setDouble(23, problemsDefective);
                preparedStatement.setDouble(24, problemsDifferent);
                preparedStatement.setDouble(25, problemsLate);
                preparedStatement.setDouble(26, problemsNotReceived);
                preparedStatement.setDouble(27, problemsNotDelivered);
                preparedStatement.setDouble(28, problemsNoAnswersOnTime);
                preparedStatement.setDouble(29, problemsOutOfStock);
                preparedStatement.setDouble(30, problemsOther);
                preparedStatement.setInt(31,visitsLastMonth);

                preparedStatement.setString(32,url);

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
        String seller=fetchSeller(runnerID);
        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        while (seller!=null){

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
            String custId=null;

            sellerUrl= PROFILE_BASE_URL +formatSeller(seller);

            long timestamp1 = System.currentTimeMillis();
            String htmlString = HttpUtils.getHTMLStringFromPage(sellerUrl,httpClient,DEBUG);
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



            if (htmlString==null){
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


                sellerUrl= fetchNewSellerURL(seller,httpClient);

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

                    seller=fetchSeller(runnerID);
                    continue;
                }

                timestamp1 = System.currentTimeMillis();
                htmlString = HttpUtils.getHTMLStringFromPage(sellerUrl,httpClient,DEBUG);
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


                if (htmlString==null){
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



                    seller=fetchSeller(runnerID);
                    continue;
                }
            }



            String htmlStringWithoutUserComments=htmlString;
            int commentPos1=htmlString.lastIndexOf("Opiniones de sus compradores");
            if (commentPos1>0){
                htmlStringWithoutUserComments=htmlString.substring(0,commentPos1);  //without user comments
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
                    seller=fetchSeller(runnerID);
                    continue;
                }

                int experiencePos2=htmlStringWithoutUserComments.indexOf("vendiendo en Mercado Libre",experiencePos1);
                if (experiencePos2==-1){
                    Logger.log("errorr !!! buscando experiencePos2");
                    seller=fetchSeller(runnerID);
                    continue;
                }
                experiencePos2--;
                if (experiencePos1>experiencePos2){
                    Logger.log("errorr !!! buscando experience III");
                    seller=fetchSeller(runnerID);
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
                        seller=fetchSeller(runnerID);
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
                    seller=fetchSeller(runnerID);
                    continue;
                }
                salesPos1 += 28;
                int salesPos2 = htmlStringWithoutUserComments.indexOf("</p>", salesPos1);
                if (salesPos2 == -1) {
                    Logger.log("errorr !!! buscando salesPos II");
                    seller=fetchSeller(runnerID);
                    continue;
                }
                if (salesPos1 > salesPos2) {
                    Logger.log("errorr !!! buscando salesPos III");
                    seller=fetchSeller(runnerID);
                    continue;
                }
                String salesOnPeriodStr = htmlStringWithoutUserComments.substring(salesPos1, salesPos2);
                if (salesOnPeriodStr == null) {
                    Logger.log("errorr !!! buscando salesPos IV");
                    seller=fetchSeller(runnerID);
                    continue;
                }
                salesOnPeriodStr = salesOnPeriodStr.trim();
                if (salesOnPeriodStr.isEmpty()) {
                    Logger.log("errorr !!! buscando salesPos V");
                    seller=fetchSeller(runnerID);
                    continue;
                }
                int salesPos3 = salesOnPeriodStr.indexOf("</span>");
                if (salesPos1 == -1) {
                    Logger.log("errorr !!! buscando salesPos VI");
                    seller=fetchSeller(runnerID);
                    continue;
                }
                String salesAmount = salesOnPeriodStr.substring(0, salesPos3);
                if (salesAmount == null) {
                    Logger.log("errorr !!! buscando salesPos VII");
                    seller=fetchSeller(runnerID);
                    continue;
                }
                salesAmount = salesAmount.trim();
                if (salesAmount.isEmpty()) {
                    Logger.log("errorr !!! buscando salesPos VIII");
                    seller=fetchSeller(runnerID);
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
                    seller=fetchSeller(runnerID);
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
                        seller=fetchSeller(runnerID);
                        continue;
                    }
                    if (salesPos4 >= salesPos5) {
                        Logger.log("errorr !!! buscando salesPos XI");
                        seller=fetchSeller(runnerID);
                        continue;
                    }
                    String periodsStr = salesOnPeriodStr.substring(salesPos4, salesPos5);
                    if (periodsStr == null || periodsStr.isEmpty()) {
                        Logger.log("errorr !!! buscando salesPos XII");
                        seller=fetchSeller(runnerID);
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
                    seller=fetchSeller(runnerID);
                    continue;
                }
                int thermometherPos2=htmlStringWithoutUserComments.indexOf("</div", thermometherPos1);
                if (thermometherPos2 == -1) {
                    Logger.log("errorr !!! buscando thermometherPos2");
                    seller=fetchSeller(runnerID);
                    continue;
                }
                if (thermometherPos1 >= thermometherPos2) {
                    Logger.log("errorr !!! buscando thermomether positions");
                    seller=fetchSeller(runnerID);
                    continue;
                }

                String thermometherStr=htmlStringWithoutUserComments.substring(thermometherPos1,thermometherPos2);
                if (thermometherStr == null || thermometherStr.isEmpty()) {
                    Logger.log("errorr !!! buscando thermometherStr");
                    seller=fetchSeller(runnerID);
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
                    seller=fetchSeller(runnerID);
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
                seller=fetchSeller(runnerID);
                continue;
            }
            locationPos1+=19;
            int locationPos2=htmlStringWithoutUserComments.indexOf("<",locationPos1);
            if (locationPos2==-1){
                Logger.log("errorr !!!");
                seller=fetchSeller(runnerID);
                continue;
            }
            if (locationPos1>locationPos2){
                Logger.log("errorr !!!");
                seller=fetchSeller(runnerID);
                continue;
            }
            location=htmlStringWithoutUserComments.substring(locationPos1,locationPos2);



            if (!noReputation) {
                kindSeller = htmlStringWithoutUserComments.indexOf("Brinda buena atenci") != -1;
                if (!kindSeller) {
                    Double theProblemsAverage=getPercentage(htmlStringWithoutUserComments,"Tuvo problemas en sus ventas");
                    if (theProblemsAverage==null){
                        Logger.log("errorr !!! buscando problemas");
                        seller=fetchSeller(runnerID);
                        continue;
                    }
                    problemsAverage=theProblemsAverage;


                    int defectuosoPos1=htmlStringWithoutUserComments.indexOf("El producto era defectuoso");
                    if (defectuosoPos1>-1){
                        Double theProblemsDefective=getPercentage(htmlStringWithoutUserComments,"El producto era defectuoso");
                        if (theProblemsDefective==null){
                            Logger.log("errorr !!! buscando theProblemsDefective");
                            seller=fetchSeller(runnerID);
                            continue;
                        }
                        problemsDefective=theProblemsDefective;
                    }
                    defectuosoPos1=htmlStringWithoutUserComments.indexOf("El producto es defectuoso");
                    if (defectuosoPos1>-1){
                        Double theProblemsDefective=getPercentage(htmlStringWithoutUserComments,"El producto es defectuoso");
                        if (theProblemsDefective==null){
                            Logger.log("errorr !!! buscando theProblemsDefective II");
                            seller=fetchSeller(runnerID);
                            continue;
                        }
                        problemsDefective+=theProblemsDefective;
                    }

                    int ningunProductoPos1=htmlStringWithoutUserComments.indexOf("El comprador no recibió el producto");
                    if (ningunProductoPos1>-1){
                        Double thePproblemsNotReceived=getPercentage(htmlStringWithoutUserComments,"El comprador no recibió el producto");
                        if (thePproblemsNotReceived==null){
                            Logger.log("errorr !!! buscando thePproblemsNotReceived");
                            seller=fetchSeller(runnerID);
                            continue;
                        }
                        problemsNotReceived=thePproblemsNotReceived;
                    }

                    int noAcordadoPos1=htmlStringWithoutUserComments.indexOf("El comprador recibió un producto diferente al acordado");
                    if (noAcordadoPos1>-1){
                        Double theProblemsDifferent=getPercentage(htmlStringWithoutUserComments,"El comprador recibió un producto diferente al acordado");
                        if (theProblemsDifferent==null){
                            Logger.log("errorr !!! buscando theProblemsDifferent");
                            seller=fetchSeller(runnerID);
                            continue;
                        }
                        problemsDifferent=theProblemsDifferent;
                    }
                    noAcordadoPos1=htmlStringWithoutUserComments.indexOf("El producto es distinto");
                    if (noAcordadoPos1>-1){
                        Double theProblemsDifferent=getPercentage(htmlStringWithoutUserComments,"El producto es distinto");
                        if (theProblemsDifferent==null){
                            Logger.log("errorr !!! buscando theProblemsDifferent");
                            seller=fetchSeller(runnerID);
                            continue;
                        }
                        problemsDifferent+=theProblemsDifferent;
                    }

                    int tardePos1=htmlStringWithoutUserComments.indexOf("El producto llegó tarde");
                    if (tardePos1>-1){

                        Double theProblemsLate=getPercentage(htmlStringWithoutUserComments,"El producto llegó tarde");
                        if (theProblemsLate==null){
                            Logger.log("errorr !!! buscando theProblemsLate");
                            seller=fetchSeller(runnerID);
                            continue;
                        }
                        problemsLate=theProblemsLate;
                    }
                    tardePos1=htmlStringWithoutUserComments.indexOf("Entregado fuera de tiempo");
                    if (tardePos1>-1){

                        Double theProblemsLate=getPercentage(htmlStringWithoutUserComments,"Entregado fuera de tiempo");
                        if (theProblemsLate==null){
                            Logger.log("errorr !!! buscando theProblemsLate 2");
                            seller=fetchSeller(runnerID);
                            continue;
                        }
                        problemsLate+=theProblemsLate;
                    }


                    int otrosPos1=htmlStringWithoutUserComments.indexOf("Otro tipo de reclamos");
                    if (otrosPos1>-1){
                        Double theProblemsOther=getPercentage(htmlStringWithoutUserComments,"Otro tipo de reclamos");
                        if (theProblemsOther==null){
                            Logger.log("errorr !!! geting theProblemsOther");
                            seller=fetchSeller(runnerID);
                            continue;
                        }
                        problemsOther=theProblemsOther;
                    }


                    int notDeliveredPos1=htmlStringWithoutUserComments.indexOf("El producto no fue entregado");
                    if (notDeliveredPos1>-1){
                        Double theProblemsNotDelivered=getPercentage(htmlStringWithoutUserComments,"El producto no fue entregado");
                        if (theProblemsNotDelivered==null){
                            Logger.log("errorr !!! geting theProblemsNotDelivered");
                            seller=fetchSeller(runnerID);
                            continue;
                        }
                        problemsNotDelivered=theProblemsNotDelivered;
                    }

                    int noAnswersOnTimePos1=htmlStringWithoutUserComments.indexOf("No respondes a tiempo al comprador");
                    if (noAnswersOnTimePos1>-1){
                        Double theProblemsNoAnswersOnTime=getPercentage(htmlStringWithoutUserComments,"No respondes a tiempo al comprador");
                        if (theProblemsNoAnswersOnTime==null){
                            Logger.log("errorr !!! geting theProblemsNoAnswersOnTime");
                            seller=fetchSeller(runnerID);
                            continue;
                        }
                        problemsNoAnswersOnTime=theProblemsNoAnswersOnTime;
                    }
                    noAnswersOnTimePos1=htmlStringWithoutUserComments.indexOf("Demora en responder al comprador");
                    if (noAnswersOnTimePos1>-1){
                        Double theProblemsNoAnswersOnTime=getPercentage(htmlStringWithoutUserComments,"Demora en responder al comprador");
                        if (theProblemsNoAnswersOnTime==null){
                            Logger.log("errorr !!! geting theProblemsNoAnswersOnTime");
                            seller=fetchSeller(runnerID);
                            continue;
                        }
                        problemsNoAnswersOnTime+=theProblemsNoAnswersOnTime;
                    }


                    int OutOfStockPos1=htmlStringWithoutUserComments.indexOf("Te quedaste sin stock");
                    if (OutOfStockPos1>-1){
                        Double theProblemsOutOfStock=getPercentage(htmlStringWithoutUserComments,"Te quedaste sin stock");
                        if (theProblemsOutOfStock==null){
                            Logger.log("errorr !!! geting theProblemsOutOfStock");
                            seller=fetchSeller(runnerID);
                            continue;
                        }
                        problemsOutOfStock=theProblemsOutOfStock;
                    }
                    OutOfStockPos1=htmlStringWithoutUserComments.indexOf("El producto está fuera de stock");
                    if (OutOfStockPos1>-1){
                        Double theProblemsOutOfStock=getPercentage(htmlStringWithoutUserComments,"El producto está fuera de stock");
                        if (theProblemsOutOfStock==null){
                            Logger.log("errorr !!! geting theProblemsOutOfStock");
                            seller=fetchSeller(runnerID);
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
                        seller=fetchSeller(runnerID);
                        continue;
                    }
                    delayAverage=theDelayAverage;
                }
            }


            int buyersFeedbackPos1=htmlString.indexOf("buyers-feedback-bar");
            if (buyersFeedbackPos1 == -1) {
                Logger.log("errorr !!! buyersFeedbackPos1 "+sellerUrl);
                seller=fetchSeller(runnerID);
                continue;
            }
            int buyersFeedbackPos2=htmlString.indexOf("</div",buyersFeedbackPos1);
            if (buyersFeedbackPos2 == -1) {
                Logger.log("errorr !!! buyersFeedbackPos2");
                seller=fetchSeller(runnerID);
                continue;
            }
            if (buyersFeedbackPos1>=buyersFeedbackPos2){
                Logger.log("errorr !!! buyersFeedback Position");
                seller=fetchSeller(runnerID);
                continue;
            }
            String buyersFeedbackStr=htmlString.substring(buyersFeedbackPos1,buyersFeedbackPos2);
            if (buyersFeedbackStr == null || buyersFeedbackStr.isEmpty()) {
                Logger.log("errorr !!! buscando buyersFeedbackStr");
                seller=fetchSeller(runnerID);
                continue;
            }

            int positiveFeedbackPos1=buyersFeedbackStr.indexOf("Buena");
            if (positiveFeedbackPos1 == -1) {
                Logger.log("errorr !!! goodFeedbackPos1");
                seller=fetchSeller(runnerID);
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
                seller=fetchSeller(runnerID);
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
                seller=fetchSeller(runnerID);
                continue;
            }
            if (positiveFeedbackPos1>=positiveFeedbackPos2){
                Logger.log("errorr !!! goodFeedbackPos1 and goodFeedbackPos2");
                seller=fetchSeller(runnerID);
                continue;
            }
            String positiveFeedbackStr=buyersFeedbackStr.substring(positiveFeedbackPos1,positiveFeedbackPos2);
            if (positiveFeedbackStr==null){
                Logger.log("errorr !!! goodFeedbackStr is null");
                seller=fetchSeller(runnerID);
                continue;
            }
            positiveFeedbackStr=positiveFeedbackStr.trim();
            if (positiveFeedbackStr.isEmpty()){
                Logger.log("errorr !!! goodFeedbackStr is empty");
                seller=fetchSeller(runnerID);
                continue;
            }
            positiveOpinions=Integer.parseInt(positiveFeedbackStr);


            int neutralFeedbackPos1=buyersFeedbackStr.indexOf("Regular");
            if (neutralFeedbackPos1 == -1) {
                Logger.log("errorr !!! neutralFeedbackPos1");
                seller=fetchSeller(runnerID);
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
                seller=fetchSeller(runnerID);
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
                seller=fetchSeller(runnerID);
                continue;
            }
            if (neutralFeedbackPos1>=neutralFeedbackPos2){
                Logger.log("errorr !!! regularFeedbackPos1 and regularFeedbackPos2");
                seller=fetchSeller(runnerID);
                continue;
            }
            String neutralFeedbackStr=buyersFeedbackStr.substring(neutralFeedbackPos1,neutralFeedbackPos2);
            if (neutralFeedbackStr==null){
                Logger.log("errorr !!! regularFeedbackStr is null");
                seller=fetchSeller(runnerID);
                continue;
            }
            neutralFeedbackStr=neutralFeedbackStr.trim();
            if (neutralFeedbackStr.isEmpty()){
                Logger.log("errorr !!! regularFeedbackStr is empty");
                seller=fetchSeller(runnerID);
                continue;
            }
            neutralOpinions=Integer.parseInt(neutralFeedbackStr);


            int negativeFeedbackPos1=buyersFeedbackStr.indexOf("Mala");
            if (negativeFeedbackPos1 == -1) {
                Logger.log("errorr !!! negativeFeedbackPos1");
                seller=fetchSeller(runnerID);
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
                seller=fetchSeller(runnerID);
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
                seller=fetchSeller(runnerID);
                continue;
            }
            if (negativeFeedbackPos1>=negativeFeedbackPos2){
                Logger.log("errorr !!! negativeFeedbackPos1 and negativeFeedbackPos2");
                seller=fetchSeller(runnerID);
                continue;
            }
            String negativeFeedbackStr=buyersFeedbackStr.substring(negativeFeedbackPos1,negativeFeedbackPos2);
            if (negativeFeedbackStr==null){
                Logger.log("errorr !!! negativeFeedbackStr is null");
                seller=fetchSeller(runnerID);
                continue;
            }
            negativeFeedbackStr=negativeFeedbackStr.trim();
            if (negativeFeedbackStr.isEmpty()){
                Logger.log("errorr !!! negativeFeedbackStr is empty");
                seller=fetchSeller(runnerID);
                continue;
            }
            negativeOpinions=Integer.parseInt(negativeFeedbackStr);

            totalOpinions=positiveOpinions+neutralOpinions+negativeOpinions;

            if (totalOpinions>0) {
                positiveAverage = 100.0 * positiveOpinions / totalOpinions;
            }

            String userId=null;
            int userIdPos1=htmlString.indexOf("user_id");
            if (userIdPos1>0){
                userIdPos1+=9;
                int userIdPos2=htmlString.indexOf(",",userIdPos1);
                if (userIdPos2>0){
                    userId=htmlString.substring(userIdPos1,userIdPos2);
                    if (userId!=null && userId.length()>0){
                        custId=userId;
                    }
                }
            }

            if (!seller.equals(seller.toUpperCase())){ //tiene minuscula / posible tienda oficial
                String officialStoreUrl = OFFICIAL_STORE_BASE_URL + formatSeller(seller);
                String officialStoreHtmlString = HttpUtils.getHTMLStringFromPage(officialStoreUrl, httpClient,DEBUG);

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

                    officialStoreHtmlString = HttpUtils.getHTMLStringFromPage(officialStoreUrl, httpClient,DEBUG);
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

            htmlString = HttpUtils.getHTMLStringFromPage(productsUrl, httpClient, DEBUG);

            if (htmlString==null) {
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

                htmlString = HttpUtils.getHTMLStringFromPage(productsUrl, httpClient,DEBUG);
                if (htmlString == null) {
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

            if (htmlString==null || htmlString.contains("Escribí en el buscador lo que querés encontrar")){//vendedor inactivo
                noActivePublications=true;
                String msg = "Vendedor sin publicaciones activas " + sellerUrl;
                System.out.println(msg);
                Logger.log(msg);
            }


            if (!noActivePublications) {
                int totalProductsPos1 = htmlString.indexOf("quantity-results");
                if (totalProductsPos1 < 0) {
                    Logger.log("errorr en totalProductsPos1 !!!");
                    seller=fetchSeller(runnerID);
                    continue;
                }
                totalProductsPos1 += 19;
                int totalProductsPos2 = htmlString.indexOf("resultado", totalProductsPos1);
                if (totalProductsPos2 < 0) {
                    Logger.log("errorr en totalProductsPos2 !!!");
                    seller=fetchSeller(runnerID);
                    continue;
                }
                totalProductsPos2--;
                String totalProductsStr = htmlString.substring(totalProductsPos1, totalProductsPos2);
                if (totalProductsStr == null) {
                    Logger.log("errorr en totalProductsStr !!!");
                    seller=fetchSeller(runnerID);
                    continue;
                }
                totalProductsStr=totalProductsStr.replace(".", "");
                totalProductsStr=totalProductsStr.trim();
                totalProducts = Integer.parseInt(totalProductsStr);

                String[] allHrefsOnPage = StringUtils.substringsBetween(htmlString, "<a href", "</a>");
                if (allHrefsOnPage == null) { //todo check
                    Logger.log("errorr en allHrefsOnPage !!!");
                    seller=fetchSeller(runnerID);
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

                int idPos1, idPos2 = 0;
                int amount = 0;
                int articles = 0;
                for (String productUrl : productsURLArrayList) {

                    idPos1 = productUrl.indexOf(ARTICLE_PREFIX);
                    idPos2 = idPos1 + 13;

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

                    int pricePos1 = productHTMLdata.indexOf("price__fraction\">") + 17;
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
                        Logger.log(" I couldn't get the price on " + productUrl);
                        Logger.log(e);
                    }


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
                    }

                    amount += price * totalSold;
                    articles += totalSold;
                }

                if (articles > 0) {
                    estimatedSalePriceAverage = amount / articles;
                }
            }

            if (custId!=null){
                String visitUrl = VISITS_URL.replaceAll("CUSTID",custId);

                boolean retry=true;
                int retries=0;
                while (retry && retries<5) {
                    retry=false;
                    retries++;
                    String htmlString3 = HttpUtils.getHTMLStringFromPage(visitUrl, httpClient,DEBUG);
                    if (htmlString3 != null) {
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
                if (visitsLastMonth == 0) {
                    String msg = "No se pudieron recuperar visitas " + retries + " " + sellerUrl;
                    System.out.println(msg);
                    Logger.log(msg);
                }

            }

            if (SAVE) {
                insertSeller(seller, oficialStore, noReputation, hiddenReputation,
                        noActivePublications, lider, years, kindSeller,
                        onTime, location, averageSalesPerDay, estimatedSalePriceAverage,
                        reputation, totalProducts, positiveOpinions, neutralOpinions, negativeOpinions,
                        totalOpinions, positiveAverage, delayAverage, problemsAverage,
                        problemsDefective, problemsDifferent, problemsLate,
                        problemsNotReceived, problemsNotDelivered, problemsNoAnswersOnTime, problemsOutOfStock,
                        problemsOther, visitsLastMonth, sellerUrl);
            }
            seller=fetchSeller(runnerID);
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


    private String fetchNewSellerURL(String seller, CloseableHttpClient client) {


        ArrayList<String>urls = getSellerProductsURLs(seller);
        String theUrl=null;

        if (urls==null || urls.size()==0){
            Logger.log("No pudo recuperar ninguna URL para el vendedor "+seller);
            return null; // todo ver que hacemos aca
        }

        String page=null;

        for (String url:urls) {
            page=HttpUtils.getHTMLStringFromPage(url,client,DEBUG);
            if (page!=null){
                break;
            }else {
                //rebuild httpClient
                try {
                    Thread.sleep(5000);
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
                ////////////////////////////////////////////
            }

        }
        if (page==null){
            Logger.log("no funcionó ninguna url "+seller);
            int statusCode=getStatusCode(PROFILE_BASE_URL +formatSeller(seller),client);
            if (statusCode!=403) {//temporarlmente deshabilitado - lo bancamos un tiempo
                if (statusCode==404) {
                    if (SAVE){
                        Logger.log("desabilitando los productos de "+seller);
                        disableSellerProducts(seller);
                    }
                }else {
                    if (DEBUG) {
                        Logger.log("new status code is " + statusCode + " no sabemos que pasa con " + seller);
                    }else {
                        if (statusCode!=403){
                            Logger.log("new status code is " + statusCode + " no sabemos que pasa con " + seller);
                        }
                    }
                }
            }
            return null;
        }

        boolean hiddenReputation=false;
        if (page.indexOf(SEE_SELLER_DETAILS_LABEL)==-1){
            hiddenReputation=true;
        }

        int pos1=0;
        int pos2=0;

        pos1=page.indexOf("dimension120");
        if (pos1<1) {
            Logger.log("Usuario dado de baja " + seller);
            return null;
        }
        pos1+=14;
        pos2=page.indexOf("dimension",pos1);
        if (pos1<1) {
            Logger.log("error buscando dimension120 seller URL " + seller + " pos2: " + pos2);
            return null;
        }

        String dimension120=page.substring(pos1,pos2);
        if (dimension120==null || dimension120.length()<0) {
            Logger.log("error buscando dimension120 "  + seller + " dimension120 " + dimension120);
            return null;
        }

        pos1=dimension120.indexOf("\"");
        if (pos1<1) {
            Logger.log("error buscando seller URL " + seller + " pos1: " + pos1);
            return null;
        }
        pos1++;
        pos2=dimension120.indexOf("\"",pos1);
        if (pos2<1) {
            Logger.log("error buscando seller URL " + seller + " pos2: " + pos2);
            return null;
        }


        String seller2=dimension120.substring(pos1,pos2);

        if (hiddenReputation){
            Logger.log("new status code is 405 CAPANGA ESCONDE SU REPUTACION "+seller+" -> "+seller2 );
        }

        String sellerURL=this.PROFILE_BASE_URL+formatSeller(seller2);
        return sellerURL;
    }

    private static synchronized ArrayList<String> getSellerProductsURLs(String seller) {
        String uRL=null;
        ArrayList<String> uRLs=new ArrayList<String>();
        if (selectPreparedStatement2==null){
            Connection connection = getSelectConnection();
            try {
                selectPreparedStatement2=connection.prepareStatement("select url from productos where deshabilitado=false and proveedor = ? order by lastupdate desc");
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
                uRL=resultSet.getString(1);
                if (!uRLs.contains(uRL)){
                    uRLs.add(uRL);
                }

            }

        } catch (SQLException e) {
            Logger.log(e);
            e.printStackTrace();
        }
        return uRLs;
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

    private static synchronized String getStringFromInputStream(InputStream is) {

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
            globalSelectResultSet = selectPreparedStatement.executeQuery();
        } catch (SQLException e) {
            Logger.log ("SQLException tratando de recuperar vendedores");
            Logger.log(e);
        }

        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
        for (int i=0; i< MAX_THREADS; i++) {
            MLSellerStatistics1 thread1 = new MLSellerStatistics1();
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
