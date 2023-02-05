package com.ml;

import com.ml.utils.Counters;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.SData;

import org.apache.http.impl.client.CloseableHttpClient;

import java.sql.Date;


public class MercadoLibre01b {
    static final String currentDateStr="DD-MM-YYYY";

    static final String DATABASE = "ML1";
    static final boolean SAVE = true;
    static final boolean IGNORE_VISITS = true;
    static final int MINIMUM_SALES = 1;
    static final boolean ONLY_RELEVANT=false;

    public static void main(String[] args) {

        Date runDate=Counters.parseDate(currentDateStr);

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

        CloseableHttpClient client = HttpUtils.buildHttpClient();
        String usuario = SData.getSomosMas();

        ReportRunner.runWeeklyReport(apiBaseUrls, client, usuario, DATABASE, ONLY_RELEVANT,
                IGNORE_VISITS, runDate, MINIMUM_SALES,SAVE );

        String msg = "******************************************************\r\n"
                + Counters.getGlobalRequestCountCount() + " requests\r\n "
                + Counters.getGlobalProductCount() + " productos procesados\r\n "
                + Counters.getGlobalDisableCount() + " productos deshabilitados\r\n "
                + Counters.getGlobalNewsCount() + " productos con novedades";
        System.out.println(msg);
        Logger.log(msg);

    }


}