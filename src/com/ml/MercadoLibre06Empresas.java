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

import org.apache.commons.lang3.StringUtils;

import org.apache.http.impl.client.CloseableHttpClient;


/**
 * Created by Muzzu on 11/12/2017.
 */

public class MercadoLibre06Empresas extends Thread {

    MercadoLibre06Empresas(String custId, int[] intervals) {
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

    static int MAX_THREADS = 40;//14
    static boolean OVERRIDE_TODAYS_RUN = false;
    static boolean SAVE = true;
    static boolean DEBUG = false;
    static boolean FOLLOWING_DAY = false;
    static boolean PRERVIOUS_DAY = false;
    static boolean ONLY_ADD_NEW_PRODUCTS = false;
    static boolean IGNORE_PAUSED = false;
    static boolean IGNORE_VISITS = false;
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
                    "DECOHOGAR",
                    "KADIMA-STUFF",
                    "JUGUETERIA+MARGARITA",
                    "OLD-RIDER",
                    "SANITARIOS-ARIETA",
                    "LACASA+DELFARO",
                    "AMITOSAI+STORES",
                    "BABY+KINGDOM",
                    "PLANETAZENOK",
                    "PRESTIGIO+STORE",
                    "SALES+COM",
                    "PINTURERIASREX",

                    "DAGOSTINA+VANITORYS",
                    "GALARDI-MUEBLES",
                    "ELEGANCE_JARDIN",
                    "CHESTER-MANIA",
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
                    "MI-BIOMBO-ES-TU-BIOMBO",
                    "PUFFYFIACAS",
                    "CARELLI+COMPANY",
                    "VST+ONLINE",
                    "SUFERRETERIAONLINE",
                    "OTHERBRANDS",
                    "CAPRICHOSA+CAROLA",
                    "PLASTICHEMONDO",
                    "INTERMARBLE-SRL",
                    "CEGEVAHOGAR",
                    "BAIRES+4",
                    "TISERA",
                    "QUIEROESO+YA",
                    "MUEBLESLANUS",
                    "MORSHOP",
                    "NEWMO",
                    "BALL_SELLINGS",
                    "MUEBO",
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

                    //hafele + frank
                    "ALLSURHERRAJES",
                    "ATILIOOSCARFARAONE",
                    "BOLZANRG",
                    "CALZAVARA+SRL",
                    "COMERCIAL+CAB",
                    "DELBRE+SRL",
                    "DISTRIBUIDORA+MECH",
                    "HERRAFE.COM",
                    "HERRAJEMANIA",
                    "HERRAJES+DIAGONAL",
                    "HERRAJES+ESTILO",
                    "HERRAJES+MAGLIOLA",
                    "HERRAJES+P.KRAMAR",
                    "HERRAJESFORTALEZA",
                    "HERRAJESPATRICIOS",
                    "HERRAJESREVELLI",
                    "HERRAJESTRONADOR",
                    "HERRAJESVEIGA+LINE",
                    "HERRAMELSC",
                    "KRAMAR+LAFERRERE",
                    "LAMARINA.",
                    "MADERERALLAVALLOLSA",
                    "MUNDOCIMA",

                    //hafele + frank otros
                    "AUTO_UNO_CASEROS",
                    "CIGAPLACSRL",
                    "ESTILO+DK",
                    "HCI+OBRAS",
                    "HERGUTI45",
                    "HERRAJES+TOTITO",
                    "HERRAJESBALDANSRL",
                    "HERRAJESHILTON1",
                    "HERRAJESMITRE",
                    "HERRAJES+TUYU",
                    "HERRAPEI",
                    "HT+HERRAJES",
                    "LUCIAZAFFRANI",


                    //alfombreros
                    "ABELEDOJAVIER",
                    "AGBPISOSYREVESTIMIENTOS",
                    "ALFOMBRAS+HIPOLITO",
                    "AMBIANCEDECO",
                    "AMBIANCEPALERMO",
                    "AYLUMA",
                    "BEELLESTYLE",
                    "COMO.EN.CASA",
                    "CORIMEL+ALFOMBRAS",
                    "DECORACIONESGIMBATTI",
                    "DECORD+CENTER",
                    "ELCHEALMOHADON",
                    "ELUNEYDECO",
                    "EXITO+DISEÑO",
                    "FULLHOUSEWEB",
                    "GLAUCO0112005",
                    "GRDO2013",
                    "IMPORT+MIS",
                    "JUAMPY-712",
                    "KEOPS_ALFOMBRAS",
                    "KREATEX_SA",
                    "LEDERHD",
                    "MENTAYROMERODECO",
                    "MGBALFOMBRAS",
                    "MICKYCO1",
                    "MUEBLES-COSAS",
                    "RASI6160647",
                    "TIENDA+BAZHARS",
                    "TODD+ARGENTINA",

                    //alfombreros otros
                    "ABETEYCIAMDP",
                    "BELLIZZIHNOSSRL",
                    "BRUMS+DECO",
                    "COLCHONEXPRESS",
                    "COSECOSRL",
                    "DECO-GSD",
                    "DECONAMOR+REGALOS",
                    "DECOR+ITUZAINGO",
                    "EL+RELLENON",
                    "ENTUCASA.ONLINE",
                    "ESTANCIA-MUEBLES",
                    "FORM+HOMESTORE",
                    "GAMULANYCAMPERAS+ARGENTINA",
                    "GPINDUSTRIAL",
                    "GUALASD",
                    "HALCON+SHOP",
                    "MARCARIMPORT",
                    "MARYL77",
                    "MASAL.BA",
                    "MEWADECO",
                    "MISIONLIVE",
                    "PRIMERO+UNO",
                    "PRODUCTOPATAGONIA",
                    "ROCIOPAISSAN",
                    "ROSANAMARICELMACIEL",
                    "RUPLESS",
                    "SHEM+GROUP",
                    "SANTAFE+MARKET",
                    "SOUL+DESIGN",
                    "TIENDA+BALLERINA",
                    "TOBLANC",

                    //mormetal
                    "MUNDO+OFI-MAX",
                    "HERRAJES+CASTELMAX",

                    "MODULAR+DRAWERS"


            };


