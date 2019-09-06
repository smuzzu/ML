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
    static boolean OVERRIDE_TODAYS_RUN = true;
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
                    "https://listado.mercadolibre.com.ar/_CustId_191605678",  //primero+uno
                    "https://hogar.mercadolibre.com.ar/articulos-limpieza-productos-limpiadores-alfombras/"         //limpia alfombras
            };


    static int[][] intervals = new int[][]{{0, 99, 149, 240, 320, 399, 450, 530, 599, 660, 700, 790, 800, 890, 900, 950, 999, 1000,
            1100, 1150, 1199, 1200, 1290, 1300, 1390, 1400, 1450, 1499, 1500, 1590, 1600, 1690, 1700, 1790, 1800, 1890, 1960, 1998, 1999,
            2000, 2100, 2200, 2300, 2400, 2499, 2500, 2600, 2700, 2800, 2900, 2999, 3000, 3100, 3200, 3300, 3399, 3499, 3500, 3700, 3800,
            3900, 3999, 4000, 4200, 4350, 4499, 4500, 4799, 4900, 4999, 5000, 5400, 5500, 5800, 5999, 6000, 6400, 6500, 6800, 6998, 7000,
            7400, 7500, 7800, 7998, 8000, 8498, 8500, 8998, 9000, 9400, 9800, 9999, 10001, 10500, 11000, 11500, 11997, 11999, 12001, 12900,
            13500, 14000, 14900, 15000, 16000, 17000, 18000, 19000, 20000, 22000, 24000, 26000, 29000, 35000, 45000, 2147483647}, //sala de estar comedor

            {0, 69, 119, 199, 290, 375, 475, 549, 660, 799, 899, 999, 1150, 1300, 1499, 1650, 1850, 2000, 2350, 2590, 2990, 3350, 3800, 4350,
                    4999, 5900, 6999, 8100, 9700, 12000, 17000, 26000, 45000, 2147483647}, // Equipamiento Oficina

            {0, 380, 610, 899, 1100, 1400, 1690, 1990, 2300, 2750, 3250, 3950, 4800, 5900, 7500, 9800, 14000, 22000, 2147483647}, // Muebles Oficina

            {0, 450, 900, 1299, 1600, 1990, 2350, 2750, 3190, 3700, 4300, 5000, 6000, 7000, 8000, 9600, 11500, 15000, 2147483647}, // Cocina

            {0, 440, 700, 990, 1299, 1698, 2050, 2600, 3300, 4400, 6000, 9000, 14000, 20000, 35000, 2147483647}, // Jardin

            {0, 1200, 2300, 3500, 5000, 8000, 14000, 2147483647}, // Baño

            {0, 200, 350, 500, 740, 1000, 1550, 2500, 6000, 2147483647}, // Perchero

            {0, 250, 550, 950, 1500, 2800, 2147483647}, // Cesto

            {0, 700, 2147483647}, // Ordenador de Publico

            {0, 700, 2147483647}, // Paragüero

            {0, 700, 2147483647}, // Acacia

            {0, 700, 2147483647}, // Misionlive

            {0, 700, 2147483647}, // Primero Uno

            {0, 700, 2147483647}  // Limpia Alfombras
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
       }

       VisitCounter.updateVisits(DATABASE,SAVE,DEBUG);

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
