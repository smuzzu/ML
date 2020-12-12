package com.ml.utils;


import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class HTMLParseUtils {

    public static String OFICIAL_STORE_LABEL = "Tienda oficial de Mercado Libre";
    public static String PROFILE_BASE_URL = "https://perfil.mercadolibre.com.ar/";
    public static String PROFILE_BASE_URL2 = "http://perfil.mercadolibre.com.ar/";
    public static String PRODUCT_LIST_BASE_URL = "https://listado.mercadolibre.com.ar/";
    public static String QUESTIONS_BASE_URL = "https://articulo.mercadolibre.com.ar/noindex/questions/";
    public static String CATALOG_PRODUCT_BASE_URL = "https://api.mercadolibre.com/products/";
    public static String MERCADOLIBRE_BASE_URL = "mercadolibre.com.ar";
    public static String ARTICLE_PREFIX = "MLA";
    public static String CATALOG_ITEM_URL_INDICATOR = "/p/"+HTMLParseUtils.ARTICLE_PREFIX;

    static String SHIPPING1_LABEL = "Envío a todo el país";
    static String SHIPPING2_LABEL = "Llega el";
    static String SHIPPING2B_LABEL = "Env&iacute;o con normalidad"; //pandemia
    static String SHIPPING2C_LABEL = "Envío con normalidad"; //pandemia
    static String SHIPPING3_LABEL = "Llega mañana";
    static String SHIPPING3B_LABEL = "Llega ma&ntilde;ana";
    static String FREE_SHIPPING1_LABEL = "Envío gratis";
    static String FREE_SHIPPING1B_LABEL = "Env&iacute;o gratis";
    static String FREE_SHIPPING2_LABEL = "Llega gratis el";
    static String FREE_SHIPPING3_LABEL = "Llega gratis mañana";
    static String FREE_SHIPPING3B_LABEL = "Llega gratis ma&ntilde;ana";
    static String INTEREST_FREE_PAYMENTS_LABEL = "cuotas sin interés";
    static String INTEREST_FREE_PAYMENTSB_LABEL = "cuotas sin inter&eacute;s";


    public static boolean getOfficialStore(String htmlStringFromProductPage) {
        boolean officialStore = false;
        int officialStorePos1 = htmlStringFromProductPage.indexOf(OFICIAL_STORE_LABEL);
        if (officialStorePos1 >= 0) {
            officialStore = true;
        }
        return officialStore;
    }

    public static String getSeller(String htmlStringFromProductPage, boolean officialStore, String productUrl) {
        int sellerPos1 = 0;
        int sellerPos2 = 0;
        String seller = null;
        if (officialStore) {
            sellerPos1 = htmlStringFromProductPage.indexOf("official-store-info");
            if (sellerPos1 > 0) {
                sellerPos1 = htmlStringFromProductPage.indexOf("title", sellerPos1);
                sellerPos1 += 7;
                sellerPos2 = htmlStringFromProductPage.indexOf("<", sellerPos1);
                seller = htmlStringFromProductPage.substring(sellerPos1, sellerPos2);
            }else {
                sellerPos1=htmlStringFromProductPage.lastIndexOf("ui-pdp-seller__header__title");
                sellerPos1=htmlStringFromProductPage.indexOf(">",sellerPos1)+1;
                sellerPos2=htmlStringFromProductPage.indexOf("<",sellerPos1);
                seller = htmlStringFromProductPage.substring(sellerPos1, sellerPos2);

            }
        } else {
            sellerPos1 = htmlStringFromProductPage.indexOf("reputation-info block");
            if (sellerPos1==-1){
                sellerPos1=htmlStringFromProductPage.indexOf("ui-pdp-seller__list-extra-info");
            }
            if (sellerPos1 > 0) {
                sellerPos1 = htmlStringFromProductPage.indexOf(PROFILE_BASE_URL,sellerPos1);
                if (sellerPos1>0){
                    sellerPos1 += 35;
                }else {
                    sellerPos1 = htmlStringFromProductPage.indexOf(PROFILE_BASE_URL2,sellerPos1);
                    if (sellerPos1 > 0){
                        sellerPos1 += 34;
                    }
                }
                sellerPos2 = htmlStringFromProductPage.indexOf("\"", sellerPos1);
                if (sellerPos2 > 0) {
                    seller = htmlStringFromProductPage.substring(sellerPos1, sellerPos2);
                }
            }else {
                sellerPos1=htmlStringFromProductPage.indexOf("Vendido por");
                if (sellerPos1>0){
                    sellerPos2=htmlStringFromProductPage.indexOf("</a>",sellerPos1);
                    String sellerStr= htmlStringFromProductPage.substring(sellerPos1,sellerPos2);
                    sellerPos2=sellerStr.lastIndexOf("</span>");
                    sellerStr=sellerStr.substring(0,sellerPos2);
                    sellerPos1=sellerStr.lastIndexOf("<span");
                    sellerPos1=sellerStr.indexOf(">",sellerPos1)+1;
                    seller=sellerStr.substring(sellerPos1,sellerPos2);
                }
            }
        }
        if (seller == null || seller.trim().isEmpty()) {
            String msg = "No se pudo encontrar al vendedor " + productUrl;
            System.out.println(msg);
            Logger.log(msg);
        } else {
            seller = unFormatSeller(seller);
        }
        return seller;

    }


    public static long getSellerId(String htmlStringFromProductPage, String productUrl) {
        int sellerPos1 = 0;
        int sellerPos2 = 0;
        long sellerId=-1;
        String sellerIdStr = null;
        sellerPos1 = htmlStringFromProductPage.indexOf("seller_id");
        if (sellerPos1 > 0) {
            sellerPos1 = htmlStringFromProductPage.indexOf(":", sellerPos1)+1;
            sellerPos2 = htmlStringFromProductPage.indexOf(",", sellerPos1);
            sellerIdStr = htmlStringFromProductPage.substring(sellerPos1, sellerPos2);
        }
        if (sellerIdStr == null || sellerIdStr.trim().isEmpty()) {
            String msg = "No se pudo encontrar el ID del vendedor 1 " + productUrl;
            System.out.println(msg);
            Logger.log(msg);
        } else {
            try {
                sellerId = Long.parseLong(sellerIdStr);
            } catch (NumberFormatException e) {
                sellerId = -1;
                String msg = "No se pudo encontrar el ID del vendedor 2 " + productUrl;
                System.out.println(msg);
                Logger.log(msg);
                Logger.log(e);
            }
        }
        return sellerId;
    }


    public static String getFormatedId(String itemId) {
        String formatedId= itemId.substring(0,3)+"-"+ itemId.substring(3);
        return formatedId;
    }

    public static String getUnformattedId(String itemId) {
        String formatedId= itemId.substring(0,3)+ itemId.substring(4);
        return formatedId;
    }

    public static String unFormatSeller(String seller) {
        try {//decode seller url
            seller = URLDecoder.decode(seller, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Logger.log("something went wrong trying to decode the seller " + seller);
            Logger.log(e);
        }
        return seller;
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
        int lastQuestionPos1 = htmlStringFromProductPage.indexOf("list__question\">");
        if (lastQuestionPos1 > 0) {
            lastQuestionPos1+=16;
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

    //este sirve para la pagina del producto y la lista de productos
    public static int getShipping(String htmlString) {
        int shipping = 0;
        if (htmlString.indexOf(SHIPPING1_LABEL) > 0) {
            shipping = 100;
        } else {
            if (htmlString.indexOf(SHIPPING2_LABEL) > 0 || htmlString.indexOf(SHIPPING2B_LABEL)>0 ||
                    htmlString.indexOf(SHIPPING2C_LABEL)>0) {
                shipping = 101;
            } else {
                if (htmlString.indexOf(SHIPPING3_LABEL) > 0 || htmlString.indexOf(SHIPPING3B_LABEL) > 0) {
                    shipping = 102;
                } else {
                    if (htmlString.indexOf(FREE_SHIPPING1_LABEL) > 0 || htmlString.indexOf(FREE_SHIPPING1B_LABEL) > 0) {
                        shipping = 200;
                    } else {
                        if (htmlString.indexOf(FREE_SHIPPING2_LABEL) > 0) {
                            shipping = 201;
                        } else {
                            if (htmlString.indexOf(FREE_SHIPPING3_LABEL) > 0 || htmlString.indexOf(FREE_SHIPPING3B_LABEL) > 0) {
                                shipping = 202;
                            }
                        }
                    }
                }
            }
        }
        return shipping;
    }

    //este sirve para la pagina del producto y la lista de productos
    public static boolean getPremium(String htmlString) {
        boolean premium = false;
        if (htmlString.indexOf(INTEREST_FREE_PAYMENTS_LABEL) > 0 || htmlString.indexOf(INTEREST_FREE_PAYMENTSB_LABEL) > 0) {
            premium = true;
        }
        return premium;
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


    public static String getProductIdFromHtmldata(String htmlData, String url) {
        String productId = null;
        if (htmlData.indexOf("itemId")>0) {
            int pos1 = htmlData.indexOf("itemId");
            if (pos1 > 0) {
                pos1 = htmlData.indexOf("value=\"", pos1);
                if (pos1 > 0) {
                    pos1 += 7;
                    int pos2 = htmlData.indexOf("\"", pos1);
                    if (pos2 > 0) {
                        productId = htmlData.substring(pos1, pos2);
                    }
                }
            }
        }else {
            if (htmlData.indexOf(HTMLParseUtils.ARTICLE_PREFIX+"-")>0){
                int pos1=htmlData.indexOf(HTMLParseUtils.ARTICLE_PREFIX+"-");
                int pos2 = htmlData.indexOf("-", pos1+4);
                String formattedId=htmlData.substring(pos1,pos2);
                productId=getUnformattedId(formattedId);
            }else { //todo aca no se puede hacer nada
                if (url.indexOf(CATALOG_ITEM_URL_INDICATOR)==-1){//no es item de catalogo, entonces que pasa?
                    String msg="No se pudo recuperar el ID en la URL "+url+" \n\0 "+htmlData;
                    System.out.println(msg);
                    Logger.log(msg);
                }
/*
                int pos1 = htmlData.indexOf(HTMLParseUtils.ARTICLE_PREFIX);
                int pos2 = htmlData.length();
                int pos21 = htmlData.indexOf("?", pos1);
                int pos22 = htmlData.indexOf("/s", pos1);
                int pos23 = htmlData.indexOf("-", pos1 + 4);
                if (pos21 > 0 && pos21 < pos2) {
                    pos2 = pos21;
                }
                if (pos22 > 0 && pos22 < pos2) {
                    pos2 = pos22;
                }
                if (pos23 > 0 && pos23 < pos2) {
                    pos2 = pos23;
                }
                productId = htmlData.substring(pos1, pos2);
                */
            }
        }
        return productId;
    }



    public static int getDiscount(String htmlString, String url) {
        int discount = 0;
        String discountStr = null;
        int pos1 = htmlString.indexOf("Conseguí un");
        int pos2 = 0;
        if (pos1 != -1) {
            pos1 += 12;
            pos2 = htmlString.indexOf("%", pos1);
            if (pos2 == -1) {
                String msg = "Error obteniendo descuento en " + url;
                Logger.log(msg);
            }
            if (pos2 > pos1) {
                discountStr = htmlString.substring(pos1, pos2);
            }
        }
        if (discountStr == null) {
            pos1 = htmlString.indexOf("discount-arrow");
            if (pos1 != -1) {
                pos1 = htmlString.indexOf("<p>", pos1) + 3;
                pos2 = htmlString.indexOf("%", pos1);
                if (pos2 == -1) {
                    String msg = "Error obteniendo descuento en II " + url;
                    Logger.log(msg);
                }
                if (pos2 > pos1) {
                    discountStr = htmlString.substring(pos1, pos2);
                }
            }
        }
        if (discountStr != null) {
            discountStr = discountStr.trim();

            try {
                discount = Integer.parseInt(discountStr);
            } catch (NumberFormatException e) {
                Logger.log(" I couldn't get the discount on " + url);
                Logger.log(e);
            }
        }
        return discount;
    }

    public static double getPrice(String htmlString, String url) {
        if (htmlString.indexOf("Precio a convenir") >0){
            return 0.01; //servicio
        }
        int pricePos1 = htmlString.indexOf("price-tag-fraction\">") + 20;
        int pricePos2 = htmlString.indexOf("<", pricePos1);
        String priceStr = htmlString.substring(pricePos1, pricePos2);
        if (priceStr != null) {
            //sacamos los puntos de miles para que no confunda con decimales
            priceStr = priceStr.replace(".", "");
            priceStr = priceStr.trim();
        }

        String priceDecimalsStr = null;
        int priceDecimalPos1 = htmlString.indexOf("price-tag-cents\">");
        if (priceDecimalPos1 >= 0) { //el tag de decimales puede no estar
            priceDecimalPos1 += 17; //le sumo los caracteres de posicion de "price__decimals">
            int priceDecimalPos2 = htmlString.indexOf("<", priceDecimalPos1);
            priceDecimalsStr = htmlString.substring(priceDecimalPos1, priceDecimalPos2);
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


    public static int getReviews(String htmlString, String url) {
        int reviews = 0;
        String msg = null;

        int pos1 = htmlString.indexOf("review__amount");
        if (pos1 == -1) {
            return 0;//no tiene reviews
        }
        pos1 = htmlString.indexOf(">", pos1);
        if (pos1 == -1) {
            msg = "Cannot find reviews II on " + url;
            Logger.log(msg);
            System.out.println(msg);
        }
        pos1++;
        int pos2 = htmlString.indexOf("opini", pos1);
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

    public static String getTitle(String htmlString, String url) {
        String title = null;
        String msg = null;
        int pos1 = htmlString.indexOf("item-title__primary");
        if (pos1 == -1) {
            pos1 = htmlString.indexOf("ui-pdp-title");
            if (pos1==-1){
                msg = "Cannot find title on " + url;
                Logger.log(msg);
                System.out.println(msg);
                return null;
            }
        }
        pos1 = htmlString.indexOf(">", pos1);
        if (pos1 == -1) {
            msg = "Cannot find title II on " + url;
            Logger.log(msg);
            System.out.println(msg);
        }
        pos1++;
        int pos2 = htmlString.indexOf("<", pos1);
        if (pos2 == -1) {
            msg = "Cannot find title III on " + url;
            Logger.log(msg);
            System.out.println(msg);
        }
        title = htmlString.substring(pos1, pos2);
        title = title.trim();
        return title;
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

    public static int getDiscount2(String productHTMLdata) {
        int discount = 0;
        int discountPos1 = productHTMLdata.indexOf("item__discount"); //procesando descuento opcional
        if (discountPos1 > 0) { //optional field
            discountPos1 += 17;
            int discountPos2 = productHTMLdata.indexOf("%", discountPos1);
            String discountStr = productHTMLdata.substring(discountPos1, discountPos2);
            try {
                discount = Integer.parseInt(discountStr);
            } catch (NumberFormatException e) {
                discount = -1;
                Logger.log(e);
            }
        }
        return discount;
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

    public static boolean isUsed2(String productHTMLdata) {
        boolean isUsed = false;
        if (productHTMLdata.indexOf("Usado") > 0) {
            isUsed = true;
        }
        return isUsed;
    }

    public static int getTotalSold2(String productHTMLdata) {
        int totalSold = 0;
        int soldPos1 = productHTMLdata.indexOf("item__condition\">") + 17;
        if (soldPos1 > 0) {//puede ser que no vendió
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
                                Logger.log(e);
                            }
                        }
                    }
                }
            }
        }
        return totalSold;
    }

    public static int getReviews2(String productHTMLdata) {
        int reviews = 0;
        int reviewsPos1 = productHTMLdata.indexOf("reviews-total\">");
        if (reviewsPos1 >= 0) {// puede ser que no tenga reviews
            reviews = -1;
            reviewsPos1 += 15;
            int reviewsPos2 = productHTMLdata.indexOf("<", reviewsPos1);
            String reviewsStr = productHTMLdata.substring(reviewsPos1, reviewsPos2);
            if (reviewsStr != null) {
                reviewsStr = reviewsStr.trim();
                if (reviewsStr.length() > 0) {
                    try {
                        reviews = Integer.parseInt(reviewsStr);
                    } catch (NumberFormatException e) {
                        Logger.log(e);
                    }
                }
            }
        }
        return reviews;
    }


    public static double getStars2(String productHTMLdata) {
        double stars = -1;
        String allStarsStr = StringUtils.substringBetween(productHTMLdata, "<div class=\"stars\">", "<div class=\"item__reviews-total\">");
        if (allStarsStr != null) {
            stars = allStarsStr.split("star-icon-full").length - 1 * 1.0;
            boolean halfStar = allStarsStr.indexOf("star-icon-half") > 0;
            if (halfStar) {
                stars += 0.5;
            }
        }
        return stars;
    }


    public static void main(String args[]){
        String url = "https://articulo.mercadolibre.com.ar/MLA-722903608-botiquin-bano-shcneider-eco-34x46x14-wengue-bm134-_JM";
        CloseableHttpClient client = HttpUtils.buildHttpClient();
        String productoPage=HttpUtils.getHTMLStringFromPage(url,client,false,false);
        boolean officialStore=getOfficialStore(productoPage);
        long sellerId=getSellerId(productoPage,"pepito");
        String seller = getSeller(productoPage,officialStore,"lalala");

        boolean b=false;


    }

}