    static int[][] intervals = new int[][]{

            {0, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3500, 4000, 5000, 8000, 2147483647}, //REPUESTODOVENTAS
            {0, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3500, 4000, 5000, 8000, 2147483647}, //ELSITIOREPUESTOS
            {0, 70, 100, 150, 200, 250, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2147483647}, //ANIKASHOP
            {0, 5000, 5500, 6000, 7000, 8000, 11000, 16000, 2147483647},  //DIGITAL_MEGA_STORE
            {0, 200, 400, 700, 1400, 2500, 5000, 2147483647},   //CERRAJERIA63
            {0, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3500, 4000, 5000, 8000, 2147483647}, //ITPROUSER
            {0, 2000, 4000, 8000, 12000, 15000, 22000, 2147483647},    //DECOHOGAR
            {0, 700, 1000, 3000, 2147483647}, //KADIMA-STUFF
            {0, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3500, 4000, 5000, 8000, 2147483647}, //JUGUETERIA+MARGARITA
            {0, 198, 200, 300, 500, 700, 900, 1000, 1200, 1350, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3400, 3500, 4000, 5000, 7000, 8000, 14000, 16000, 17999, 18000, 25000, 2147483647}, //OLD-RIDER
            {0, 5000, 9000, 13000, 20000, 2147483647},   //SANITARIOS-ARIETA
            {0, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3500, 4000, 5000, 2147483647},  //LACASA+DELFARO
            {0, 300, 500, 700, 900, 1000, 1200, 1500, 1800, 2000, 2200, 2500, 2800, 3000, 3500, 4000, 5000, 8000, 2147483647}, //AMITOSAI+STORES
            {0, 1500, 2500, 2147483647},  //BABY+KINGDOM
            {0, 5000, 2147483647},//PLANETAZENOK
            {0, 5000, 2147483647},//PRESTIGIO+STORE
            {0, 5000, 2147483647},//SALES+COM
            {0, 5000, 2147483647},//PINTURERIASREX

            {0, 2147483647}, //DAGOSTINA+VANITORYS
            {0, 2147483647}, //GALARDI-MUEBLES
            {0, 2147483647}, //ELEGANCE_JARDIN
            {0, 2147483647}, //CHESTER-MANIA
            {0, 2147483647}, //GUIK+DESIGN
            {0, 2147483647}, //ELECTROMEDINA

            {0, 2147483647}, //SASI36787
            {0, 2147483647}, //.COLGALO.COMO.QUIERAS.
            {0, 2147483647}, //AC-TEC
            {0, 2147483647}, //ALAVUELTA+CERAMICOS

            {0, 2147483647}, //GREENDECO
            {0, 2000, 2147483647}, //SHESHU+WEB
            {0, 2147483647}, //DISTRITOBLANCO
            {0, 2147483647}, //N2M-ELECTRONICA
            {0, 2147483647}, //ARGENSHOP+BSAS
            {0, 2147483647}, //CASAANDREA+LOCAL
            {0, 2147483647}, //INTER+VENT
            {0, 2147483647}, //DECOR+ITUZAINGO
            {0, 2147483647}, //GUIRNALDA+DELUCES
            {0, 2147483647}, //VENTAIMPORTACION
            {0, 2147483647}, //CONFORTSURSA
            {0, 2147483647}, //MLAT_ARG
            {0, 2147483647}, //ELECTROLED+SA
            {0, 2147483647}, //MI-BIOMBO-ES-TU-BIOMBO
            {0, 2147483647}, //PUFFYFIACAS
            {0, 2147483647}, //CARELLI+COMPANY
            {0, 2147483647}, //VST+ONLINE
            {0, 2147483647}, //SUFERRETERIAONLINE
            {0, 2147483647}, //OTHERBRANDS
            {0, 2147483647}, //CAPRICHOSA+CAROLA
            {0, 2147483647}, //PLASTICHEMONDO
            {0, 2147483647}, //INTERMARBLE-SRL
            {0, 2147483647}, //CEGEVAHOGAR
            {0, 2147483647}, //BAIRES+4
            {0, 2147483647}, //TISERA
            {0, 2147483647}, //QUIEROESO+YA
            {0, 2147483647}, //MUEBLESLANUS
            {0, 2147483647}, //MORSHOP
            {0, 2147483647}, //NEWMO
            {0, 2147483647}, //BALL_SELLINGS
            {0, 2147483647}, //MUEBO
            {0, 2147483647}, //DAMPLAST-RAMOS
            {0, 2147483647}, //MOBILARG
            {0, 2147483647}, //INTEGRAL+DECO
            {0, 2147483647}, //Desillas
            {0, 2147483647}, //LIVINGSTYLEDESIGN
            {0, 2147483647}, //SU-OFFICE
            {0, 2147483647}, //MUNDO+SHOPS
            {0, 2147483647}, //LEATHERPLAST_MUEBLES
            {0, 2147483647}, //INSUOFFICE
            {0, 2147483647}, //AMV-TIENDA+ONLINE

            //hafele + frank
            {0, 2147483647}, //ALLSURHERRAJES
            {0, 2147483647}, //ATILIOOSCARFARAONE
            {0, 2147483647}, //BOLZANRG
            {0, 2147483647}, //CALZAVARA+SRL
            {0, 2147483647}, //COMERCIAL+CAB
            {0, 2147483647}, //DELBRE+SRL
            {0, 2147483647}, //DISTRIBUIDORA+MECH
            {0, 2147483647}, //HERRAFE.COM
            {0, 2147483647}, //HERRAJEMANIA
            {0, 2147483647}, //HERRAJES+DIAGONAL
            {0, 2147483647}, //HERRAJES+ESTILO
            {0, 2147483647}, //HERRAJES+MAGLIOLA
            {0, 2147483647}, //HERRAJES+P.KRAMAR
            {0, 2147483647}, //HERRAJESFORTALEZA
            {0, 2147483647}, //HERRAJESPATRICIOS
            {0, 2147483647}, //HERRAJESREVELLI
            {0, 2147483647}, //HERRAJESTRONADOR
            {0, 2147483647}, //HERRAJESVEIGA+LINE
            {0, 2147483647}, //HERRAMELSC
            {0, 2147483647}, //KRAMAR+LAFERRERE
            {0, 2147483647}, //LAMARINA.
            {0, 2147483647}, //MADERERALLAVALLOLSA
            {0, 2147483647}, //MUNDOCIMA

            //hafele + frank otros
            {0, 2147483647}, //AUTO_UNO_CASEROS
            {0, 2147483647}, //CIGAPLACSRL
            {0, 2147483647}, //ESTILO+DK
            {0, 2147483647}, //HCI+OBRAS
            {0, 2147483647}, //HERGUTI45
            {0, 2147483647}, //HERRAJES+TOTITO
            {0, 2147483647}, //HERRAJESBALDANSRL
            {0, 2147483647}, //HERRAJESHILTON1
            {0, 2147483647}, //HERRAJESMITRE
            {0, 2147483647}, //HERRAJES+TUYU
            {0, 2147483647}, //HERRAPEI"
            {0, 2147483647}, //HT+HERRAJES
            {0, 2147483647}, //LUCIAZAFFRANI

            //alfombreros
            {0, 2147483647}, //ABELEDOJAVIER
            {0, 2147483647}, //AGBPISOSYREVESTIMIENTOS
            {0, 2147483647}, //ALFOMBRAS+HIPOLITO
            {0, 2147483647}, //AMBIANCEDECO
            {0, 2147483647}, //AMBIANCEPALERMO
            {0, 2147483647}, //AYLUMA
            {0, 2147483647}, //BEELLESTYLE
            {0, 2147483647}, //COMO.EN.CASA
            {0, 2147483647}, //CORIMEL+ALFOMBRAS
            {0, 2147483647}, //DECORACIONESGIMBATTI
            {0, 2147483647}, //DECORD+CENTER
            {0, 2147483647}, //ELCHEALMOHADON
            {0, 2147483647}, //ELUNEYDECO
            {0, 2147483647}, //EXITO+DISEÑO
            {0, 2147483647}, //FULLHOUSEWEB
            {0, 2147483647}, //GLAUCO0112005
            {0, 2147483647}, //GRDO2013
            {0, 2147483647}, //IMPORT+MIS
            {0, 2147483647}, //JUAMPY-712
            {0, 2147483647}, //KEOPS_ALFOMBRAS
            {0, 2147483647}, //KREATEX_SA
            {0, 2147483647}, //LEDERHD
            {0, 2147483647}, //MENTAYROMERODECO
            {0, 2147483647}, //MGBALFOMBRAS
            {0, 2147483647}, //MICKYCO1
            {0, 2147483647}, //MUEBLES-COSAS
            {0, 2147483647}, //RASI6160647
            {0, 2147483647}, //TIENDA+BAZHARS
            {0, 2147483647}, //TODD+ARGENTINA

            //alfombreros otros
            {0, 2147483647}, //ABETEYCIAMDP
            {0, 2147483647}, //BELLIZZIHNOSSRL
            {0, 2147483647}, //BRUMS+DECO
            {0, 2147483647}, //COLCHONEXPRESS
            {0, 2147483647}, //COSECOSRL
            {0, 2147483647}, //DECO-GSD
            {0, 2147483647}, //DECONAMOR+REGALOS
            {0, 2147483647}, //DECOR+ITUZAINGO
            {0, 2147483647}, //EL+RELLENON
            {0, 2147483647}, //ENTUCASA.ONLINE
            {0, 2147483647}, //ESTANCIA-MUEBLES
            {0, 2147483647}, //FORM+HOMESTORE
            {0, 2147483647}, //GAMULANYCAMPERAS+ARGENTINA
            {0, 2147483647}, //GPINDUSTRIAL
            {0, 2147483647}, //GUALASD
            {0, 2147483647}, //HALCON+SHOP
            {0, 2147483647}, //MARCARIMPORT
            {0, 2147483647}, //MARYL77
            {0, 2147483647}, //MASAL.BA
            {0, 2147483647}, //MEWADECO
            {0, 2147483647}, //MISIONLIVE
            {0, 2147483647}, //PRIMERO+UNO
            {0, 2147483647}, //PRODUCTOPATAGONIA
            {0, 2147483647}, //ROCIOPAISSAN
            {0, 2147483647}, //ROSANAMARICELMACIEL
            {0, 2147483647}, //RUPLESS
            {0, 2147483647}, //SHEM+GROUP
            {0, 2147483647}, //SANTAFE+MARKET
            {0, 2147483647}, //SOUL+DESIGN
            {0, 2147483647}, //TIENDA+BALLERINA
            {0, 2147483647}, //TOBLANC

            //mormetal
            {0, 2147483647},//MUNDO+OFI-MAX,
            {0, 2147483647},//HERRAJES+CASTELMAX

            {0, 2147483647} //MODULAR+DRAWERS

    };


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


