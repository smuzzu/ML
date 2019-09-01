package com.ml;

import java.io.IOException;

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

import org.apache.http.impl.client.CloseableHttpClient;

import com.ml.utils.Counters;
import com.ml.utils.Logger;
import com.ml.utils.HttpUtils;
import com.ml.utils.HTMLParseUtils;
import com.ml.utils.ProductPageProcessor;


/**
 * Created by Muzzu on 11/12/2017.
 */

public class MercadoLibre02 extends Thread {

    MercadoLibre02(String baseURL, int[] intervals) {
        this.baseURL = baseURL;
        this.theIntervals = intervals;
    }

    static int[] globalPageArray = new int[]
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

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

    static int globalAndFirstInterval;
    static ArrayList<String> globalProcesedProductList;

    static Connection globalSelectConnection = null;
    static Connection globalUpadteConnection = null;
    static Connection globalAddProductConnection = null;
    static Connection globalAddActivityConnection = null;
    static Date globalDate = null;
    static DateFormat globalDateformat = null;
    static Calendar globalCalendar1 = null;
    static Calendar globalCalendar2 = null;

    static int MAX_THREADS = 18;//14
    static boolean OVERRIDE_TODAYS_RUN = false;
    static boolean SAVE = true;
    static boolean DEBUG = false;
    static boolean FOLLOWING_DAY = false;
    static boolean PRERVIOUS_DAY = true;
    static boolean ONLY_ADD_NEW_PRODUCTS = false;
    static int MINIMUM_SALES = 1;
    static int TIMEOUT_MIN = 10;
    static int MAX_THREADS_VISITS = 30;
    static String DATABASE = "ML2";

    static boolean BRAZIL = false;

    static PreparedStatement globalInsertProduct = null;
    static PreparedStatement globalInsertActivity = null;
    static PreparedStatement globalRemoveActivity = null;
    static PreparedStatement globalUpdateProduct = null;
    static PreparedStatement globalSelectProduct = null;
    static PreparedStatement globalSelectTotalSold = null;
    static PreparedStatement globalSelectLastQuestion = null;
    static PreparedStatement globalSelectPossiblyPaused = null;
    static PreparedStatement globalUpdateVisits = null;


    private String baseURL = null;
    private int[] theIntervals = null;

    static String[] urls = new String[]
            {"https://hogar.mercadolibre.com.ar/decoracion/[_SUBINTERVAL]",
                    "https://listado.mercadolibre.com.ar/[_SUBINTERVAL]espejo",
                    "https://listado.mercadolibre.com.ar/[_SUBINTERVAL]alfombra"};


    static int[][] intervals = new int[][]{
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

            {0, 11, 19, 26, 34, 39, 44, 49, 50, 59, 63, 69, 74, 79, 80, 89, 94, 98, 99, 100, 109, 114, 119, 120, 129, 133, 139, 144, 149, 150, 158,
                    159, 164, 169, 174, 179, 180, 189, 190, 198, 199, 200, 209, 210, 219, 220, 229, 230, 239, 244, 248, 249, 250, 259, 268, 274, 279,
                    280, 289, 294, 298, 299, 300, 318, 324, 332, 344, 349, 350, 360, 374, 379, 382, 389, 394, 398, 399, 400, 419, 428, 436, 449, 450,
                    467, 479, 489, 494, 498, 499, 500, 519, 520, 539, 549, 550, 569, 588, 598, 599, 600, 618, 619, 639, 649, 650, 674, 689, 698, 699,
                    700, 733, 749, 750, 769, 789, 799, 800, 839, 849, 851, 882, 898, 899, 900, 942, 959, 989, 998, 999, 1000, 1049, 1085, 1099, 1112,
                    1149, 1189, 1199, 1200, 1249, 1284, 1299, 1307, 1349, 1389, 1399, 1423, 1484, 1499, 1500, 1559, 1599, 1631, 1689, 1699, 1729,
                    1789, 1799, 1828, 1890, 1931, 1998, 1999, 2022, 2098, 2140, 2199, 2276, 2374, 2458, 2499, 2500, 2633, 2760, 2879, 2999, 3111,
                    3299, 3499, 3648, 3939, 4199, 4499, 4861, 5268, 5942, 6623, 7562, 8799, 10563, 13998, 19721, 74999, 2147483647},


            {0, 550, 1000, 1850, 3300, 15000, 2147483647},

            {0, 300, 700, 1500, 3100, 9000, 2147483647}

    };

