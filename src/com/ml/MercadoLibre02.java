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
import java.util.Properties;

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



/**
 * Created by Muzzu on 11/12/2017.
 */

public class MercadoLibre02 extends Thread {

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

    static int globalPageCount=0;
    static int globalProdutCount=0;
    static int globalNewsCount=0;
    static int globalRunnerCount;
    static int globalAndFirstInterval;

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
    static boolean SAVE =false;
    static boolean DEBUG=false;
    static boolean FOLLOWING_DAY = false;
    static boolean PRERVIOUS_DAY = false;
    static boolean ONLY_ADD_NEW_PRODUCTS=false;
    static int MINIMUM_SALES=1;
    static int TIMEOUT_MIN = 10;
    static int MAX_THREADS_VISITS = 30;


    static String DATABASE="ML2";

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
    static PreparedStatement globalUpdateVisits = null;
    static String globalBaseURL=null;
    static int[] golbalIntervals = null;

    static String[] urls = new String[]
            {"https://hogar.mercadolibre.com.ar/decoracion/[_SUBINTERVAL]",
            "https://listado.mercadolibre.com.ar/[_SUBINTERVAL]espejo",
            "https://listado.mercadolibre.com.ar/[_SUBINTERVAL]alfombra"};




    static int[][] intervals = new int[][] {
            /*
            {
            0,10,19,25,30,39,44,49,50,59,60,69,74,79,80,89,90,98,99,100,109,110,
            111,119,120,129,130,139,140,149,150,159,160,169,172,179,180,181,189,191,198,199,200,209,210,219,220,
            229,230,239,240,248,249,250,259,260,269,270,279,280,289,290,298,299,300,310,319,320,329,330,340,349,
            350,359,360,370,379,380,389,390,398,399,400,410,419,420,430,440,449,450,460,469,470,479,480,489,490,
            498,499,500,520,530,540,549,550,560,570,579,580,590,598,599,600,610,615,620,630,640,649,650,660,670,
            680,689,690,699,700,730,740,749,750,770,780,785,790,799,800,830,849,850,870,898,899,900,944,950,970,
            989,990,999,1000,1050,1090,1099,1100,1140,1180,1199,1200,1249,1270,1298,1300,1350,1399,1400,1450,1499,
            1500,1550,1599,1600,1649,1650,1698,1700,1750,1799,1800,1850,1898,1899,1900,1950,1998,1999,2000,2020,
            2100,2199,2250,2299,3000,3200,3400,3500,3700,3900,4000,4300,4600,4999,5000,5500,6000,6500,7000,8000,
            9000,10000,12000,15000,20000,30000,2147483647},*/

                    {0,11,19,26,34,39,44,49,50,59,63,69,74,79,80,89,94,98,99,100,109,114,119,120,129,133,139,144,149,150,158,
                            159,164,169,174,179,180,189,190,198,199,200,209,210,219,220,229,230,239,244,248,249,250,259,268,274,279,
                            280,289,294,298,299,300,318,324,332,344,349,350,360,374,379,382,389,394,398,399,400,419,428,436,449,450,
                            467,479,489,494,498,499,500,519,520,539,549,550,569,588,598,599,600,618,619,639,649,650,674,689,698,699,
                            700,733,749,750,769,789,799,800,839,849,851,882,898,899,900,942,959,989,998,999,1000,1049,1085,1099,1112,
                            1149,1189,1199,1200,1249,1284,1299,1307,1349,1389,1399,1423,1484,1499,1500,1559,1599,1631,1689,1699,1729,
                            1789,1799,1828,1890,1931,1998,1999,2022,2098,2140,2199,2276,2374,2458,2499,2500,2633,2760,2879,2999,3111,
                            3299,3499,3648,3939,4199,4499,4861,5268,5942,6623,7562,8799,10563,13998,19721,74999,2147483647},


                    {0,550,1000,1850,3300,15000,2147483647},

                    {0,300,700,1500,3100,9000,2147483647}

    };

    private static void initVars(){
        globalRunnerCount=0;
        globalAndFirstInterval =0; //arranca en 0
        for (int i=0; i<globalPageArray.length; i++) {
            globalPageArray[i]=0;
        }
    }

