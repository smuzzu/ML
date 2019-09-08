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

public class MercadoLibre06b extends Thread {

    MercadoLibre06b(String custId, int[] intervals) {
        this.custId = custId;
        this.theIntervals = intervals;
    }


/*
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
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};*/


    static ArrayList<String> globalProcesedProductList;

    static Date globalDate = null;
    static DateFormat globalDateformat = null;
    static Calendar globalCalendar1 = null;
    static Calendar globalCalendar2 = null;

    static int MAX_THREADS = 14;
    static boolean OVERRIDE_TODAYS_RUN = false;
    static boolean SAVE = true;
    static boolean DEBUG = false;
    static boolean FOLLOWING_DAY = false;
    static boolean PRERVIOUS_DAY = false;
    static boolean ONLY_ADD_NEW_PRODUCTS = false;
    static int MINIMUM_SALES = 1;
    static int TIMEOUT_MIN = 50;
    static String DATABASE = "ML6";

    static boolean BRAZIL = false;

    private String custId = null;
    private int[] theIntervals = null;

    static String[] sellers = new String[]
            {
                    //empresas

                    "REPUESTODOVENTAS",
                    "ELSITIOREPUESTOS",
                    "ANIKASHOP",
                    "DIGITAL_MEGA_STORE",
                    "CERRAJERIA63",
                    "ITPROUSER",
                    "CONGRESOINSUMOSSA",
                    "EGRENTUS2007",
                    "DECOHOGAR",
                    "TIENDAIKEA.COM.AR",
                    "KADIMA-STUFF",
                    "JUGUETERIA+MARGARITA",
                    "WARNESTELAS2001",
                    "OLD-RIDER",
                    "COMERCIALICSA",
                    "SANITARIOS-ARIETA",
                    "LACASA+DELFARO",
                    "AMITOSAI+STORES",
                    "BABY+KINGDOM",

                    "DAGOSTINA+VANITORYS",
                    "GALARDI-MUEBLES",
                    "ELEGANCE_JARDIN",
                    "CHESTER-MANIA",
                    "DEBORAOCHOTECO",
                    "DEBUENDISE%C3%91O",
                    "GUIK+DESIGN",
                    "ELECTROMEDINA",

                    "SASI36787",
                    ".COLGALO.COMO.QUIERAS.",
                    "AC-TEC",
                    "ALAVUELTA+CERAMICOS",

                    "GREENDECO",
                    "SHESHU+WEB",
                    "DISTRITOBLANCO",
                    "N2M-ELECTRONICA",
                    "ARGENSHOP+BSAS",
                    "CASAANDREA+LOCAL",
                    "INTER+VENT",
                    "DECOR+ITUZAINGO",
                    "GUIRNALDA+DELUCES",
                    "VENTAIMPORTACION",
                    "CONFORTSURSA",
                    "MLAT_ARG",
                    "ELECTROLED+SA",
                    "DANPER+COMPLEMENTOS",
                    "MI-BIOMBO-ES-TU-BIOMBO",
                    "PUFFYFIACAS",
                    "ARBOVERDE.SA",
                    "CARELLI+COMPANY",
                    "ESCANDINAVIA+ARG",
                    "VST+ONLINE",
                    "ELMUNDODELASCAJAS",
                    "COLOMBRARO+ONLINE",
                    "SUFERRETERIAONLINE",
                    "OTHERBRANDS",
                    "CAPRICHOSA+CAROLA",
                    "PLASTICHEMONDO",
                    "IMPERIO+DESIGN",
                    "INTERMARBLE-SRL",
                    "CEGEVAHOGAR",
                    "BAIRES+4",
                    "BALL_SELLINGS",
                    "AMOBLAMIENTOS+A.S",
                    "MUEBO",
                    "GLOBAL+GROUP10",
                    "DAMPLAST-RAMOS",
                    "MOBILARG",
                    "INTEGRAL+DECO",
                    "Desillas",
                    "LIVINGSTYLEDESIGN",
                    "SU-OFFICE",
                    "MUNDO+SHOPS",
                    "LEATHERPLAST_MUEBLES",
                    "INSUOFFICE",
                    "AMV-TIENDA+ONLINE"


            };


/*
    static String[] custIds = new String[]
            {
                    //empresas

                    "188818717", //REPUESTODOVENTAS
                    "142783840", //ELSITIOREPUESTOS
                    "269965609", //ANIKASHOP
                    "34916517",  //DIGITAL_MEGA_STORE
                    "8184118",   //CERRAJERIA63
                    "115764017", //ITPROUSER
                    "381884980", //CONGRESOINSUMOSSA
                    "34801784",  //EGRENTUS2007
                    "716559",    //DECOHOGAR
                    "326345769", //TIENDAIKEA.COM.AR
                    "248636763", //KADIMA-STUFF
                    "218026996", //JUGUETERIA+MARGARITA //todo ver que paso
                    "51855129",  //WARNESTELAS2001
                    "279131651", //OLD-RIDER
                    "61842076",  //COMERCIALICSA
                    "7625198",   //SANITARIOS-ARIETA
                    "22138542",  //LACASA+DELFARO
                    "156168198", //AMITOSAI+STORES
                    "27420742",  //BABY+KINGDOM

                    "193932040", //DAGOSTINA+VANITORYS
                    "80687042",  //GALARDI-MUEBLES
                    "289913573", //ELEGANCE_JARDIN
                    "49041694",  //CHESTER-MANIA
                    "321242158", //DEBORAOCHOTECO
                    "22547481",  //DEBUENDISE%C3%91O
                    "177237076", //GUIK+DESIGN
                    "5170079",   //ELECTROMEDINA

                    "239427239", //SASI36787
                    "259840875", //.COLGALO.COMO.QUIERAS.
                    "44758411",  //AC-TEC
                    "188747318", //ALAVUELTA+CERAMICOS

                    "205076801", //GREENDECO
                    "145263251", //SHESHU+WEB
                    "230484551", //DISTRITOBLANCO
                    "76393523",  //N2M-ELECTRONICA
                    "150718965", //ARGENSHOP+BSAS
                    "276064236", //CASAANDREA+LOCAL
                    "129456144", //INTER+VENT
                    "63658014",  //DECOR+ITUZAINGO
                    "255677435", //GUIRNALDA+DELUCES
                    "120289637", //VENTAIMPORTACION
                    "148361972", //CONFORTSURSA
                    "81375635",  //MLAT_ARG
                    "137391595", //ELECTROLED+SA
                    "3137551",   //DANPER+COMPLEMENTOS
                    "95707744",  //MI-BIOMBO-ES-TU-BIOMBO
                    "79732240",  //PUFFYFIACAS
                    "147876488", //ARBOVERDE.SA
                    "185866176", //CARELLI+COMPANY
                    "90892683",  //ESCANDINAVIA+ARG
                    "91472071",  //VST+ONLINE
                    "65600763",  //ELMUNDODELASCAJAS
                    "141994870", //COLOMBRARO+ONLINE
                    "92418743",  //SUFERRETERIAONLINE
                    "235476092", //OTHERBRANDS
                    "94070763",  //CAPRICHOSA+CAROLA
                    "299909924", //PLASTICHEMONDO
                    "217249881", //IMPERIO+DESIGN
                    "46312868",  //INTERMARBLE-SRL
                    "95496785",  //CEGEVAHOGAR
                    "76762412",  //BAIRES+4
                    "36831755",  //BALL_SELLINGS
                    "54376753",  //AMOBLAMIENTOS+A.S
                    "31544243",  //MUEBO
                    "64473055",  //GLOBAL+GROUP10
                    "222656594", //DAMPLAST-RAMOS
                    "95680100",  //MOBILARG
                    "24951567",  //INTEGRAL+DECO
                    "139386086", //Desillas
                    "49355841",  //LIVINGSTYLEDESIGN
                    "78794591",  //SU-OFFICE
                    "99356770",  //MUNDO+SHOPS
                    "158835319", //LEATHERPLAST_MUEBLES
                    "97562082",  //INSUOFFICE
                    "121000172"  //AMV-TIENDA+ONLINE
            };

*/