    private static void initVars() {
        Counters.initGlobalRunnerCount();
        globalAndFirstInterval = 0; //arranca en 0
        for (int i = 0; i < globalPageArray.length; i++) {
            globalPageArray[i] = 0;
        }
    }

    private static void updateVisits(String database) {

        String msg = "\nProcesando Visitas";
        System.out.println(msg);
        Logger.log(msg);
        Counters.initGlobalRunnerCount();

        Connection connection = getSelectConnection(database);
        ArrayList<String> allProductIDs = new ArrayList<String>();
        Date date1 = null;
        Date date2 = null;
        String dateOnQueryStr = null;
        try {

            PreparedStatement datesPreparedStatement = connection.prepareStatement("SELECT fecha FROM public.movimientos group by fecha order by fecha desc");
            ResultSet rs = datesPreparedStatement.executeQuery();
            if (rs == null) {
                msg = "Error getting dates";
                System.out.println(msg);
                Logger.log(msg);
            }
            if (!rs.next()) {
                msg = "Error getting dates II";
                System.out.println(msg);
                Logger.log(msg);
            }
            date2 = rs.getDate(1);
            if (!rs.next()) {
                msg = "Error getting dates III";
                System.out.println(msg);
                Logger.log(msg);
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
                msg = "Error getting latest movements " + date2;
                System.out.println(msg);
                Logger.log(msg);
            }

            while (rs.next()) {
                String productId = rs.getString(1);
                allProductIDs.add(productId);
            }


        } catch (SQLException e) {
            e.printStackTrace();
            Logger.log(e);
        }

        ArrayList<String> zeroVisitsList = processAllVisits(allProductIDs, date2, dateOnQueryStr);
        msg = "Reintentando los ceros";
        System.out.println(msg);
        Logger.log(msg);
        zeroVisitsList = processAllVisits(zeroVisitsList, date2, dateOnQueryStr); //insistimos 2 veces mas cuando visitas devuelve cero
        System.out.println(msg);
        Logger.log(msg);
        processAllVisits(zeroVisitsList, date2, dateOnQueryStr);

        msg = "Visitas Procesadas: " + allProductIDs.size();
        System.out.println(msg);
        Logger.log(msg);

    }


    private static ArrayList<String> processAllVisits(ArrayList<String> allProductIDs, Date date, String dateOnQuery) {
        int count = 0;

        ArrayList<String> fiftyProductIDs = new ArrayList<String>();
        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();


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
        //clone
        ArrayList<String> zeroVisitsList=new ArrayList<String>();
        if (threadArrayList.size()>0){
            VisitCounter aVisitCounter = (VisitCounter)threadArrayList.get(0);
            for (String productIdWithZeroVisits: aVisitCounter.getZeroVisitsList()){
                zeroVisitsList.add(productIdWithZeroVisits);
            }
            aVisitCounter.resetZeroVisitsList();
        }else {
            String msg="No product with 0 vitists";
            System.out.println(msg);
            Logger.log(msg);
        }

        return zeroVisitsList;

    }

