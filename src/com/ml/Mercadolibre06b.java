package com.ml;

import com.ml.utils.Counters;
import com.ml.utils.HttpUtils;
import com.ml.utils.Item;
import com.ml.utils.Logger;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.HashMap;

public class Mercadolibre06b {

    static final String DATABASE = "ML6";
    static final Boolean ONLY_RELEVANT = true;
    static final int MINIMUM_SALES = 10;
    static final boolean FOLLOWING_DAY = true;
    static final boolean PREVIOUS_DAY = false;

    static String[] webBaseUrls = new String[]
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
                    "https://telefonia.mercadolibre.com.ar/ip/",
                    "https://telefonia.mercadolibre.com.ar/centrales-telefonicas/",
                    "https://telefonia.mercadolibre.com.ar/fax/",
                    "https://telefonia.mercadolibre.com.ar/otros/",

                    "https://camaras-digitales.mercadolibre.com.ar/camaras-accesorios/",
                    "https://videojuegos.mercadolibre.com.ar/", //consolas

                    "https://computacion.mercadolibre.com.ar/all-in-one/",
                    "https://computacion.mercadolibre.com.ar/apple/",
                    "https://computacion.mercadolibre.com.ar/componentes-pc/",
                    "https://computacion.mercadolibre.com.ar/almacenamiento-discos-accesorios/",
                    "https://computacion.mercadolibre.com.ar/estabilizadores-ups/",
                    "https://computacion.mercadolibre.com.ar/impresion/",
                    "https://computacion.mercadolibre.com.ar/lectores-scanners/",
                    "https://computacion.mercadolibre.com.ar/memorias-ram/",
                    "https://computacion.mercadolibre.com.ar/mini-pcs/",
                    "https://computacion.mercadolibre.com.ar/monitores-accesorios/",
                    "https://notebooks.mercadolibre.com.ar/",
                    "https://computacion.mercadolibre.com.ar/pc-escritorio/",
                    "https://computacion.mercadolibre.com.ar/perifericos-pc/",
                    "https://computacion.mercadolibre.com.ar/componentes-pc/procesadores/",
                    "https://computacion.mercadolibre.com.ar/proyectores-pantallas/",
                    "https://computacion.mercadolibre.com.ar/conectividad-redes/",
                    "https://computacion.mercadolibre.com.ar/software/",
                    "https://computacion.mercadolibre.com.ar/tablets-accesorios/",
                    "https://computacion.mercadolibre.com.ar/otros/",

                    // **** BUSQUEDAS 1 (No repetir)
                    // gift cards
                    "https://videojuegos.mercadolibre.com.ar/psn",
                    //****************************

                    //electronica audio y video
                    "https://electronica.mercadolibre.com.ar/accesorios-audio-y-video/",
                    "https://electronica.mercadolibre.com.ar/audio/",
                    "https://electronica.mercadolibre.com.ar/libreria-calculadoras-agendas/",
                    "https://electronica.mercadolibre.com.ar/componentes-electronicos/",
                    "https://electronica.mercadolibre.com.ar/drones-accesorios/",
                    "https://listado.mercadolibre.com.ar/industrias-oficinas/equipamiento-oficinas/maquinas-oficina/",
                    "https://autos.mercadolibre.com.ar/navegadores-gps/",
                    "https://electronica.mercadolibre.com.ar/pilas-cargadores-baterias/",
                    "https://hogar.mercadolibre.com.ar/seguridad/",
                    "https://electronica.mercadolibre.com.ar/accesorios-tv/",
                    "https://electronica.mercadolibre.com.ar/video/",
                    "https://electronica.mercadolibre.com.ar/otros/",


                    "https://televisores.mercadolibre.com.ar/televisores/",   //TVs

                    //accesorios de vehiculos
                    "https://autos.mercadolibre.com.ar/accesorios/",


                    // alimentos y bebidas
                    "https://listado.mercadolibre.com.ar/alimentos-bebidas/",

                    // animales y mascotas
                    "https://listado.mercadolibre.com.ar/animales-mascotas/",

                    // arte libreria y merceria
                    "https://listado.mercadolibre.com.ar/arte-artesanias/",

                    //belleza y cuidado personal
                    "https://listado.mercadolibre.com.ar/belleza-y-cuidado-personal/",

