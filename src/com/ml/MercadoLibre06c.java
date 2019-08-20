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

public class MercadoLibre06c extends Thread {

    int[] currentIinterval;
    String currentUrl;



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
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            */

/*
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
            "chaco/"};*/

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

    static int MAX_THREADS=12;
    static int TIMEOUT_MIN=180;
    static boolean OVERRIDE_TODAYS_RUN=false;
    static boolean SAVE =false;
    static boolean DEBUG=false;
    static boolean FOLLOWING_DAY = true;
    static boolean PRERVIOUS_DAY = false;
    static boolean ONLY_ADD_NEW_PRODUCTS=false;
    static int MINIMUM_SALES=10;
    static int MINIMUM_INVOICES =40000;
    static String DATABASE="ML6";

    static int MAX_THREADS_VISITS = 30;

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
    //static String globalBaseURL=null;
    //static int[] golbalIntervals = null;

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

    static int[][] intervals = new int[][] {

            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //antigüedades

            //gitf cards
            {0,300,1100,2147483647}, //ps plus
            {0,300,1100,2147483647}, //tarjeta
            {0,300,1100,2147483647}, //gift cards (categoria)
            {0,300,1100,2147483647}, //tarjetas-prepagas-juegos
            {0,300,1100,2147483647}, //ps4-otros




            //telefonia
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //accesorios celulares
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //repuestos celulares
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, // celulares
            {0,3000,2147483647}, //telefonia fina e inalambrica
            {0,3000,2147483647}, //handies
            {0,3000,2147483647}, //radio frequencia
            {0,3000,2147483647}, //telefonia ip
            {0,3000,2147483647}, //centrales telefonicas
            {0,3000,2147483647}, //tarifadores y cabinas
            {0,3000,2147483647}, //fax
            {0,3000,2147483647}, //otros

            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //camaras
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //consolas

            //computacion
            {0,3000,2147483647}, // all in one
            {0,3000,2147483647}, // apple
            {0,3000,2147483647}, // componentes PC
            {0,3000,2147483647}, // cajas software
            {0,3000,2147483647}, // discos virgenes
            {0,3000,2147483647}, // diskettes
            {0,3000,2147483647}, // alimentacion
            {0,3000,2147483647}, // impresoras
            {0,3000,2147483647}, // lectores escanners
            {0,3000,2147483647}, // memorias ram
            {0,3000,2147483647}, // mini pcs
            {0,3000,2147483647}, // monitores y accesorios
            {0,3000,2147483647}, // notebooks
            {0,3000,2147483647}, // palms
            {0,3000,2147483647}, // PCs
            {0,3000,2147483647}, // pendrives
            {0,3000,2147483647}, // perisféricos
            {0,3000,2147483647}, // procesadores
            {0,3000,2147483647}, // proyectores
            {0,3000,2147483647}, // redes
            {0,3000,2147483647}, // software
            {0,3000,2147483647}, // tablets
            {0,3000,2147483647}, // ultrabooks
            {0,3000,2147483647}, // otros

            //busquedas 1  **************
            {0,300,1100,2147483647}, //psn
            //***************************

            //electronica audio y video

            {0,3000,2147483647}, // accesorios audio y video
            {0,3000,2147483647}, // audio
            {0,3000,2147483647}, // calculadoras y agendas
            {0,3000,2147483647}, // componentes electronicos
            {0,3000,2147483647}, // drones accesorios
            {0,3000,2147483647}, // fotocopiadoras
            {0,3000,2147483647}, // gps
            {0,3000,2147483647}, // pilas cargadores baterias
            {0,3000,2147483647}, // portarretratos digitales
            {0,3000,2147483647}, // proyectores pantallas
            {0,3000,2147483647}, // seguridad vigilancia
            {0,3000,2147483647}, // soportes TV
            {0,3000,2147483647}, // tablets accesorios
            {0,3000,2147483647}, // video
            {0,3000,2147483647}, // otros

            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //televisores
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //accesorios de vehiculos
            {0,3000,2147483647}, //alimentos y bebidas
            {0,3000,2147483647}, //animales y mascotas
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //arte
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //belleza y cuidado personal
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //deportes y fitnes
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //electrodomesticos y aires
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //cocina
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //jardines exteriores
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //pisos paredes aberturas
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //industrias y oficinas
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //herramientas
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //electricidad
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //construccion
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //bebes
            {0,3000,2147483647}, //libros
            {0,3000,2147483647}, //moda (ropa)
            {0,3000,2147483647}, //moda (bolsos)
            {0,3000,2147483647}, //moda (zapatos)
            {0,3000,2147483647}, //moda (zapatillas)
//            {0,3000,2147483647}, //alimenos y bebidas
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //instrumentos musicales
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //joyas y relojes
            {0,3000,4000,5000,6000,8000,10000,15000,25000,2147483647}, //juguetes
            {0,3000,2147483647}, //musica y peliculas
            {0,3000,2147483647}, //salud
            {0,3000,2147483647}, //servicios
            {0,3000,2147483647}, //otros
            {0,3000,2147483647}, //bitcoin

            // busquedas 2 *************
            {0,300,1100,2147483647} //gift card
            //***************************
    };

/*
    private static void initVars(){
        globalRunnerCount=0;
        globalAndFirstInterval =0; //arranca en 0
        for (int i=0; i<globalPageArray.length; i++) {
            globalPageArray[i]=0;
        }
    }*/

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

