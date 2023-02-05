package com.ml.utils;


import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class HTMLParseUtils {

    public static String OFICIAL_STORE_LABEL = "Tienda oficial de Mercado Libre";
    public static String PROFILE_BASE_URL = "https://perfil.mercadolibre.com.ar/";
    public static String QUESTIONS_BASE_URL = "https://articulo.mercadolibre.com.ar/noindex/questions/";
    public static String ARTICLE_PREFIX = "MLA";
    public static String SERVICIO_URL = "https://servicio.";



    public static String getFormatedId(String itemId) {
        String formatedId= itemId.substring(0,3)+"-"+ itemId.substring(3);
        return formatedId;
    }

    public static String getUnformattedId(String itemId) {
        String formatedId= itemId.substring(0,3)+ itemId.substring(4);
        return formatedId;
    }

    public static int getTotalSold(String htmlStringFromProductPage, String productUrl) {
        int totalSold = 0;
        try {
            if (!htmlStringFromProductPage.contains("vendido")
                    && !htmlStringFromProductPage.contains("contrataci")){ //servicios
                return 0;
            }
            String vendidoStr="vendido";
            String clossingTag="";
            int soldPos1 = htmlStringFromProductPage.indexOf("item-conditions\">");
            if (soldPos1>-1){
                soldPos1+=18;
                clossingTag="</div>";
            }else {
                soldPos1 = htmlStringFromProductPage.indexOf("ui-pdp-subtitle\">");
                if (soldPos1>-1){
                    soldPos1+=17;
                    clossingTag="</span>";
                }else {
                    soldPos1 = htmlStringFromProductPage.indexOf("vip-classified-info\">");
                    if (soldPos1>-1){
                        soldPos1+=21;
                        clossingTag="</article>";
                        vendidoStr="contratac";
                    }else {
                        Logger.log("XXXXXXXXXXXXXXXXXXXXXXXXX LA PUTA QUE TE PARIO ML ");
                        Logger.log("No se pudo reconocer cantidad vendida en " + productUrl);
                        return -1;
                    }
                }
            }

            int soldPos2 = htmlStringFromProductPage.indexOf(clossingTag, soldPos1);

            String soldStr = htmlStringFromProductPage.substring(soldPos1, soldPos2);
            soldPos2 = soldStr.indexOf(vendidoStr);
            if (soldPos2 == -1) {
                return 0;//no vendio
            }
            soldStr = soldStr.substring(0, soldPos2);
            //cualquier cosa que no es numero se elimina
            soldStr=soldStr.replaceAll("[^\\d.]", "");
            totalSold = Integer.parseInt(soldStr);

        } catch (Exception e) {
            String msg = "I couldn't get total sold on " + productUrl;
            System.out.println(msg);
            Logger.log(msg);
            Logger.log(e);
            return -1;
        }
        return totalSold;
    }

    public static String getLastQuestion(String htmlStringFromProductPage) {
        String lastQuestion = null;
        int lastQuestionPos1 = htmlStringFromProductPage.indexOf("list__question__label\">");
        if (lastQuestionPos1 > 0) {
            lastQuestionPos1+=23;
            int lastQuestionPos2 = htmlStringFromProductPage.indexOf("<", lastQuestionPos1);
            if (lastQuestionPos2==lastQuestionPos1){
                lastQuestionPos2++;
                lastQuestionPos1 = htmlStringFromProductPage.indexOf(">", lastQuestionPos2)+1;
                lastQuestionPos2 = htmlStringFromProductPage.indexOf("<", lastQuestionPos2);
            }
            lastQuestion = htmlStringFromProductPage.substring(lastQuestionPos1, lastQuestionPos2);
            if (lastQuestion != null) {
                lastQuestion = lastQuestion.trim();
            }
        }
        return lastQuestion;
    }

    public static String getQuestionsURL(String productId) {
        String questionsURL = QUESTIONS_BASE_URL + ARTICLE_PREFIX + productId.substring(4);
        return questionsURL;
    }


    public static String getProductIdFromURL(String productUrl) {
        int idPos1 = productUrl.indexOf(ARTICLE_PREFIX);
        int idPos2 = idPos1 + 13;
        if (idPos2>productUrl.length()){
            idPos2=productUrl.length();
        }
        String productId = null;
        try {
            productId = productUrl.substring(idPos1, idPos2);
        }catch (Exception e){
            boolean b=false;
        }

        if (!productId.contains("-")){
            productId=getFormatedId(productId);
        }

        return productId;
    }

    public static double getPrice(String htmlString, String url) {
        if (htmlString.indexOf("Precio a convenir") >0){
            return 0.01; //servicio
        }
        int pricePos1 = htmlString.indexOf("\"price\":") + 8;
        int pricePos2 = htmlString.indexOf(",", pricePos1);
        String priceStr = htmlString.substring(pricePos1, pricePos2);

        Double price = null;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Logger.log(" I couldn't get the price on " + url);
            Logger.log(e);
        }
        return price;
    }


    public static double getStars(String htmlString, String url) {
        double stars = 0l;
        String msg = null;

        String allStarsStr = StringUtils.substringBetween(htmlString, "ui-pdp-review__ratings", "/span");

        if (allStarsStr == null) {
            msg = "Cannot find stars on " + url;
            Logger.log(msg);
            System.out.println(msg);
        }else{
            stars = allStarsStr.split("star-full").length - 1 * 1.0;
            boolean halfStar = allStarsStr.indexOf("star-half") > 0;
            if (halfStar) {
                stars += 0.5;
            }
        }
        return stars;
    }

    public static int getStock(String htmlString, String url) {

        int stock=0;
        boolean lastone=false;
        String disponibleStr="disponible";
        String clossingTag="";
        int stockPos1 = htmlString.indexOf("quantity__available\">");
        if (stockPos1>-1){
            stockPos1+=21;
            clossingTag="</span>";
        }else {
            stockPos1 = htmlString.indexOf("buybox__quantity");
            if (stockPos1>-1){
                lastone=true;
                stockPos1+=18;
                stockPos1=htmlString.indexOf(">",stockPos1)+1;
                disponibleStr="ltima disponible";
                clossingTag="</p>";
            }else {//todo ver aca variantes de esto
                Logger.log("XXXXXXXXXXXXXXXXXXXXXXXXX LA PUTA QUE TE PARIO ML ");
                Logger.log("No se pudo reconocer cantidad vendida en " + url);
                return -1;
            }
        }

        int stockPos2 = htmlString.indexOf(clossingTag, stockPos1);

        String stockStr = htmlString.substring(stockPos1, stockPos2);
        if (lastone && stockStr.contains(disponibleStr)){//ultima disponible
            return 1;
        }
        stockPos2 = stockStr.indexOf(disponibleStr);
        if (stockPos2 == -1) {
            return 0;//no vendio
        }
        stockStr = stockStr.substring(0, stockPos2);
        //cualquier cosa que no es numero se elimina
        stockStr=stockStr.replaceAll("[^\\d.]", "");
        stock = Integer.parseInt(stockStr);

        return stock;
    }

    public static ArrayList<String> getSelectedVariation(String htmlString, String url) {
        ArrayList<String> result  = new ArrayList<String>();
        String metadata = "variations__selected";
        int pos1 = htmlString.indexOf(metadata);
        while (pos1 > -1) {
            int pos2=htmlString.indexOf(">",pos1)+1;
            int pos3=htmlString.indexOf("<",pos2);
            String selectedVariation = htmlString.substring(pos2, pos3);
            if (!selectedVariation.isEmpty() && !selectedVariation.startsWith("Seleccion")) {
                if (!result.contains(selectedVariation)) {
                    result.add(selectedVariation);
                }
            }
            pos1 = htmlString.indexOf(metadata,pos3);
        }
        return result;
    }

    public static ArrayList<String> getSelectedVariationTypeURLs(String htmlString, String url, HashMap<String, ArrayList<String>> variationsMap) {
        int minQty=Integer.MAX_VALUE;
        String selectedVariation=null;
        for (String variation:variationsMap.keySet()){
            int qty=variationsMap.get(variation).size();
            if (qty<minQty){
                minQty=qty;
                selectedVariation=variation;
            }
        }
        ArrayList<String> result= new ArrayList<String>();
        if (variationsMap.containsKey(selectedVariation)) {
            result=variationsMap.get(selectedVariation);
        }

        return result;
    }

    public static HashMap<String, ArrayList<String>> getVariationsMap(String htmlString, String url) {
        HashMap<String,ArrayList<String>> variationsMap=new HashMap<String,ArrayList<String>>();
        String metadata1="\"action\":\"PICKER_SELECTION\"";
        String metadata2="\"category\":\"ITEM\"";
        int pos1 = htmlString.indexOf(metadata1);
        while (pos1>-1){
            int pos2= htmlString.indexOf("}",pos1)-1;
            String variationHtml= htmlString.substring(pos1,pos2);
            if (variationHtml.contains(metadata2)){
                int pos3=variationHtml.indexOf("label");
                pos3=variationHtml.indexOf("\"",pos3)+1;
                pos3=variationHtml.indexOf("\"",pos3)+1;
                int pos4=variationHtml.indexOf("-",pos3);
                int pos5=pos4+1;
                int pos6=variationHtml.indexOf("-",pos5);
                int pos7=pos6+1;

                String variationClass=variationHtml.substring(pos3,pos4);
                String variationName=variationHtml.substring(pos5,pos6);
                String variationPath=variationHtml.substring(pos7);
                String variationUrl=variationClass+":"+variationPath;
                try {//: se combierte en %3A y otras conversiones
                    variationUrl= URLEncoder.encode(variationUrl, String.valueOf(StandardCharsets.UTF_8));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                variationUrl= url +"?attributes="+variationUrl;
                String compound=variationName+"|"+variationUrl;

                ArrayList<String> myVariationsArrayList=null;
                if (variationsMap.containsKey(variationClass)){
                    myVariationsArrayList = variationsMap.get(variationClass);

                }else {
                    myVariationsArrayList=new ArrayList<String>();
                    variationsMap.put(variationClass, myVariationsArrayList);
                }
                if (!myVariationsArrayList.contains(compound)){
                    myVariationsArrayList.add(compound);
                }
            }
            pos1++;
            pos1 = htmlString.indexOf(metadata1,pos1);
        }
        return variationsMap;
    }

    public static boolean isPaused(String productPage){
        return productPage.contains("Publicación pausada") || productPage.contains("Publicación finalizada");
    }

    //todo muchas veces no devueve el dato correcto, la pagina no carga completa
    public static boolean isPromoted(String htmlString){
        return htmlString.contains("ui-pdp-promotions-pill ui-pdp-highlights");
    }

    //todo muchas veces no devueve el dato correcto, la pagina no carga completa
    public static boolean isMostSold(String htmlString){
        return htmlString.contains("MÁS VENDIDO");
    }



    public static int getReviews(String htmlString, String url) {
        int reviews = 0;
        String msg = null;

        int pos1 = htmlString.indexOf("review__amount");
        if (pos1 == -1) {
            return 0;//no tiene reviews
        }
        pos1 = htmlString.indexOf("(", pos1);
        if (pos1 == -1) {
            msg = "Cannot find reviews II on " + url;
            Logger.log(msg);
            System.out.println(msg);
        }
        pos1++;
        int pos2 = htmlString.indexOf(")", pos1);
        if (pos2 == -1) {
            msg = "Cannot find reviews III on " + url;
            Logger.log(msg);
            System.out.println(msg);
        }
        String reviewsStr = htmlString.substring(pos1, pos2);
        reviewsStr = reviewsStr.trim();

        try {
            reviews = Integer.parseInt(reviewsStr);
        } catch (NumberFormatException e) {
            msg = "Cannot find reviews IV on " + url;
            Logger.log(msg);
            System.out.println(msg);
            Logger.log(e);
        }

        return reviews;
    }


    /*********************************************  productHTMLdata parsers */

    public static String getTitle2(String productHTMLdata) {
        int titlePos1 = productHTMLdata.indexOf("main-title\">");
        if (titlePos1>0){
            titlePos1+=12;
        }else {
            titlePos1=productHTMLdata.indexOf("item-title__primary");
            if (titlePos1>0){
                titlePos1+=22;
            }
            else {
                titlePos1 = productHTMLdata.indexOf("item__title ui-search-item__group__element");
                if (titlePos1 > 0) {
                    titlePos1 += 44;
                } else {
                    titlePos1=productHTMLdata.indexOf("ui-pdp-title");
                    if (titlePos1<0){
                        titlePos1+=14;
                    }else {
                        titlePos1=productHTMLdata.indexOf("<title>");
                        if (titlePos1>0){
                            titlePos1+=7; //ver
                        }else {
                            Logger.log("XXXXXXXXXXXXXXXXXXXXXXXXX LA PUTA QUE TE PARIO ML ");
                            Logger.log("No se pudo reconocer el titulo en " + productHTMLdata);
                        }
                    }
                }
            }
        }

        int titlePos2 = productHTMLdata.indexOf("<", titlePos1);
        return productHTMLdata.substring(titlePos1, titlePos2).trim();
    }


    public static double getPrice2(String productHTMLdata) {
        int pricePos1 = productHTMLdata.indexOf("price__fraction\">");
        if (pricePos1>0){
            pricePos1+=17;
        }else { //hacemos esto porque hay productos con tags distintos
            pricePos1 = productHTMLdata.indexOf("price-tag-fraction\">");
            if (pricePos1>0){
                pricePos1+=20;
            } else  {
                if (productHTMLdata.indexOf("Precio a convenir") >0){
                    return 0.01; //servicio
                }
            }
        }
        int pricePos2 = productHTMLdata.indexOf("<", pricePos1);
        if (pricePos1<0 || pricePos2<0 || pricePos1>pricePos2){
            String errorMsg="No se pudo sacar el precio en "+productHTMLdata;
            Logger.log(errorMsg);
            return 0;
        }

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

        double price = 0;
        try {
            price = Double.parseDouble(priceStr);
            if (priceDecimalsStr != null) {
                price += Double.parseDouble(priceDecimalsStr) / 100;
            }
        } catch (NumberFormatException e) {
            Logger.log(e);
        }
        return price;
    }


    public static void main(String args[]){

    }

}