    static int[][] intervals = new int[][]{

            {0, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3500, 4000, 5000, 8000, 2147483647}, //REPUESTODOVENTAS
            {0, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3500, 4000, 5000, 8000, 2147483647}, //ELSITIOREPUESTOS
            {0, 70, 100, 150, 200, 250, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2147483647}, //ANIKASHOP
            {0, 5000, 5500, 6000, 7000, 8000, 11000, 16000, 2147483647},  //DIGITAL_MEGA_STORE
            {0, 200, 400, 700, 1400, 5000, 2147483647},   //CERRAJERIA63
            {0, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3500, 4000, 5000, 8000, 2147483647}, //ITPROUSER
            {0, 8000, 2147483647}, //CONGRESOINSUMOSSA
            {0, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3500, 4000, 5000, 8000, 2147483647},  //EGRENTUS2007
            {0, 2000, 4000, 8000, 12000, 15000, 22000, 2147483647},    //DECOHOGAR
            {0, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3500, 4000, 5000, 8000, 2147483647}, //TIENDAIKEA.COM.AR
            {0, 700, 1000, 3000, 2147483647}, //KADIMA-STUFF
            {0, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3500, 4000, 5000, 8000, 2147483647}, //JUGUETERIA+MARGARITA
            {0, 200, 300, 400, 500, 600, 750, 850, 1000, 1200, 1500, 1800, 2100, 2500, 2900, 3400, 4000, 5000, 6000, 8000, 12000, 2147483647},  //WARNESTELAS2001
            {0, 198, 200, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3400, 3500, 4000, 5000, 7000, 8000, 14000, 16000, 17999, 18000, 25000, 2147483647}, //OLD-RIDER
            {0, 60, 150, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3500, 4000, 5000, 8000, 2147483647},  //COMERCIALICSA
            {0, 5000, 9000, 13000, 20000, 2147483647},   //SANITARIOS-ARIETA
            {0, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3500, 4000, 5000, 2147483647},  //LACASA+DELFARO
            {0, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3500, 4000, 5000, 8000, 2147483647}, //AMITOSAI+STORES
            {0, 1500, 2500, 2147483647},  //BABY+KINGDOM

            {0, 2147483647}, //DAGOSTINA+VANITORYS
            {0, 2147483647},  //GALARDI-MUEBLES
            {0, 2147483647}, //ELEGANCE_JARDIN
            {0, 2147483647},  //CHESTER-MANIA
            {0, 2147483647}, //DEBORAOCHOTECO
            {0, 2147483647},  //DEBUENDISE%C3%91O
            {0, 2147483647}, //GUIK+DESIGN
            {0, 2147483647},   //ELECTROMEDINA

            {0, 2147483647}, //SASI36787
            {0, 2147483647}, //.COLGALO.COMO.QUIERAS.
            {0, 2147483647},  //AC-TEC
            {0, 2147483647}, //ALAVUELTA+CERAMICOS

            {0, 2147483647}, //GREENDECO
            {0, 2147483647}, //SHESHU+WEB
            {0, 2147483647}, //DISTRITOBLANCO
            {0, 2147483647},  //N2M-ELECTRONICA
            {0, 2147483647}, //ARGENSHOP+BSAS
            {0, 2147483647}, //CASAANDREA+LOCAL
            {0, 2147483647}, //INTER+VENT
            {0, 2147483647},  //DECOR+ITUZAINGO
            {0, 2147483647}, //GUIRNALDA+DELUCES
            {0, 2147483647}, //VENTAIMPORTACION
            {0, 2147483647}, //CONFORTSURSA
            {0, 2147483647},  //MLAT_ARG
            {0, 2147483647}, //ELECTROLED+SA
            {0, 2147483647},   //DANPER+COMPLEMENTOS
            {0, 2147483647},  //MI-BIOMBO-ES-TU-BIOMBO
            {0, 2147483647},  //PUFFYFIACAS
            {0, 2147483647}, //ARBOVERDE.SA
            {0, 2147483647}, //CARELLI+COMPANY
            {0, 2147483647},  //ESCANDINAVIA+ARG
            {0, 2147483647},  //VST+ONLINE
            {0, 2147483647},  //ELMUNDODELASCAJAS
            {0, 2147483647}, //COLOMBRARO+ONLINE
            {0, 2147483647},  //SUFERRETERIAONLINE
            {0, 2147483647}, //OTHERBRANDS
            {0, 2147483647},  //CAPRICHOSA+CAROLA
            {0, 2147483647}, //PLASTICHEMONDO
            {0, 2147483647}, //IMPERIO+DESIGN
            {0, 2147483647},  //INTERMARBLE-SRL
            {0, 2147483647},  //CEGEVAHOGAR
            {0, 2147483647},  //BAIRES+4
            {0, 2147483647},  //BALL_SELLINGS
            {0, 2147483647},  //AMOBLAMIENTOS+A.S
            {0, 2147483647},  //MUEBO
            {0, 2147483647},  //GLOBAL+GROUP10
            {0, 2147483647}, //DAMPLAST-RAMOS
            {0, 2147483647},  //MOBILARG
            {0, 2147483647},  //INTEGRAL+DECO
            {0, 2147483647}, //Desillas
            {0, 2147483647},  //LIVINGSTYLEDESIGN
            {0, 2147483647},  //SU-OFFICE
            {0, 2147483647},  //MUNDO+SHOPS
            {0, 2147483647}, //LEATHERPLAST_MUEBLES
            {0, 2147483647},  //INSUOFFICE
            {0, 2147483647} //AMV-TIENDA+ONLINE

    };

