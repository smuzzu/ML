package com.ml;


import com.ml.utils.Counters;
import com.ml.utils.HttpUtils;
import com.ml.utils.Item;
import com.ml.utils.Logger;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.HashMap;


public class Mercadolibre02b {


    static final String DATABASE="ML2";
    static final boolean ONLY_RELEVANT = false;



    public static void main(String[] args) {

        String[] webBaseUrls = {
                "https://hogar.mercadolibre.com.ar/adornos-decoracion-del/", //decoracion
                "https://hogar.mercadolibre.com.ar/adornos-decoracion-del-cuadros-carteles-espejos/",//espejos (no esta abajo)
                "https://hogar.mercadolibre.com.ar/textiles-decoracion-alfombras-carpetas/", //alfombras

                "https://hogar.mercadolibre.com.ar/articulos-limpieza-productos-limpiadores-alfombras/", //limpiadores alfombra
                "https://listado.mercadolibre.com.ar/lustramuebles_DisplayType_G", //lustramuebles
                "https://listado.mercadolibre.com.ar/rugbee_DisplayType_G",
                "https://listado.mercadolibre.com.ar/stp_DisplayType_G",
                "https://listado.mercadolibre.com.ar/blem_DisplayType_G",

                "https://listado.mercadolibre.com.ar/hafele_DisplayType_G",
                "https://listado.mercadolibre.com.ar/genoud_DisplayType_G",
                "https://hogar.mercadolibre.com.ar/fark_DisplayType_G",

        };


        String[] apiBaseUrls = {
                "https://api.mercadolibre.com/sites/MLA/search?category=MLA1631",   //decoracion
                "https://api.mercadolibre.com/sites/MLA/search?category=MLA2513",   //alfombras

                "https://api.mercadolibre.com/sites/MLA/search?category=MLA417311", //limpiadores de alfombras
                "https://api.mercadolibre.com/sites/MLA/search?q=lustramuebles",
                "https://api.mercadolibre.com/sites/MLA/search?q=rugbee",
                "https://api.mercadolibre.com/sites/MLA/search?q=stp",
                "https://api.mercadolibre.com/sites/MLA/search?q=blem",

                "https://api.mercadolibre.com/sites/MLA/search?q=hafele",
                "https://api.mercadolibre.com/sites/MLA/search?q=genoud",
                "https://api.mercadolibre.com/sites/MLA/search?q=fark"


        };

        int [][] intervals = {
                {0,22,25,29,36,49,50,59,65,69,74,79,80,89,94,99,100,110,119,120,129,130,139,140,149,
                150,159,160,169,170,179,180,189,190,198,199,200,210,219,220,224,225,230,239,240,249,250,259,260,269,270,
                279,280,289,290,298,299,300,315,320,329,333,340,349,350,359,360,369,370,379,380,389,390,398,399,400,410,
                419,420,429,430,439,440,449,450,459,460,469,470,479,480,481,489,490,498,499,500,509,510,519,520,525,529,
                535,545,549,550,560,570,579,580,584,585,589,590,598,599,600,610,619,620,629,630,639,640,649,650,659,660,
                669,670,679,680,689,690,698,699,700,710,720,729,730,739,740,748,749,750,760,770,779,780,789,790,798,799,
                800,819,820,829,830,840,849,850,860,870,879,880,889,890,898,899,900,909,910,919,920,927,930,939,
                940,949,950,960,970,979,980,989,990,998,999,1000,1030,1049,1050,1079,1080,1090,1099,1100,1140,1149,1150,
                1170,1180,1190,1198,1199,1200,1240,1249,1250,1280,1298,1299,1300,1330,1349,1350,1370,1380,1390,1398,1399,
                1400,1440,1449,1450,1480,1498,1499,1530,1540,1550,1560,1569,1570,1580,1588,1590,1600,1640,1649,1650,1659,
                1670,1680,1689,1690,1698,1699,1700,1720,1729,1730,1740,1749,1750,1759,1760,1769,1770,1779,1780,1789,1790,
                1798,1799,1800,1830,1849,1850,1889,1890,1899,1900,1949,1950,1989,1990,1998,1999,2000,2050,2070,2080,2090,
                2099,2100,2150,2190,2199,2200,2210,2250,2260,2270,2280,2290,2299,2300,2350,2398,2399,2400,2450,2480,2490,
                2499,2500,2550,2590,2599,2600,2650,2690,2699,2700,2749,2750,2790,2799,2800,2809,2810,2850,2890,2899,3000,
                3049,3050,3099,3100,3150,3190,3199,3200,3250,3290,3299,4000,4050,4090,4099,4100,4198,4199,4200,4220,4250,
                4290,4299,4300,4350,4399,4400,4450,4490,4499,4500,4550,4599,4600,4650,4699,4700,4750,4799,4800,4850,4899,
                4900,4950,4990,4999,5000,5099,5100,5199,5200,5299,5300,5399,5400,5499,5500,5599,5600,5699,5700,5799,5800,
                5900,5998,5999,6000,6100,6200,6400,6498,6499,6500,6600,6700,6800,6900,6998,6999,7000,7200,7400,7498,7499,
                7500,7700,7900,7998,7999,8000,8400,8498,8499,8500,8700,8900,8998,8999,9000,9400,9500,9600,9700,9800,9990,
                9999,10000,10500,10900,10999,11000,11500,11900,11999,12000,12500,12900,12999,13000,13500,13900,13999,14000,
                14500,14900,14990,14999,15000,15500,16000,16500,17000,17500,18000,18500,19000,19900,20000,21000,22000,
                23000,24000,25000,27000,29000,30000,35000,40000,40500,45000,49000,53000,70000,147483647},
                {0,350,500,700,900,1000,1300,1600,1900,2200,2500,3000,3500,4000,4500,5000,6000,7000
                ,8000,9000,10000,12000,14000,16000,18000,20000,25000,35000,50000,2147483647}, //alfombras

                {0,2147483647}, //limpieza de alfombras
                {0,2147483647}, //lustra muebles
                {0,2147483647}, //rugbee
                {0,499,600,900,1300,2000,2147483647}, //stp
                {0,2147483647}, //blem

                {0,300,650,1300,2500,7000,2147483647}, //hafele
                {0,150,200,300,400,500,700,900,1000,1300,1600,1900,2000,2500,5000,15000,2147483647}, //genoud
                {0,2147483647}, //fark
        };

        for (int i = 0; i < intervals.length; i++) { //validacion de intervalos
            for (int j = 1; j < intervals[i].length; j++) {
                if (intervals[i][j - 1] >= intervals[i][j]) {
                    System.out.println("Error en intervalo #" + i + " // " + intervals[i][j - 1] + "-" + intervals[i][j]);
                    System.exit(0);
                }
            }
        }

        CloseableHttpClient client = HttpUtils.buildHttpClient();
        HashMap<String, Item> itemHashMap = new HashMap<String, Item>();
        String usuario = "SOMOS_MAS";

        ReportRunner.runWeeklyReport(webBaseUrls, apiBaseUrls, intervals, client, usuario, DATABASE, ONLY_RELEVANT);

        String msg = "******************************************************\r\n"
                + Counters.getGlobalPageCount() + " paginas procesadas\r\n "
                + Counters.getGlobalProductCount() + " productos procesados\r\n "
                + Counters.getGlobalNewsCount() + " productos con novedades";
        System.out.println(msg);
        Logger.log(msg);

        boolean b = false;

    }

}