    private static void process50Visits(Date date, String dateOnQuery, ArrayList<String> fiftyProductIDs, ArrayList<Thread> threadArrayList) {
        long currentTime;
        long timeoutTime;

        VisitCounter visitCounter = new VisitCounter(fiftyProductIDs, date, dateOnQuery, SAVE, DEBUG, DATABASE);
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
                System.out.println("Error en de timeout.  Demasiado tiempo sin terminar de procesar una visita entre " + MAX_THREADS_VISITS + " visitas");
                System.exit(0);
            }
        }
    }

    private static synchronized void updateVisits(String productId, int quantity, Date date, String database) {

        if (globalUpdateVisits == null) {
            Connection connection = getUpdateConnection(database);
            try {
                globalUpdateVisits = connection.prepareStatement("update public.movimientos set visitas=? where idproducto=? and fecha =?");
                globalUpdateVisits.setDate(3, date);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            globalUpdateVisits.setInt(1, quantity);
            globalUpdateVisits.setString(2, productId);


            int updatedRecords = globalUpdateVisits.executeUpdate();
            globalUpdateVisits.getConnection().commit();

            if (updatedRecords != 1) {
                Logger.log("Error updating visits " + productId + " " + quantity + " " + date);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Muzzu\\IdeaProjects\\selenium\\chromedriver_win32_2.4\\chromedriver.exe");
        System.setProperty("webdriver.gecko.driver", "C:\\Users\\Muzzu\\IdeaProjects\\selenium\\geckodriver-v0.20.0-win64\\geckodriver.exe");

        globalDateformat = new SimpleDateFormat("HH:mm:ss.SSS");
        globalCalendar1 = Calendar.getInstance();
        globalCalendar2 = Calendar.getInstance();

        for (int i = 0; i < intervals.length; i++) { //validacion de intervalo
            for (int j = 1; j < intervals[i].length; j++) {
                if (intervals[i][j - 1] >= intervals[i][j]) {
                    System.out.println("Error en intervalo #" + i + " // " + intervals[i][j - 1] + "-" + intervals[i][j]);
                    System.exit(0);
                }
            }
        }

        if (SAVE) {
            saveRunInitialization("url", MAX_THREADS, DATABASE);
        }


        for (int j = 2; j < urls.length; j++) { //todo tiene que empezar de 0
            initVars();

            int numberOfThreads = (int) Math.round(intervals[j].length / 2.5);
            if (numberOfThreads > MAX_THREADS) {
                numberOfThreads = MAX_THREADS;
            }
            String msg = "()()()()()()()() ++++++  Processing new Category with " + numberOfThreads + " threads";
            System.out.println(msg);
            Logger.log(msg);

            ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
            for (int i = 0; i < numberOfThreads; i++) {
                MercadoLibre02 thread = new MercadoLibre02(urls[j], intervals[j]);
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

        if (!ONLY_ADD_NEW_PRODUCTS) {
            processPossiblyPausedProducts(DATABASE);
        }

        updateVisits(DATABASE);

        String msg = "******************************************************\n"
                + Counters.getGlobalPageCount() + " paginas procesadas\n "
                + Counters.getGlobalProductCount() + " productos procesados\n "
                + Counters.getGlobalNewsCount() + " productos con novedades";
        System.out.println(msg);
        Logger.log(msg);

    }


    private static long saveRunInitialization(String url, int threads, String database) {
        PreparedStatement ps = null;
        Connection connection = getUpdateConnection(database);
        long runId = -1;
        try {
            ps = connection.prepareStatement("INSERT INTO public.corridas(fecha, inicio, url, threads) VALUES (?, ?, ?, ?) RETURNING id;", Statement.RETURN_GENERATED_KEYS);

            Calendar cal = Calendar.getInstance();
            long milliseconds = cal.getTimeInMillis();
            Timestamp timestamp = new Timestamp(milliseconds);

            ps.setDate(1, getGlobalDate());
            ps.setTimestamp(2, timestamp);
            ps.setString(3, url);
            ps.setInt(4, threads);

            int insertedRecords = ps.executeUpdate();
            connection.commit();

            if (insertedRecords != 1) {
                Logger.log("Couldn't insert record into runs table");
            }
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                runId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return runId;
    }

    private static long saveRunEnding(long runId, int productostotal, int productosdetailstotal, String database) {
        PreparedStatement ps = null;
        Connection connection = getUpdateConnection(database);
        try {
            ps = connection.prepareStatement("UPDATE public.corridas SET fin=?, productostotal=?, productosdetalletotal=? WHERE id=?;");

            Calendar cal = Calendar.getInstance();
            long milliseconds = cal.getTimeInMillis();
            Timestamp timestamp = new Timestamp(milliseconds);

            ps.setTimestamp(1, timestamp);
            ps.setInt(2, productostotal);
            ps.setInt(3, productosdetailstotal);
            ps.setLong(4, runId);

            int insertedRecords = ps.executeUpdate();
            connection.commit();

            if (insertedRecords != 1) {
                Logger.log("couldn't update row in runs table");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return runId;
    }

    private static synchronized void insertProduct(String idProduct, String seller, int totalSold, String latestquestion, String url, boolean officialStore) {
        Connection connection = getAddProductConnection();

        try {
            if (globalInsertProduct == null) {
                globalInsertProduct = connection.prepareStatement("INSERT INTO public.productos(id, proveedor, ingreso, lastupdate, lastquestion, totalvendidos, url, tiendaoficial) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
            }

            globalInsertProduct.setString(1, idProduct);
            globalInsertProduct.setString(2, seller);
            globalInsertProduct.setDate(3, getGlobalDate());
            globalInsertProduct.setDate(4, getGlobalDate());
            globalInsertProduct.setString(5, latestquestion);
            globalInsertProduct.setInt(6, totalSold);
            globalInsertProduct.setString(7, url);
            globalInsertProduct.setBoolean(8, officialStore);

            int registrosInsertados = globalInsertProduct.executeUpdate();

            if (registrosInsertados != 1) {
                Logger.log("Couldn't insert product I");
            }
        } catch (SQLException e) {
            Logger.log("Couldn't insert product II");
            Logger.log(e);
        }
    }

    private synchronized static Date getGlobalDate() {
        if (globalDate == null) {
            Calendar cal = Calendar.getInstance();
            long milliseconds = cal.getTimeInMillis();
            long oneDayinMiliseconds = 0;
            if (FOLLOWING_DAY) {
                oneDayinMiliseconds = 86400000; //this will add a complete day on milliseconds
            }
            if (PRERVIOUS_DAY) {
                oneDayinMiliseconds = -86400000; //this will add a complete day on milliseconds
            }
            Date date = new Date(milliseconds + oneDayinMiliseconds);
            globalDate = date;
        }
        return globalDate;
    }

    public static synchronized void updateProductAddActivity(String productId, String seller, boolean officialStore, int totalSold, int newSold, String title, String url, int feedbacksTotal, double feedbacksAverage, double price, int newQuestions, String lastQuestion, int pagina, int shipping, int discount, boolean premium) {
        Connection connection = getAddActivityConnection();
        try {
            if (globalUpdateProduct == null) {
                globalUpdateProduct = connection.prepareStatement("UPDATE public.productos SET totalvendidos = ?, lastupdate=?, url=?, lastquestion=?, proveedor=?, tiendaoficial=?, deshabilitado=false WHERE id = ?;");
            }

            globalUpdateProduct.setInt(1, totalSold);
            globalUpdateProduct.setDate(2, getGlobalDate());
            globalUpdateProduct.setString(3, url);
            globalUpdateProduct.setString(4, lastQuestion);
            globalUpdateProduct.setString(5, seller);
            globalUpdateProduct.setBoolean(6, officialStore);
            globalUpdateProduct.setString(7, productId);

            int insertedRecords = globalUpdateProduct.executeUpdate();
            if (insertedRecords != 1) {
                Logger.log("Couldn't update product " + productId);
            }

            if (OVERRIDE_TODAYS_RUN) {//no sabemos si hay registro pero por las dudas removemos
                if (globalRemoveActivity == null) {
                    globalRemoveActivity = connection.prepareStatement("DELETE FROM public.movimientos WHERE idproducto=? and fecha=?");
                }
                globalRemoveActivity.setString(1, productId);
                globalRemoveActivity.setDate(2, getGlobalDate());
                int removedRecords = globalRemoveActivity.executeUpdate();
                if (removedRecords >= 1) {
                    Logger.log("Record removed on activity table date: " + getGlobalDate() + " productId: " + productId);
                }
            }

            if (globalInsertActivity == null) {
                globalInsertActivity = connection.prepareStatement("INSERT INTO public.movimientos(fecha, idproducto, titulo, url, opinionestotal, opinionespromedio, precio, vendidos, totalvendidos, nuevaspreguntas, pagina, proveedor, tiendaoficial, envio, descuento, premium) VALUES (?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
            }

            globalInsertActivity.setDate(1, getGlobalDate());
            globalInsertActivity.setString(2, productId);
            globalInsertActivity.setString(3, title);
            globalInsertActivity.setString(4, url);
            globalInsertActivity.setInt(5, feedbacksTotal);
            globalInsertActivity.setDouble(6, feedbacksAverage);
            globalInsertActivity.setDouble(7, price);
            globalInsertActivity.setInt(8, newSold);
            globalInsertActivity.setInt(9, totalSold);
            globalInsertActivity.setInt(10, newQuestions);
            globalInsertActivity.setInt(11, pagina);
            globalInsertActivity.setString(12, seller);
            globalInsertActivity.setBoolean(13, officialStore);

            globalInsertActivity.setInt(14, shipping);
            globalInsertActivity.setInt(15, discount);
            globalInsertActivity.setBoolean(16, premium);

            insertedRecords = globalInsertActivity.executeUpdate();
            if (insertedRecords != 1) {
                Logger.log("Couln't insert a record in activity table " + productId);
            }

            connection.commit();

        } catch (SQLException e) {
            Logger.log("I couldn't add activity due to SQLException " + url);
            Logger.log(e);
            if (connection != null) {
                try {
                    //connection reset
                    connection.close();
                    connection = null;
                    globalAddActivityConnection = null;

                    //prepared statement's reset
                    globalInsertActivity = null;
                    globalRemoveActivity = null;
                    globalUpdateProduct = null;
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    private static synchronized Date lastUpdate(String productId, String database) {
        Date lastUpdate = null;
        Connection connection = getSelectConnection(database);
        try {
            if (globalSelectProduct == null) {
                globalSelectProduct = connection.prepareStatement("SELECT lastUpdate FROM public.productos WHERE id=?;");
            }

            globalSelectProduct.setString(1, productId);

            ResultSet rs = globalSelectProduct.executeQuery();
            if (rs == null) {
                Logger.log("Couldn't get last update I " + productId);
            }
            if (rs.next()) {
                lastUpdate = rs.getDate(1);
            }
        } catch (SQLException e) {
            Logger.log("Couldn't get last update II " + productId);
            Logger.log(e);
        }
        return lastUpdate;
    }


    public static synchronized int getTotalSold(String productId, String database) {
        int totalSold = 0;
        Connection connection = getSelectConnection(database);
        try {
            if (globalSelectTotalSold == null) {
                globalSelectTotalSold = connection.prepareStatement("SELECT totalvendidos FROM public.productos WHERE id=?;");
            }

            globalSelectTotalSold.setString(1, productId);

            ResultSet rs = globalSelectTotalSold.executeQuery();
            if (rs == null) {
                Logger.log("Couldn't get total sold i" + productId);
                return 0;
            }

            if (rs.next()) {
                totalSold = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.log("Couldn't get total sold ii" + productId);
        }
        return totalSold;
    }

    public static synchronized String getLastQuestion(String productId, String database) {
        String lastQuestion = null;
        Connection connection = getSelectConnection(database);
        try {
            if (globalSelectLastQuestion == null) {
                globalSelectLastQuestion = connection.prepareStatement("SELECT lastQuestion FROM public.productos WHERE id=?;");
            }

            globalSelectLastQuestion.setString(1, productId);

            ResultSet rs = globalSelectLastQuestion.executeQuery();
            if (rs == null) {
                Logger.log("Couldn't get last question i " + productId);
                return null;
            }

            if (rs.next()) {
                lastQuestion = rs.getString(1);
            }
        } catch (SQLException e) {
            Logger.log("Couldn't get last question ii " + productId);
            Logger.log(e);
        }
        return lastQuestion;
    }


    private static void processPossiblyPausedProducts(String database) {

        String msg="*** Procesando pausados  / novedades antes del proceso "+Counters.getGlobalNewsCount();;
        System.out.println(msg);
        Logger.log(msg);

        Connection connection = getSelectConnection(database);

        String productId = null;
        String productUrl = null;
        ArrayList<String> possiblyPausedProductList = new ArrayList<String>();
        try {
            PreparedStatement datesPreparedStatement = connection.prepareStatement("SELECT fecha FROM public.movimientos group by fecha order by fecha desc");
            ResultSet rs = datesPreparedStatement.executeQuery();
            if (rs == null) {
                msg = "Error getting dates B";
                System.out.println(msg);
                Logger.log(msg);
            }
            if (!rs.next()) {
                msg = "Error getting dates B II";
                System.out.println(msg);
                Logger.log(msg);
            }
            if (!rs.next()) {
                msg = "Error getting dates B III";
                System.out.println(msg);
                Logger.log(msg);
            }
            Date previousWeekRunDate = rs.getDate(1);

            //option 1
            globalSelectPossiblyPaused = connection.prepareStatement("SELECT id,url FROM public.productos WHERE lastupdate<? and deshabilitado=false");
            globalSelectPossiblyPaused.setDate(1, globalDate);

            //option 2
            //globalSelectPossiblyPaused = connection.prepareStatement("SELECT id,url FROM public.productos WHERE lastupdate=? and deshabilitado=false");
            //globalSelectPossiblyPaused.setDate(1, previousWeekRunDate);

            msg="revisando pausados "+" - "+ globalSelectPossiblyPaused.toString();
            System.out.println(msg);
            Logger.log(msg);

            ResultSet rs2 = globalSelectPossiblyPaused.executeQuery();
            if (rs2 == null) {
                Logger.log("Couldn't get Possibly Paused Products");
                return;
            }

            while (rs2.next()) {
                productId = rs2.getString(1);
                if (!globalProcesedProductList.contains(productId)) {
                    productUrl = rs2.getString(2);
                    possiblyPausedProductList.add(productUrl);
                }
            }
        } catch (SQLException e) {
            Logger.log("Couldn't get Possibly Paused Products II");
            Logger.log(e);
        }
        //return possiblyPausedProductList;

        msg="posibles pausados: "+possiblyPausedProductList.size()+" de "+globalProcesedProductList.size();
        System.out.println(msg);
        Logger.log(msg);

        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
        long currentTime;
        long timeoutTime;

        Counters.initGlobalRunnerCount();
        for (String url : possiblyPausedProductList) {
            //processArticle(url,0,possiblyPausedProductList,)


            ProductPageProcessor productPageProcessor = new ProductPageProcessor(url, SAVE, DEBUG, DATABASE);
            threadArrayList.add(productPageProcessor);
            productPageProcessor.start();
            currentTime = System.currentTimeMillis();
            timeoutTime = currentTime + TIMEOUT_MIN * 60l * 1000l;

            while (MAX_THREADS < (Thread.activeCount() - 1)) {

                try {
                    Thread.sleep(10l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentTime = System.currentTimeMillis();
                if (currentTime > timeoutTime) {
                    System.out.println("Error en de timeout.  Demasiado tiempo sin terminar de procesar un producto pausado entre " + MAX_THREADS + " visitas");
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
    }


    synchronized private static int getPage(int interval, boolean reset) {
        if (reset) {
            globalPageArray[interval] = 0;
        } else {
            globalPageArray[interval]++;
        }
        return globalPageArray[interval];
    }

    synchronized private static int getInterval() {
        globalAndFirstInterval++;
        return globalAndFirstInterval;
    }


    private static Connection getSelectConnection(String database) {
        if (globalSelectConnection == null) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/" + database;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "password");
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


    protected static Connection getUpdateConnection(String database) {
        if (globalUpadteConnection == null) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/" + database;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "password");
            try {
                globalUpadteConnection = DriverManager.getConnection(url, props);
                globalUpadteConnection.setAutoCommit(false);
            } catch (SQLException e) {
                Logger.log("I couldn't make an update connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalUpadteConnection;
    }


    private static Connection getAddProductConnection() {
        if (globalAddProductConnection == null) {

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
                globalAddProductConnection = DriverManager.getConnection(url, props);
            } catch (SQLException e) {
                Logger.log("I couldn't make addproduct connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalAddProductConnection;
    }

    private static Connection getAddActivityConnection() {
        if (globalAddActivityConnection == null) {

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
                globalAddActivityConnection = DriverManager.getConnection(url, props);
                globalAddActivityConnection.setAutoCommit(false);
            } catch (SQLException e) {
                Logger.log("I couldn't make add activity connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalAddActivityConnection;
    }

    public void run() {

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
        int itemsInPage = 48;
        String runnerID = "R" + Counters.getGlobalRunnerCount();

        boolean processFinished = false;
        while (!processFinished) {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
            int interval = getInterval();
            if (interval >= theIntervals.length) {
                processFinished = true;
                continue;
            }
            int range1 = theIntervals[interval - 1] + 1;
            int range2 = theIntervals[interval];
            String priceRangeStr = "_PriceRange_" + range1 + "-" + range2;
            Logger.log("XXXXXXXXXXXXXXXXXXXXXX " + runnerID + " new interval : " + interval + " " + priceRangeStr);
            String[] subintervals = new String[]{""};
            if (range1 == range2) {
                subintervals = globalSubIntervals;
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            }
            for (String subinterval : subintervals) {
                boolean endInterval = false;
                getPage(interval, true);
                while (!endInterval) {
                    int page = getPage(interval, false);
                    if (page == 43) {
                        Logger.log("XXXXXXXXXXXXXXXXXXXXXX este intervalo no pudo ser recorrido por completo "
                                + baseURL + priceRangeStr);
                        System.out.println("XXXXXXXXXXXXXXXXXXXXXX este intervalo no pudo ser recorrido por completo "
                                + baseURL + priceRangeStr);
                        endInterval = true;
                        continue;
                    }//se acabose

                    Counters.incrementGlobalPageCount();
                    int since = (page - 1) * itemsInPage + 1;
                    String sinceStr = "_Desde_" + since;
                    String uRL = baseURL + sinceStr + priceRangeStr;
                    if (page == 1) {
                        uRL = baseURL + priceRangeStr;
                    }
                    uRL = uRL.replace("[_SUBINTERVAL]", subinterval);
                    uRL+="_DisplayType_G";

                    System.out.println(runnerID + " " + uRL);
                    Logger.log(runnerID + " " + uRL);
                    String htmlStringFromPage = HttpUtils.getHTMLStringFromPage(uRL, httpClient, DEBUG);
                    if (htmlStringFromPage == null) { //suponemos que se terminó
                        // pero tambien hacemos pausa por si es problema de red
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            Logger.log(e);
                        }
                        Logger.log(runnerID + " hmlstring from page is null " + uRL);
                        try {
                            httpClient.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        httpClient = null;
                        httpClient = HttpUtils.buildHttpClient();
                        endInterval = true;
                        continue;
                    }

                    htmlStringFromPage = htmlStringFromPage.toString();
                    int resultSectionPos = htmlStringFromPage.indexOf("results-section");
                    String resultListHMTLData = null;
                    if (resultSectionPos == -1) {
                        Logger.log("Error getting results-section on page " + page);
                        Logger.log(htmlStringFromPage);
                        resultListHMTLData = htmlStringFromPage;
                    } else {
                        resultListHMTLData = htmlStringFromPage.substring(resultSectionPos);
                    }

                    String[] allHrefsOnPage = StringUtils.substringsBetween(resultListHMTLData, "<a href", "</a>");
                    if (allHrefsOnPage == null) { //todo check
                        System.out.println("this page has no Hrefs !!! " + allHrefsOnPage);
                        Logger.log("this page has no Hrefs !!!" + allHrefsOnPage);
                        endInterval = true;
                        continue;
                    }

                    ArrayList<String> productsURLArrayList = new ArrayList();
                    for (String href : allHrefsOnPage) {
                        if (href.indexOf(HTMLParseUtils.ARTICLE_PREFIX) > 0 && href.indexOf("-_JM") > 0) {
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
                        Counters.incrementGlobalProductCount();

                        String productId = HTMLParseUtils.getProductIdFromURL(productUrl);

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

                        String title = HTMLParseUtils.getTitle2(productHTMLdata);
                        if (title != null) {
                            title = title.trim();
                        }
                        if (title==null || title.length()==0) {
                            Logger.log(runnerID+" invalid title on page " + page + " url " + uRL);
                        }

                        int discount = HTMLParseUtils.getDiscount2(productHTMLdata);
                        if (discount < 0) {
                            Logger.log(runnerID + " I couldn't get the discount on " + productUrl);
                        }

                        int shipping = HTMLParseUtils.getShipping(productHTMLdata);

                        boolean premium = HTMLParseUtils.getPremium(productHTMLdata);

                        double price = HTMLParseUtils.getPrice2(productHTMLdata);
                        if (price == 0) {
                            Logger.log(runnerID + " I couldn't get the price on " + productUrl);
                        }

                        boolean isUsed = HTMLParseUtils.isUsed2(productHTMLdata);

                        int totalSold = 0;
                        String htmlStringFromProductPage = null;

                        if (!isUsed) {
                            totalSold = HTMLParseUtils.getTotalSold2(productHTMLdata);
                        } else {
                            htmlStringFromProductPage = HttpUtils.getHTMLStringFromPage(productUrl, httpClient, DEBUG);
                            if (htmlStringFromProductPage == null) {
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
                                continue; //ignoramos este item
                            } else {
                                totalSold = HTMLParseUtils.getTotalSold(htmlStringFromProductPage, productUrl);
                                if (totalSold == -1) {//error
                                    continue;
                                }
                            }
                        }


                        int reviews = HTMLParseUtils.getReviews2(productHTMLdata);
                        if (reviews < 0) {
                            reviews = 0;
                            Logger.log(runnerID + " I couldn't get the reviews on " + productUrl);
                        }

                        double stars = 0.0;
                        if (reviews > 0) {
                            stars = HTMLParseUtils.getStars2(productHTMLdata);
                            if (stars < 0.0) {
                                stars = 0.0;
                                Logger.log(runnerID + " I couldn't get the stars on " + productUrl);
                            }
                        }


                        String msg = runnerID + " processing page " + page + " | " + productId + " | " + totalSold + " | " + reviews + " | " + stars + " | " + price + " | " + title + " | " + productUrl;
                        System.out.println(msg);

                        if (totalSold >= MINIMUM_SALES) { //si no figura venta no le doy bola
                            Date lastUpdate = lastUpdate(productId, DATABASE);
                            if (lastUpdate != null) {//producto existente
                                if (!ONLY_ADD_NEW_PRODUCTS) { //ignora los updates
                                    boolean sameDate = isSameDate(lastUpdate, getGlobalDate());
                                    if (!sameDate || (sameDate && OVERRIDE_TODAYS_RUN)) { //actualizar
                                        int previousTotalSold = getTotalSold(productId, DATABASE);
                                        if (totalSold != previousTotalSold) { //actualizar
                                            int newSold = totalSold - previousTotalSold;

                                            if (htmlStringFromProductPage == null) {
                                                htmlStringFromProductPage = HttpUtils.getHTMLStringFromPage(productUrl, httpClient, DEBUG);
                                                if (htmlStringFromProductPage == null) {
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
                                                    continue;
                                                    //ignoramos este item
                                                }
                                            }

                                            boolean officialStore = HTMLParseUtils.getOfficialStore(htmlStringFromProductPage);

                                            String seller = HTMLParseUtils.getSeller(htmlStringFromProductPage, officialStore, productUrl);

                                            String lastQuestion = HTMLParseUtils.getLastQuestion(htmlStringFromProductPage);

                                            String previousLastQuestion = getLastQuestion(productId, DATABASE);
                                            ArrayList<String> newQuestionsList = HttpUtils.getNewQuestionsFromPreviousLastQuestion(htmlStringFromProductPage, productUrl, httpClient, runnerID, DEBUG, previousLastQuestion);
                                            int newQuestions = newQuestionsList.size();

                                            Counters.incrementGlobalNewsCount();

                                            msg = runnerID + " new sale. productID: " + productId + " quantity: " + newSold;
                                            System.out.println(msg);
                                            Logger.log(msg);

                                            if (SAVE) {
                                                updateProductAddActivity(productId, seller, officialStore, totalSold, newSold, title, productUrl, reviews, stars, price, newQuestions, lastQuestion, page, shipping, discount, premium);
                                            }
                                        } else {//no vendió esta semana
                                            addProcesedProductToList(productId);
                                        }
                                    }
                                }
                            } else { //agregar vendedor

                                if (htmlStringFromProductPage == null) {
                                    htmlStringFromProductPage = HttpUtils.getHTMLStringFromPage(productUrl, httpClient, DEBUG);
                                    if (htmlStringFromProductPage == null) {
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
                                        httpClient = HttpUtils.buildHttpClient();
                                        continue;
                                        //ignoramos este item
                                    }
                                }

                                boolean officialStore = HTMLParseUtils.getOfficialStore(htmlStringFromProductPage);

                                String seller = HTMLParseUtils.getSeller(htmlStringFromProductPage, officialStore, productUrl);

                                String lastQuestion = HTMLParseUtils.getLastQuestion(htmlStringFromProductPage);

                                Counters.incrementGlobalNewsCount();

                                msg = runnerID + " new product ID: " + productId + " Total Sold: " + totalSold;
                                System.out.println(msg);
                                Logger.log(msg);

                                if (SAVE) {
                                    insertProduct(productId, seller, totalSold, lastQuestion, productUrl, officialStore);
                                }
                            }
                        }
                    }
                }
            }
        }


        String msg = "XXXXXXXXXXXXXX Este es el fin " + runnerID;
        System.out.println(msg);
        Logger.log(msg);
        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClient = null;
    }

    private static synchronized boolean isSameDate(Date date1, Date date2) {
        globalCalendar1.setTime(date1);
        globalCalendar2.setTime(date2);
        boolean sameDay = globalCalendar1.get(Calendar.YEAR) == globalCalendar2.get(Calendar.YEAR) &&
                globalCalendar1.get(Calendar.DAY_OF_YEAR) == globalCalendar2.get(Calendar.DAY_OF_YEAR);
        return sameDay;
    }

    private synchronized void addProcesedProductToList(String productId) {
        if (globalProcesedProductList == null) {
            globalProcesedProductList = new ArrayList<String>();
        }
        globalProcesedProductList.add(productId);
    }

}
