package com.ml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;


/**
 * Created by Muzzu on 11/12/2017.
 */

public class MercadoLibre03 extends Thread {

    static int[] globalPageArray = new int[]
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    /*
    static String[] globalSubIntervals = new String[]{
            "capital-federal/agronomia-o-almagro-o-balvanera-o-barracas-o-barrio-norte-o-belgrano-o-belgrano-barrancas-o-belgrano-c-o-belgrano-chico-o-belgrano-r-o-boedo-o-botanico-o-caballito-o-chacarita-o-coghlan-o-colegiales-o-congreso-o-constitucion/",
            "capital-federal/flores-o-floresta-o-la-boca-o-las-canitas-o-liniers-o-mataderos-o-monserrat-o-monte-castro-o-nueva-pompeya-o-nunez-o-once/",
            "capital-federal/palermo-o-palermo-chico-o-palermo-hollywood-o-palermo-nuevo-o-palermo-soho-o-palermo-viejo-o-parque-avellaneda-o-parque-chacabuco-o-parque-chas-o-parque-patricios-o-paternal-o-puerto-madero-o-recoleta-o-retiro/",
            "capital-federal/saavedra-o-san-cristobal-o-san-nicolas-o-san-telmo-o-santa-rita-o-velez-sarsfield-o-versalles-o-villa-crespo-o-villa-devoto-o-villa-gral-mitre-o-villa-lugano-o-villa-luro-o-villa-ortuzar-o-villa-pueyrredon-o-villa-real-o-villa-riachuelo-o-villa-soldati-o-villa-urquiza-o-villa-del-parque/",
            "bsas-gba-norte/",
            "bsas-gba-oeste/",
            "bsas-gba-sur/",
            "bsas-costa-atlantica/",
            "buenos-aires-interior/",
            "santa-fe/",
            "cordoba/",
            "mendoza/",
            "corrientes/",
            "entre-rios/",
            "misiones/",
            "tucuman/"};*/

    static String[] globalSubIntervals = new String[]{""};  //este subinterval es para que no haga nada

    static int globalPageCount=0;
    static int globalProdutCount=0;
    static int globalNewsCount=0;
    static int globalRunnerCount=0;

    static CloseableHttpClient globalClient=null;
    static Connection globalSelectConnection=null;
    static Connection globalUpadteConnection=null;
    static Connection globalAddProductConnection=null;
    static Connection globalAddActivityConnection =null;
    static Date globalDate=null;
    static BufferedWriter globalLogger=null;
    static DateFormat globalDateformat=null;
    static Calendar globalCalendar1=null;
    static Calendar globalCalendar2=null;

    static int globalAndFirstInterval =0; //arranca en 0
    static int MAX_THREADS=3;
    static boolean OVERRIDE_TODAYS_RUN=false;
    static boolean SAVE =true;
    static boolean FOLLOWING_DAY = false;
    static boolean PRERVIOUS_DAY = false;
    static boolean ONLY_ADD_NEW_PRODUCTS=false;
    static boolean LOAD_IMAGES =false;
    static boolean SILENT = true;
    static boolean FIREFOX = false;
    static boolean CHROME = true;
    static boolean VISIT_PRODUCT_PAGE=false;
    static int MINIMUM_SALES=10;
    static String DATABASE="ML3";

    static int[] intervals = new int[]{0,36,70,81,92,104,119,133,149,167,183,199,219,249,285,325,373,430,548,750,900,
            1150,1450,1650,1950,2400,3000,4000,2147483647};

    //static int[] intervals = new int[]{0,200,400,600,800,1000,1300,1600,1900,2400,3000,4000,5000,10000,2147483647};

    /*
    static int[] intervals = new int[]{0,400,700,998,1300,1500,1800,2100,2500,2999,3500,4200,5000,6000,7300,8700,10500,
            14000,20000,30000,2147483647};
*/
    //regional settings
    static String ARTICLE_PREFIX="MLB";
    static String SEE_SELLER_DETAILS_LABEL = "Ver mais dados deste vendedor";
    static boolean BRAZIL=true;

    static PreparedStatement globalInsertProduct = null;
    static PreparedStatement globalInsertActivity = null;
    static PreparedStatement globalRemoveActivity = null;
    static PreparedStatement globalUpdateProduct = null;
    static PreparedStatement globalSelectProduct = null;
    static PreparedStatement globalSelectTotalSold = null;
    static PreparedStatement globalSelectLastQuestion = null;
    static String globalBaseURL=null;
    static String[] urls = new String[]
            {"https://lista.mercadolivre.com.br/casa-moveis-decoracao/decoracao/",
                    "https://lista.mercadolivre.com.br/casa-moveis-decoracao/moveis-escritorio/",
                    "https://lista.mercadolivre.com.br/casa-moveis-decoracao/moveis-escritorio/outros/",
                    "https://lista.mercadolivre.com.br/casa-moveis-decoracao/sala-estar/",
                    "https://lista.mercadolivre.com.br/casa-moveis-decoracao/quarto/",
                    "https://lista.mercadolivre.com.br/casa-moveis-decoracao/quarto/arara-roupas/",
                    "https://lista.mercadolivre.com.br/casa-moveis-decoracao/sala-jantar/",
                    "https://lista.mercadolivre.com.br/casa-moveis-decoracao/jardim/moveis-jardim/",
                    "https://lista.mercadolivre.com.br/casa-moveis-decoracao/cozinha/moveis-de-cozinha/",
                    "https://lista.mercadolivre.com.br/casa-moveis-decoracao/lavanderia/armario/",
                    "https://lista.mercadolivre.com.br/bebes/bercos-moveis/",
                    "https://lista.mercadolivre.com.br/mancebo",
                    "https://lista.mercadolivre.com.br/porta-guarda-chuvas"};

