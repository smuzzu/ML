package com.ml;

import com.ml.utils.Counters;
import com.ml.utils.DatabaseHelper;
import com.ml.utils.HTMLParseUtils;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.ProductPageProcessor;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Date;

import java.util.*;

public class MercadoLibre01b {

    static final int RESULTS_WITHOUT_TOKEN = 1000;
    static final int RESULTS_LIMIT = 10000;
    static final String DATABASE = "ML1";
    static int MAX_THREADS = 20;//14
    static final boolean SAVE = false;
    static final boolean DEBUG = false;
    static final int MINIMUM_SALES = 1;
    static final boolean FOLLOWING_DAY = false;
    static final boolean PRERVIOUS_DAY = false;
    static final boolean ONLY_RELEVANT = false;


    static int requestCount = 0;
    static Date globalDate = null;

    static class Item {
        String id;
        String title;
        String permalink;
        double price;
        int discount;
        boolean advertised;
        boolean officialStore;
        int shipping;
        boolean premium;
        int ranking;
        int page;
        int totalSold;
        String sellerName;
        int sellerId;

        Item() {
            this.advertised = false;
            this.ranking = 10000;
            this.totalSold = -1;
            this.price = -1.0;
            this.discount = -1;
            this.sellerId = -1;
        }

        public boolean equals(Object obj) {
            if (this.id == null || this.id.isEmpty()) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Mercadolibre02b.Item)) {
                return false;
            }
            Mercadolibre02b.Item otherItem = (Mercadolibre02b.Item) obj;
            if (otherItem.id == null || otherItem.id.isEmpty()) {
                return false;
            }
            return this.id.equals(otherItem.id);
        }

    }


    public static void main(String[] args) {

        String[] webBaseUrls = {
                "https://hogar.mercadolibre.com.ar/muebles/",
                "https://listado.mercadolibre.com.ar/industrias-oficinas/equipamiento-oficinas/",
                "https://listado.mercadolibre.com.ar/herramientas-y-construccion/mobiliario-cocinas/",
                "https://hogar.mercadolibre.com.ar/organizacion/",  //organizacion hogar
                "https://hogar.mercadolibre.com.ar/jardines-exteriores-muebles-exterior/",
                "https://hogar.mercadolibre.com.ar/muebles-bano/",

                "https://listado.mercadolibre.com.ar/perchero",
                "https://listado.mercadolibre.com.ar/cesto",
                "https://listado.mercadolibre.com.ar/ordenador-de-publico",
                "https://listado.mercadolibre.com.ar/paraguero",

                "https://listado.mercadolibre.com.ar/_CustId_241751796",  //acacia
                "https://listado.mercadolibre.com.ar/_CustId_67537324",   //somos_mas
                "https://listado.mercadolibre.com.ar/_CustId_233230004",  //misionlive
                "https://listado.mercadolibre.com.ar/_CustId_191605678",  //primero+uno
                "https://listado.mercadolibre.com.ar/_CustId_77061780",   //marcarimport
                "https://listado.mercadolibre.com.ar/_CustId_292512475",  //mundo+ofi-max
                "https://listado.mercadolibre.com.ar/_CustId_75186672",   //herrajes+castelmax
                "https://listado.mercadolibre.com.ar/_CustId_273716062"   //modular drawers
        };
        String[] apiBaseUrls = {
                "https://api.mercadolibre.com/sites/MLA/search?category=MLA436380", //muebles
                "https://api.mercadolibre.com/sites/MLA/search?category=MLA2102",   //equipamiento ofi
                "https://api.mercadolibre.com/sites/MLA/search?category=MLA411938", //mobiliario cocinas
                "https://api.mercadolibre.com/sites/MLA/search?category=MLA436414", //organizacion hogar
                "https://api.mercadolibre.com/sites/MLA/search?category=MLA9961",   //muebles exterior
                "https://api.mercadolibre.com/sites/MLA/search?category=MLA454704", //muebles bano

                "https://api.mercadolibre.com/sites/MLA/search?q=perchero",
                "https://api.mercadolibre.com/sites/MLA/search?q=cesto",
                "https://api.mercadolibre.com/sites/MLA/search?q=ordenador%20de%20publico",
                "https://api.mercadolibre.com/sites/MLA/search?q=paraguero",

                "https://api.mercadolibre.com/sites/MLA/search?seller_id=241751796", //acacia
                "https://api.mercadolibre.com/sites/MLA/search?seller_id=67537324",  //somos_mas
                "https://api.mercadolibre.com/sites/MLA/search?seller_id=233230004", //misionlive
                "https://api.mercadolibre.com/sites/MLA/search?seller_id=191605678", //primero+uno
                "https://api.mercadolibre.com/sites/MLA/search?seller_id=77061780",  //marcarimport
                "https://api.mercadolibre.com/sites/MLA/search?seller_id=292512475", //mundo+ofi-max
                "https://api.mercadolibre.com/sites/MLA/search?seller_id=75186672",  //herrajes+castelmax
                "https://api.mercadolibre.com/sites/MLA/search?seller_id=273716062"  //modular drawers
        };
        int[][] intervals = {

                {
                    0,31,63,95,103,127,159,191,207,223,255,287,303,319,351,399,415,447,479,499,500,501,511,575,599,600,603,639,687,
                    695,703,735,767,799,800,801,863,899,900,903,959,991,999,1000,1001,1087,1119,1199,1200,1201,1279,1299,1300,1303,
                    1391,1407,1487,1499,1500,1501,1599,1603,1695,1727,1799,1800,1801,1899,1900,1901,1983,1999,2000,2001,2047,2111,
                    2199,2200,2201,2299,2300,2301,2399,2431,2495,2499,2500,2501,2591,2623,2687,2719,2799,2800,2801,2899,2900,2901,
                    2975,2991,2999,3000,3001,3071,3135,3199,3200,3201,3295,3327,3391,3423,3487,3499,3500,3501,3599,3615,3679,3711,
                    3799,3800,3801,3871,3899,3900,3901,3967,3991,3999,4000,4001,4095,4159,4199,4200,4201,4287,4303,4351,4415,4495,
                    4499,4500,4501,4591,4607,4671,4703,4799,4800,4801,4899,4900,4901,4959,4991,4999,5000,5001,5055,5119,5199,5215,
                    5279,5311,5375,5439,5499,5500,5501,5599,5631,5695,5759,5799,5807,5899,5900,5901,5983,5991,5999,6000,6001,6143,
                    6175,6207,6271,6335,6399,6463,6499,6500,6501,6591,6655,6719,6799,6815,6899,6900,6901,6991,6999,7000,7001,7103,
                    7199,7231,7295,7359,7423,7499,7500,7501,7551,7615,7679,7775,7791,7807,7895,7903,7983,7999,8000,8001,8063,8191,
                    8255,8319,8383,8479,8499,8500,8501,8575,8639,8703,8767,8831,8895,8911,8991,8999,9000,9001,9087,9151,9215,9343,
                    9471,9499,9500,9501,9599,9663,9727,9791,9823,9899,9903,9991,9999,10000,10001,10111,10239,10367,10495,10499,10500,
                    10501,10623,10751,10879,10975,10999,11000,11001,11135,11263,11391,11499,11500,11501,11647,11775,11839,11967,
                    11999,12000,12001,12159,12287,12479,12499,12500,12501,12671,12799,12863,12927,12999,13000,13001,13183,13311,
                    13499,13500,13503,13695,13823,13983,13999,14000,14001,14207,14335,14495,14511,14719,14847,14975,14999,15000,
                    15001,15103,15359,15487,15551,15743,15871,15935,15999,16000,16001,16127,16383,16479,16511,16767,16895,16991,
                    16999,17000,17001,17151,17407,17471,17535,17791,17919,17999,18000,18001,18175,18431,18495,18559,18687,18815,
                    18975,18999,19000,19001,19199,19455,19519,19711,19839,19967,19999,20000,20001,20223,20479,20735,20991,21055,
                    21375,21503,21887,21999,22000,22001,22271,22463,22527,22783,22975,22999,23000,23001,23295,23551,23807,23999,
                    24000,24015,24319,24575,24831,24991,24999,25000,25001,25343,25599,25855,25999,26047,26367,26623,26943,26991,
                    27007,27391,27647,27967,27999,28000,28001,28415,28671,28991,29055,29439,29695,29983,29999,30000,30001,30719,
                    31231,31743,31999,32063,32255,32767,32895,33023,33279,33791,34047,34303,34815,36863,37375,37887,38015,38399,
                    38911,39423,39935,39999,40000,40001,40959,41983,42495,43007,43519,44031,44999,45000,45001,45567,46079,47103,
                    47615,48127,48639,49151,49999,50000,50015,50687,51199,52223,53247,54783,55295,56319,57343,58367,59391,59903,
                    60159,61439,63487,64511,65023,67583,69631,69999,70000,70015,73727,75775,77823,79871,81919,83967,86015,88063,
                    90111,94207,98303,102399,106495,114687,122879,131071,147455,163839,196607,262143,2147483647
                }, //muebles

                {0, 99, 150, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100, 1200, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2200, 2400, 2500, 2700,
                        2900, 2990, 3000, 3200, 3400, 3500, 3700, 3900, 4000, 4200, 4400, 4500, 4700, 4900, 4990, 5000, 5200, 5400, 5500, 5700, 5900, 5990,
                        6000, 6200, 6400, 6500, 6700, 6900, 7000, 7200, 7400, 7500, 7700, 7900, 8000, 8200, 8400, 8500, 8799, 8900, 9000, 9200,
                        9400, 9500, 9900, 9990, 10000, 10200, 10400, 10500, 10700, 10900, 11000, 11350, 11500, 11850, 12000, 12350,
                        12500, 12850, 13000, 13500, 14000, 14500, 14900, 15000, 15500, 16000, 16500, 17000, 17500, 18000, 18500,
                        19000, 19500, 19900, 20000, 20500, 21000, 21500, 22000, 22500, 23000, 23500, 24000, 24500, 25000, 25500,
                        26000, 26500, 27000, 27500, 28000, 28500, 29000, 29900, 30000, 31000, 32000, 33000, 34000, 35000, 37000,
                        39000, 40000, 43500, 45000, 48500, 50000, 55000, 60000, 65000, 70000, 75000, 80000, 90000, 100000,120000,
                        2147483647}, //equipamiento ofi

                {0, 150, 400, 600, 900, 1000, 1400, 1500, 1900, 2000, 2200, 2400, 2500, 2700, 2900, 3000, 3200, 3400, 3500,
                        3700, 3900, 4000, 4200, 4400, 4500, 4700, 4900, 4990, 5000, 5200, 5400, 5500, 5700, 5900, 5990, 6000, 6200,
                        6400, 6500, 6700, 6900, 7000, 7200, 7400, 7500, 7700, 7900, 8000, 8200, 8400, 8500, 8799, 8900, 9000, 9200,
                        9400, 9500, 9900, 9990, 10000, 10200, 10400, 10500, 10700, 10900, 11000, 11350, 11500, 11850, 12000, 12350,
                        12500, 12850, 13000, 13500, 14000, 14500, 14900, 15000, 15500, 16000, 16500, 17000, 17500, 18000, 18500,
                        19000, 19500, 19900, 20000, 20500, 21000, 21500, 22000, 22500, 23000, 23500, 24000, 24500, 25000, 25500,
                        26000, 26500, 27000, 27500, 28000, 28500, 29000, 29900, 30000, 31000, 32000, 33000, 34000, 35000, 37000,
                        39000, 40000, 43500, 45000, 48500, 50000, 55000, 60000, 65000, 70000, 75000, 80000, 90000, 100000,
                        2147483647}, //mobiliario cocinas

                {0, 50, 100, 150, 190, 200, 240, 250, 290, 300, 340, 350, 390, 400, 440, 450, 490, 499, 500, 540, 550, 590, 600,
                        640, 650, 690, 700, 740, 750, 790, 800, 840, 890, 900, 940, 950, 990, 999, 1000, 1100, 1200, 1300, 1400,
                        1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2300, 2400, 2490,
                        2500, 2600, 2700, 2800, 2900, 2990, 3000, 3200, 3400, 3500, 3700, 3900, 4000, 4200, 4400, 4500, 4600, 4900,
                        5000, 5200, 5400, 5500, 5700, 5900, 6000, 6200, 6400, 6500, 6700, 6900, 7000, 7200, 7400, 7500, 7900, 8000,
                        8500, 8900, 9000, 9500, 9900, 10000, 10500, 11000, 11500, 12000, 12500, 13000, 13500, 14000, 14500, 14900,
                        15000, 15500, 16000, 16500, 17000, 17500, 18000, 18500, 19000, 19500, 19900, 20000, 21000, 22000, 23000,
                        24000, 25000, 27000, 29000, 30000, 33000, 35000, 40000, 45000, 50000, 60000, 75000, 100000,
                        2147483647}, //organizacion hogar

                {0, 600, 900, 1000, 1300, 1500, 1700, 1900, 2000, 2300, 2500, 2700, 2900, 3000, 3400, 3700, 4000, 4500, 5000,
                        5500, 6000, 6500, 7000, 7500, 8000, 8500, 9000, 9500, 10000, 11000, 12000, 13000, 14000, 15000, 17000,
                        19000, 22000, 25000, 30000, 35000, 40000, 50000, 60000, 70000, 80000, 90000, 100000, 150000, 200000,
                        2147483647}, //muebles exterior

                {0, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000, 11000, 12000, 13000, 14000, 15000, 17000, 19000, 20000,
                        22000, 24000, 26000, 28000, 30000, 33000, 37000, 45000, 55000, 75000, 100000, 2147483647}, //muebles bano

                {0, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1200, 1400, 1500, 1700, 1900, 2000, 2200, 2400, 2500,
                        2700, 2900, 3000, 3300, 3500, 3900, 4000, 4500, 5000, 6000, 7000, 9000, 10000, 13000, 16000, 20000,
                        30000, 2147483647}, //perchero

                {0, 250, 390, 500, 650, 800, 950, 1100, 1300, 1500, 1700, 1900, 2200, 2500, 3000, 3500, 4000, 4500, 5000,
                        6000, 7000, 8000, 9000, 10000, 15000, 2147483647}, //cesto

                {0, 2147483647}, //ordenador de publico

                {0, 2147483647}, //paraguero

                {0, 2147483647}, //acacia
                {0, 2147483647}, //somos_mas
                {0, 2147483647}, //misionlive
                {0, 2147483647}, //primero+uno
                {0, 2147483647}, //marcarimport
                {0, 2147483647}, //mundo+ofi-max
                {0, 2147483647}, //herrajes+castelmax
                {0, 2147483647}, //modular drawers
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
        HashMap<String, Mercadolibre02b.Item> itemHashMap = new HashMap<String, Mercadolibre02b.Item>();
        String usuario = "SOMOS_MAS";

        //////////////////////////////////////////////////////////////////
        ArrayList<Integer> interval1 = new ArrayList<Integer>(Arrays.asList(0,5000,15000,Integer.MAX_VALUE));
        String url1 = apiBaseUrls[0];

        buildIntervals(url1, 800, client);

        /*
        for (int i=1; i<interval1.size(); i++){
            int since = interval1.get(i-1);
            int to = interval1.get(i);

        }*/
        //////////////////////////////////////////////////////////////////


        for (String webBaseUrl : webBaseUrls) {
            Logger.log("XXXXXXXXXXXXX Procesando nueava web url " + webBaseUrl);
            processItemsOnUrl(webBaseUrl, client, itemHashMap);
        }

        if (!ONLY_RELEVANT) {
            for (String apiBaseUrl : apiBaseUrls) {
                Logger.log("XXXXXXXXXX Procesando nueava api url " + apiBaseUrl);
                processItemsWithApi(apiBaseUrl, -1, -1, client, itemHashMap, usuario);
            }
        }

        for (int i = 0; i < intervals.length; i++) {
            int[] interval=intervals[i];
            String url=apiBaseUrls[i];
            for (int j = 1; j < interval.length; j++) {
                int since = interval[j - 1] + 1;
                int upto = interval[j];
                Logger.log("XXXXXXXXXX Procesando intervalo " + since + "-" + upto + " " + url);
                processItemsWithApi(url, since, upto, client, itemHashMap, usuario);
            }
        }

        Logger.log("XXXXXXXXXX Agregando posibles pausados.  itemHashMap=" + itemHashMap.size());
        addPossiblePaused(itemHashMap);

        //removemos lo que no nos interesa o ya fue procesado
        Logger.log("XXXXXXXXXX Purgando items 1. itemHashMap=" + itemHashMap.size());
        ArrayList<String> incompleteList = purgeItemHashMap(itemHashMap);
        Logger.log("XXXXXXXXXX Completando items 1.  itemHashMap=" + itemHashMap.size());
        completeWebItems(client, itemHashMap, incompleteList);
        Logger.log("XXXXXXXXXX Purgando items 2. itemHashMap=" + itemHashMap.size());
        incompleteList = purgeItemHashMap(itemHashMap);
        if (incompleteList.size() > 0) {
            System.out.println("algo salio mal aca");
        }

        Logger.log("XXXXXXXXXX Purgando items 3. itemHashMap=" + itemHashMap.size());
        purgeItemHashMap2(itemHashMap);

        Logger.log("XXXXXXXXXX Completando items 2. itemHashMap=" + itemHashMap.size());
        completeSellerName(itemHashMap);

        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
        ArrayList<Thread> removeList = new ArrayList<Thread>();
        Logger.log("XXXXXXXXXX Procesando productos. itemHashMap=" + itemHashMap.size());
        for (Mercadolibre02b.Item item : itemHashMap.values()) {


            ProductPageProcessor ppp = new ProductPageProcessor(item.permalink, item.sellerId, item.page, item.ranking, SAVE, DEBUG, DATABASE, getGlobalDate(), false);
            threadArrayList.add(ppp);
            ppp.start();

            while (threadArrayList.size() >= MAX_THREADS) {
                removeList.clear();
                for (Thread t : threadArrayList) {
                    if (!t.isAlive()) {
                        removeList.add(t);
                    }
                }
                for (Thread t : removeList) {
                    threadArrayList.remove(t);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Logger.log(e);
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

        String msg = "******************************************************\r\n"
                + Counters.getGlobalPageCount() + " paginas procesadas\r\n "
                + Counters.getGlobalProductCount() + " productos procesados\r\n "
                + Counters.getGlobalNewsCount() + " productos con novedades";
        System.out.println(msg);
        Logger.log(msg);

        boolean b = false;

    }

    private static  ArrayList<Integer> buildIntervals(String url1, int maxItemsInRange, CloseableHttpClient client) {
        ArrayList<Integer> intervals = new ArrayList<Integer>();
        rebuildInterval(url1,0,Integer.MAX_VALUE,intervals,maxItemsInRange, client);
        Collections.sort(intervals);

        ArrayList<Integer> removelist=new ArrayList<Integer>();
        for (int i = 1; i < (intervals.size() - 1); i++) {
            int since = intervals.get(i - 1);
            int to = intervals.get(i + 1);
            String newurl = url1 + "&price=" + since + "-" + to;
            JSONObject itemListObject = HttpUtils.getJsonObjectWithoutToken(newurl, client, false);
            JSONObject pagingObject = itemListObject.getJSONObject("paging");
            int total = pagingObject.getInt("total");
            if (total == 0 ) {
                removelist.add(intervals.get(i));
            }
        }
        for (Integer itemToRemove: removelist){
            intervals.remove(itemToRemove);
        }
        removelist.clear();


        boolean itemRemoved=true;
        while (itemRemoved) {
            itemRemoved=false;
            for (int i = 1; i < (intervals.size() - 1); i++) {
                int since = intervals.get(i - 1) + 1;
                int to = intervals.get(i + 1);
                String newurl = url1 + "&price=" + since + "-" + to;
                JSONObject itemListObject = HttpUtils.getJsonObjectWithoutToken(newurl, client, false);
                JSONObject pagingObject = itemListObject.getJSONObject("paging");
                int total = pagingObject.getInt("total");
                if (total < maxItemsInRange) {
                    removelist.add(intervals.get(i));
                    i++;
                }
            }
            itemRemoved=removelist.size()>0;
            for (Integer itemToRemove: removelist){
                intervals.remove(itemToRemove);
            }
            removelist.clear();
        }


        for (int i = 0; i < intervals.size(); i++) {
            System.out.print(intervals.get(i)+",");
        }


        for (int i = 1; 0 < intervals.size(); i++) {
            int since = intervals.get(i - 1) + 1;
            int to = intervals.get(i);
            String newurl = url1 + "&price=" + since + "-" + to;
            JSONObject itemListObject = HttpUtils.getJsonObjectWithoutToken(newurl, client, false);
            JSONObject pagingObject = itemListObject.getJSONObject("paging");
            int total = pagingObject.getInt("total");
            if (total >maxItemsInRange) {
                System.out.println("XXXXXXXXXXXX "+since+"-"+to+" = " + total);
            }else {
                System.out.println(""+since+"-"+to+" = " + total);
            }

        }
        return intervals;
    }

    private static void rebuildInterval(String url, int since, int to, ArrayList<Integer> valuesArrayList,
                                        int maxItemsInRange, CloseableHttpClient client){
        String newurl=url+"&price="+since+"-"+to;
        JSONObject itemListObject = HttpUtils.getJsonObjectWithoutToken(newurl,client,false);
        JSONObject pagingObject = itemListObject.getJSONObject("paging");
        int total = pagingObject.getInt("total");
        if (total>800 && since<to){
            int intermediateValue=((to-since)/2)+since;
            if (!valuesArrayList.contains(intermediateValue)){
                valuesArrayList.add(intermediateValue);
                rebuildInterval(url,since,intermediateValue,valuesArrayList,maxItemsInRange,client);
                rebuildInterval(url,intermediateValue,to,valuesArrayList,maxItemsInRange,client);
            }
        }
        boolean b=false;
    }

    private static void completeSellerName(HashMap<String, Mercadolibre02b.Item> itemHashMap) {
        for (Mercadolibre02b.Item item : itemHashMap.values()) {
            if (item.sellerName == null || item.sellerName.isEmpty()) {
                item.sellerName = DatabaseHelper.fetchSellerName(HTMLParseUtils.getFormatedId(item.id), DATABASE);
            }
        }
    }

    private static boolean addPossiblePaused(HashMap<String, Mercadolibre02b.Item> itemHashMap) {
        //XXXXXXXXXXXXXXXXXXXXXXXXXXXX posibles pausados
        ArrayList<String> proccessedItemsArrayList = new ArrayList<>();
        for (String itemId : itemHashMap.keySet()) {
            String formatedItem = HTMLParseUtils.getFormatedId(itemId);
            proccessedItemsArrayList.add(formatedItem);
        }

        ArrayList<String> possiblyPausedProductList = DatabaseHelper.getPossiblePausedProducts(DATABASE, proccessedItemsArrayList, getGlobalDate());

        int totalItemsSofar = 0;
        if (proccessedItemsArrayList != null) {
            totalItemsSofar = proccessedItemsArrayList.size();
        }
        String msg = "posibles pausados: " + possiblyPausedProductList.size() + " de " + totalItemsSofar;
        System.out.println(msg);
        Logger.log(msg);

        for (String itemId : possiblyPausedProductList) {
            Mercadolibre02b.Item item = new Mercadolibre02b.Item();
            item.id = HTMLParseUtils.getUnformattedId(itemId);
            itemHashMap.put(item.id, item);
        }
        return false;
    }


    private static void purgeItemHashMap2(HashMap<String, Mercadolibre02b.Item> itemHashMap) {
        ArrayList<String> removeList = new ArrayList<String>();
        for (Mercadolibre02b.Item item : itemHashMap.values()) {
            String formatedId = getFormatedId(item);
            int totalSold = DatabaseHelper.fetchTotalSold(formatedId, DATABASE);
            if (totalSold > 0) {
                if (item.totalSold < 5) {
                    if (totalSold == item.totalSold) {
                        removeList.add(item.id);
                    }
                } else {
                    int topOfRange = getTopOfRange(item.totalSold);
                    if (totalSold == topOfRange) {
                        removeList.add(item.id);
                    }
                }
            }
        }
        for (String itemId : removeList) {
            itemHashMap.remove(itemId);
        }
    }


    private static int getTopOfRange(int totalSold) {
        int result = -1;
        if (totalSold == 5) {
            result = 25;
        } else {
            if (totalSold == 25) {
                result = 50;
            } else {
                if (totalSold == 50) {
                    result = 100;
                } else {
                    if (totalSold == 100) {
                        result = 150;
                    } else {
                        if (totalSold == 150) {
                            result = 200;
                        } else {
                            if (totalSold == 200) {
                                result = 250;
                            } else {
                                if (totalSold == 250) {
                                    result = 500;
                                } else {
                                    if (totalSold == 500) {
                                        result = 5000;
                                    } else {
                                        if (totalSold == 5000) {
                                            result = 50000;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private static void completeWebItems(CloseableHttpClient client, HashMap<String, Mercadolibre02b.Item> itemHashMap, ArrayList<String> incompleteList) {
        boolean processFinished = false;
        int i = -1;
        while (!processFinished) {
            String itemsIds = "";
            for (int j = 0; j < 20; j++) {
                i++;
                if (i == incompleteList.size()) {
                    processFinished = true;
                    break;
                }
                itemsIds += incompleteList.get(i) + ",";
            }
            if (itemsIds.contains(",")) {
                itemsIds = itemsIds.substring(0, itemsIds.length() - 1);
                String itemsUrl = "https://api.mercadolibre.com/items?ids=" + itemsIds;
                JSONObject jsonObject = HttpUtils.getJsonObjectWithoutToken(itemsUrl, client, true);
                JSONArray jsonArray = jsonObject.getJSONArray("elArray");
                for (int j = 0; j < jsonArray.length(); j++) {
                    JSONObject itemObject2 = jsonArray.getJSONObject(j);
                    int code = itemObject2.getInt("code");
                    if (code != 200) {
                        continue; //nunca paso
                    }
                    JSONObject productObj = itemObject2.getJSONObject("body");
                    String id = productObj.getString("id");
                    Mercadolibre02b.Item item = itemHashMap.get(id);
                    boolean completed = completeItem(productObj, item);
                    if (!completed) {
                        itemHashMap.remove(id);
                    }
                }
            }
        }
    }

    private static ArrayList<String> purgeItemHashMap(HashMap<String, Mercadolibre02b.Item> itemHashMap) {
        ArrayList<String> removeList = new ArrayList<String>();
        ArrayList<String> incompleteList = new ArrayList<String>();
        for (Mercadolibre02b.Item item : itemHashMap.values()) {
            if (item.totalSold > -1 && item.totalSold < MINIMUM_SALES) {
                removeList.add(item.id);
                continue;
            }
            String formatedId = getFormatedId(item);
            Date lastUpdate = DatabaseHelper.fetchLastUpdate(formatedId, DATABASE);
            if (lastUpdate != null) {//producto existente
                boolean sameDate = Counters.isSameDate(lastUpdate, getGlobalDate());
                if (sameDate) {
                    removeList.add(item.id);
                }
            }
            if (item.totalSold == -1) {
                incompleteList.add(item.id);
            }
        }
        for (String itemId : removeList) {
            itemHashMap.remove(itemId);
        }
        return incompleteList;
    }

    private static String getFormatedId(Mercadolibre02b.Item item) {
        return HTMLParseUtils.getFormatedId(item.id);
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


    private static void processItemsOnUrl(String webBaseUrl, CloseableHttpClient client, HashMap<String, Mercadolibre02b.Item> itemHashMap) {
        int page = 0;
        int ITEMS_PER_PAGE = 48;
        boolean DEBUG = false;
        boolean processFinished = false;


        while (!processFinished) {
            page++;
            if (page == 43) {
                processFinished = true;
                continue;
            }

            Counters.incrementGlobalPageCount();
            int since = (page - 1) * ITEMS_PER_PAGE + 1;
            String sinceStr = "_Desde_" + since;
            String uRL = webBaseUrl + sinceStr;
            uRL += "_DisplayType_G";

            String htmlStringFromPage = HttpUtils.getHTMLStringFromPage(uRL, client, DEBUG, false);
            requestCount++;
            if (!HttpUtils.isOK(htmlStringFromPage)) { //suponemos que se terminó
                // pero tambien hacemos pausa por si es problema de red
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Logger.log(e);
                }
                Logger.log("AA hmlstring from page is null " + uRL);
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                client = null;
                client = HttpUtils.buildHttpClient();
                continue;
            }

            htmlStringFromPage = htmlStringFromPage.toString();
            int resultSectionPos = htmlStringFromPage.indexOf("search-results");
            String resultListHMTLData = null;
            if (resultSectionPos == -1) {
                if (htmlStringFromPage.indexOf("Escrib") > 0
                        && htmlStringFromPage.indexOf("en el buscador lo que quer") > 0
                        && htmlStringFromPage.indexOf("s encontrar") > 0) {
                    String msg = "No se pudo obtener ningun resultado en este intervalo " + uRL;
                    Logger.log(msg);
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
            if (productsOnPage < 47) {//EL NUMERO ES 47 TODO VER QUE PASA CON LOS ITEMS QUE TIENEN URL DISTINTA
                processFinished = true;
            }

            for (String productUrl : productsURLArrayList) {
                Counters.incrementGlobalProductCount();
                Mercadolibre02b.Item item = null;
                String productId = HTMLParseUtils.getProductIdFromURL(productUrl);
                productId = productId.substring(0, 3) + productId.substring(4);
                if (itemHashMap.containsKey(productId)) {
                    item = itemHashMap.get(productId);
                } else {
                    item = new Mercadolibre02b.Item();
                    item.id = productId;
                    itemHashMap.put(productId, item);
                }

                item.page = page;
                item.permalink = productUrl;

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

                item.title = HTMLParseUtils.getTitle2(productHTMLdata);
                if (item.title != null) {
                    item.title = item.title.trim();
                }
                if (item.title == null || item.title.length() == 0) {
                    Logger.log("AA invalid title on page " + page + " url " + uRL);
                }

                item.discount = HTMLParseUtils.getDiscount2(productHTMLdata);
                if (item.discount < 0) {
                    Logger.log("AA I couldn't get the discount on " + productUrl);
                }

                item.shipping = HTMLParseUtils.getShipping(productHTMLdata);

                item.premium = HTMLParseUtils.getPremium(productHTMLdata);

                item.price = HTMLParseUtils.getPrice2(productHTMLdata);
                if (item.price == 0) {
                    Logger.log("AA I couldn't get the price on " + productUrl);
                }

                item.advertised = productHTMLdata.contains("Promocionado"); //todo check

            }

        }
    }

    private static void processItemsWithApi(String apiBaseUrl, int since, int upto, CloseableHttpClient client, HashMap<String, Mercadolibre02b.Item> itemHashMap, String usuario) {
        if (since >= 0) {
            apiBaseUrl += "&price=" + since + "-" + upto;
        }

        String apiSearchUrl = apiBaseUrl;

        int offset = 0;
        int ranking = 0;
        int totalResults = -1; //valor inicial que se reemplazará

        JSONObject jsonObject = null;
        boolean rankingOnly = since == -1;

        while (offset < totalResults || totalResults == -1) {
            if (offset < RESULTS_WITHOUT_TOKEN) {
                jsonObject = HttpUtils.getJsonObjectWithoutToken(apiSearchUrl, client, false);
                if (totalResults == -1) {
                    JSONObject pagingObject = jsonObject.getJSONObject("paging");
                    totalResults = pagingObject.getInt("total");
                    if (totalResults > RESULTS_LIMIT) {
                        totalResults = RESULTS_LIMIT;
                    }
                }
            } else {
                if (ONLY_RELEVANT) {
                    return;
                }
                String msg = "No se pudo recorrer el intervalo por completo sin token " + apiSearchUrl;
                Logger.log(msg);
                jsonObject = HttpUtils.getJsonObjectUsingToken(apiSearchUrl, client, usuario);
            }
            requestCount++;

            JSONArray resultsArray = jsonObject.getJSONArray("results");
            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject productObj = resultsArray.getJSONObject(i);
                Mercadolibre02b.Item item = null;
                String id = productObj.getString("id");
                if (itemHashMap.containsKey(id)) {
                    item = itemHashMap.get(id);
                } else {
                    item = new Mercadolibre02b.Item();
                    item.id = id;
                    itemHashMap.put(id, item);
                }


                ranking++;
                if (item.ranking == 10000) {
                    item.ranking = ranking;
                }
                if (rankingOnly) {
                    continue;
                }


                boolean completed = completeItem(productObj, item);
                if (!completed) {
                    itemHashMap.remove(id);
                }
            }

            offset += 50;
            apiSearchUrl = apiBaseUrl + "&offset=" + offset;
        }
    }

    private static boolean completeItem(JSONObject productObj, Mercadolibre02b.Item item) {

        if (!productObj.has("sold_quantity")) {
            return false;//under review y otros casos raros
        }

        item.totalSold = productObj.getInt("sold_quantity");

        if (item.permalink == null || item.permalink.isEmpty()) {
            item.permalink = productObj.getString("permalink");
        }
        if (item.title == null || item.title.isEmpty()) {
            item.title = productObj.getString("title");
        }
        if (item.price <= 0) {
            item.price = productObj.getDouble("price");
        }
        if (item.discount < 0) {
            item.discount = 0;
            if (productObj.has("original_price") && !productObj.isNull("original_price")) {
                double originalPrice = productObj.getDouble("original_price");
                if (item.price > 0 && originalPrice > 0 && originalPrice > item.price) {
                    item.discount = (int) ((originalPrice - item.price) / originalPrice * 100.0);
                }

            }
        }

        item.shipping = 0;
        if (productObj.has("shipping") && !productObj.isNull("shipping")) {
            JSONObject shippingObject = productObj.getJSONObject("shipping");
            String shippingMode = shippingObject.getString("mode");
            if (shippingMode.equals("me2")) {
                item.shipping = 100;
            }
            if (shippingObject.has("free_shipping") && !shippingObject.isNull("free_shipping")) {
                boolean freeShipping = shippingObject.has("free_shipping");
                if (freeShipping) {
                    item.shipping = 200;
                }
            }

        }

        String listingTypeId = productObj.getString("listing_type_id");
        if (listingTypeId.equals("gold_pro")) {
            item.premium = true;
        } else {
            item.premium = false;
        }

        item.officialStore = false;
        if (productObj.has("official_store_id") && !productObj.isNull("official_store_id")) {
            item.officialStore = true;
        }

        if (productObj.has("seller") && !productObj.isNull("seller")) {
            JSONObject sellerObject = productObj.getJSONObject("seller");
            if (sellerObject.has("permalink") && !sellerObject.isNull("permalink")) {
                String sellerPermalink = sellerObject.getString("permalink");
                item.sellerName = sellerPermalink.substring(sellerPermalink.lastIndexOf("/") + 1);
            }
            item.sellerId = sellerObject.getInt("id");
        } else {
            if (productObj.has("seller_id") && !productObj.isNull("seller_id")) {
                item.sellerId = productObj.getInt("seller_id");
            }
        }
        return true;
    }


}