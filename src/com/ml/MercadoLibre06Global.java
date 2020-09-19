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

public class MercadoLibre06Global extends Thread {

    MercadoLibre06Global(String baseURL, int[] intervals) {
        this.baseURL = baseURL;
        this.theIntervals = intervals;
    }

    static int globalAndFirstInterval;
    static ArrayList<String> globalProcesedProductList;

    static Date globalDate = null;
    static DateFormat globalDateformat = null;
    static Calendar globalCalendar1 = null;
    static Calendar globalCalendar2 = null;

    static int MAX_THREADS = 40;//14
    static boolean OVERRIDE_TODAYS_RUN = false;
    static boolean SAVE = true;
    static boolean DEBUG = false;
    static boolean FOLLOWING_DAY = true;
    static boolean PRERVIOUS_DAY = false;
    static boolean ONLY_ADD_NEW_PRODUCTS = false;
    static boolean IGNORE_PAUSED = false;
    static boolean IGNORE_VISITS = false;
    static int MINIMUM_SALES = 10;
    static int TIMEOUT_MIN = 180;
    static String DATABASE = "ML6";

    static int MINIMUM_INVOICES = 40000;

    static boolean BRAZIL = false;

    private String baseURL = null;
    private int[] theIntervals = null;

    static String[] urls = new String[]
            {

                    // antigüedades
                    "https://listado.mercadolibre.com.ar/antiguedades/",

                    //gift cards
                    "https://videojuegos.mercadolibre.com.ar/ps-plus",
                    "https://videojuegos.mercadolibre.com.ar/tarjeta",
                    "https://listado.mercadolibre.com.ar/otras-categorias/gift-cards/",
                    "https://videojuegos.mercadolibre.com.ar/tarjetas-prepagas-juegos/",
                    "https://videojuegos.mercadolibre.com.ar/playstation/ps4/otros/",


                    //tecnologia

                    //celulares y telefonia
                    "https://celulares.mercadolibre.com.ar/accesorios/",
                    "https://celulares.mercadolibre.com.ar/repuestos/",
                    "https://celulares.mercadolibre.com.ar/",
                    "https://telefonia.mercadolibre.com.ar/fija-e-inalambrica/",
                    "https://telefonia.mercadolibre.com.ar/handies/",
                    "https://telefonia.mercadolibre.com.ar/radiofrecuencia/",
                    "https://telefonia.mercadolibre.com.ar/ip/",
                    "https://telefonia.mercadolibre.com.ar/centrales-telefonicas/",
                    "https://telefonia.mercadolibre.com.ar/tarifadores-cabinas/",
                    "https://telefonia.mercadolibre.com.ar/fax/",
                    "https://telefonia.mercadolibre.com.ar/otros/",

                    "https://camaras-digitales.mercadolibre.com.ar/camaras-accesorios/",
                    "https://videojuegos.mercadolibre.com.ar/", //consolas

                    "https://computacion.mercadolibre.com.ar/all-in-one/",
                    "https://computacion.mercadolibre.com.ar/apple/",
                    "https://computacion.mercadolibre.com.ar/componentes-pc/",
                    "https://computacion.mercadolibre.com.ar/cajas-sobres-porta-cds/",
                    "https://computacion.mercadolibre.com.ar/discos-virgenes/",
                    "https://computacion.mercadolibre.com.ar/diskettes/",
                    "https://computacion.mercadolibre.com.ar/alimentacion/",
                    "https://computacion.mercadolibre.com.ar/impresoras-accesorios/",
                    "https://computacion.mercadolibre.com.ar/lectores-scanners/",
                    "https://computacion.mercadolibre.com.ar/memorias-ram/",
                    "https://computacion.mercadolibre.com.ar/mini-pcs/",
                    "https://computacion.mercadolibre.com.ar/monitores-accesorios/",
                    "https://notebooks.mercadolibre.com.ar/",
                    "https://computacion.mercadolibre.com.ar/palms-handhelds/",
                    "https://computacion.mercadolibre.com.ar/pc/",
                    "https://computacion.mercadolibre.com.ar/pendrives/",
                    "https://computacion.mercadolibre.com.ar/perifericos/",
                    "https://computacion.mercadolibre.com.ar/procesadores/",
                    "https://computacion.mercadolibre.com.ar/proyectores-pantallas/",
                    "https://computacion.mercadolibre.com.ar/redes/",
                    "https://computacion.mercadolibre.com.ar/software/",
                    "https://computacion.mercadolibre.com.ar/tablets-accesorios/",
                    "https://computacion.mercadolibre.com.ar/ultrabooks/",
                    "https://computacion.mercadolibre.com.ar/otros/",

                    // **** BUSQUEDAS 1 (No repetir)
                    // gift cards
                    "https://videojuegos.mercadolibre.com.ar/psn",
                    //****************************


                    //electronica audio y video
                    "https://electronica.mercadolibre.com.ar/accesorios-audio-y-video/",
                    "https://electronica.mercadolibre.com.ar/audio/",
                    "https://electronica.mercadolibre.com.ar/calculadoras-y-agendas/",
                    "https://electronica.mercadolibre.com.ar/componentes-electronicos/",
                    "https://electronica.mercadolibre.com.ar/drones-accesorios/",
                    "https://electronica.mercadolibre.com.ar/fotocopiadoras/",
                    "https://electronica.mercadolibre.com.ar/gps/",
                    "https://electronica.mercadolibre.com.ar/pilas-cargadores-baterias/",
                    "https://electronica.mercadolibre.com.ar/portarretratos-digitales/",
                    "https://electronica.mercadolibre.com.ar/proyectores-pantallas/",
                    "https://electronica.mercadolibre.com.ar/seguridad-vigilancia/",
                    "https://electronica.mercadolibre.com.ar/soportes/",
                    "https://electronica.mercadolibre.com.ar/tablets-accesorios/",
                    "https://electronica.mercadolibre.com.ar/video/",
                    "https://electronica.mercadolibre.com.ar/otros/",


                    "https://televisores.mercadolibre.com.ar/televisores/",

                    //accesorios de vehiculos
                    "https://vehiculos.mercadolibre.com.ar/accesorios/",


                    // alimentos y bebidas
                    "https://listado.mercadolibre.com.ar/alimentos-bebidas/",

                    // animales y mascotas
                    "https://listado.mercadolibre.com.ar/animales-mascotas/",

                    // arte
                    "https://listado.mercadolibre.com.ar/arte-artesanias/",

                    //belleza y cuidado personal
                    "https://listado.mercadolibre.com.ar/belleza-y-cuidado-personal/",

                    //deportes y fitnes
                    "https://deportes.mercadolibre.com.ar/",

                    //hogar y electrodomesticos
                    "https://listado.mercadolibre.com.ar/electrodomesticos/",  //electrodomesticos y aires
                    "https://hogar.mercadolibre.com.ar/cocina/", // cocina
                    "https://hogar.mercadolibre.com.ar/jardines-exteriores/",
                    "https://listado.mercadolibre.com.ar/herramientas-y-construccion/pisos-paredes-aberturas/",
                    "https://listado.mercadolibre.com.ar/industrias-oficinas/", //industrias y oficinas
                    "https://listado.mercadolibre.com.ar/herramientas-y-construccion/herramientas/",
                    "https://listado.mercadolibre.com.ar/electricidad/",
                    "https://listado.mercadolibre.com.ar/herramientas-y-construccion/construccion/",

                    //bebes
                    "https://listado.mercadolibre.com.ar/bebes/",

                    //libros
                    "https://libros.mercadolibre.com.ar/",

                    //moda (ropa y accesorios)
                    "https://ropa.mercadolibre.com.ar/",
                    "https://bolsos.mercadolibre.com.ar/",
                    "https://zapatillas.mercadolibre.com.ar/",
                    "https://zapatos.mercadolibre.com.ar/",

                    //instrumentos musicales
                    "https://instrumentos.mercadolibre.com.ar/",

                    //joyas y relojes
                    "https://joyas.mercadolibre.com.ar/",

                    //juguetes
                    "https://juegos-juguetes.mercadolibre.com.ar/",

                    //musica y peliculas
                    "https://listado.mercadolibre.com.ar/musica-peliculas-series/",

                    //salud
                    "https://listado.mercadolibre.com.ar/salud-y-equipamiento-medico/",

                    //servicios
                    "https://servicios.mercadolibre.com.ar/",

                    //otros
                    "https://listado.mercadolibre.com.ar/otras-categorias/otros/",

                    //bitcoin
                    "https://listado.mercadolibre.com.ar/criptomonedas/",

                    // **** BUSQUEDAS 2 (No repetir)
                    // gift cards
                    "https://listado.mercadolibre.com.ar/gift-card"
                    //****************************

            };

