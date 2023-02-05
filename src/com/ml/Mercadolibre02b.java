package com.ml;


import com.ml.utils.Counters;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.SData;
import org.apache.http.impl.client.CloseableHttpClient;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;


public class Mercadolibre02b {
    static final String currentDateStr="DD-MM-YYYY";


    static final String DATABASE="ML2";
    static final boolean SAVE = true;
    static final boolean IGNORE_VISITS = true;
    static final int MINIMUM_SALES = 1;
    static final boolean ONLY_RELEVANT=true;


    public static void main(String[] args) {

        Date runDate=Counters.parseDate(currentDateStr);

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
