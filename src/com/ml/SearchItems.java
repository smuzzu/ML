package com.ml;

import com.ml.utils.DatabaseHelper;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.SData;
import com.ml.utils.TokenUtils;

import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchItems {

    public static void main(String[] args) {
        String searchTerms="eurohard";

        int priceInterval=400;
        int maxPrice=18000;

        int totalIntervals= (int)Math.round(maxPrice/priceInterval*1.0)+1;
        String priceArray[]=new String[totalIntervals];
        String price1,price2;
        for (int i=0; i<totalIntervals; i++){
            price1=(priceInterval*i+1)+".0";
            price2=priceInterval*(i+1)+".0";
            if (i==0){
                price1="*";
            }
            if (i==(totalIntervals-1)){
                price2="*";
            }
            priceArray[i]=price1+"-"+price2;
        }


        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        ArrayList<String> idArrayList = new ArrayList<String>();

        String msg = "Total de registros encontrados (tienen que ser menos de 1000)";
        System.out.println(msg);
        Logger.log(msg);

        String urlSearchTerms=searchTerms.replace(" ","-");
        for (String priceRangeId: priceArray) {
            int totalItems = 999999;
            for (int offset = 0; offset <= totalItems; offset += 50) {
                String searchUrl = "https://api.mercadolibre.com/sites/MLA/search?q=" + urlSearchTerms + "&price=" + priceRangeId + "&offset=" + offset;
                JSONObject jsonSearchDetails = HttpUtils.getJsonObjectWithoutToken(searchUrl, httpClient, false);
                if (offset == 0) {
                    JSONObject pagingObj = jsonSearchDetails.getJSONObject("paging");
                    totalItems = pagingObj.getInt("total");
                    msg = "Buscando online en ML rango " + priceRangeId + " = " + totalItems;
                    System.out.println(msg);
                    Logger.log(msg);
                    if (totalItems>1000){
                        msg = "Rango demasiado grande " + priceRangeId + " contiene " + totalItems + " items.  Por favor corrija los rangos y reintente";
                        System.out.println(msg);
                        Logger.log(msg);
                        System.exit(0);
                    }

                }

                if (jsonSearchDetails != null) {
                    JSONArray jsonSearchArray = (JSONArray) jsonSearchDetails.get("results");
                    for (Object searchObjectArray : jsonSearchArray) {
                        JSONObject searchItem = (JSONObject) searchObjectArray;
                        String itemId = searchItem.getString("id");
                        String formattedId = "MLA-" + itemId.substring(3);
                        if (!idArrayList.contains(formattedId)) {
                            idArrayList.add(formattedId);
                        }
                    }
                }
            }
        }
        msg=idArrayList.size() + " elementos encontrados online en ML";
        System.out.println(msg);
        Logger.log(msg);

        String hostname = TokenUtils.getHostname();
        if (hostname!=null && hostname.equals(SData.getHostname1())) {
            msg = "Buscando en ML1... ";
            System.out.println(msg);
            Logger.log(msg);
            int newItems = 0;
            String databaseSearchTerms = searchTerms.replace(" ", "%");
            ArrayList<String> idsML1 = DatabaseHelper.fetchProductsLike(databaseSearchTerms, "ML1");
            for (String id : idsML1) {
                if (!idArrayList.contains(id)) {
                    idArrayList.add(id);
                    newItems++;
                }
            }

            msg = idsML1.size() + " elementos encontrados en base ML1, de los cuales " + newItems + " son nuevos";
            System.out.println(msg);
            Logger.log(msg);


            msg = "Buscando en ML2... ";
            System.out.println(msg);
            Logger.log(msg);
            newItems = 0;
            ArrayList<String> idsML2 = DatabaseHelper.fetchProductsLike(searchTerms, "ML2");
            for (String id : idsML2) {
                if (!idArrayList.contains(id)) {
                    idArrayList.add(id);
                    newItems++;
                }
            }
            msg = idsML2.size() + " elementos encontrados en base ML2, de los cuales " + newItems + " son nuevos";
            System.out.println(msg);
            Logger.log(msg);
        }else {
            msg = "Buscando en ML6... ";
            System.out.println(msg);
            Logger.log(msg);
            int newItems = 0;
            String databaseSearchTerms = searchTerms.replace(" ", "%");
            ArrayList<String> idsML6 = DatabaseHelper.fetchProductsLike(databaseSearchTerms, "ML6");
            for (String id : idsML6) {
                if (!idArrayList.contains(id)) {
                    idArrayList.add(id);
                    newItems++;
                }
            }
            msg = idsML6.size() + " elementos encontrados en base ML6, de los cuales " + newItems + " son nuevos";
            System.out.println(msg);
            Logger.log(msg);
        }

        msg="Total de elementos encontrados "+idArrayList.size();
        System.out.println(msg);
        Logger.log(msg);

        String inClause="in(";
        for (String id: idArrayList){
            inClause+="'"+id+"',";
        }
        inClause=inClause.substring(0,inClause.length()-1);
        inClause+=")";
        System.out.println(inClause);
        Logger.log(inClause);

        msg="Fin";
        System.out.println(msg);
        Logger.log(msg);
    }
}
