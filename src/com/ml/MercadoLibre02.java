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

    static Date globalDate = null;
    static DateFormat globalDateformat = null;
    static Calendar globalCalendar1 = null;
    static Calendar globalCalendar2 = null;

    static int MAX_THREADS = 14;//14
    static boolean OVERRIDE_TODAYS_RUN = false;
    static boolean SAVE = true;
    static boolean DEBUG = false;
    static boolean FOLLOWING_DAY = false;
    static boolean PRERVIOUS_DAY = false;
    static boolean ONLY_ADD_NEW_PRODUCTS = false;
    static int MINIMUM_SALES = 1;
    static String DATABASE = "ML2";

    static boolean BRAZIL = false;

    private String baseURL = null;
    private int[] theIntervals = null;

    static String[] urls = new String[]
            {"https://hogar.mercadolibre.com.ar/adornos-decoracion-del/[_SUBINTERVAL]",
             "https://hogar.mercadolibre.com.ar/adornos-decoracion-del-cuadros-carteles-espejos/",
             "https://hogar.mercadolibre.com.ar/textiles-decoracion-alfombras-carpetas/"};


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
                    280, 289, 294, 298, 299, 300, 309, 310, 318, 320, 329, 332, 344, 349, 350, 360, 374, 379, 382, 389, 390, 394, 398, 399, 400, 419, 420,
                    429, 430, 436, 449, 450, 467, 479, 489, 494, 498, 499, 500, 515, 519, 520, 539, 549, 550, 569, 588, 598, 599, 600, 618, 619, 639, 649,
                    650, 674, 689, 698, 699, 700, 719, 720, 733, 749, 750, 779, 780, 789, 799, 800, 819, 839, 849, 851, 859, 862, 882, 898, 899, 900, 949,
                    950, 959, 974, 975, 989, 998, 999, 1000, 1049, 1070, 1085, 1099, 1112, 1149, 1169, 1189, 1199, 1200, 1249, 1273, 1284, 1299, 1307, 1349,
                    1389, 1399, 1423, 1450, 1494, 1496, 1499, 1500, 1559, 1599, 1631, 1689, 1690, 1698, 1699, 1729, 1790, 1798, 1799, 1800, 1828, 1890, 1931,
                    1998, 1999, 2000, 2098, 2140, 2199, 2219, 2276, 2299, 2330, 2340, 2390, 2400, 2458, 2499, 2500, 2580, 2600, 2700, 2790, 2879, 2900, 2999,
                    3000, 3111, 3299, 3499, 3501, 3570, 3648, 3800, 3939, 3999, 4199, 4499, 4650, 4861, 4950, 5000, 5268, 5600, 5942, 6100, 6623, 7562, 8799,
                    10563, 13998, 17000, 19721, 25000, 74999, 2147483647},


            {2500, 2550, 2600, 2799, 2899, 2999, 3090, 3390, 3500, 3600, 3850, 4000, 4500, 5000, 5950, 7000, 9000, 13000, 17999, 23000, 40000, 2147483647},

            {0, 400, 750, 1400, 2300, 3500, 7000, 14000, 30000, 2147483647}

    };

    private static void initVars() {
        Counters.initGlobalRunnerCount();
        globalAndFirstInterval = 0; //arranca en 0
        for (int i = 0; i < globalPageArray.length; i++) {
            globalPageArray[i] = 0;
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



        for (int j = 0; j < urls.length; j++) { //todo tiene que empezar de 0
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
           ProductPageProcessor.processPossiblyPausedProducts(DATABASE, getGlobalDate(),globalProcesedProductList,SAVE,DEBUG);

            VisitCounter.updateVisits(DATABASE, SAVE,DEBUG);
        }


        String msg = "******************************************************\r\n"
                + Counters.getGlobalPageCount() + " paginas procesadas\r\n "
                + Counters.getGlobalProductCount() + " productos procesados\r\n "
                + Counters.getGlobalNewsCount() + " productos con novedades";
        System.out.println(msg);
        Logger.log(msg);

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
                    if (!HttpUtils.isOK(htmlStringFromPage)) { //suponemos que se terminó
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
                            Date lastUpdate = DatabaseHelper.fetchLastUpdate(productId,DATABASE);
                            if (lastUpdate != null) {//producto existente
                                if (!ONLY_ADD_NEW_PRODUCTS) { //ignora los updates
                                    boolean sameDate = Counters.isSameDate(lastUpdate, getGlobalDate());
                                    if (!sameDate || (sameDate && OVERRIDE_TODAYS_RUN)) { //actualizar
                                        int previousTotalSold = DatabaseHelper.fetchTotalSold(productId,DATABASE);
                                        if (totalSold != previousTotalSold) { //actualizar
                                            int newSold = totalSold - previousTotalSold;

                                            if (!HttpUtils.isOK(htmlStringFromProductPage)) {
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
                                                    continue;
                                                    //ignoramos este item
                                                }
                                            }

                                            boolean officialStore = HTMLParseUtils.getOfficialStore(htmlStringFromProductPage);

                                            String seller = HTMLParseUtils.getSeller(htmlStringFromProductPage, officialStore, productUrl);

                                            String lastQuestion = HTMLParseUtils.getLastQuestion(htmlStringFromProductPage);

                                            String previousLastQuestion = DatabaseHelper.fetchLastQuestion(productId,DATABASE);
                                            ArrayList<String> newQuestionsList = HttpUtils.getNewQuestionsFromPreviousLastQuestion(productUrl, httpClient, runnerID, DEBUG, previousLastQuestion);
                                            int newQuestions = newQuestionsList.size();

                                            Counters.incrementGlobalNewsCount();

                                            msg = runnerID + " new sale. productID: " + productId + " quantity: " + newSold;
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
                                    DatabaseHelper.insertProduct(DATABASE,OVERRIDE_TODAYS_RUN,getGlobalDate(),productId, seller, totalSold, lastQuestion, productUrl, officialStore);
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

    private synchronized void addProcesedProductToList(String productId) {
        if (globalProcesedProductList == null) {
            globalProcesedProductList = new ArrayList<String>();
        }
        globalProcesedProductList.add(productId);
    }

}
