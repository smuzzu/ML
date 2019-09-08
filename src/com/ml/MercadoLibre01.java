package com.ml;

import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;

import com.ml.utils.Counters;
import com.ml.utils.DatabaseHelper;
import com.ml.utils.HTMLParseUtils;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.ProductPageProcessor;

import org.apache.commons.lang3.StringUtils;

import org.apache.http.impl.client.CloseableHttpClient;


/**
 * Created by Muzzu on 11/12/2017.
 */

public class MercadoLibre01  extends Thread {

    MercadoLibre01(String baseURL,int[] intervals){
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

    static Date globalDate = null;
    static DateFormat globalDateformat = null;
    static Calendar globalCalendar1 = null;
    static Calendar globalCalendar2 = null;

    static int MAX_THREADS=14;//
    static boolean OVERRIDE_TODAYS_RUN = false;
    static boolean SAVE =true;
    static boolean DEBUG=false;
    static boolean FOLLOWING_DAY = false;
    static boolean PRERVIOUS_DAY = false;
    static boolean ONLY_ADD_NEW_PRODUCTS = false;
    static int MINIMUM_SALES = 1;
    static String DATABASE = "ML1";

    static boolean BRAZIL = false;

    private String baseURL =null;
    private int[] theIntervals = null;

    static String[] urls = new String[]
            {
                    //"https://hogar.mercadolibre.com.ar/sala-estar-comedor/[_SUBINTERVAL]",
                    "https://hogar.mercadolibre.com.ar/muebles/[_SUBINTERVAL]",
                    "https://listado.mercadolibre.com.ar/industrias-oficinas/equipamiento-oficinas/",
                    //"https://hogar.mercadolibre.com.ar/cocina/amoblamientos/",
                    "https://listado.mercadolibre.com.ar/herramientas-y-construccion/mobiliario-cocinas/",
                    "https://hogar.mercadolibre.com.ar/organizacion/",
                    //"https://hogar.mercadolibre.com.ar/jardines-exteriores/muebles-de-jardin/",
                    "https://hogar.mercadolibre.com.ar/jardines-exteriores-muebles-exterior/",
                    //"https://hogar.mercadolibre.com.ar/banos/muebles/",
                    "https://hogar.mercadolibre.com.ar/banos-vanitorys-botiquines-bano/",

                    "https://listado.mercadolibre.com.ar/perchero",
                    "https://listado.mercadolibre.com.ar/cesto",
                    "https://listado.mercadolibre.com.ar/ordenador-de-publico",
                    "https://listado.mercadolibre.com.ar/paraguero",
                    "https://listado.mercadolibre.com.ar/_CustId_241751796",  //acacia
                    "https://listado.mercadolibre.com.ar/_CustId_233230004",  //misionlive
                    "https://listado.mercadolibre.com.ar/_CustId_191605678",  //primero+uno
                    "https://listado.mercadolibre.com.ar/_CustId_77061780"    //marcaimport
            };


    static int[][] intervals = new int[][]{
            {0,70,110,160,220,290,340,399,449,490,499,500,590,600,690,700,780,799,800,850,898,900,980,999,1000,1099,
                    1100,1199,1200,1299,1301,1399,1401,1480,1499,1500,1590,1600,1601,1690,1700,1780,1799,1800,1899,1920,1980,
                    1999,2000,2100,2180,2199,2201,2299,2301,2399,2401,2450,2499,2500,2599,2601,2699,2701,2799,2801,2899,2901,
                    2980,2999,3000,3100,3199,3201,3299,3350,3400,3480,3499,3500,3580,3600,3601,3680,3700,3780,3799,3801,3899,
                    3901,3980,3999,4000,4100,4180,4200,4280,4300,4400,4499,4500,4600,4700,4790,4850,4900,4970,4999,5000,5180,
                    5280,5380,5480,5499,5500,5650,5790,5890,5980,5999,6000,6190,6300,6400,6499,6500,6650,6798,6898,6980,6999,
                    7000,7200,7400,7499,7500,7700,7890,7950,7990,7999,8000,8200,8440,8499,8501,8750,8899,8950,8999,9000,9300,
                    9499,9501,9750,9890,9997,9999,10000,10350,10500,10850,10998,11000,11400,11600,11900,11999,12000,12400,
                    12500,12850,12998,13000,13480,13700,13999,14001,14499,14800,14999,15000,15500,15900,16000,16500,16900,
                    17200,17700,17999,18001,18500,18998,19300,19850,19999,20001,20900,21400,21999,22499,23000,23800,24600,
                    24999,25001,25999,26999,27999,28999,29999,31000,32500,34500,35500,37501,39900,42000,44500,47000,50000,
                    56000,64000,75000,100000,170000, 2147483647}, //muebles para el hogar

            {0,110,240,390,499,600,790,900,999,1180,1390,1500,1800,1999,2200,2499,2750,2999,3350,3700,4000,4500,4999,5400,
                    5999,6600,7450,8000,8999,9999,11500,13500,15900,19999,27000,42000,100000,2147483647}, // Equipamiento Oficina

            {0, 600,1300,1850,2250,2550,2880,3190,3500,3900,4300,4750,5200,5700,6200,6850,7550,8400,9500,10900,12500,
                    14500,17000,20000,25000,40000, 2147483647}, // Cocina

            {4000, 4300, 5000, 5900, 6500, 7000, 7500, 8500, 9600, 11500, 13000, 15000, 18000, 24000, 35000, 2147483647}, // Organizacion

            {0, 440, 700, 990, 1299, 1698, 2050, 2600, 3300, 4300, 5000, 6000, 8000, 11000, 17000, 25000, 35000, 2147483647}, // Jardin y exterior

            {0, 3500,5500,7600,9900,13000,17100,28000, 2147483647}, // Baño

            {0, 300,540,790,1100,1650,2500,4500,10000, 2147483647}, // Perchero

            {0, 500,1000,1800,3700,10000,2147483647}, // Cesto

            {0, 2147483647}, // Ordenador de Publico

            {0, 2147483647}, // Paragüero

            {0, 2147483647}, // Acacia

            {0, 2147483647}, // Misionlive

            {0, 2147483647}, // Primero Uno

            {0, 2147483647}  // marcaimport
    };

    private static void initVars() {
        Counters.initGlobalRunnerCount();
        globalAndFirstInterval = 0; //arranca en 0
        for (int i = 0; i < globalPageArray.length; i++) {
            globalPageArray[i] = 0;
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



       for (int j=0; j<urls.length; j++) { //todo tiene que empezar de 0  prueba de hoy usamos j=10
            initVars();

            int numberOfThreads = (int) Math.round(intervals[j].length / 2.5);
            if (numberOfThreads>MAX_THREADS){
                numberOfThreads=MAX_THREADS;
            }
            String msg = "()()()()()()()() ++++++  Processing new Category with "+numberOfThreads+" threads";
            System.out.println(msg);
           Logger.log(msg);

            ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
            for (int i = 0; i < numberOfThreads; i++) {
                MercadoLibre01 thread = new MercadoLibre01(urls[j],intervals[j]);
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
           ProductPageProcessor.processPossiblyPausedProducts(DATABASE, getGlobalDate(),globalProcesedProductList,SAVE,DEBUG);

           VisitCounter.updateVisits(DATABASE,SAVE,DEBUG);
       }



        String msg = "******************************************************\r\n"
                    +Counters.getGlobalPageCount() +" paginas procesadas\r\n "
                    +Counters.getGlobalProductCount()+" productos procesados\r\n "
                    +Counters.getGlobalNewsCount()+" productos con novedades";
        System.out.println(msg);
        Logger.log(msg);

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


    public void run(){

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
        int itemsInPage=48;
        String runnerID="R"+Counters.getGlobalRunnerCount();

        boolean processFinished=false;
        while (!processFinished){
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
            int interval=getInterval();
            if (interval>= theIntervals.length){
                processFinished=true;
                continue;
            }
            int range1= theIntervals[interval-1]+1;
            int range2= theIntervals[interval];
            String priceRangeStr="_PriceRange_"+range1+"-"+range2;
            Logger.log ("XXXXXXXXXXXXXXXXXXXXXX "+runnerID+" new interval : "+ interval+" "+priceRangeStr);
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
                    uRL=uRL.replace("[_SUBINTERVAL]",subinterval);
                    uRL+="_DisplayType_G";

                    System.out.println(runnerID+" "+uRL);
                    Logger.log(runnerID+" "+uRL);
                    String htmlStringFromPage = HttpUtils.getHTMLStringFromPage(uRL,httpClient,DEBUG);
                    if (!HttpUtils.isOK(htmlStringFromPage)) { //suponemos que se terminó
                        // pero tambien hacemos pausa por si es problema de red
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            Logger.log(e);
                        }
                        Logger.log(runnerID+" hmlstring from page is null " + uRL);
                        try {
                            httpClient.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        httpClient=null;
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
                        if (discount<0){
                            Logger.log(runnerID+" I couldn't get the discount on " + productUrl);
                        }

                        int shipping=HTMLParseUtils.getShipping(productHTMLdata);

                        boolean premium=HTMLParseUtils.getPremium(productHTMLdata);

                        double price = HTMLParseUtils.getPrice2(productHTMLdata);
                        if (price==0){
                            Logger.log(runnerID+" I couldn't get the price on " + productUrl);
                        }

                        boolean isUsed = HTMLParseUtils.isUsed2(productHTMLdata);

                        int totalSold = 0;
                        String htmlStringFromProductPage = null;

                        if (!isUsed){
                            totalSold=HTMLParseUtils.getTotalSold2(productHTMLdata);
                        } else {
                            htmlStringFromProductPage = HttpUtils.getHTMLStringFromPage(productUrl, httpClient, DEBUG);
                            if (!HttpUtils.isOK(htmlStringFromProductPage)) {
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
                        if (reviews<0){
                            reviews=0;
                            Logger.log(runnerID+" I couldn't get the reviews on " + productUrl);
                        }

                        double stars = 0.0;
                        if (reviews>0){
                            stars=HTMLParseUtils.getStars2(productHTMLdata);
                            if (stars<0.0){
                                stars=0.0;
                                Logger.log(runnerID+" I couldn't get the stars on " + productUrl);
                            }
                        }


                        String msg = runnerID+" processing page " + page + " | " + productId + " | " + totalSold + " | " + reviews + " | " + stars + " | " + price + " | " + title + " | " + productUrl;
                        System.out.println(msg);

                        if (totalSold >= MINIMUM_SALES) { //si no figura venta no le doy bola
                            Date lastUpdate = DatabaseHelper.fetchLastUpdate(productId,DATABASE);
                            if (lastUpdate != null) {//producto existente
                                if (!ONLY_ADD_NEW_PRODUCTS) { //ignora los updates
                                    boolean sameDate = Counters.isSameDate(lastUpdate, getGlobalDate());
                                    if (!sameDate || (sameDate && OVERRIDE_TODAYS_RUN)) { //actualizar
                                        int previousTotalSold = DatabaseHelper.fetchTotalSold(productId,DATABASE);
                                        if (totalSold != previousTotalSold) { //actualizar
                                            int newSold = totalSold - previousTotalSold;

                                            if (!HttpUtils.isOK(htmlStringFromProductPage)) {
                                                htmlStringFromProductPage = HttpUtils.getHTMLStringFromPage(productUrl, httpClient,DEBUG);
                                                if (!HttpUtils.isOK(htmlStringFromProductPage)) {
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

                                            boolean officialStore=HTMLParseUtils.getOfficialStore(htmlStringFromProductPage);

                                            String seller=HTMLParseUtils.getSeller(htmlStringFromProductPage,officialStore,productUrl);

                                            String lastQuestion=HTMLParseUtils.getLastQuestion(htmlStringFromProductPage);

                                            String previousLastQuestion = DatabaseHelper.fetchLastQuestion(productId,DATABASE);
                                            ArrayList<String> newQuestionsList= HttpUtils.getNewQuestionsFromPreviousLastQuestion(productUrl,httpClient,runnerID,DEBUG,previousLastQuestion);
                                            int newQuestions = newQuestionsList.size();

                                            Counters.incrementGlobalNewsCount();

                                            msg = runnerID+" new sale. productID: " + productId + " quantity: " + newSold;
                                            System.out.println(msg);
                                            Logger.log(msg);

                                            if (SAVE) {
                                                DatabaseHelper.updateProductAddActivity(DATABASE,OVERRIDE_TODAYS_RUN,getGlobalDate(),productId, seller, officialStore, totalSold, newSold, title, productUrl, reviews, stars, price, newQuestions, lastQuestion, page, shipping, discount, premium);
                                            }
                                        } else {//no vendió esta semana
                                            addProcesedProductToList(productId);
                                        }
                                    }
                                }
                            } else { //agregar vendedor

                                if (!HttpUtils.isOK(htmlStringFromProductPage)) {
                                    htmlStringFromProductPage = HttpUtils.getHTMLStringFromPage(productUrl, httpClient,DEBUG);
                                    if (!HttpUtils.isOK(htmlStringFromProductPage)) {
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

                                boolean officialStore=HTMLParseUtils.getOfficialStore(htmlStringFromProductPage);

                                String seller=HTMLParseUtils.getSeller(htmlStringFromProductPage,officialStore,productUrl);

                                String lastQuestion=HTMLParseUtils.getLastQuestion(htmlStringFromProductPage);

                                Counters.incrementGlobalNewsCount();

                                msg = runnerID+" new product ID: " + productId + " Total Sold: " + totalSold;
                                System.out.println(msg);
                                Logger.log(msg);

                                if (SAVE) {
                                    DatabaseHelper.insertProduct(DATABASE,OVERRIDE_TODAYS_RUN,getGlobalDate(),productId, seller, totalSold, lastQuestion, productUrl, officialStore);
                                }
                            }
                        }
                    }
                }
            }
        }


        String msg="XXXXXXXXXXXXXX Este es el fin "+runnerID;
        System.out.println(msg);
        Logger.log(msg);
        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClient=null;
    }

    private synchronized void addProcesedProductToList(String productId){
        if (globalProcesedProductList==null){
            globalProcesedProductList=new ArrayList<String>();
        }
        globalProcesedProductList.add(productId);
    }

}
