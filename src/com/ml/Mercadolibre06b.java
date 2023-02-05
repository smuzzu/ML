package com.ml;

import com.ml.utils.Counters;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.SData;
import org.apache.http.impl.client.CloseableHttpClient;

import java.sql.Date;

public class Mercadolibre06b {
    static final String currentDateStr="DD-MM-YYYY";

    static final String DATABASE = "ML6";
    static final boolean SAVE = true;
    static final boolean IGNORE_VISITS = true;
    static final int MINIMUM_SALES = 5;
    static final boolean ONLY_RELEVANT = true;


    static String[] apiBaseUrls = {

            //autos (repuestos)
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA86360",  //accesorios camiones
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA6520",   //accesorio auto y camioneta
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA2227",   //herramientas (auto)
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA8531",   //navegadores GPS
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1747",   //repuestos autos y camionetas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA86080",  //seguridad vehicular
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA377674", //servicio programado
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA4711",   //accesorios de motos
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1771",   //repuestos motos y cuatriciclos
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA402999", //performance vehiculos
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA4589",   //tuning
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA417044", //repuestos nauticos
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA6177",   //otros accesorios para vehiculos
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA3381",   //audio vehiculos
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA4610",   // gnc
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA86838",  //limpieza vehiculos
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA6537",   //llantas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA400928", //neumaticos
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA419936", //repuestos linea pesada

            //agro
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1071",   //animales y mascotas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA454448", //infraestructura rural
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA442351", //repuestos maquinaria agricola
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA442344", //agro generador de energia
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA442343", //insumos agricolas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA404085", //agro otros

            //alimentos y bebidas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1423",   //almacen
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA178700", // bebidas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA410883", //comida preparada
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA455292", //congelados
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA194324", //frescos
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1417",   //alimentos otros

            // antigüedades
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA436789",

            //arte libreria y merceria
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA5982",  //libreria
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA10934", //merceria
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA34263", //arte y manualidades
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA34294", //otros

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
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA4641",    //pilas y cargadores
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA4624",    //seguridad para el hogar
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA431414",  //accesorios TV
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1015",    //video
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1070",    //electronica - otros


            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1002",   //TVs

            // animales y mascotas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1071",

            //belleza y cuidado personal
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1246",

            //construcción
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA30088",  // aberturas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA455443", // accesorios de construcción
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA411920", // baños y sanitarios
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA2467",   // electricidad
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1502",   // maquinarias para la construccion
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA403697", // materiales de obra
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA30081",  // mobiliario de cocinas
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA403700", // pintureria
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA14548",  // pisos y revestimientos
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA435273", // plomeria
            "https://api.mercadolibre.com/sites/MLA/search?category=MLA1503",   // otros construccion

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

            //muebludeces
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=213476162",  //CERSARYDESIGN
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=129993069",  //JOY+TIENDA
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=58693232",   //MANU_CHAMBAIDEAS
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=10301558",   //OFITOPMUEBLES
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=187186141",  //HIVICKY.
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=165804006",  //URWA9971249
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=24088738",  //MAURO+MUSUMECI
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=193071955",  //MALOR.

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
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=231944987",  //HERRAJESVEIGA LINE
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
            "https://api.mercadolibre.com/sites/MLA/search?seller_id=156064186",  //HERRAJESREVELLI

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



    //hafeleros
    //(141782424,136191303,246210362,115353457,214222788,169746075,278683358,262462365,306649995,238647032,342744334,183914122,206071515,237269243,89507437,156064186,115573070,231944987,444226698,323271487,265586075,168573716,87052493,7477051,75471238,355221540,141299067,323780517,22579885,57770672,121020308,3154882,92758705,498988242,173565332,298571225,191506758,307237566,321446616,45962628,208943707,177776871,357639575,163421387,172619074,209025171)


    public static void main(String[] args) {

        Date runDate=Counters.parseDate(currentDateStr);

        CloseableHttpClient client = HttpUtils.buildHttpClient();

        String usuario = SData.getSomosMas();

        ReportRunner.runWeeklyReport(apiBaseUrls, client, usuario, DATABASE, ONLY_RELEVANT,
                IGNORE_VISITS, runDate, MINIMUM_SALES, SAVE);

        String msg = "******************************************************\r\n"
                + Counters.getGlobalRequestCountCount() + " requests\r\n "
                + Counters.getGlobalProductCount() + " productos procesados\r\n "
                + Counters.getGlobalDisableCount() + " productos deshabilitados\r\n "
                + Counters.getGlobalNewsCount() + " productos con novedades";
        System.out.println(msg);
        Logger.log(msg);

    }

}
