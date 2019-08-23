package com.ml;

import org.apache.commons.lang3.StringUtils;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;

import com.ml.utils.Logger;
import com.ml.utils.HttpUtils;

/**
 * Created by Muzzu on 11/12/2017.
 */

public class MercadoLibre06b extends Thread {

    int[] currentIinterval;
    String currentCustId;



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

    static int globalPageCount=0;
    static int globalProdutCount=0;
    static int globalNewsCount=0;
    static int globalRunnerCount=0; //static int globalRunnerCount;
    //static int globalAndFirstInterval;

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
    static boolean DEBUG=false;
    static boolean FOLLOWING_DAY = false;
    static boolean PRERVIOUS_DAY = false;
    static boolean ONLY_ADD_NEW_PRODUCTS=false;
    static int MINIMUM_SALES=1;
    static int TIMEOUT_MIN=50;
    static int MAX_THREADS_VISITS = 30;
    static String DATABASE="ML6";

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

    static String BASE_URL = "https://listado.mercadolibre.com.ar/";
    //static int[] golbalIntervals = null;


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
                    "AMV-TIENDA+ONLINE",
                    "COMPUSOLUTIONS.SUC.JUAN",
                    "COMPUSOLUTIONS.SUC.TONY"


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


    static int[][] intervals = new int[][] {

            {0,300,500,700,900,1000,1200,1500,1800,2000,2200,2500,2800,3000,3500,4000,5000,8000,2147483647}, //REPUESTODOVENTAS
            {0,300,500,700,900,1000,1200,1500,1800,2000,2200,2500,2800,3000,3500,4000,5000,8000,2147483647}, //ELSITIOREPUESTOS
            {0,70,100,150,200,250,300,500,700,900,1000,1200,1500,1800,2000,2200,2147483647}, //ANIKASHOP
            {0,5000,5500,6000,7000,8000,11000,16000,2147483647},  //DIGITAL_MEGA_STORE
            {0,200,400,700,1400,5000,2147483647},   //CERRAJERIA63
            {0,300,500,700,900,1000,1200,1500,1800,2000,2200,2500,2800,3000,3500,4000,5000,8000,2147483647}, //ITPROUSER
            {0,8000,2147483647}, //CONGRESOINSUMOSSA
            {0,300,500,700,900,1000,1200,1500,1800,2000,2200,2500,2800,3000,3500,4000,5000,8000,2147483647},  //EGRENTUS2007
            {0,2000,4000,8000,12000,15000,22000,2147483647},    //DECOHOGAR
            {0,300,500,700,900,1000,1200,1500,1800,2000,2200,2500,2800,3000,3500,4000,5000,8000,2147483647}, //TIENDAIKEA.COM.AR
            {0,700,1000,3000,2147483647}, //KADIMA-STUFF
            {0,300,500,700,900,1000,1200,1500,1800,2000,2200,2500,2800,3000,3500,4000,5000,8000,2147483647}, //JUGUETERIA+MARGARITA
            {0,200,300,400,500,600,750,850,1000,1200,1500,1800,2100,2500,2900,3400,4000,5000,6000,8000,12000,2147483647},  //WARNESTELAS2001
            {0,198,200,300,500,700,900,1000,1200,1500,1800,2000,2200,2500,2800,3000,3400,3500,4000,5000,7000,8000,14000,16000,17999,18000,25000,2147483647}, //OLD-RIDER
            {0,60,150,300,500,700,900,1000,1200,1500,1800,2000,2200,2500,2800,3000,3500,4000,5000,8000,2147483647},  //COMERCIALICSA
            {0,5000,9000,13000,20000,2147483647},   //SANITARIOS-ARIETA
            {0,300,500,700,900,1000,1200,1500,1800,2000,2200,2500,2800,3000,3500,4000,5000,2147483647},  //LACASA+DELFARO
            {0,300,500,700,900,1000,1200,1500,1800,2000,2200,2500,2800,3000,3500,4000,5000,8000,2147483647}, //AMITOSAI+STORES
            {0,1500,2500,2147483647},  //BABY+KINGDOM

            {0,2147483647}, //DAGOSTINA+VANITORYS
            {0,2147483647},  //GALARDI-MUEBLES
            {0,2147483647}, //ELEGANCE_JARDIN
            {0,2147483647},  //CHESTER-MANIA
            {0,2147483647}, //DEBORAOCHOTECO
            {0,2147483647},  //DEBUENDISE%C3%91O
            {0,2147483647}, //GUIK+DESIGN
            {0,2147483647},   //ELECTROMEDINA

            {0,2147483647}, //SASI36787
            {0,2147483647}, //.COLGALO.COMO.QUIERAS.
            {0,2147483647},  //AC-TEC
            {0,2147483647}, //ALAVUELTA+CERAMICOS

            {0,2147483647}, //GREENDECO
            {0,2147483647}, //SHESHU+WEB
            {0,2147483647}, //DISTRITOBLANCO
            {0,2147483647},  //N2M-ELECTRONICA
            {0,2147483647}, //ARGENSHOP+BSAS
            {0,2147483647}, //CASAANDREA+LOCAL
            {0,2147483647}, //INTER+VENT
            {0,2147483647},  //DECOR+ITUZAINGO
            {0,2147483647}, //GUIRNALDA+DELUCES
            {0,2147483647}, //VENTAIMPORTACION
            {0,2147483647}, //CONFORTSURSA
            {0,2147483647},  //MLAT_ARG
            {0,2147483647}, //ELECTROLED+SA
            {0,2147483647},   //DANPER+COMPLEMENTOS
            {0,2147483647},  //MI-BIOMBO-ES-TU-BIOMBO
            {0,2147483647},  //PUFFYFIACAS
            {0,2147483647}, //ARBOVERDE.SA
            {0,2147483647}, //CARELLI+COMPANY
            {0,2147483647},  //ESCANDINAVIA+ARG
            {0,2147483647},  //VST+ONLINE
            {0,2147483647},  //ELMUNDODELASCAJAS
            {0,2147483647}, //COLOMBRARO+ONLINE
            {0,2147483647},  //SUFERRETERIAONLINE
            {0,2147483647}, //OTHERBRANDS
            {0,2147483647},  //CAPRICHOSA+CAROLA
            {0,2147483647}, //PLASTICHEMONDO
            {0,2147483647}, //IMPERIO+DESIGN
            {0,2147483647},  //INTERMARBLE-SRL
            {0,2147483647},  //CEGEVAHOGAR
            {0,2147483647},  //BAIRES+4
            {0,2147483647},  //BALL_SELLINGS
            {0,2147483647},  //AMOBLAMIENTOS+A.S
            {0,2147483647},  //MUEBO
            {0,2147483647},  //GLOBAL+GROUP10
            {0,2147483647}, //DAMPLAST-RAMOS
            {0,2147483647},  //MOBILARG
            {0,2147483647},  //INTEGRAL+DECO
            {0,2147483647}, //Desillas
            {0,2147483647},  //LIVINGSTYLEDESIGN
            {0,2147483647},  //SU-OFFICE
            {0,2147483647},  //MUNDO+SHOPS
            {0,2147483647}, //LEATHERPLAST_MUEBLES
            {0,2147483647},  //INSUOFFICE
            {0,2147483647}, //AMV-TIENDA+ONLINE
            {0,2147483647}, //COMPUSOLUTIONS.SUC.JUAN
            {0,2147483647}  //COMPUSOLUTIONS.SUC.TONY

    };