    protected static CloseableHttpClient buildHttpClient() {
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
            if (file.exists()) {
                try {
                    fileWriter = new FileWriter(file,true);//if file exists append to file. Works fine.
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
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

    protected static synchronized void log(String string) {
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

    protected static void log(Throwable throwable) {
        log(ExceptionUtils.getStackTrace(throwable));
    }


    private static void updateVisits() {

        String msg = "\nProcesando Visitas";
        System.out.println(msg);
        log(msg);

        Connection connection = getSelectConnection();
        ArrayList<String> allProductIDs = new ArrayList<String>();
        Date date1=null;
        Date date2=null;
        String dateOnQueryStr=null;
        try {

            PreparedStatement datesPreparedStatement = connection.prepareStatement("SELECT fecha FROM public.movimientos group by fecha order by fecha desc");
            ResultSet rs = datesPreparedStatement.executeQuery();
            if (rs == null) {
                msg = "Error getting dates";
                System.out.println(msg);
                log(msg);
            }
            if (!rs.next()) {
                msg = "Error getting dates II";
                System.out.println(msg);
                log(msg);
            }
            date2 = rs.getDate(1);
            if (!rs.next()) {
                msg = "Error getting dates III";
                System.out.println(msg);
                log(msg);
            }
            date1 = rs.getDate(1);

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String strDate1 = dateFormat.format(date1);
            String strDate2 = dateFormat.format(date2);
            dateOnQueryStr = "&date_from=" + strDate1 + "T00:00:00.000-00:00&date_to=" + strDate2 + "T23:59:00.000-00:00";


            PreparedStatement selectPreparedStatement = connection.prepareStatement("SELECT idproducto FROM public.movimientos WHERE fecha=?");
            selectPreparedStatement.setDate(1, date2);


            rs = selectPreparedStatement.executeQuery();
            if (rs == null) {
                msg = "Error getting latest movements "+date2;
                System.out.println(msg);
                log(msg);
            }

            while (rs.next()) {
                String productId = rs.getString(1);
                allProductIDs.add(productId);
            }


        } catch (SQLException e) {
            e.printStackTrace();
            log(e);
        }

        ArrayList<String> zeroVisitsList=processAllVisits(allProductIDs, date2, dateOnQueryStr);
        msg="Reintentando los ceros";
        System.out.println(msg);
        log(msg);
        zeroVisitsList=processAllVisits(zeroVisitsList, date2, dateOnQueryStr); //insistimos 2 veces mas cuando visitas devuelve cero
        System.out.println(msg);
        log(msg);
        processAllVisits(zeroVisitsList, date2, dateOnQueryStr);

        msg="Visitas Procesadas: "+allProductIDs.size();
        System.out.println(msg);
        log(msg);

    }


    private static ArrayList<String> processAllVisits(ArrayList<String> allProductIDs, Date date, String dateOnQuery) {
        int count = 0;

        ArrayList<String> fiftyProductIDs = new ArrayList<String>();
        ArrayList<Thread> threadArrayList = new ArrayList<Thread>(); //mover al anterior


        for (String productId : allProductIDs) {
            count++;

            fiftyProductIDs.add(productId);

            if (count >= 50) {
                process50Visits(date, dateOnQuery, fiftyProductIDs, threadArrayList);
                fiftyProductIDs = new ArrayList<String>();
                count = 0;
            }
        }
        if (fiftyProductIDs.size() > 0) {  //processing last record
            process50Visits(date, dateOnQuery, fiftyProductIDs, threadArrayList);
        }

        for (Thread thread : threadArrayList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        VisitCounter aVisitCounter = (VisitCounter)threadArrayList.get(0);

        //clone
        ArrayList<String> zeroVisitsList=new ArrayList<String>();
        for (String productIdWithZeroVisits: aVisitCounter.getZeroVisitsList()){
            zeroVisitsList.add(productIdWithZeroVisits);
        }
        aVisitCounter.resetZeroVisitsList();

        return zeroVisitsList;

    }

    private static void process50Visits(Date date, String dateOnQuery, ArrayList<String> fiftyProductIDs, ArrayList<Thread> threadArrayList) {
        long currentTime;
        long timeoutTime;

        VisitCounter visitCounter = new VisitCounter(fiftyProductIDs, date, dateOnQuery, SAVE, DEBUG);
        threadArrayList.add(visitCounter);
        visitCounter.start();
        currentTime = System.currentTimeMillis();
        timeoutTime = currentTime + TIMEOUT_MIN * 60l * 1000l;

        while (MAX_THREADS_VISITS < Thread.activeCount()) {

            try {
                Thread.sleep(10l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentTime = System.currentTimeMillis();
            if (currentTime > timeoutTime) {
                System.out.println("Error en de timeout.  Demasiado tiempo sin terminar de procesar uno entre " + MAX_THREADS_VISITS + " visitas");
                System.exit(0);
            }
        }
    }

    private static synchronized void updateVisits(String productId,int quantity, Date date){

        if (globalUpdateVisits ==null) {
            Connection connection = getUpdateConnection();
            try {
                globalUpdateVisits = connection.prepareStatement("update public.movimientos set visitas=? where idproducto=? and fecha =?");
                globalUpdateVisits.setDate(3,date);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            globalUpdateVisits.setInt(1,quantity);
            globalUpdateVisits.setString(2,productId);


            int updatedRecords=globalUpdateVisits.executeUpdate();
            globalUpdateVisits.getConnection().commit();

            if (updatedRecords!=1){
                log("Error updating visits "+productId+" "+ quantity + " " +date);
            }



        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        System.setProperty("webdriver.chrome.driver","C:\\Users\\Muzzu\\IdeaProjects\\selenium\\chromedriver_win32_2.4\\chromedriver.exe");
        System.setProperty("webdriver.gecko.driver", "C:\\Users\\Muzzu\\IdeaProjects\\selenium\\geckodriver-v0.20.0-win64\\geckodriver.exe");

        globalDateformat = new SimpleDateFormat("HH:mm:ss.SSS");
        globalCalendar1 = Calendar.getInstance();
        globalCalendar2 = Calendar.getInstance();

        for (int i=0; i<intervals.length; i++){ //validacion de intervalo
            for (int j=1; j<intervals[i].length; j++) {
                if (intervals[i][j - 1] >= intervals[i][j]) {
                    System.out.println("Error en intervalo #" + i + " // " + intervals[i][j - 1] + "-" + intervals[i][j]);
                    System.exit(0);
                }
            }
        }

        if (SAVE){
            saveRunInitialization(globalBaseURL,MAX_THREADS);
        }


        for (int j=0; j<urls.length; j++) { //todo tiene que empezar de 0
            initVars();
            globalBaseURL=urls[j];
            golbalIntervals=intervals[j];
            int numberOfThreads = (int) Math.round(intervals[j].length / 2.5);
            if (numberOfThreads>MAX_THREADS){
                numberOfThreads=MAX_THREADS;
            }
            String msg = "()()()()()()()() ++++++  Processing new Category with "+numberOfThreads+" threads";
            System.out.println(msg);
            log(msg);

            ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
            for (int i = 0; i < numberOfThreads; i++) {
                MercadoLibre02 thread = new MercadoLibre02();
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

        updateVisits();

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
                globalUpdateProduct = connection.prepareStatement("UPDATE public.productos SET totalvendidos = ?, lastupdate=?, url=?, lastquestion=?, proveedor=?, tiendaoficial=?, deshabilitado=false WHERE id = ?;");
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


    protected static Connection getUpdateConnection(){
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

        boolean processFinished=false;
        while (!processFinished){
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
            int interval=getInterval();
            if (interval>=golbalIntervals.length){
                processFinished=true;
                continue;
            }
            int range1=golbalIntervals[interval-1]+1;
            int range2=golbalIntervals[interval];
            String priceRangeStr="_PriceRange_"+range1+"-"+range2;
            log ("XXXXXXXXXXXXXXXXXXXXXX "+runnerID+" new interval : "+ interval+" "+priceRangeStr);
            String[] subintervals = new String[]{""};
            if (range1==range2){
                subintervals = globalSubIntervals;
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            }
            for (String subinterval : subintervals) {
                boolean endInterval = false;
                getPage(interval, true);
                while (!endInterval) {
                    int page = getPage(interval, false);
                    if (page == 43) {
                        log("XXXXXXXXXXXXXXXXXXXXXX este intervalo no pudo ser recorrido por completo "
                                + globalBaseURL + priceRangeStr);
                        System.out.println("XXXXXXXXXXXXXXXXXXXXXX este intervalo no pudo ser recorrido por completo "
                                + globalBaseURL + priceRangeStr);
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

                    System.out.println(runnerID+" "+uRL);
                    log(runnerID+" "+uRL);
                    String htmlStringFromPage = getHTMLStringFromPage(uRL,httpClient);
                    if (htmlStringFromPage == null) { //suponemos que se terminó
                        // pero tambien hacemos pausa por si es problema de red
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            log(e);
                        }
                        log(runnerID+" hmlstring from page is null " + uRL);
                        try {
                            httpClient.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        httpClient=null;
                        httpClient = buildHttpClient();
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

                        int discount = 0;
                        int discountPos1=productHTMLdata.indexOf("item__discount"); //procesando descuento opcional
                        if (discountPos1>0){ //optional field
                            discountPos1+=17;
                            int discountPos2=productHTMLdata.indexOf("%",discountPos1);
                            String discountStr = productHTMLdata.substring(discountPos1, discountPos2);
                            try {
                                discount = Integer.parseInt(discountStr);
                            } catch (NumberFormatException e) {
                                log(runnerID+" I couldn't get the discount on " + productUrl);
                                log(e);
                            }
                        }

                        int shipping=0;
                        if (productHTMLdata.indexOf(SHIPPING1_LABEL)>0){
                            shipping=1;
                        }else {
                            if (productHTMLdata.indexOf(FREE_SHIPPING1_LABEL)>0){
                                shipping=101;
                            }
                        }
                        //todo ENVIO EXPRESS
/*
                        boolean shipping2=productHTMLdata.indexOf(SHIPPING2_LABEL)>0;
                        boolean shipping3=productHTMLdata.indexOf(SHIPPING3_LABEL)>0;
                        boolean freeShipping2=productHTMLdata.indexOf(FREE_SHIPPING2_LABEL)>0;
                        boolean freeShipping3=productHTMLdata.indexOf(FREE_SHIPPING3_LABEL)>0;
*/

                        boolean premium=false;
                        if (productHTMLdata.indexOf(INTEREST_FREE_PAYMENTS_LABEL)>0) {
                            premium = true;
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

                        String htmlStringFromProductPage = null;
                        if (totalSold == 0 && isUsed) {
                            htmlStringFromProductPage = getHTMLStringFromPage(productUrl,httpClient);
                            if (htmlStringFromProductPage == null) {
                                // hacemos pausa por si es problema de red
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    log(e);
                                }
                                log(runnerID+" hmlstring from page 2 is null " + uRL);
                                try {
                                    httpClient.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                httpClient=null;
                                httpClient = buildHttpClient();
                                continue; //ignoramos este item
                            }else {
                                soldPos1 = htmlStringFromProductPage.indexOf("item-conditions\">") + 18;
                                if (soldPos1 > 18) {
                                    soldPos1 = htmlStringFromProductPage.indexOf("Usado")+5;
                                    if (soldPos1>5) {
                                        soldPos1=htmlStringFromProductPage.indexOf("&nbsp;-&nbsp;", soldPos1)+13;
                                        if (soldPos1>13) {
                                            int soldPos2 = htmlStringFromProductPage.indexOf("vendido", soldPos1);
                                            if (soldPos2 > 0) {
                                                String soldStr = htmlStringFromProductPage.substring(soldPos1, soldPos2);
                                                if (soldStr != null) {
                                                    soldStr = soldStr.trim();
                                                    try {
                                                        totalSold = Integer.parseInt(soldStr);
                                                    } catch (NumberFormatException e) {
                                                        log("I couldn't get total sold on " + productUrl);
                                                        continue;
                                                    }
                                                }
                                            }
                                        }
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

                                            if (htmlStringFromProductPage == null) {
                                                htmlStringFromProductPage = getHTMLStringFromPage(productUrl, httpClient);
                                                if (htmlStringFromProductPage == null) {
                                                    // hacemos pausa por si es problema de red
                                                    try {
                                                        Thread.sleep(5000);
                                                    } catch (InterruptedException e) {
                                                        log(e);
                                                    }
                                                    log(runnerID + " hmlstring from page 2 is null " + uRL);
                                                    try {
                                                        httpClient.close();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    httpClient = null;
                                                    httpClient = buildHttpClient();
                                                    continue;
                                                    //ignoramos este item
                                                }
                                            }

                                            boolean officialStore=false;
                                            int officialStorePos1=htmlStringFromProductPage.indexOf(OFICIAL_STORE_LABEL);
                                            if (officialStorePos1>=0){
                                                officialStore=true;
                                            }

                                            String seller=null;
                                            int sellerPos1,sellerPos2;
                                            if (officialStore){
                                                sellerPos1 = htmlStringFromProductPage.indexOf("official-store-info");
                                                if (sellerPos1>0){
                                                    sellerPos1=htmlStringFromProductPage.indexOf("title",sellerPos1);
                                                    sellerPos1+=7;
                                                    sellerPos2=htmlStringFromProductPage.indexOf("<",sellerPos1);
                                                    seller=htmlStringFromProductPage.substring(sellerPos1,sellerPos2);
                                                }
                                            }else {
                                                int sellerPos0 = htmlStringFromProductPage.indexOf("reputation-info");
                                                if (sellerPos0 > 0){
                                                    sellerPos1 = htmlStringFromProductPage.indexOf(PROFILE_BASE_URL,sellerPos0);
                                                    if (sellerPos1 > 0) {
                                                        sellerPos1 += 35;
                                                        sellerPos2 = htmlStringFromProductPage.indexOf("\"", sellerPos1);
                                                        if (sellerPos2 > 0) {
                                                            seller = htmlStringFromProductPage.substring(sellerPos1, sellerPos2);
                                                        }
                                                    }
                                                }
                                            }
                                            if (seller == null) {
                                                msg = "No se pudo encontrar al venedor " + productUrl;
                                                System.out.println(msg);
                                                log(msg);
                                            }else {
                                                seller= unFormatSeller(seller);
                                            }

                                            String lastQuestion=null;
                                            int lastQuestionPos1=htmlStringFromProductPage.indexOf("questions__content");
                                            if (lastQuestionPos1>0) {
                                                lastQuestionPos1 = htmlStringFromProductPage.indexOf("p>",lastQuestionPos1)+2;
                                                int lastQuestionPos2=htmlStringFromProductPage.indexOf("</p>",lastQuestionPos1);
                                                lastQuestion=htmlStringFromProductPage.substring(lastQuestionPos1,lastQuestionPos2);
                                                if (lastQuestion!=null){
                                                    lastQuestion=lastQuestion.trim();
                                                }
                                            }


                                            int newQuestions = 0;

                                            incrementGlobalNewsCount();

                                            String previousLastQuestion = getLastQuestion(productId);

                                            String questionsURL=QUESTIONS_BASE_URL+ARTICLE_PREFIX+productId.substring(4);
                                            String htmlStringFromQuestionsPage = getHTMLStringFromPage(questionsURL,httpClient);
                                            if (htmlStringFromQuestionsPage == null) {
                                                // hacemos pausa por si es problema de red
                                                try {
                                                    Thread.sleep(5000);
                                                } catch (InterruptedException e) {
                                                    log(e);
                                                }
                                                log(runnerID+" hmlstring from page 2 is null " + uRL);
                                                try {
                                                    httpClient.close();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                httpClient=null;
                                                httpClient = buildHttpClient();
                                                //endInterval = true;
                                                //continue;
                                            }else {//procesamos las preguntas
                                                String[] allquestionsOnPage = StringUtils.substringsBetween(htmlStringFromQuestionsPage, "<p>", "</p>");
                                                if (allquestionsOnPage != null) { //todo check
                                                    int i=0;
                                                    for (String question: allquestionsOnPage){
                                                        if (lastQuestion==null){
                                                            lastQuestion=question;
                                                        }
                                                        if (question.equals(previousLastQuestion)){
                                                            newQuestions=i;
                                                            break;
                                                        }
                                                        i++;
                                                    }
                                                }
                                            }

                                            msg = runnerID+" new sale. productID: " + productId + " quantity: " + newSold;
                                            System.out.println(msg);
                                            log(msg);
                                            if (SAVE) {
                                                updateProductAddActivity(productId, seller, officialStore, totalSold, newSold, title, productUrl, reviews, stars, price, newQuestions, lastQuestion, page, shipping, discount, premium);
                                            }
                                        }
                                    }
                                }
                            } else { //agregar vendedor

                                if (htmlStringFromProductPage == null) {
                                    htmlStringFromProductPage = getHTMLStringFromPage(productUrl, httpClient);
                                    if (htmlStringFromProductPage == null) {
                                        // hacemos pausa por si es problema de red
                                        try {
                                            Thread.sleep(5000);
                                        } catch (InterruptedException e) {
                                            log(e);
                                        }
                                        log(runnerID + " hmlstring from page 2 is null " + uRL);
                                        try {
                                            httpClient.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        httpClient = null;
                                        httpClient = buildHttpClient();
                                        continue;
                                        //ignoramos este item
                                    }
                                }

                                boolean officialStore=false;
                                int officialStorePos1=htmlStringFromProductPage.indexOf(OFICIAL_STORE_LABEL);
                                if (officialStorePos1>=0){
                                    officialStore=true;
                                }

                                String seller=null;
                                int sellerPos1,sellerPos2;
                                if (officialStore){
                                    sellerPos1 = htmlStringFromProductPage.indexOf("official-store-info");
                                    if (sellerPos1>0){
                                        sellerPos1=htmlStringFromProductPage.indexOf("title",sellerPos1);
                                        sellerPos1+=7;
                                        sellerPos2=htmlStringFromProductPage.indexOf("<",sellerPos1);
                                        seller=htmlStringFromProductPage.substring(sellerPos1,sellerPos2);
                                    }
                                }else {
                                    sellerPos1 = htmlStringFromProductPage.indexOf(PROFILE_BASE_URL);
                                    if (sellerPos1 > 0) {
                                        sellerPos1 += 35;
                                        sellerPos2 = htmlStringFromProductPage.indexOf("\"", sellerPos1);
                                        if (sellerPos2 > 0) {
                                            seller = htmlStringFromProductPage.substring(sellerPos1, sellerPos2);
                                        }
                                    }
                                }
                                if (seller == null) {
                                    msg = "No se pudo encontrar al venedor " + productUrl;
                                    System.out.println(msg);
                                    log(msg);
                                }else {
                                    seller= unFormatSeller(seller);
                                }

                                String lastQuestion=null;
                                int lastQuestionPos1=htmlStringFromProductPage.indexOf("questions__content");
                                if (lastQuestionPos1>0) {
                                    lastQuestionPos1 = htmlStringFromProductPage.indexOf("p>",lastQuestionPos1)+2;
                                    int lastQuestionPos2=htmlStringFromProductPage.indexOf("</p>",lastQuestionPos1);
                                    lastQuestion=htmlStringFromProductPage.substring(lastQuestionPos1,lastQuestionPos2);
                                    if (lastQuestion!=null){
                                        lastQuestion=lastQuestion.trim();
                                    }
                                }


                                incrementGlobalNewsCount();

                                msg = runnerID+" new product ID: " + productId + " Total Sold: " + totalSold;
                                System.out.println(msg);
                                log(msg);
                                if (SAVE) {
                                    insertProduct(productId, seller, totalSold, lastQuestion, productUrl, officialStore);
                                }
                            }
                        }
                    }
                }
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

    protected static String getHTMLStringFromPage(String uRL, CloseableHttpClient client) {

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
