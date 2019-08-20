package com.ml;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.net.URLDecoder;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;


/**
 * Created by Muzzu on 11/12/2017.
 */

public class MercadoLibre07 extends Thread {

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
            "tucuman/",
            "salta/",
            "rio-negro/",
            "neuquen/",
            "chaco/"};

    static String[] globalUrlList =null;
    static boolean finished=false;

    static int globalPageCount=0;
    static int globalProdutCount=0;
    static int globalNewsCount=0;
    static int globalRunnerCount;
    static int globalAndFirstInterval;
    static int globalUrlCount;

    static Connection globalSelectConnection=null;
    static Connection globalUpadteConnection=null;
    static Connection globalAddProductConnection=null;
    static Connection globalAddActivityConnection =null;
    static Date globalDate=null;
    static BufferedWriter globalLogger=null;
    static DateFormat globalDateformat=null;
    static Calendar globalCalendar1=null;
    static Calendar globalCalendar2=null;

    static int MAX_THREADS=14;
    static boolean OVERRIDE_TODAYS_RUN=false;
    static boolean SAVE =true;
    static boolean FOLLOWING_DAY = false;
    static boolean PRERVIOUS_DAY = false;
    static boolean ONLY_ADD_NEW_PRODUCTS=false;
    static int MINIMUM_SALES=1;
    static String DATABASE="ML1";
    static int TIMEOUT_MIN=10;


    //regional settings
    static String ARTICLE_PREFIX="MLA";
    static String PROFILE_BASE_URL ="https://perfil.mercadolibre.com.ar/";
    static String QUESTIONS_BASE_URL ="https://articulo.mercadolibre.com.ar/noindex/questions/";
    static String OFICIAL_STORE_LABEL ="Tienda oficial de Mercado Libre";
    static String INTEREST_FREE_PAYMENTS_LABEL="cuotas sin interés";


    static String SHIPPING1_LABEL="Envío a todo el país";
    static String SHIPPING2_LABEL="Llega el";
    static String SHIPPING3_LABEL="Llega mañana";
    static String FREE_SHIPPING1_LABEL="Envío gratis a todo el país";
    static String FREE_SHIPPING2_LABEL="Llega gratis el";
    static String FREE_SHIPPING3_LABEL="Llega gratis mañana";


    static boolean BRAZIL=false;

    static PreparedStatement globalInsertProduct = null;
    static PreparedStatement globalInsertActivity = null;
    static PreparedStatement globalRemoveActivity = null;
    static PreparedStatement globalUpdateProduct = null;
    static PreparedStatement globalSelectProduct = null;
    static PreparedStatement globalSelectTotalSold = null;
    static PreparedStatement globalSelectLastQuestion = null;
    static String globalBaseURL=null;
    static int[] golbalIntervals = null;

    static String[] urls = new String[]
            {
            "https://hogar.mercadolibre.com.ar/sala-estar-comedor/[_SUBINTERVAL]",
            "https://listado.mercadolibre.com.ar/industrias-oficinas/equipamiento-oficinas/",
            "https://hogar.mercadolibre.com.ar/muebles-oficinas/",
            "https://hogar.mercadolibre.com.ar/cocina/amoblamientos/",
            "https://hogar.mercadolibre.com.ar/jardines-exteriores/muebles-de-jardin/",
            "https://hogar.mercadolibre.com.ar/banos/muebles/",

            "https://listado.mercadolibre.com.ar/perchero",
            "https://listado.mercadolibre.com.ar/cesto",
            "https://listado.mercadolibre.com.ar/ordenador-de-publico",
            "https://listado.mercadolibre.com.ar/paraguero",
            "https://listado.mercadolibre.com.ar/_CustId_241751796",  //acacia
            "https://listado.mercadolibre.com.ar/_CustId_233230004",  //misionlive
            "https://listado.mercadolibre.com.ar/_CustId_191605678"}; //primero+uno



    static int[][] intervals = new int[][] {{0,99,149,240,320,399,450,530,599,660,700,790,800,890,900,950,999,1000,
            1100,1150,1199,1200,1290,1300,1390,1400,1450,1499,1500,1590,1600,1690,1700,1790,1800,1890,1960,1998,1999,
            2000,2100,2200,2300,2400,2499,2500,2600,2700,2800,2900,2999,3000,3100,3200,3300,3399,3499,3500,3700,3800,
            3900,3999,4000,4200,4350,4499,4500,4799,4900,4999,5000,5400,5500,5800,5999,6000,6400,6500,6800,6998,7000,
            7400,7500,7800,7998,8000,8498,8500,8998,9000,9400,9800,9999,10001,10500,11000,11500,11997,11999,12001,12900,
            13500,14000,14900,15000,16000,17000,18000,19000,20000,22000,24000,26000,29000,35000,45000,2147483647}, //sala de estar comedor

            {0,69,119,199,290,375,475,549,660,799,899,999,1150,1300,1499,1650,1850,2000,2350,2590,2990,3350,3800,4350,
             4999,5900,6999,8100,9700,12000,17000,26000,45000,2147483647}, // Equipamiento Oficina

            {0,380,610,899,1100,1400,1690,1990,2300,2750,3250,3950,4800,5900,7500,9800,14000,22000,2147483647}, // Muebles Oficina

            {0,450,900,1299,1600,1990,2350,2750,3190,3700,4300,5000,6000,7000,8000,9600,11500,15000,2147483647}, // Cocina

            {0,440,700,990,1299,1698,2050,2600,3300,4400,6000,9000,14000,20000,35000,2147483647}, // Jardin

            {0,1200,2300,3500,5000,8000,14000,2147483647}, // Baño

            {0,200,350,500,740,1000,1550,2500,6000,2147483647}, // Perchero

            {0,250,550,950,1500,2800,2147483647}, // Cesto

            {0,700,2147483647}, // Ordenador de Publico

            {0,700,2147483647}, // Paragüero

            {0,700,2147483647}, // Acacia

            {0,700,2147483647}, // Misionlive

            {0,700,2147483647}  // Primero Uno

    };

    private static void initVars(){
        globalUrlCount=0;
        globalRunnerCount=0;
        globalAndFirstInterval =0; //arranca en 0
        for (int i=0; i<globalPageArray.length; i++) {
            globalPageArray[i]=0;
        }
    }

    private static CloseableHttpClient buildHttpClient() {
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
                        .build();
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

    private static synchronized void log(String string){
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


    private static String[] getPreviousDaysProductsURLs(int days) {
        String uRL=null;
        PreparedStatement preparedStatement = null;
        ArrayList<String> uRLs=new ArrayList<String>();


        long oneDayinMiliseconds=86400000;
        globalDate=new Date(getGlobalDate().getTime()-oneDayinMiliseconds*3);//TODO SACAR !!!!!!!!!!!!!!!!!!!!!!!!!!!!
        long globalDateMilliseconds=getGlobalDate().getTime();
        Date daysBeforeDate = new Date(globalDateMilliseconds-oneDayinMiliseconds*days);


        if (preparedStatement==null){
            Connection connection = getSelectConnection();
            try {
                String queryStr="select url from productos " +
                        "where id in (" +
                        "select idproducto from movimientos where fecha > ? and idproducto not in" +
                        "(select idproducto from movimientos where fecha = ?) group by idproducto)";
                preparedStatement=connection.prepareStatement(queryStr);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            preparedStatement.setDate(1,daysBeforeDate);
            preparedStatement.setDate(2,globalDate);


            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet==null){
                log("getPreviousDaysProductsURLs() Couldn't get URLs");
            }
            while (resultSet.next()){
                uRL=resultSet.getString(1);
                if (!uRLs.contains(uRL)){
                    uRLs.add(uRL);
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (String[]) uRLs.toArray();
    }


    public static void main(String[] args) {

        System.setProperty("webdriver.chrome.driver","C:\\Users\\Muzzu\\IdeaProjects\\selenium\\chromedriver_win32_2.4\\chromedriver.exe");
        System.setProperty("webdriver.gecko.driver", "C:\\Users\\Muzzu\\IdeaProjects\\selenium\\geckodriver-v0.20.0-win64\\geckodriver.exe");

        globalDateformat = new SimpleDateFormat("HH:mm:ss.SSS");
        globalCalendar1 = Calendar.getInstance();
        globalCalendar2 = Calendar.getInstance();


        globalUrlList = getPreviousDaysProductsURLs(25);
/*
        CloseableHttpClient httpClient = buildHttpClient();


        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
        for (String url: globalUrlList) { //todo tiene que empezar de 0


            MercadoLibre07 thread1 = new MercadoLibre07();


            int retries = 0;
            boolean retry=true;

            String htmlStringFromPage =null;

            while (retry && retries<4) {
                retries++;
                htmlStringFromPage = thread1.getHTMLStringFromPage(url,httpClient);
                if (htmlStringFromPage!=null){
                    retry=false;
                }else {
                    try {
                        httpClient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    httpClient=null;
                    httpClient = buildHttpClient();
                    log("R000 Error en getHTMLStringFromPage II intento #"+retries+" "+url);
                    try {
                        Thread.sleep(25000);
                    } catch (InterruptedException e) {
                        log(e);
                    }
                }
            }
            if (htmlStringFromPage == null) { //suponemos que se terminó
                // pero tambien hacemos pausa por si es problema de red
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    log(e);
                }
                log("R000 hmlstring from page is null " + url);
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                httpClient=null;
                httpClient = buildHttpClient();
                continue;
            }

            int pos1=htmlStringFromPage.indexOf("Publicación pausada.");
            if (pos1!=-1){
                boolean b = false;
            }

            long currentTime=System.currentTimeMillis();
            long timeoutTime=currentTime+TIMEOUT_MIN*60l*1000l;


            while (MAX_THREADS<Thread.activeCount()){

                try {
                    Thread.sleep(30000l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentTime=System.currentTimeMillis();
                if (currentTime>timeoutTime){
                    System.out.println("Error en de timeout.  Demasiado tiempo sin terminar de procesar uno entre "+MAX_THREADS+" vendedores");
                    System.exit(0);
                }
            }
        }

        for (Thread thread : threadArrayList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
*/


        for (String url: globalUrlList) { //todo tiene que empezar de 0
            initVars();
            int numberOfThreads=MAX_THREADS;
            String msg = "()()()()()()()() ++++++  Processing new Category with "+numberOfThreads+" threads";
            System.out.println(msg);
            log(msg);

            ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
            for (int i = 0; i < numberOfThreads; i++) {
                MercadoLibre07 thread = new MercadoLibre07();
                threadArrayList.add(thread);
                thread.start();
            }
            for (Thread thread : threadArrayList) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        String msg = globalPageCount+" paginas procesadas\n "
                    +globalProdutCount+" productos procesados\n "
                    +globalNewsCount+" productos con novedades";
        System.out.println(msg);
        log(msg);

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

    private static synchronized void updateProductAddActivity(String productId, String seller, boolean officialStore, int totalSold, int newSold, String title, String url, int feedbacksTotal, double feedbacksAverage, double price, int newQuestions, String lastQuestion, int pagina, int shipping, int discount, boolean premium) {
        Connection connection = getAddActivityConnection();
        try{
            if (globalUpdateProduct ==null) {
                globalUpdateProduct = connection.prepareStatement("UPDATE public.productos SET totalvendidos = ?, lastupdate=?, url=?, lastquestion=?, proveedor=?, tiendaoficial=? WHERE id = ?;");
            }

            globalUpdateProduct.setInt(1,totalSold);
            globalUpdateProduct.setDate(2,getGlobalDate());
            globalUpdateProduct.setString(3,url);
            globalUpdateProduct.setString(4,lastQuestion);
            globalUpdateProduct.setString(5,seller);
            globalUpdateProduct.setBoolean(6,officialStore);
            globalUpdateProduct.setString(7,productId);

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
                globalInsertActivity =connection.prepareStatement("INSERT INTO public.movimientos(fecha, idproducto, titulo, url, opinionestotal, opinionespromedio, precio, vendidos, totalvendidos, nuevaspreguntas, pagina, proveedor, tiendaoficial, envio, descuento, premium) VALUES (?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
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
            globalInsertActivity.setString(12,seller);
            globalInsertActivity.setBoolean(13,officialStore);

            globalInsertActivity.setInt(14,shipping);
            globalInsertActivity.setInt(15,discount);
            globalInsertActivity.setBoolean(16,premium);

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
        return globalAndFirstInterval;
    }

    synchronized private static String getNextUrl(){
        globalUrlCount++;
        if (globalUrlCount==globalUrlList.length){
            finished=true;
            return null;
        }
        return globalUrlList[globalUrlCount];
    }


    synchronized private static void incrementGlobalPageCount(){
        globalPageCount++;
    }

    synchronized private static void incrementGlobalNewsCount(){
        globalNewsCount++;
    }


    synchronized private static void incrementGlobalProductCount(){
        globalProdutCount++;
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

        CloseableHttpClient httpClient = buildHttpClient();
        int itemsInPage=48;
        String runnerID="R"+getGlobalRunnerCount();

        while (!finished){
            //Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
            String url = getNextUrl();
            if (url==null || finished){
                break;
            }
        }


        String msg="XXXXXXXXXXXXXX Este es el fin "+runnerID;
        System.out.println(msg);
        log(msg);
        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClient=null;
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

    private String getHTMLStringFromPage(String uRL, CloseableHttpClient client) {

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
                log("Error en getHTMLStringFromPage intento #"+retries+" "+uRL);
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
            log("XX new status code "+statusCode+" "+uRL);
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

}