    static int[][] intervals = new int[][]{

            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //antigüedades

            //gitf cards
            {0, 300, 1100, 2147483647}, //ps plus
            {0, 300, 1100, 2147483647}, //tarjeta
            {0, 300, 1100, 2147483647}, //gift cards (categoria)
            {0, 300, 1100, 2147483647}, //tarjetas-prepagas-juegos
            {0, 300, 1100, 2147483647}, //ps4-otros


            //telefonia
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //accesorios celulares
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //repuestos celulares
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, // celulares
            {0, 3000, 2147483647}, //telefonia fina e inalambrica
            {0, 3000, 2147483647}, //handies
            {0, 3000, 2147483647}, //radio frequencia
            {0, 3000, 2147483647}, //telefonia ip
            {0, 3000, 2147483647}, //centrales telefonicas
            {0, 3000, 2147483647}, //tarifadores y cabinas
            {0, 3000, 2147483647}, //fax
            {0, 3000, 2147483647}, //otros

            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //camaras
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //consolas

            //computacion
            {0, 3000, 2147483647}, // all in one
            {0, 3000, 2147483647}, // apple
            {0, 3000, 2147483647}, // componentes PC
            {0, 3000, 2147483647}, // cajas software
            {0, 3000, 2147483647}, // discos virgenes
            {0, 3000, 2147483647}, // diskettes
            {0, 3000, 2147483647}, // alimentacion
            {0, 3000, 2147483647}, // impresoras
            {0, 3000, 2147483647}, // lectores escanners
            {0, 3000, 2147483647}, // memorias ram
            {0, 3000, 2147483647}, // mini pcs
            {0, 3000, 2147483647}, // monitores y accesorios
            {0, 3000, 2147483647}, // notebooks
            {0, 3000, 2147483647}, // palms
            {0, 3000, 2147483647}, // PCs
            {0, 3000, 2147483647}, // pendrives
            {0, 3000, 2147483647}, // perisféricos
            {0, 3000, 2147483647}, // procesadores
            {0, 3000, 2147483647}, // proyectores
            {0, 3000, 2147483647}, // redes
            {0, 3000, 2147483647}, // software
            {0, 3000, 2147483647}, // tablets
            {0, 3000, 2147483647}, // ultrabooks
            {0, 3000, 2147483647}, // otros

            //busquedas 1  **************
            {0, 300, 1100, 2147483647}, //psn
            //***************************

            //electronica audio y video

            {0, 3000, 2147483647}, // accesorios audio y video
            {0, 3000, 2147483647}, // audio
            {0, 3000, 2147483647}, // calculadoras y agendas
            {0, 3000, 2147483647}, // componentes electronicos
            {0, 3000, 2147483647}, // drones accesorios
            {0, 3000, 2147483647}, // fotocopiadoras
            {0, 3000, 2147483647}, // gps
            {0, 3000, 2147483647}, // pilas cargadores baterias
            {0, 3000, 2147483647}, // portarretratos digitales
            {0, 3000, 2147483647}, // proyectores pantallas
            {0, 3000, 2147483647}, // seguridad vigilancia
            {0, 3000, 2147483647}, // soportes TV
            {0, 3000, 2147483647}, // tablets accesorios
            {0, 3000, 2147483647}, // video
            {0, 3000, 2147483647}, // otros

            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //televisores
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //accesorios de vehiculos
            {0, 3000, 2147483647}, //alimentos y bebidas
            {0, 3000, 2147483647}, //animales y mascotas
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //arte
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //belleza y cuidado personal
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //deportes y fitnes
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //electrodomesticos y aires
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //cocina
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //jardines exteriores
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //pisos paredes aberturas
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //industrias y oficinas
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //herramientas
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //electricidad
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //construccion
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //bebes
            {0, 3000, 2147483647}, //libros
            {0, 3000, 2147483647}, //moda (ropa)
            {0, 3000, 2147483647}, //moda (bolsos)
            {0, 3000, 2147483647}, //moda (zapatos)
            {0, 3000, 2147483647}, //moda (zapatillas)
//            {0,3000,2147483647}, //alimenos y bebidas
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //instrumentos musicales
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //joyas y relojes
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //juguetes
            {0, 3000, 2147483647}, //musica y peliculas
            {0, 3000, 2147483647}, //salud
            {0, 3000, 2147483647}, //servicios
            {0, 3000, 2147483647}, //otros
            {0, 3000, 2147483647}, //bitcoin

            // busquedas 2 *************
            {0, 300, 1100, 2147483647} //gift card
            //***************************
    };