    /*
    private static void initVars(){
        globalRunnerCount=0;
        globalAndFirstInterval =0; //arranca en 0

        for (int i=0; i<globalPageArray.length; i++) {
            globalPageArray[i]=0;
        }
    }*/

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

    private static void updateVisits() {

        String msg = "\nProcesando Visitas";
        System.out.println(msg);
        Logger.log(msg);

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
                msg = "Error getting latest movements "+date2;
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

        ArrayList<String> zeroVisitsList=processAllVisits(allProductIDs, date2, dateOnQueryStr);
        msg="Reintentando los ceros";
        System.out.println(msg);
        Logger.log(msg);
        zeroVisitsList=processAllVisits(zeroVisitsList, date2, dateOnQueryStr); //insistimos 2 veces mas cuando visitas devuelve cero
        System.out.println(msg);
        Logger.log(msg);
        processAllVisits(zeroVisitsList, date2, dateOnQueryStr);

        msg="Visitas Procesadas: "+allProductIDs.size();
        System.out.println(msg);
        Logger.log(msg);

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
                Logger.log("Error updating visits "+productId+" "+ quantity + " " +date);
            }



        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {


        globalDateformat = new SimpleDateFormat("HH:mm:ss.SSS");
        globalCalendar1 = Calendar.getInstance();
        globalCalendar2 = Calendar.getInstance();

        updateVisits();
        System.exit(0);

        if (intervals.length != sellers.length){//validacion de intervalo
            System.out.println("Error en largos. Intervals:" + intervals.length + " //  sellers: " + sellers.length);
            System.exit(0);
        }

        for (int i=0; i<intervals.length; i++){ //validacion de intervalo
            for (int j=1; j<intervals[i].length; j++) {
                if (intervals[i][j - 1] >= intervals[i][j]) {
                    System.out.println("Error en intervalo #" + i + " // " + intervals[i][j - 1] + "-" + intervals[i][j]);
                    System.exit(0);
                }
            }
        }

        if (SAVE){
            saveRunInitialization(BASE_URL,MAX_THREADS);
        }


        CloseableHttpClient httpClient = buildHttpClient();


        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
        for (int j=0; j<sellers.length; j++) { //todo tiene que empezar de 0


            MercadoLibre06b thread1 = new MercadoLibre06b();


            int retries = 0;
            boolean retry=true;
            String seller = sellers[j];
            String uRL=PROFILE_BASE_URL+seller;

            String htmlStringFromPage =null;

            while (retry && retries<4) {
                retries++;
                htmlStringFromPage = HttpUtils.getHTMLStringFromPage(uRL,httpClient,DEBUG);
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
                    Logger.log("R000 Error en getHTMLStringFromPage II intento #"+retries+" "+uRL);
                    try {
                        Thread.sleep(25000);
                    } catch (InterruptedException e) {
                        Logger.log(e);
                    }
                }
            }
            if (htmlStringFromPage == null) { //suponemos que se terminó
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
                httpClient=null;
                httpClient = buildHttpClient();
                continue;
            }

            int pos1=htmlStringFromPage.indexOf("_CustId_");
            if (pos1==-1){
                String errMsg="***** Este vendedor no tiene productos publicados ahora "+uRL;
                System.out.println(errMsg);
                Logger.log(errMsg);
                continue;
            }
            pos1+=8;
            int pos2 = htmlStringFromPage.indexOf("\"",pos1);

            String custId=htmlStringFromPage.substring(pos1,pos2);


            thread1.currentCustId=custId;
            thread1.currentIinterval=intervals[j];
            threadArrayList.add(thread1);
            thread1.start();

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

        updateVisits();

        String msg = globalPageCount+" paginas procesadas\n "
                    +globalProdutCount+" productos procesados\n "
                    +globalNewsCount+" productos con novedades";
        System.out.println(msg);
        Logger.log(msg);

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
                Logger.log("Couldn't insert record into runs table");
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
                Logger.log("couldn't update row in runs table");
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
                Logger.log("Couldn't insert product I");
            }
        }catch(SQLException e){
            Logger.log("Couldn't insert product II");
            Logger.log(e);
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
                globalUpdateProduct = connection.prepareStatement("UPDATE public.productos SET totalvendidos = ?, lastupdate=?, url=?, lastquestion=?, proveedor=?, tiendaoficial=?, inhabilitado=false WHERE id = ?;");
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
                Logger.log("Couldn't update product "+productId);
            }

            if (OVERRIDE_TODAYS_RUN){//no sabemos si hay registro pero por las dudas removemos
                if (globalRemoveActivity==null){
                    globalRemoveActivity=connection.prepareStatement("DELETE FROM public.movimientos WHERE idproducto=? and fecha=?");
                }
                globalRemoveActivity.setString(1,productId);
                globalRemoveActivity.setDate(2,getGlobalDate());
                int removedRecords=globalRemoveActivity.executeUpdate();
                if (removedRecords>=1){
                    Logger.log("Record removed on activity table date: "+getGlobalDate()+" productId: "+productId);
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
                Logger.log("Couln't insert a record in activity table "+productId);
            }

            connection.commit();

        }catch(SQLException e){
            Logger.log("I couldn't add activity due to SQLException "+url);
            Logger.log(e);
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
                Logger.log("Couldn't get last update I "+productId);
            }
            if (rs.next()){
                lastUpdate=rs.getDate(1);
            }
        }catch(SQLException e){
            Logger.log("Couldn't get last update II "+productId);
            Logger.log(e);
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
                Logger.log("Couldn't get total sold i"+productId);
                return 0;
            }

            if (rs.next()){
                totalSold=rs.getInt(1);
            }
        }catch(SQLException e){
            e.printStackTrace();
            Logger.log("Couldn't get total sold ii"+productId);
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
                Logger.log("Couldn't get last question i "+productId);
                return null;
            }

            if (rs.next()){
                lastQuestion=rs.getString(1);
            }
        }catch(SQLException e){
            Logger.log("Couldn't get last question ii "+productId);
            Logger.log(e);
        }
        return lastQuestion;
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

    public void run(){

        CloseableHttpClient httpClient = buildHttpClient();
        int itemsInPage=48;
        String runnerID="R"+getGlobalRunnerCount();

       // boolean processFinished=false;

        String custIdStr= "_CustId_"+this.currentCustId;
        String sellerIdStr="_seller*id_"+this.currentCustId;

        System.out.println(runnerID+" "+ BASE_URL + custIdStr);
        Logger.log(runnerID+" "+ BASE_URL + custIdStr);


        for (int i=1; i<this.currentIinterval.length; i++){
            int range1=this.currentIinterval[i-1]+1;
            int range2=this.currentIinterval[i];
            String priceRangeStr="_PriceRange_"+range1+"-"+range2;
            //log ("XXXXXXXXXXXXXXXXXXXXXX "+runnerID+" new customer : "+ custIds[i]);

            boolean endInterval = false;
            //getPage(interval, true);
            int page=0;
            while (!endInterval) {
                page++;
                if (page == 43) {
                    Logger.log("XXXXXXXXXXXXXXXXXXXXXX este intervalo no pudo ser recorrido por completo "+
                            BASE_URL + custIdStr + priceRangeStr + sellerIdStr);
                    System.out.println("XXXXXXXXXXXXXXXXXXXXXX este intervalo no pudo ser recorrido por completo "+
                            BASE_URL + custIdStr + priceRangeStr + sellerIdStr);
                    endInterval = true;
                    continue;
                }//se acabose

                incrementGlobalPageCount();
                int since = (page - 1) * itemsInPage + 1;
                String sinceStr = "_Desde_" + since;
                String uRL = BASE_URL +  sinceStr + custIdStr + priceRangeStr + sellerIdStr;
                if (page == 1) {
                    uRL = BASE_URL + custIdStr + priceRangeStr + sellerIdStr;
                }

                    int retries = 0;
                    boolean retry=true;
                    String htmlStringFromPage =null;

                    while (retry && retries<20) {
                        retries++;
                        htmlStringFromPage = HttpUtils.getHTMLStringFromPage(uRL,httpClient,DEBUG);
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
                            Logger.log(runnerID+" Error en getHTMLStringFromPage II intento #"+retries+" "+uRL);
                            try {
                                Thread.sleep(25000);
                            } catch (InterruptedException e) {
                                Logger.log(e);
                            }
                        }
                    }
                    if (htmlStringFromPage == null) { //suponemos que se terminó
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
                        httpClient = buildHttpClient();
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
                        Logger.log(runnerID+" null title on page " + page + " url " + uRL);
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
                            Logger.log(runnerID+" I couldn't get the discount on " + productUrl);
                            Logger.log(e);
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
                        Logger.log(runnerID+" I couldn't get the price on " + productUrl);
                        Logger.log(e);
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
                                                Logger.log("I couldn't get total sold on " + productUrl);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    String htmlStringFromProductPage = null;
                    if (totalSold == 0 && isUsed) {
                        htmlStringFromProductPage = HttpUtils.getHTMLStringFromPage(productUrl,httpClient,DEBUG);
                        if (htmlStringFromProductPage == null) {
                            // hacemos pausa por si es problema de red
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                Logger.log(e);
                            }
                            Logger.log(runnerID+" hmlstring from page 2 is null " + uRL);
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
                                                    Logger.log("I couldn't get total sold on " + productUrl);
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
                                    Logger.log(runnerID+" I couldn't get reviews on " + reviewsStr);
                                    Logger.log(e);
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
                                            htmlStringFromProductPage = HttpUtils.getHTMLStringFromPage(productUrl, httpClient,DEBUG);
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
                                            if (sellerPos0 > 0) {
                                                sellerPos1 = htmlStringFromProductPage.indexOf(PROFILE_BASE_URL, sellerPos0);
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
                                            Logger.log(msg);
                                        }else {
                                            seller=formatSeller(seller);
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
                                        String htmlStringFromQuestionsPage = HttpUtils.getHTMLStringFromPage(questionsURL,httpClient,DEBUG);
                                        if (htmlStringFromQuestionsPage == null) {
                                            // hacemos pausa por si es problema de red
                                            try {
                                                Thread.sleep(5000);
                                            } catch (InterruptedException e) {
                                                Logger.log(e);
                                            }
                                            Logger.log(runnerID+" hmlstring from page 2 is null " + uRL);
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
                                                int j=0;
                                                for (String question: allquestionsOnPage){
                                                    if (lastQuestion==null){
                                                        lastQuestion=question;
                                                    }
                                                    if (question.equals(previousLastQuestion)){
                                                        newQuestions=j;
                                                        break;
                                                    }
                                                    j++;
                                                }
                                            }
                                        }

                                        msg = runnerID+" new sale. productID: " + productId + " quantity: " + newSold;
                                        System.out.println(msg);
                                        Logger.log(msg);
                                        if (SAVE) {
                                            updateProductAddActivity(productId, seller, officialStore, totalSold, newSold, title, productUrl, reviews, stars, price, newQuestions, lastQuestion, page, shipping, discount, premium);
                                        }
                                    }
                                }
                            }
                        } else { //agregar vendedor

                            if (htmlStringFromProductPage == null) {
                                htmlStringFromProductPage = HttpUtils.getHTMLStringFromPage(productUrl, httpClient,DEBUG);
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
                                Logger.log(msg);
                            }else {
                                seller=formatSeller(seller);
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
                            Logger.log(msg);
                            if (SAVE) {
                                insertProduct(productId, seller, totalSold, lastQuestion, productUrl, officialStore);
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

    private static String formatSeller(String seller) {
        try {//decode seller url
            seller = URLDecoder.decode(seller, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Logger.log("something went wrong trying to decode the seller " + seller);
            Logger.log(e);
        }
        return seller;
    }

    private static synchronized boolean isSameDate(Date date1, Date date2){
        globalCalendar1.setTime(date1);
        globalCalendar2.setTime(date2);
        boolean sameDay = globalCalendar1.get(Calendar.YEAR) == globalCalendar2.get(Calendar.YEAR) &&
                globalCalendar1.get(Calendar.DAY_OF_YEAR) == globalCalendar2.get(Calendar.DAY_OF_YEAR);
        return sameDay;
    }

}