    static int DECORACAO=0;
    static int MOVIES_ESCRITORIO=1;
    static int SALA_ESTAR=2;


    private static CloseableHttpClient buildHttpClient() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("random", "random"));
        CloseableHttpClient httpclient =
                HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
        return httpclient;
    }


    private static BufferedWriter getLogger(){
        if (globalLogger==null){
            Calendar cal = Calendar.getInstance();
            long milliseconds = cal.getTimeInMillis();
            Timestamp timestamp = new Timestamp(milliseconds);

            String fileName=("salida"+timestamp.getTime()/1000+".txt");
            File file= new File (fileName);
            FileWriter fileWriter=null;
            if (file.exists())
            {
                try {
                    fileWriter = new FileWriter(file,true);//if file exists append to file. Works fine.
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                try {
                    file.createNewFile();
                    fileWriter = new FileWriter(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                globalLogger=new BufferedWriter(fileWriter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return globalLogger;
    }

    private static void log(String string){
        Calendar cal = Calendar.getInstance();
        long milliseconds = cal.getTimeInMillis();
        Timestamp timestamp = new Timestamp(milliseconds);
        String timeStr = globalDateformat.format(timestamp);
        try {
            BufferedWriter log = getLogger();
            log.write(timeStr+" | "+string+"\n");
            log.newLine();
            log.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void log(Throwable throwable){
        log(ExceptionUtils.getStackTrace(throwable));
    }


    public static void main(String[] args) {

        System.setProperty("webdriver.chrome.driver","C:\\Users\\Muzzu\\IdeaProjects\\selenium\\chromedriver_win32_2.4\\chromedriver.exe");
        System.setProperty("webdriver.gecko.driver", "C:\\Users\\Muzzu\\IdeaProjects\\selenium\\geckodriver-v0.20.0-win64\\geckodriver.exe");

        globalDateformat = new SimpleDateFormat("HH:mm:ss.SSS");
        globalCalendar1 = Calendar.getInstance();
        globalCalendar2 = Calendar.getInstance();

        for (int i=1; i<intervals.length; i++){ //validacion de intervalo
            if (intervals[i-1]>=intervals[i]){
                System.out.println("Error en intervalo "+intervals[i-1]+"-"+intervals[i]);
                System.exit(0);
            }
        }

        globalClient = buildHttpClient();

        globalBaseURL=urls[12];//ir hasta el 12
        if (SAVE){
            saveRunInitialization(globalBaseURL,MAX_THREADS);
        }

        for (int i=0; i< MAX_THREADS; i++) {
            MercadoLibre03 thread1 = new MercadoLibre03();
            thread1.start();
        }
    }


    private static long saveRunInitialization(String url, int threads) {
        PreparedStatement ps = null;
        Connection connection = getUpdateConnection();
        long runId=-1;
        try{
            ps = connection.prepareStatement("INSERT INTO public.corridas(fecha, inicio, url, threads) VALUES (?, ?, ?, ?) RETURNING id;",Statement.RETURN_GENERATED_KEYS);

            Calendar cal = Calendar.getInstance();
            long milliseconds = cal.getTimeInMillis();
            Timestamp timestamp = new Timestamp(milliseconds);

            ps.setDate(1,getGlobalDate());
            ps.setTimestamp(2,timestamp);
            ps.setString(3,url);
            ps.setInt(4,threads);

            int insertedRecords = ps.executeUpdate();
            connection.commit();

            if (insertedRecords!=1){
                log("Couldn't insert record into runs table");
            }
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()){
                runId=rs.getInt(1);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }finally{
            try {
                if (ps!=null) {
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return runId;
    }

    private static long saveRunEnding(long runId,int productostotal, int productosdetailstotal) {
        PreparedStatement ps = null;
        Connection connection = getUpdateConnection();
        try{
            ps = connection.prepareStatement("UPDATE public.corridas SET fin=?, productostotal=?, productosdetalletotal=? WHERE id=?;");

            Calendar cal = Calendar.getInstance();
            long milliseconds = cal.getTimeInMillis();
            Timestamp timestamp = new Timestamp(milliseconds);

            ps.setTimestamp(1,timestamp);
            ps.setInt(2,productostotal);
            ps.setInt(3,productosdetailstotal);
            ps.setLong(4,runId);

            int insertedRecords = ps.executeUpdate();
            connection.commit();

            if (insertedRecords!=1){
                log("couldn't update row in runs table");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }finally{
            try {
                if (ps!=null) {
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return runId;
    }

    private static synchronized void insertProduct(String idProduct, String seller, int totalSold, String latestquestion, String url, boolean officialStore) {
        Connection connection= getAddProductConnection();

        try{
            if (globalInsertProduct ==null) {
                globalInsertProduct = connection.prepareStatement("INSERT INTO public.productos(id, proveedor, ingreso, lastupdate, lastquestion, totalvendidos, url, tiendaoficial) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
            }

            globalInsertProduct.setString(1,idProduct);
            globalInsertProduct.setString(2,seller);
            globalInsertProduct.setDate(3,getGlobalDate());
            globalInsertProduct.setDate(4,getGlobalDate());
            globalInsertProduct.setString(5,latestquestion);
            globalInsertProduct.setInt(6,totalSold);
            globalInsertProduct.setString(7,url);
            globalInsertProduct.setBoolean(8,officialStore);

            int registrosInsertados = globalInsertProduct.executeUpdate();

            if (registrosInsertados!=1){
                log("Couldn't insert product I");
            }
        }catch(SQLException e){
            log("Couldn't insert product II");
            log(e);
        }
    }

    private synchronized static Date getGlobalDate(){
        if (globalDate==null){
            Calendar cal = Calendar.getInstance();
            long milliseconds = cal.getTimeInMillis();
            long oneDayinMiliseconds=0;
            if (FOLLOWING_DAY){
                oneDayinMiliseconds=86400000; //this will add a complete day on milliseconds
            }
            if (PRERVIOUS_DAY){
                oneDayinMiliseconds=-86400000; //this will add a complete day on milliseconds
            }
            Date date = new Date(milliseconds+oneDayinMiliseconds);
            globalDate=date;
        }
        return globalDate;
    }

    private static synchronized void updateProductAddActivity(String productId, int totalSold, int newSold, String title, String url, int feedbacksTotal, double feedbacksAverage, double price, int newQuestions, int pagina) {
        Connection connection = getAddActivityConnection();
        try{
            if (globalUpdateProduct ==null) {
                globalUpdateProduct = connection.prepareStatement("UPDATE public.productos SET totalvendidos = ?, lastupdate=?, url=? WHERE id = ?;");
            }

            globalUpdateProduct.setInt(1,totalSold);
            globalUpdateProduct.setDate(2,getGlobalDate());
            globalUpdateProduct.setString(3,url);
            globalUpdateProduct.setString(4,productId);

            int insertedRecords = globalUpdateProduct.executeUpdate();
            if (insertedRecords!=1){
                log("Couldn't update product "+productId);
            }

            if (OVERRIDE_TODAYS_RUN){//no sabemos si hay registro pero por las dudas removemos
                if (globalRemoveActivity==null){
                    globalRemoveActivity=connection.prepareStatement("DELETE FROM public.movimientos WHERE idproducto=? and fecha=?");
                }
                globalRemoveActivity.setString(1,productId);
                globalRemoveActivity.setDate(2,getGlobalDate());
                int removedRecords=globalRemoveActivity.executeUpdate();
                if (removedRecords>=1){
                    log("Record removed on activity table date: "+getGlobalDate()+" productId: "+productId);
                }
            }

            if (globalInsertActivity ==null){
                globalInsertActivity =connection.prepareStatement("INSERT INTO public.movimientos(fecha, idproducto, titulo, url, opinionestotal, opinionespromedio, precio, vendidos, totalvendidos, nuevaspreguntas, pagina) VALUES (?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?);");
            }

            globalInsertActivity.setDate(1,getGlobalDate());
            globalInsertActivity.setString(2,productId);
            globalInsertActivity.setString(3,title);
            globalInsertActivity.setString(4,url);
            globalInsertActivity.setInt(5,feedbacksTotal);
            globalInsertActivity.setDouble(6, feedbacksAverage);
            globalInsertActivity.setDouble(7, price);
            globalInsertActivity.setInt(8,newSold);
            globalInsertActivity.setInt(9,totalSold);
            globalInsertActivity.setInt(10,newQuestions);
            globalInsertActivity.setInt(11,pagina);

            insertedRecords = globalInsertActivity.executeUpdate();
            if (insertedRecords!=1){
                log("Couln't insert a record in activity table "+productId);
            }

            connection.commit();

        }catch(SQLException e){
            log("I couldn't add activity due to SQLException "+url);
            log(e);
            if (connection!=null){
                try {
                    //connection reset
                    connection.close();
                    connection=null;
                    globalAddActivityConnection=null;

                    //prepared statement's reset
                    globalInsertActivity=null;
                    globalRemoveActivity=null;
                    globalUpdateProduct=null;
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    private static synchronized Date lastUpdate(String productId) {
        Date lastUpdate=null;
        Connection connection=getSelectConnection();
        try{
            if (globalSelectProduct ==null) {
                globalSelectProduct = connection.prepareStatement("SELECT lastUpdate FROM public.productos WHERE id=?;");
            }

            globalSelectProduct.setString(1,productId);

            ResultSet rs = globalSelectProduct.executeQuery();
            if (rs==null){
                log("Couldn't get last update I "+productId);
            }
            if (rs.next()){
                lastUpdate=rs.getDate(1);
            }
        }catch(SQLException e){
            log("Couldn't get last update II "+productId);
            log(e);
        }
        return lastUpdate;
    }

    private static synchronized int getGlobalRunnerCount() {
        return ++globalRunnerCount;
    }



    private static synchronized int getTotalSold(String productId) {
        int totalSold=0;
        Connection connection = getSelectConnection();
        try{
            if (globalSelectTotalSold ==null) {
                globalSelectTotalSold = connection.prepareStatement("SELECT totalvendidos FROM public.productos WHERE id=?;");
            }

            globalSelectTotalSold.setString(1,productId);

            ResultSet rs = globalSelectTotalSold.executeQuery();
            if (rs==null){
                log("Couldn't get total sold i"+productId);
                return 0;
            }

            if (rs.next()){
                totalSold=rs.getInt(1);
            }
        }catch(SQLException e){
            e.printStackTrace();
            log("Couldn't get total sold ii"+productId);
        }
        return totalSold;
    }

    private static synchronized String getLastQuestion(String productId) {
        String lastQuestion=null;
        Connection connection=getSelectConnection();
        try{
            if (globalSelectLastQuestion==null) {
                globalSelectLastQuestion = connection.prepareStatement("SELECT lastQuestion FROM public.productos WHERE id=?;");
            }

            globalSelectLastQuestion.setString(1,productId);

            ResultSet rs = globalSelectLastQuestion.executeQuery();
            if (rs==null){
                log("Couldn't get last question i "+productId);
                return null;
            }

            if (rs.next()){
                lastQuestion=rs.getString(1);
            }
        }catch(SQLException e){
            log("Couldn't get last question ii "+productId);
            log(e);
        }
        return lastQuestion;
    }


    synchronized private static int  getPage(int interval, boolean reset){
        if (reset){
            globalPageArray[interval]=0;
        } else {
            globalPageArray[interval]++;
        }
        return globalPageArray[interval];
    }

    synchronized private static int  getInterval(){
        globalAndFirstInterval++;
        log ("XXXXXXXXXXXXXXXXXXXXXX last interval : "+ globalAndFirstInterval);
        return globalAndFirstInterval;
    }

    synchronized private static void incrementGlobalPageCount(){
        globalPageCount++;
    }

    synchronized private static void incrementGlobalProductCount(){
        globalProdutCount++;
    }

    private static CloseableHttpClient getClient() {
        return globalClient;
    }


    private static Connection getSelectConnection(){
        if (globalSelectConnection==null) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/"+DATABASE;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "postgres");
            try {
                globalSelectConnection = DriverManager.getConnection(url, props);
            } catch (SQLException e) {
                log("I couldn't make a select connection");
                log(e);
                e.printStackTrace();
            }
        }
        return globalSelectConnection;
    }


    private static Connection getUpdateConnection(){
        if (globalUpadteConnection==null) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/"+DATABASE;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "postgres");
            try {
                globalUpadteConnection = DriverManager.getConnection(url, props);
                globalUpadteConnection.setAutoCommit(false);
            } catch (SQLException e) {
                log("I couldn't make an update connection");
                log(e);
                e.printStackTrace();
            }
        }
        return globalUpadteConnection;
    }


    private static Connection getAddProductConnection(){
        if (globalAddProductConnection==null) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/"+DATABASE;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "postgres");
            try {
                globalAddProductConnection = DriverManager.getConnection(url, props);
            } catch (SQLException e) {
                log("I couldn't make addproduct connection");
                log(e);
                e.printStackTrace();
            }
        }
        return globalAddProductConnection;
    }

    private static Connection getAddActivityConnection(){
        if (globalAddActivityConnection ==null) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/"+DATABASE;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "postgres");
            try {
                globalAddActivityConnection = DriverManager.getConnection(url, props);
                globalAddActivityConnection.setAutoCommit(false);
            } catch (SQLException e) {
                log("I couldn't make add activity connection");
                log(e);
                e.printStackTrace();
            }
        }
        return globalAddActivityConnection;
    }

    public void run(){

        WebDriver webDriver=getWebDriver();
        int itemsInPage=48;
        String runnerID="R"+getGlobalRunnerCount();

        boolean processFinished=false;
        while (!processFinished){
            int interval=getInterval();
            if (interval>=intervals.length){
                processFinished=true;
                continue;
            }
            int range1=intervals[interval-1]+1;
            int range2=intervals[interval];
            String priceRangeStr="_PriceRange_"+range1+"-"+range2;
            String[] subintervals = new String[]{""};
            if (range1==range2){
                subintervals = globalSubIntervals;
            }
            for (String subinterval : subintervals) {
                boolean endInterval = false;
                getPage(interval, true);
                while (!endInterval) {
                    int page = getPage(interval, false);
                    if (page == 43) {
                        log("este intervalo no pudo ser recorrido por completo " + globalBaseURL + priceRangeStr);
                        System.out.println("este intervalo no pudo ser recorrido por completo " + globalBaseURL + priceRangeStr);
                        endInterval = true;
                        continue;
                    }//se acabose

                    incrementGlobalPageCount();
                    int since = (page - 1) * itemsInPage + 1;
                    String sinceStr = "_Desde_" + since;
                    String uRL = globalBaseURL + sinceStr + priceRangeStr;
                    if (page == 1) {
                        uRL = globalBaseURL + priceRangeStr;
                    }
                    uRL=uRL.replace("[_SUBINTERVAL]",subinterval);

                    System.out.println(uRL);
                    log(uRL);
                    String htmlStringFromPage = getHTMLStringFromPage(uRL);
                    if (htmlStringFromPage == null) { //suponemos que se terminó
                        // pero tambien hacemos pausa por si es problema de red
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            log(e);
                        }
                        log(runnerID+" hmlstring from page is null " + uRL);
                        globalClient = buildHttpClient();
                        endInterval = true;
                        continue;
                    }

                    htmlStringFromPage = htmlStringFromPage.toString();
                    int resultSectionPos = htmlStringFromPage.indexOf("results-section");
                    String resultListHMTLData = null;
                    if (resultSectionPos == -1) {
                        log("Error getting results-section on page " + page);
                        log(htmlStringFromPage);
                        resultListHMTLData = htmlStringFromPage;
                    } else {
                        resultListHMTLData = htmlStringFromPage.substring(resultSectionPos);
                    }

                    String[] allHrefsOnPage = StringUtils.substringsBetween(resultListHMTLData, "<a href", "</a>");
                    if (allHrefsOnPage == null) { //todo check
                        System.out.println("this page has no Hrefs !!! " + allHrefsOnPage);
                        log("this page has no Hrefs !!!" + allHrefsOnPage);
                        endInterval = true;
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
                    int productsOnPage = productsURLArrayList.size();
                    if (productsOnPage < 48) {
                        endInterval = true;
                    }

                    int idPos1, idPos2 = 0;
                    for (String productUrl : productsURLArrayList) {
                        incrementGlobalProductCount();
                        //log(productUrl);
                        //System.out.println(productUrl);

                        idPos1 = productUrl.indexOf(ARTICLE_PREFIX);
                        idPos2 = idPos1 + 13;
                        String productId = productUrl.substring(idPos1, idPos2);

                        int initPoint = resultListHMTLData.indexOf(productUrl);
                        int nextPoint = resultListHMTLData.length();//just for the last item #48 o #50 depending on the page layout

                        String productHTMLdata = null;
                        int nextItem = productsURLArrayList.indexOf(productUrl) + 1;
                        if (nextItem < productsURLArrayList.size()) {
                            String nextURL = productsURLArrayList.get(nextItem);
                            nextPoint = resultListHMTLData.indexOf(nextURL);
                        }

                        productHTMLdata = resultListHMTLData.substring(initPoint, nextPoint);
                        if (productHTMLdata != null) {
                            productHTMLdata = productHTMLdata.toString(); //aca le sacamos los caracteres de control que impiden hacer los search dentro del string
                        }

                        int titlePos1 = productHTMLdata.indexOf("main-title\">") + 12;
                        int titlePos2 = productHTMLdata.indexOf("<", titlePos1);
                        String title = productHTMLdata.substring(titlePos1, titlePos2);
                        if (title != null) {
                            title = title.trim();
                        } else {
                            log(runnerID+" null title on page " + page + " url " + uRL);
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
                            log(runnerID+" I couldn't get the price on " + productUrl);
                            log(e);
                        }

                        int totalSold = 0;
                        boolean isUsed = false;
                        if (productHTMLdata.indexOf("Usado") > 0) {
                            isUsed = true;
                        }

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
                                                if (!isUsed) {
                                                    log("I couldn't get total sold on " + productUrl);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }


                        if (totalSold == 0 && isUsed && page < 100) {

                            if (!VISIT_PRODUCT_PAGE) {
                                continue;  //just ignore this item
                            }
                            //get total sold from product page
                            int retries = 1;
                            boolean retry = true;

                            String currentUrl=null;
                            try{
                                currentUrl=webDriver.getCurrentUrl();
                            }catch(Exception e){
                                log(runnerID+" something went wrong trying to get the webdriver current url " + productUrl);
                                log(e);
                            }

                            if (currentUrl==null || !currentUrl.equals(productUrl)) {
                                while (retry && retries <7) {
                                    retries++;
                                    try {
                                        webDriver.navigate().to(productUrl);
                                    } catch (Exception e) {
                                        log(runnerID+" something went wrong trying to reach url(i) " + productUrl);
                                        log(e);
                                    }
                                    int pageLenth=0;
                                    try {
                                        pageLenth=webDriver.getPageSource().length();
                                    } catch (Exception e) {
                                        log("something went wrong trying to get page lengh " + productUrl);
                                        log(e);
                                    }

                                    if (pageLenth > 300) {
                                        retry = false;
                                    } else {
                                        log(runnerID+" retry #" + retries + " on url to get seller " + productUrl);
                                        try {
                                            Thread.sleep(5000*retries);//aguantamos los trapos 5 segundos antes de reintentar
                                        } catch (InterruptedException e) {
                                            log(e);
                                        }
                                        if (retries==6){
                                            webDriver=getWebDriver(); //after 5 retries I reset the driver
                                        }
                                    }
                                }
                            }

                            String currentUrl2=null;
                            try {
                                currentUrl2=webDriver.getCurrentUrl();
                            } catch (Exception e) {
                                log(runnerID+ "something went wrong trying to get page lengh " + productUrl);
                                log(e);
                            }


                            if (currentUrl2!=null && currentUrl2.equals(productUrl)) {
                                String totalSoldStr = null;

                                WebElement webElement = null;

                                try {
                                    webElement = webDriver.findElement(By.className("item-conditions"));

                                    if (webElement != null) {
                                        totalSoldStr = webElement.getText();
                                        if (totalSoldStr != null) {
                                            totalSoldStr = totalSoldStr.trim();
                                        }
                                    }

                                } catch (Exception e) {
                                    log(runnerID+" something went wrong trying to get the seller (i) in " + productUrl);
                                    log(e);
                                }

                                int totalSoldPos1 = 0;
                                int totalSoldPos2 = 0;

                                try {
                                    totalSoldPos1 = totalSoldStr.indexOf("-") + 1;
                                    totalSoldPos2 = totalSoldStr.indexOf("vendido");
                                }catch (Exception e){
                                    System.out.println(runnerID+" NPE in totalSoldStr"+productUrl);
                                }

                                if (totalSoldPos1 > 0 && totalSoldPos2 > 0) {
                                    totalSoldStr = totalSoldStr.substring(totalSoldPos1, totalSoldPos2);
                                    if (totalSoldStr != null) {
                                        totalSoldStr = totalSoldStr.trim();
                                    }
                                    try {
                                        totalSold = Integer.parseInt(totalSoldStr);
                                    } catch (Exception e) {
                                        log(runnerID+" I couldn't get total sold on product page for " + productUrl);
                                        log(e);
                                    }
                                }
                            }
                        }

                        ///////////////////////// fin get total sold from product page


                        int reviews = 0;
                        double stars = 0.0;
                        int reviewsPos1 = productHTMLdata.indexOf("reviews-total\">");
                        if (reviewsPos1 >= 0) {// puede ser que no tenga reviews
                            reviewsPos1 += 15;
                            int reviewsPos2 = productHTMLdata.indexOf("<", reviewsPos1);
                            String reviewsStr = productHTMLdata.substring(reviewsPos1, reviewsPos2);
                            if (reviewsStr != null) {
                                reviewsStr = reviewsStr.trim();
                                if (reviewsStr.length() > 0) {
                                    try {
                                        reviews = Integer.parseInt(reviewsStr);
                                    } catch (NumberFormatException e) {
                                        log(runnerID+" I couldn't get reviews on " + reviewsStr);
                                        log(e);
                                    }
                                    String allStarsStr = StringUtils.substringBetween(productHTMLdata, "<div class=\"stars\">", "<div class=\"item__reviews-total\">");
                                    if (allStarsStr != null) {
                                        stars = allStarsStr.split("star-icon-full").length - 1 * 1.0;
                                        boolean halfStar = allStarsStr.indexOf("star-icon-half") > 0;
                                        if (halfStar) {
                                            stars += 0.5;
                                        }
                                    }
                                }
                            }
                        }

                        String msg = runnerID+" processing page " + page + " | " + productId + " | " + totalSold + " | " + reviews + " | " + stars + " | " + price + " | " + title + " | " + productUrl;
                        System.out.println(msg);

                        if (totalSold >= MINIMUM_SALES) { //si no figura venta no le doy bola
                            Date lastUpdate = lastUpdate(productId);

                            if (lastUpdate != null) {//producto existente
                                if (!ONLY_ADD_NEW_PRODUCTS) { //ignora los updates
                                    boolean sameDate = isSameDate(lastUpdate, getGlobalDate());
                                    if (!sameDate || (sameDate && OVERRIDE_TODAYS_RUN)) { //actualizar
                                        int previousTotalSold = getTotalSold(productId);
                                        if (totalSold != previousTotalSold) { //actualizar
                                            int newSold = totalSold - previousTotalSold;
                                            int newQuestions = 0;
                                            globalNewsCount++;

                                            if (VISIT_PRODUCT_PAGE) {//details
                                                String previousLastQuestion = getLastQuestion(productId);
                                                int retries = 1;
                                                boolean retry = true;

                                                if (!webDriver.getCurrentUrl().equals(productUrl)) {
                                                    while (retry && retries < 7) {
                                                        retries++;
                                                        try {
                                                            webDriver.navigate().to(productUrl);
                                                        } catch (Exception e) {
                                                            log(runnerID+" something went wrong trying to reach url(ii) " + productUrl);
                                                            log(e);
                                                        }
                                                        if (webDriver.getPageSource().length() > 300) {
                                                            retry = false;
                                                        } else {
                                                            log(runnerID+" retry #" + retries + " on url to get allquestions " + productUrl);
                                                            try {
                                                                Thread.sleep(5000*retries);//aguantamos los trapos 5 segundos antes de reintentar
                                                            } catch (InterruptedException e) {
                                                                log(e);
                                                            }
                                                            if (retries==6){
                                                                webDriver=getWebDriver(); //after 5 retries I reset the driver
                                                            }
                                                        }
                                                    }
                                                }
                                                if (webDriver.getCurrentUrl().equals(productUrl)) {
                                                    try {
                                                        WebElement moreQuestions = webDriver.findElement(By.id("showMoreQuestionsButton"));
                                                        boolean modalIframe = false;
                                                        while (moreQuestions != null && moreQuestions.isEnabled() && moreQuestions.isDisplayed() && !modalIframe) {
                                                            moreQuestions.click();
                                                            moreQuestions = webDriver.findElement(By.id("showMoreQuestionsButton"));
                                                            if (CHROME) {  // en firefox no hace falta chequear esto
                                                                modalIframe = existsElement(webDriver, "modal-iframe");
                                                            }
                                                        }
                                                    } catch (ElementClickInterceptedException e) {
                                                        //fin de la lista
                                                    } catch (NoSuchElementException e) {
                                                        //fin de la lista
                                                    } catch (Exception e) {
                                                        log(runnerID+" something went wrong trying to get the questions" + productUrl);
                                                        log(e);
                                                    }

                                                    List<String> questions = new ArrayList<String>();

                                                    List<WebElement> allLis = webDriver.findElements(By.tagName("li"));

                                                    for (WebElement li : allLis) {
                                                        WebElement div = null;
                                                        try {
                                                            div = li.findElement(By.className("questions__content"));
                                                        } catch (NoSuchElementException e) {
                                                            continue;
                                                        }
                                                        catch (Exception e) {
                                                            log(runnerID+" something went wrong trying to get the seller questions II" + productUrl);
                                                            log(e);
                                                            break;
                                                        }
                                                        if (div != null) {
                                                            String innerHTML =null;
                                                            try {
                                                                innerHTML = div.getAttribute("innerHTML");
                                                            }
                                                            catch (Exception e) {
                                                                log(runnerID+" something went wrong trying to get the seller questions III" + productUrl);
                                                                log(e);
                                                                break;
                                                            }
                                                            if (innerHTML != null && innerHTML.length() > 0) {
                                                                String trimmedInnerHTML = innerHTML.trim();
                                                                if (trimmedInnerHTML.length() > 0 && trimmedInnerHTML.indexOf("<p>") >= 0) {
                                                                    String question = StringUtils.substringBetween(trimmedInnerHTML, "<p>", "</p>");
                                                                    if (question != null && question.length() > 0) {
                                                                        questions.add(question.trim());
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    if (previousLastQuestion != null && previousLastQuestion.length() > 0 && questions != null && questions.size() > 0) {
                                                        if (questions.contains(previousLastQuestion)) {
                                                            newQuestions = questions.indexOf(previousLastQuestion);
                                                        }
                                                    }
                                                }
                                            }
                                            msg = runnerID+" new sale. productID: " + productId + " quantity: " + newSold;
                                            System.out.println(msg);
                                            log(msg);
                                            if (SAVE) {
                                                updateProductAddActivity(productId, totalSold, newSold, title, productUrl, reviews, stars, price, newQuestions, page);
                                            }
                                        }
                                    }
                                }
                            } else { //agregar vendedor
                                boolean officialStore = false;
                                String latestQuestion = null;
                                String seller = null;
                                globalNewsCount++;
                                if (VISIT_PRODUCT_PAGE) {
                                    int retries = 1;
                                    boolean retry = true;

                                    if (!webDriver.getCurrentUrl().equals(productUrl)) {
                                        while (retry && retries < 7) {
                                            retries++;
                                            try {
                                                webDriver.navigate().to(productUrl);
                                            } catch (Exception e) {
                                                log(runnerID+" something went wrong trying to reach url(i) " + productUrl);
                                                log(e);
                                            }
                                            if (webDriver.getPageSource().length() > 300) {
                                                retry = false;
                                            } else {
                                                log(runnerID+" retry #" + retries + " on url to get seller " + productUrl);
                                                try {
                                                    Thread.sleep(5000*retries);//aguantamos los trapos 5 segundos antes de reintentar
                                                } catch (InterruptedException e) {
                                                    log(e);
                                                }
                                                if (retries==6){
                                                    webDriver=getWebDriver(); //after 5 retries I reset the driver
                                                }
                                            }
                                        }
                                    }

                                    if (webDriver.getCurrentUrl().equals(productUrl)) { //estamos en la url correcta osea lo anterior no falló
                                        ///////////////////////////////////////////////////////// buscando al vendedor
                                        WebElement webElement = null;
                                        String sellerStr = null;

                                        if (BRAZIL) {
                                            try { //esto es nuevo para brasil !!!
                                                webElement = webDriver.findElement(By.xpath("//a[contains(text(), 'Entend')]"));
                                                if (webElement != null && webElement.isDisplayed()) {
                                                    webElement.click();
                                                }
                                            } catch (NoSuchElementException se) {
                                                // esta bien! lo normal es que se vaya por aca
                                            } catch (Exception e) {
                                                log(runnerID+" something went wrong trying to get the seller (i) in " + productUrl);
                                                log(e);
                                            }
                                            webElement = null; //new !
                                        }


                                        try {
                                            webElement = webDriver.findElement(By.partialLinkText(SEE_SELLER_DETAILS_LABEL));
                                            sellerStr = webElement.getAttribute("href");
                                            if (sellerStr == null || sellerStr.length() == 0) {
                                                System.out.println("Seller couldn't be found I " + productUrl);
                                            }
                                            int pos = sellerStr.lastIndexOf("/");
                                            if (pos == -1) {
                                                log(runnerID+" seller couldn't be found II " + productUrl);
                                            }
                                            pos++;
                                            seller = sellerStr.substring(pos);

                                        } catch (NoSuchElementException noSuchElementException) { //tienda oficial
                                            officialStore = true;
                                        } catch (Exception e) {
                                            log(runnerID+" something went wrong trying to get the seller (i) in " + productUrl);
                                            log(e);
                                            //return null; seller will remain null
                                        }

                                        if (officialStore) {
                                            try {
                                                webElement = webDriver.findElement(By.className("store-header__logo-name"));
                                                seller = webElement.getText();
                                                if (seller == null || seller.length() == 0) {
                                                    log(runnerID+" something went wrong trying to get the seller (iii) in " + productUrl);
                                                }
                                            } catch (Exception e) {
                                                log(runnerID+ "something went wrong trying to get the seller (iV) in " + productUrl);
                                                log(e);
                                            }
                                        }


                                        seller = unFormatSeller(seller);
                                        List<WebElement> allLis = webDriver.findElements(By.tagName("li"));
                                        for (WebElement li : allLis) {
                                            WebElement div = null;
                                            try {
                                                div = li.findElement(By.className("questions__content"));
                                            } catch (NoSuchElementException e) {
                                                continue;
                                            }
                                            catch (Exception e) {
                                                log(runnerID+" something went wrong trying to get the seller questions " + productUrl);
                                                log(e);
                                                break;
                                            }
                                            if (div != null) {
                                                String innerHTML = div.getAttribute("innerHTML");
                                                if (innerHTML != null && innerHTML.length() > 0) {
                                                    String trimmedInnerHTML = innerHTML.trim();
                                                    if (trimmedInnerHTML.length() > 0 && trimmedInnerHTML.indexOf("<p>") >= 0) {
                                                        String question = StringUtils.substringBetween(trimmedInnerHTML, "<p>", "</p>");
                                                        if (question != null && question.length() > 0) {
                                                            latestQuestion = question.trim();
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                msg = runnerID+" new product ID: " + productId + " Total Sold: " + totalSold;
                                System.out.println(msg);
                                log(msg);
                                if (SAVE) {
                                    insertProduct(productId, seller, totalSold, latestQuestion, productUrl, officialStore);
                                }
                            }
                        }
                    }
                }
            }
        }


        System.out.println(globalPageCount+" paginas procesadas ");
        System.out.println(globalProdutCount+" productos procesados ");
        System.out.println(globalNewsCount+" productos con novedades ");
        log(globalPageCount+" paginas procesadas "+globalProdutCount+" productos procesados ");

        System.out.println("Este es el fin");
    }

    private static String unFormatSeller(String seller) {
        try {//decode seller url
            seller = URLDecoder.decode(seller, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log("something went wrong trying to decode the seller " + seller);
            log(e);
        }
        return seller;
    }

    private static WebDriver getWebDriver() {
        WebDriver webDriver=null;
        if (VISIT_PRODUCT_PAGE) {
            if (FIREFOX) {
                FirefoxBinary firefoxBinary = new FirefoxBinary();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (SILENT) {
                    firefoxBinary.addCommandLineOptions("--headless");
                    firefoxOptions.setBinary(firefoxBinary);
                }
                if (!LOAD_IMAGES) {
                    firefoxOptions.addPreference("permissions.default.image", 2);
                }
                webDriver = new FirefoxDriver(firefoxOptions);
            }

            if (CHROME) {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--disable-extensions");
                if (SILENT) {
                    options.addArguments("--headless");
                }
                if (!LOAD_IMAGES) {
                    HashMap<String, Object> images = new HashMap<String, Object>();
                    images.put("images", 2);
                    HashMap<String, Object> prefs = new HashMap<String, Object>();
                    prefs.put("profile.default_content_setting_values", images);
                    options.setExperimentalOption("prefs", prefs);
                }
                webDriver = new ChromeDriver(options);
            }
        }
        return webDriver;
    }

    private String getHTMLStringFromPage(String uRL) {

        CloseableHttpClient client = getClient();
        HttpGet httpGet = new HttpGet(uRL);
        CloseableHttpResponse response= null;

        int retries=0;
        boolean retry=true;
        int statusCode=0;

        while (retry && retries<5) {
            retries++;
            try {
                response = client.execute(httpGet);
            } catch (IOException e) {
                response=null;
                log("Error en getHTMLStringFromPage");
                log(e);
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
                    Thread.sleep(5000);//aguantamos los trapos 5 segundos antes de reintentar
                } catch (InterruptedException e) {
                    log(e);
                }
            }/// todo fin
        }



        if (statusCode!=200){
            log("new status code "+statusCode);
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


    // convert InputStream to String
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

    private static synchronized boolean isSameDate(Date date1, Date date2){
        globalCalendar1.setTime(date1);
        globalCalendar2.setTime(date2);
        boolean sameDay = globalCalendar1.get(Calendar.YEAR) == globalCalendar2.get(Calendar.YEAR) &&
                globalCalendar1.get(Calendar.DAY_OF_YEAR) == globalCalendar2.get(Calendar.DAY_OF_YEAR);
        return sameDay;
    }


    private static boolean existsElement(WebDriver driver, String id) {
        try {
            driver.findElement(By.id(id));
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

}