    public static void main(String[] args) {

        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Muzzu\\IdeaProjects\\selenium\\chromedriver_win32_2.4\\chromedriver.exe");
        System.setProperty("webdriver.gecko.driver", "C:\\Users\\Muzzu\\IdeaProjects\\selenium\\geckodriver-v0.20.0-win64\\geckodriver.exe");

        globalDateformat = new SimpleDateFormat("HH:mm:ss.SSS");
        globalCalendar1 = Calendar.getInstance();
        globalCalendar2 = Calendar.getInstance();


        if (intervals.length != urls.length) {//validacion de intervalo
            System.out.println("Error en largos. Intervals:" + intervals.length + " //  urls: " + urls.length);
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

        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
        for (int j = 0; j < urls.length; j++) { //todo tiene que empezar de 0
            //initVars();
            //globalBaseURL=urls[j];
            //golbalIntervals=intervals[j];

            MercadoLibre06Global thread1 = new MercadoLibre06Global(urls[j], intervals[j]);
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
/*
        if (!ONLY_ADD_NEW_PRODUCTS) {
            if (!IGNORE_PAUSED) {
           ProductPageProcessor.processPossiblyPausedProducts(DATABASE, getGlobalDate(),globalProcesedProductList,SAVE,DEBUG);
            }
            if (!IGNORE_VISITS) {
                VisitCounter.updateVisits(DATABASE,SAVE,DEBUG);
            }
        }*/


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

        for (int i = 1; i < this.theIntervals.length; i++) {
            int range1 = this.theIntervals[i - 1] + 1;
            int range2 = this.theIntervals[i];
            String priceRangeStr = "_PriceRange_" + range1 + "-" + range2;
            Logger.log("XXXXXXXXXXXXXXXXXXXXXX " + runnerID + " new interval : " + this.baseURL + priceRangeStr);
            boolean endInterval = false;

            int page = 0;
            while (!endInterval) {
                page++;
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
                uRL += "_DisplayType_G";

                System.out.println(runnerID + " " + uRL);
                Logger.log(runnerID + " " + uRL);
                String htmlStringFromPage = HttpUtils.getHTMLStringFromPage(uRL, httpClient, DEBUG, true);
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
                        continue;//salteamos
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
                                DatabaseHelper.insertProduct(DATABASE, getGlobalDate(), productId, seller, totalSold, lastQuestion, productUrl, officialStore);
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