    private static void log(Throwable throwable){
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
                log("Error updating visits "+productId+" "+ quantity + " " +date);
            }



        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {


        globalDateformat = new SimpleDateFormat("HH:mm:ss.SSS");
        globalCalendar1 = Calendar.getInstance();
        globalCalendar2 = Calendar.getInstance();


        if (intervals.length != urls.length){//validacion de intervalo
            System.out.println("Error en largos. Intervals:" + intervals.length + " //  urls: " + urls.length);
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
            saveRunInitialization("MercadoLibre06c",MAX_THREADS);
        }

        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
        for (int j=0; j<urls.length; j++) { //todo tiene que empezar de 0
            //initVars();
            //globalBaseURL=urls[j];
            //golbalIntervals=intervals[j];

            MercadoLibre06c thread1 = new MercadoLibre06c();
            thread1.currentUrl=urls[j];
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

        //updateVisits(); lo hacemos en el b

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

/*
    synchronized private static int  getPage(int interval, boolean reset){
        if (reset){
            globalPageArray[interval]=0;
        } else {
            globalPageArray[interval]++;
        }
        return globalPageArray[interval];
    }*/

/*
    synchronized private static int  getInterval(){
        globalAndFirstInterval++;
        return globalAndFirstInterval;
    }*/

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
            props.setProperty("password", "password");
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
            props.setProperty("password", "password");
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
            props.setProperty("password", "password");
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

        //boolean processFinished=false;
        for (int i=1; i<this.currentIinterval.length; i++){
            //Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
            //int interval=getInterval();
/*
            if (interval>=golbalIntervals.length){
                processFinished=true;
                continue;
            }*/
            int range1=this.currentIinterval[i-1]+1;
            int range2=this.currentIinterval[i];
            String priceRangeStr="_PriceRange_"+range1+"-"+range2;
            log ("XXXXXXXXXXXXXXXXXXXXXX "+runnerID+" new interval : "+ this.currentUrl+priceRangeStr);
/*
            String[] subintervals = new String[]{""};
            if (range1==range2){
                subintervals = globalSubIntervals;
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            }*/
//            for (String subinterval : subintervals) {
                boolean endInterval = false;
                //getPage(interval, true);
                int page=0;
                while (!endInterval) {
                    page++;
                    if (page == 43) {
                        log("XXXXXXXXXXXXXXXXXXXXXX este intervalo no pudo ser recorrido por completo "
                                + this.currentUrl + priceRangeStr);
                        System.out.println("XXXXXXXXXXXXXXXXXXXXXX este intervalo no pudo ser recorrido por completo "
                                + this.currentUrl + priceRangeStr);
                        endInterval = true;
                        continue;
                    }//se acabose

                    incrementGlobalPageCount();
                    int since = (page - 1) * itemsInPage + 1;
                    String sinceStr = "_Desde_" + since;
                    String uRL = this.currentUrl + sinceStr + priceRangeStr;
                    if (page == 1) {
                        uRL = this.currentUrl + priceRangeStr;
                    }

                    int retries = 0;
                    boolean retry=true;
                    String htmlStringFromPage =null;

                    while (retry && retries<20) {
                        retries++;
                        htmlStringFromPage = getHTMLStringFromPage(uRL,httpClient);
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
                            log(runnerID+" Error en getHTMLStringFromPage II intento #"+retries+" "+uRL);
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

                        if (totalSold >= MINIMUM_SALES || (totalSold * price>= MINIMUM_INVOICES)) { //si no figura venta no le doy bola
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
                                                log(msg);
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
                                log(msg);
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
        log(msg);
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