                    //deportes y fitnes
                    "https://deportes.mercadolibre.com.ar/",

                    //hogar y electrodomesticos
                    "https://listado.mercadolibre.com.ar/electrodomesticos/",  //electrodomesticos y aires
                    "https://hogar.mercadolibre.com.ar/cocina/", // bazar y cocina
                    "https://hogar.mercadolibre.com.ar/jardines-exteriores/",
                    "https://listado.mercadolibre.com.ar/aberturas/",
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
                    "https://zapatos.mercadolibre.com.ar/calzado/",

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

                    //criptomonedas
                    "https://listado.mercadolibre.com.ar/criptomonedas/",

                    // **** BUSQUEDAS 2 (No repetir)
                    // gift cards
                    "https://listado.mercadolibre.com.ar/gift-card"
                    //****************************

            };

    static String[] apiBaseUrls = {
            // antigüedades
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA436789",

            //gift cards
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1144&q=ps%20plus",
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1144&q=tarjeta",
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA401316",
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA411425",
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA121962",


            //tecnologia

            //celulares y telefonia
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA3502",
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA3813",
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1055", //celulares y smartphones
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1053", //telefonía fija e inalambrica
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1058", //handies y radiofrecuencia
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA5237", //telefonia IP
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1052", // centrales telefonicas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1054", //fax
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1915", //celulares - otros

            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1039", //camaras y accesorios
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1144", //consolas y videojuegos

            "https://api.mercadolibre.com/sites/MLA/search?category=MLA126843", //all in one
            "https://api.mercadolibre.com/sites/MLA/search?q=apple",            //apple
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA3794",   //componentes de pc
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1669",   //discos y accesorios
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1718",   //estabilizadores y ups
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA2141",   //impresion
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1660",   //lectores y scanners
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1694",   //memorias ram
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA407379", //mini pc
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1656",   //monitores y accesorios
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA430687", //laptops y accesorios
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA430637", //pcs
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA454379/",//perifericos
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1693",   //procesadores
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA4800",   //proyectores y pantallas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1700",   //conectividad y redes
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1723",   //software
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA400950", //tablets y accesorios
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1912",   //computacion - otros

            // **** BUSQUEDAS 1 (No repetir)
            // gift cards
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1144&q=psn",
            //****************************


            //electronica audio y video
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA3690",    //accesorios audio video
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA409810",  //audio
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1060",    //calculadoras y agendas (libreria)
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA11830",   //componentes electronicos
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA352294",  //drones y accesorios
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA5983",    //maquinas de oficina
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA8531",    //navegadores y gps
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA4641",    //pilas y cargadores
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA4624",    //seguridad para el hogar
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA431414",  //accesorios TV
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1015",    //video
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1070",    //electronica - otros


            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1002",   //TVs

            //accesorios de vehiculos
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA6520",  //accesorios de auto y camioneta


            // alimentos y bebidas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1403",

            // animales y mascotas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1071",

            // arte libteria y merceria
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1368",

            //belleza y cuidado personal
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1246",

            //deportes y fitnes
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1276",

            //hogar y electrodomesticos
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA5726",  //electrodomesticos y aires
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1592",  // bazar y cocina
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1621",  //jardines y exteriores
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA30088", //aberturas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1499",  //industrias y oficinas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA5226",  //herramientas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA2467",  //electricidad
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1500",  //construccion

            //bebes
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1384",

            //libros
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA3025",

            //moda (ropa y accesorios)
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1430",   //ropa y accesorios
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA110761", //equipaje bolsos y carteras
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA109026", //calzado

            //instrumentos musicales
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1182",

            //joyas y relojes
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA3937",

            //juguetes
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1132",

            //musica y peliculas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1168",

            //salud
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA409431",

            //servicios
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1540",

            //otros
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA3530",

            //criptomonedas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA417785",

            // gift cards
            "https://api.mercadolibre.com/sites/MLA/search?q=gift%20card",
            //****************************

            //////////////////// EMPRESAS ////////////////////////////////
            //////////////////// EMPRESAS ////////////////////////////////
            //////////////////// EMPRESAS ////////////////////////////////
            //////////////////// EMPRESAS ////////////////////////////////

            //empresas

            "https://api.mercadolibre.com/sites/MLA/search?seller_id=188818717",  //REPUESTODOVENTAS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=142783840",  //ELSITIOREPUESTOS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=269965609",  //ANIKASHOP
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=34916517",   //DIGITAL_MEGA_STORE
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=8184118",    //CERRAJERIA63
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=115764017",  //ITPROUSER
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=716559",     //DECOHOGAR
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=248636763",  //KADIMA-STUFF
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=218026996",  //JUGUETERIA+MARGARITA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=279131651",  //OLD-RIDER
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=7625198",    //SANITARIOS-ARIETA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=22138542",   //LACASA+DELFARO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=156168198",  //AMITOSAI+STORES
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=27420742",   //BABY+KINGDOM
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=47316577",   //PLANETAZENOK
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=165962418",  //PRESTIGIO+STORE
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=37133205",   //SALES+COM
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=132453365",  //PINTURERIASREX

            "https://api.mercadolibre.com/sites/MLA/search?seller_id=193932040",  //DAGOSTINA+VANITORYS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=80687042",   //GALARDI-MUEBLES
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=289913573",  //ELEGANCE_JARDIN
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=49041694",   //CHESTER-MANIA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=177237076",  //GUIK+DESIGN
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=5170079",    //ELECTROMEDINA

            "https://api.mercadolibre.com/sites/MLA/search?seller_id=239427239",  //SASI36787
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=259840875",  //.COLGALO.COMO.QUIERAS.
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=44758411,",  //AC-TEC
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=188747318",  //ALAVUELTA+CERAMICOS

            "https://api.mercadolibre.com/sites/MLA/search?seller_id=205076801",  //GREENDECO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=145263251",  //SHESHU+WEB
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=230484551",  //DISTRITOBLANCO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=76393523",   //N2M-ELECTRONICA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=150718965",  //ARGENSHOP+BSAS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=276064236",  //CASAANDREA+LOCAL
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=129456144",  //INTER+VENT
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=63658014",   //DECOR+ITUZAINGO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=255677435",  //GUIRNALDA+DELUCES
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=120289637",  //VENTAIMPORTACION
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=148361972",  //CONFORTSURSA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=81375635",   //MLAT_ARG
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=137391595",  //ELECTROLED+SA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=95707744",   //MI-BIOMBO-ES-TU-BIOMBO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=79732240",   //PUFFYFIACAS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=185866176",  //CARELLI+COMPANY
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=91472071",   //VST+ONLINE
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=92418743",   //SUFERRETERIAONLINE
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=235476092",  //OTHERBRANDS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=94070763",   //CAPRICHOSA+CAROLA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=299909924",  //PLASTICHEMONDO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=46312868",   //INTERMARBLE-SRL
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=95496785",   //CEGEVAHOGAR
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=76762412",   //BAIRES+4
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=81794304",   //TISERA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=273338818",  //QUIEROESO+YA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=54898332",   //MUEBLESLANUS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=78212055",   //MORSHOP
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=294941023",  //NEWMO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=36831755",   //BALL_SELLINGS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=31544243",   //MUEBO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=222656594",  //DAMPLAST-RAMOS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=95680100",   //MOBILARG
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=24951567",   //INTEGRAL+DECO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=139386086",  //Desillas
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=49355841",   //LIVINGSTYLEDESIGN
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=78794591",   //SU-OFFICE
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=99356770",   //MUNDO+SHOPS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=158835319",  //LEATHERPLAST_MUEBLES
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=97562082",   //INSUOFFICE
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=121000172",  //AMV-TIENDA+ONLINE

            "https://api.mercadolibre.com/sites/MLA/search?seller_id=213476162",  //CERSARYDESIGN
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=129993069",  //JOY+TIENDA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=58693232",   //MANU_CHAMBAIDEAS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=10301558",   //OFITOPMUEBLES
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=187186141",  //HIVICKY.
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=165804006",  //URWA9971249
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=24088738",  //MAURO+MUSUMECI

            //hafele + frank
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=141782424",  //ALLSURHERRAJES
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=136191303",  //ATILIOOSCARFARAONE
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=246210362",  //BOLZANRG
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=115353457",  //CALZAVARA+SRL
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=214222788",  //COMERCIAL+CAB
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=169746075",  //DELBRE+SRL
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=278683358",  //DISTRIBUIDORA+MECH
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=262462365",  //HERRAFE.COM
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=306649995",  //HERRAJEMANIA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=238647032",  //HERRAJES+DIAGONAL
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=342744334",  //HERRAJES+ESTILO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=183914122",  //HERRAJES+MAGLIOLA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=206071515",  //HERRAJES+P.KRAMAR
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=237269243",  //HERRAJESFORTALEZA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=89507437",   //HERRAJESPATRICIOS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=156064186",  //HERRAJESREVELLI
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=115573070",  //HERRAJESTRONADOR
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=231944987",  //HERRAJESVEIGA+LINE
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=444226698",  //HERRAMELSC
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=323271487",  //KRAMAR+LAFERRERE
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=265586075",  //LAMARINA.
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=168573716",  //MADERERALLAVALLOLSA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=87052493",   //MUNDOCIMA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=7477051",    //HERRAJES-ARGENTINOS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=75471238",   //HERRAJES-ARGENTINOS-BALLESTER
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=355221540",  //HERRAJES OLIVER
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=141299067",  //HERRAJESSOIFER
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=323780517",  //HOME.MARKET
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=22579885",   //KEUKEN HERRAJES
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=577706727",  //REALHERRAJES
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=121020308",  //QUIEROHERRAJES
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=3154882",    //STOVI

            //hafele + frank otros
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=92758705",   //AUTO_UNO_CASEROS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=498988242",  //CIGAPLACSRL
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=173565332",  //ESTILO+DK
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=298571225",  //HCI+OBRAS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=191506758",  //HERGUTI45
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=307237566",  //HERRAJES+TOTITO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=321446616",  //HERRAJESBALDANSRL
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=45962628",   //HERRAJESHILTON1
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=208943707",  //HERRAJESMITRE
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=177776871",  //HERRAJES+TUYU
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=357639575",  //HERRAPEI
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=163421387",  //HT+HERRAJES
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=172619074",  //LUCIAZAFFRANI
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=209025171",  //AMOBLAMIENTOS INTEVIL

            //ducasse y euro hard
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=159950125",  //DISTRIBUIDORA+SYG
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=60397285",   //EL-PLACARD
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=230054006",  //HERRAJES+DANSER
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=26142695",   //HIERROS+TORRENT
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=389848612",  //LACASADELOSHERRAJES+MDP
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=136191303",  //ATILIOOSCARFARAONE
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=189983130",  //SOLUZIONE HOGAR

            //alfombreros
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=425673298",  //ABELEDOJAVIER
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=86263724",   //AGBPISOSYREVESTIMIENTOS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=92882034",   //ALFOMBRAS+HIPOLITO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=110799894",  //AMBIANCEDECO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=110799894",  //AMBIANCEPALERMO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=258553339",  //AYLUMA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=312077259",  //BEELLESTYLE
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=90892683",   //COMO.EN.CASA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=158967981",  //CORIMEL+ALFOMBRAS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=76368078",   //DECORACIONESGIMBATTI
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=405067960",  //DECORD+CENTER
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=271912764",  //ELCHEALMOHADON
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=236437129",  //ELUNEYDECO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=180642211",  //EXITO+DISEÑO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=97711988",   //FULLHOUSEWEB
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=78131205",   //GLAUCO0112005
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=144309203",  //GRDO2013
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=66096514",   //IMPORT+MIS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=120948541",  //JUAMPY-712
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=539770909",  //KEOPS_ALFOMBRAS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=132647768",  //KREATEX_SA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=410760288",  //LEDERHD
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=60606790",   //MENTAYROMERODECO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=139254148",  //MGBALFOMBRAS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=48984242",   //MICKYCO1
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=78986844",   //MUEBLES-COSAS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=247120990",  //RASI6160647
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=245625502",  //TIENDA+BAZHARS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=454648849",  //TODD+ARGENTINA

            //alfombreros otros
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=84524325",   //ABETEYCIAMDP
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=338339653",  //BELLIZZIHNOSSRL
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=146394184",  //BRUMS+DECO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=92240397",   // COLCHONEXPRESS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=284865807",  //COSECOSRL
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=224380581",  //DECO-GSD
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=20822033",   //DECONAMOR+REGALOS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=63658014",   //DECOR+ITUZAINGO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=336291698",  //EL+RELLENON
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=439333508",  //ENTUCASA.ONLINE
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=94388320",   //ESTANCIA-MUEBLES
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=244079611",  //FORM+HOMESTORE
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=42823437",   //GAMULANYCAMPERAS+ARGENTINA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=52590440",   //GPINDUSTRIAL
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=92906091",   //GUALASD
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=272773499",  //HALCON+SHOP
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=77061780",   //MARCARIMPORT
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=36281070",   //MARYL77
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=383011431",  //MASAL.BA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=182478050",  //MEWADECO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=233230004",  //MISIONLIVE
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=191605678",  //PRIMERO+UNO
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=263221933",  //PRODUCTOPATAGONIA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=63293804",   //ROCIOPAISSAN
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=298644779",  //ROSANAMARICELMACIEL
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=166452907",  //RUPLESS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=250906539",  //SHEM+GROUP
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=78326747",   //SANTAFE+MARKET
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=91554184",   //SOUL+DESIGN
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=243361725",  //TIENDA+BALLERINA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=197945411",  //TOBLANC

            //mormetal
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=292512475",  //MUNDO+OFI-MAX
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=75186672",   //HERRAJES+CASTELMAX

            "https://api.mercadolibre.com/sites/MLA/search?seller_id=273716062"   //MODULAR+DRAWERS



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
            {0, 3000, 2147483647}, //telefonia ip
            {0, 3000, 2147483647}, //centrales telefonicas
            {0, 3000, 2147483647}, //fax
            {0, 3000, 2147483647}, //otros

            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //camaras
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //consolas

            //computacion
            {0, 3000, 2147483647}, // all in one
            {0, 3000, 2147483647}, // apple
            {0, 3000, 2147483647}, // componentes PC
            {0, 3000, 2147483647}, // discos y accesorios
            {0, 3000, 2147483647}, // estabilizadores y ups
            {0, 3000, 2147483647}, // impresion
            {0, 3000, 2147483647}, // lectores escanners
            {0, 3000, 2147483647}, // memorias ram
            {0, 3000, 2147483647}, // mini pcs
            {0, 3000, 2147483647}, // monitores y accesorios
            {0, 3000, 2147483647}, // laptops
            {0, 3000, 2147483647}, // PCs
            {0, 3000, 2147483647}, // perisféricos
            {0, 3000, 2147483647}, // procesadores
            {0, 3000, 2147483647}, // proyectores
            {0, 3000, 2147483647}, // conectividad y redes
            {0, 3000, 2147483647}, // software
            {0, 3000, 2147483647}, // tablets
            {0, 3000, 2147483647}, // electronica - otros

            //busquedas 1  **************
            {0, 300, 1100, 2147483647}, //psn
            //***************************

            //electronica audio y video

            {0, 3000, 2147483647}, // accesorios audio y video
            {0, 3000, 2147483647}, // audio
            {0, 3000, 2147483647}, // calculadoras y agendas
            {0, 3000, 2147483647}, // componentes electronicos
            {0, 3000, 2147483647}, // drones accesorios
            {0, 3000, 2147483647}, // maquinas de oficina
            {0, 3000, 2147483647}, // gps
            {0, 3000, 2147483647}, // pilas cargadores baterias
            {0, 3000, 2147483647}, // seguridad para el hogar
            {0, 3000, 2147483647}, // accesorios TV
            {0, 3000, 2147483647}, // video
            {0, 3000, 2147483647}, // otros

            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //TVs
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //accesorios de auto y camioneta
            {0, 3000, 2147483647}, //alimentos y bebidas
            {0, 3000, 2147483647}, //animales y mascotas
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //arte libteria y merceria
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //belleza y cuidado personal
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //deportes y fitnes
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //electrodomesticos y aires
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //bazar y cocina
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //jardines exteriores
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //aberturas
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //industrias y oficinas
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //herramientas
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //electricidad
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //construccion
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //bebes
            {0, 3000, 2147483647}, //libros
            {0, 3000, 2147483647}, //moda (ropa)
            {0, 3000, 2147483647}, //moda (bolsos)
            {0, 3000, 2147483647}, //moda (calzado)
//            {0,3000,2147483647}, //alimenos y bebidas
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //instrumentos musicales
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //joyas y relojes
            {0, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 25000, 2147483647}, //juguetes
            {0, 3000, 2147483647}, //musica y peliculas
            {0, 3000, 2147483647}, //salud
            {0, 3000, 2147483647}, //servicios
            {0, 3000, 2147483647}, //otros
            {0, 3000, 2147483647}, //criptomonedas

            // busquedas 2 *************
            {0, 300, 1100, 2147483647}, //gift card
            //***************************

            //aca van las empresas

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

            //MUEBLUDECES
            {0, 2147483647}, //CERSARYDESIGN
            {0, 2147483647}, //JOY+TIENDA
            {0, 2147483647}, //MANU_CHAMBAIDEAS
            {0, 2147483647}, //OFITOPMUEBLES
            {0, 2147483647}, //HIVICKY.
            {0, 2147483647}, //URWA9971249
            {0, 2147483647}, //MAURO+MUSUMECI

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
            {0, 2147483647}, //HERRAJES-ARGENTINOS
            {0, 2147483647}, //HERRAJES-ARGENTINOS-BALLESTER
            {0, 2147483647}, //HERRAJES OLIVER
            {0, 2147483647}, //HERRAJESSOIFER
            {0, 2147483647}, //HOME.MARKET
            {0, 2147483647}, //KEUKEN HERRAJES
            {0, 2147483647}, //REALHERRAJES
            {0, 2147483647}, //QUIEROHERRAJES
            {0, 2147483647}, //STOVI

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
            {0, 2147483647}, //AMOBLAMIENTOS INTEVIL

            //euro hard
            {0, 2147483647}, //DISTRIBUIDORA+SYG
            {0, 2147483647}, //EL-PLACARD
            {0, 2147483647}, //HERRAJES+DANSER
            {0, 2147483647}, //HIERROS+TORRENT
            {0, 2147483647}, //LACASADELOSHERRAJES+MDP

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


    //hafeleros
    //(141782424,136191303,246210362,115353457,214222788,169746075,278683358,262462365,306649995,238647032,342744334,183914122,206071515,237269243,89507437,156064186,115573070,231944987,444226698,323271487,265586075,168573716,87052493,7477051,75471238,355221540,141299067,323780517,22579885,57770672,121020308,3154882,92758705,498988242,173565332,298571225,191506758,307237566,321446616,45962628,208943707,177776871,357639575,163421387,172619074,209025171)


    public static void main(String[] args) {


        /* version reducida para pruebas

        String[] webBaseUrls1=new String[]{webBaseUrls[6]};
        webBaseUrls=webBaseUrls1;

        String[] apiBaseUrls1=new String[]{apiBaseUrls[6]};
        apiBaseUrls=apiBaseUrls1;

        int[][] intervals1=new int[][]{intervals[6]};
        intervals=intervals1;
        */

        for (int i = 0; i < intervals.length; i++) { //validacion de intervalos
            for (int j = 1; j < intervals[i].length; j++) {
                if (intervals[i][j - 1] >= intervals[i][j]) {
                    System.out.println("Error en intervalo #" + i + " // " + intervals[i][j - 1] + "-" + intervals[i][j]);
                    System.exit(0);
                }
            }
        }

        CloseableHttpClient client = HttpUtils.buildHttpClient();

        String usuario = "SOMOS_MAS";

        ReportRunner.runWeeklyReport(webBaseUrls, apiBaseUrls, intervals, client, usuario, DATABASE, ONLY_RELEVANT,
                PREVIOUS_DAY, FOLLOWING_DAY, MINIMUM_SALES);

        String msg = "******************************************************\r\n"
                + Counters.getGlobalPageCount() + " paginas procesadas\r\n "
                + Counters.getGlobalRequestCountCount() + " requests\r\n "
                + Counters.getGlobalProductCount() + " productos procesados\r\n "
                + Counters.getGlobalDisableCount() + " productos deshabilitados\r\n "
                + Counters.getGlobalNewsCount() + " productos con novedades";
        System.out.println(msg);
        Logger.log(msg);

    }

}