            MercadoLibre06Empresas thread1 = new MercadoLibre06Empresas(sellers[j], intervals[j]);


            int retries = 0;
            boolean retry = true;
            String seller = sellers[j];
            String uRL = HTMLParseUtils.PROFILE_BASE_URL + seller;

            String htmlStringFromPage = null;

            while (retry && retries < 4) {
                retries++;
                htmlStringFromPage = HttpUtils.getHTMLStringFromPage(uRL, httpClient, DEBUG, true);
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
            if (!IGNORE_PAUSED) {
                //ProductPageProcessor.processPossiblyPausedProducts(DATABASE, getGlobalDate(),globalProcesedProductList,SAVE,DEBUG);
            }
            if (!IGNORE_VISITS) {
                VisitCounter.updateVisits(DATABASE, SAVE, DEBUG);
            }
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
                uRL += "_DisplayType_G";

                int retries = 0;
                boolean retry = true;
                String htmlStringFromPage = null;

                while (retry && retries < 20) {
                    retries++;
                    htmlStringFromPage = HttpUtils.getHTMLStringFromPage(uRL, httpClient, DEBUG, true);
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
                    int resultSectionPos = htmlStringFromPage.indexOf("search-results");
                String resultListHMTLData = null;
                if (resultSectionPos == -1) {
                        if (htmlStringFromPage.indexOf("Escrib")>0
                                && htmlStringFromPage.indexOf("en el buscador lo que quer")>0
                                && htmlStringFromPage.indexOf("s encontrar")>0) {
                            String msg = "No se pudo obtener ningun resultado en este intervalo "+ uRL;
                            Logger.log(msg);
                            endInterval = true;
                            continue;
                        }
                        Logger.log("Error getting search-results tag on page " + page + " " + uRL);
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
                    if (title == null || title.length() == 0) {
                        Logger.log(runnerID + " invalid title on page " + page + " url " + uRL);
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

                    String htmlStringFromProductPage = HttpUtils.getHTMLStringFromPage(productUrl, httpClient, DEBUG, true);
                    if (!HttpUtils.isOK(htmlStringFromProductPage)) {
                        String msg = "All retries failed.  Ignoring this url " + uRL;
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
                        httpClient=null;
                        httpClient = HttpUtils.buildHttpClient();
                        continue;

                    }

                    int totalSold = HTMLParseUtils.getTotalSold(htmlStringFromProductPage, productUrl);
                    if (totalSold == -1) {//error
                        String msg = "totalSold unknown " + uRL;
                        System.out.println(msg);
                        Logger.log(msg);
                        continue;//salteamos este producto
                    }

                    String msg = runnerID + " processing page " + page + " | " + productId + " | " + totalSold + " | " + price + " | " + title + " | " + productUrl;
                    System.out.println(msg);

                    if (totalSold >= MINIMUM_SALES) { //si no figura venta no le doy bola
                        Date lastUpdate = DatabaseHelper.fetchLastUpdate(productId, DATABASE);
                        if (lastUpdate != null) {//producto existente
                            if (!ONLY_ADD_NEW_PRODUCTS) { //ignora los updates
                                boolean sameDate = Counters.isSameDate(lastUpdate, getGlobalDate());
                                if (!sameDate || (sameDate && OVERRIDE_TODAYS_RUN)) { //actualizar
                                    int previousTotalSold = DatabaseHelper.fetchTotalSold(productId, DATABASE);
                                    if (totalSold != previousTotalSold) { //actualizar
                                        int newSold = totalSold - previousTotalSold;

                                        boolean officialStore = HTMLParseUtils.getOfficialStore(htmlStringFromProductPage);

                                        String seller = HTMLParseUtils.getSeller(htmlStringFromProductPage, officialStore, productUrl);

                                        int reviews = HTMLParseUtils.getReviews(htmlStringFromProductPage, uRL);
                                        double stars = 0.0;
                                        if (reviews > 0) {
                                            stars = HTMLParseUtils.getStars(htmlStringFromProductPage, productUrl);
                                        }

                                        String lastQuestion = HTMLParseUtils.getLastQuestion(htmlStringFromProductPage);

                                        String previousLastQuestion = DatabaseHelper.fetchLastQuestion(productId, DATABASE);
                                        ArrayList<String> newQuestionsList = HttpUtils.getNewQuestionsFromPreviousLastQuestion(productUrl, productId, httpClient, runnerID, DEBUG, previousLastQuestion);
                                        int newQuestions = newQuestionsList.size();

                                        Counters.incrementGlobalNewsCount();

                                        msg = runnerID + " new sale. productID: " + productId + " quantity: " + newSold;
                                        System.out.println(msg);
                                        Logger.log(msg);

                                        if (SAVE) {
                                            DatabaseHelper.updateProductAddActivity(DATABASE, OVERRIDE_TODAYS_RUN, getGlobalDate(), productId, seller, officialStore, totalSold, newSold, title, productUrl, reviews, stars, price, newQuestions, lastQuestion, page, 0, shipping, discount, premium);
                                        }
                                    } else {//no vendió esta semana
                                        addProcesedProductToList(productId);
                                    }
                                }
                            }
                        } else { //agregar vendedor

                            boolean officialStore = HTMLParseUtils.getOfficialStore(htmlStringFromProductPage);

                            String seller = HTMLParseUtils.getSeller(htmlStringFromProductPage, officialStore, productUrl);

                            String lastQuestion = HTMLParseUtils.getLastQuestion(htmlStringFromProductPage);

                            Counters.incrementGlobalNewsCount();

                            msg = runnerID + " new product ID: " + productId + " Total Sold: " + totalSold;
                            System.out.println(msg);
                            Logger.log(msg);

                            if (SAVE) {
                                DatabaseHelper.insertProduct(DATABASE, getGlobalDate(), productId, seller, -1, totalSold, lastQuestion, productUrl, officialStore);
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