    /*
    private static void initVars(){
        globalRunnerCount=0;
        globalAndFirstInterval =0; //arranca en 0

        for (int i=0; i<globalPageArray.length; i++) {
            globalPageArray[i]=0;
        }
    }*/


    public static void main(String[] args) {

        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Muzzu\\IdeaProjects\\selenium\\chromedriver_win32_2.4\\chromedriver.exe");
        System.setProperty("webdriver.gecko.driver", "C:\\Users\\Muzzu\\IdeaProjects\\selenium\\geckodriver-v0.20.0-win64\\geckodriver.exe");

        globalDateformat = new SimpleDateFormat("HH:mm:ss.SSS");
        globalCalendar1 = Calendar.getInstance();
        globalCalendar2 = Calendar.getInstance();


        if (intervals.length != sellers.length) {//validacion de intervalo
            System.out.println("Error en largos. Intervals:" + intervals.length + " //  sellers: " + sellers.length);
            System.exit(0);
        }

        for (int i = 0; i < intervals.length; i++) { //validacion de intervalo
            for (int j = 1; j < intervals[i].length; j++) {
                if (intervals[i][j - 1] >= intervals[i][j]) {
                    System.out.println("Error en intervalo #" + i + " // " + intervals[i][j - 1] + "-" + intervals[i][j]);
                    System.exit(0);
                }
            }
        }

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();


        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
        for (int j = 0; j < sellers.length; j++) { //todo tiene que empezar de 0


            MercadoLibre06b thread1 = new MercadoLibre06b(sellers[j], intervals[j]);


            int retries = 0;
            boolean retry = true;
            String seller = sellers[j];
            String uRL = HTMLParseUtils.PROFILE_BASE_URL + seller;

            String htmlStringFromPage = null;

            while (retry && retries < 4) {
                retries++;
                htmlStringFromPage = HttpUtils.getHTMLStringFromPage(uRL, httpClient, DEBUG);
                if (HttpUtils.isOK(htmlStringFromPage)) {
                    retry = false;
                } else {
                    try {
                        httpClient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    httpClient = null;
                    httpClient = HttpUtils.buildHttpClient();
                    Logger.log("R000 Error en getHTMLStringFromPage II intento #" + retries + " " + uRL);
                    try {
                        Thread.sleep(25000);
                    } catch (InterruptedException e) {
                        Logger.log(e);
                    }
                }
            }
            if (!HttpUtils.isOK(htmlStringFromPage)) { //suponemos que se terminó
                // pero tambien hacemos pausa por si es problema de red
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Logger.log(e);
                }
                Logger.log("R000 hmlstring from page is null " + uRL);
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                httpClient = null;
                httpClient = HttpUtils.buildHttpClient();
                continue;
            }

            int pos1 = htmlStringFromPage.indexOf("_CustId_");
            if (pos1 == -1) {
                String errMsg = "***** Este vendedor no tiene productos publicados ahora " + uRL;
                System.out.println(errMsg);
                Logger.log(errMsg);
                continue;
            }
            pos1 += 8;
            int pos2 = htmlStringFromPage.indexOf("\"", pos1);

            String custId = htmlStringFromPage.substring(pos1, pos2);


            thread1.custId = custId;
            thread1.theIntervals = intervals[j];
            threadArrayList.add(thread1);
            thread1.start();

            long currentTime = System.currentTimeMillis();
            long timeoutTime = currentTime + TIMEOUT_MIN * 60l * 1000l;

            while (MAX_THREADS < Thread.activeCount()) {

                try {
                    Thread.sleep(30000l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentTime = System.currentTimeMillis();
                if (currentTime > timeoutTime) {
                    System.out.println("Error en de timeout.  Demasiado tiempo sin terminar de procesar uno entre " + MAX_THREADS + " vendedores");
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

        if (!ONLY_ADD_NEW_PRODUCTS) {
           //ProductPageProcessor.processPossiblyPausedProducts(DATABASE, getGlobalDate(),globalProcesedProductList,SAVE,DEBUG);

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



    public void run() {

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
        int itemsInPage = 48;
        String runnerID = "R" + Counters.getGlobalRunnerCount();

        // boolean processFinished=false;

        String custIdStr = "_DisplayType_G_CustId_" + this.custId;
        String sellerIdStr = "_DisplayType_G_seller*id_" + this.custId;

        System.out.println(runnerID + " " + HTMLParseUtils.PRODUCT_LIST_BASE_URL + custIdStr);
        Logger.log(runnerID + " " + HTMLParseUtils.PRODUCT_LIST_BASE_URL + custIdStr);


        for (int i = 1; i < this.theIntervals.length; i++) {
            int range1 = this.theIntervals[i - 1] + 1;
            int range2 = this.theIntervals[i];
            String priceRangeStr = "_PriceRange_" + range1 + "-" + range2;
            //log ("XXXXXXXXXXXXXXXXXXXXXX "+runnerID+" new customer : "+ custIds[i]);

            boolean endInterval = false;
            //getPage(interval, true);
            int page = 0;
            while (!endInterval) {
                page++;
                if (page == 43) {
                    Logger.log("XXXXXXXXXXXXXXXXXXXXXX este intervalo no pudo ser recorrido por completo " +
                            HTMLParseUtils.PRODUCT_LIST_BASE_URL + custIdStr + priceRangeStr + sellerIdStr);
                    System.out.println("XXXXXXXXXXXXXXXXXXXXXX este intervalo no pudo ser recorrido por completo " +
                            HTMLParseUtils.PRODUCT_LIST_BASE_URL + custIdStr + priceRangeStr + sellerIdStr);
                    endInterval = true;
                    continue;
                }//se acabose

                Counters.incrementGlobalPageCount();
                int since = (page - 1) * itemsInPage + 1;
                String sinceStr = "_Desde_" + since;
                String uRL = HTMLParseUtils.PRODUCT_LIST_BASE_URL + sinceStr + custIdStr + priceRangeStr + sellerIdStr;
                if (page == 1) {
                    uRL = HTMLParseUtils.PRODUCT_LIST_BASE_URL + custIdStr + priceRangeStr + sellerIdStr;
                }
                uRL+="_DisplayType_G";

                int retries = 0;
                boolean retry = true;
                String htmlStringFromPage = null;

                while (retry && retries < 20) {
                    retries++;
                    htmlStringFromPage = HttpUtils.getHTMLStringFromPage(uRL, httpClient, DEBUG);
                    if (HttpUtils.isOK(htmlStringFromPage)) {
                        retry = false;
                    } else {
                        try {
                            httpClient.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        httpClient = null;
                        httpClient = HttpUtils.buildHttpClient();
                        Logger.log(runnerID + " Error en getHTMLStringFromPage II intento #" + retries + " " + uRL);
                        try {
                            Thread.sleep(25000);
                        } catch (InterruptedException e) {
                            Logger.log(e);
                        }
                    }
                }
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
