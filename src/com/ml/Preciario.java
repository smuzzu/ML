package com.ml;

import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.SData;
import com.ml.utils.TokenUtils;

import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.stream.Stream;


public class Preciario {

    static int version = 3;

    //Alan   400
    //Gio    350
    //Nico   250
    //Charly 500
    static Double COSTO_MOTO = null;
    static Double COSTO_TAXI = null;
    static Double COSTO_ENVOLTORIOS = null;

    static Double PRECIO_DE_CORTE = 4000.0;

    static long[] idsArray = new long[]
            {831749248,
                    837410777,
                    837424807,
                    867228623,
                    867232491,
                    831770478,
                    831769691,
                    867287419,
                    867289006,
                    831772965,
                    841308624,
                    834666219,
                    901398938,
                    832013469,
                    832020699,
                    841208700,
                    841207611,
                    838284741,
                    838289546,
                    873079832,
                    841318827,
                    841315351,
                    855898686,
                    855899577,
                    855893649,
                    860363717,
                    860218651,
                    860231657,
                    860249763,
                    885525697,
                    885527878,
                    885532825,
                    868847943,
                    868847163,
                    903382441,
                    907462814,
                    870370980,
                    870406366,
                    870410295,
                    870837240,
                    870839059,
                    870837504,
                    872468125,
                    873273498,
                    872467559,
                    880932563,
                    880933397,
                    881643376,
                    881926537,
                    881711642,
                    881728606,
                    881730594,
                    906171952,
                    886329192,
                    898091166,
                    898091400,
                    883153701,
                    883155256,
                    883250723
            };
    static double[] costArray = new double[]
            {
                    908.19,
                    1210.92,
                    1513.65,
                    302.73,
                    302.73,
                    1308.89,
                    1308.89,
                    436.30,
                    4.36,
                    1553.40,
                    1553.40,
                    1924.63,
                    1924.63,
                    2883.96,
                    2883.96,
                    232.91,
                    232.91,
                    11587.36,
                    11587.36,
                    11587.36,
                    1686.33,
                    1686.33,
                    16376.37,
                    16376.37,
                    16376.37,
                    2634.06,
                    3512.08,
                    4390.10,
                    878.02,
                    255.35,
                    255.35,
                    255.35,
                    293.61,
                    293.61,
                    293.61,
                    293.61,
                    5424.11,
                    5424.11,
                    5424.11,
                    3486.12,
                    3486.12,
                    3486.12,
                    3092.28,
                    3092.28,
                    3092.28,
                    5805.80,
                    5805.80,
                    5805.80,
                    80.69,
                    645.50,
                    1291.01,
                    1936.51,
                    216.00,
                    205.19,
                    863.98,
                    820.76,
                    33166.38,
                    33166.38,
                    33166.38            };

    private static void checkHafele() {
        if (costArray.length != idsArray.length) {
            System.out.println("no coinciden los arrays");
            System.exit(0);
        }
        for (int i = 0; i < idsArray.length; i++) {
            long id = idsArray[i];
            double costBeforeTaxes = costArray[i];
            Price priceObj = getPrice(SData.getSomosMas(), "MLA" + id, costBeforeTaxes);
            if (priceObj == null) {
                System.out.println("priceObj is null " + id);
                System.exit(0);
            }
            priceObj.print(false);
            boolean b = false;
        }
    }

    public static String getPrice2(String usuario, String itemID, double costoSinIva, boolean showDetails) {
        String result = "";
        Price price = getPrice(usuario, itemID, costoSinIva);
        if (price != null) {
            result = price.print(showDetails);
        }
        return result;
    }

    private static void loadCostsFromFile(){
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get("data.dat"), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e) {
            String msg="No se pudo leer el data.dat No es posible continuar";
            Logger.log(msg);
            System.out.println(msg);
            Logger.log(e);
            e.printStackTrace();
        }
        String costs = contentBuilder.toString();
        String [] cost=costs.split("\n");
        if (cost==null || cost.length!=4){
            String msg="No se pudo leer correctamente el archivo de costos. ";
            Logger.log(msg);
            System.out.println(msg);
        }
        for (int i=0; i<cost.length; i++){
            String costStr=cost[i];
            costStr=costStr.substring(0,costStr.indexOf("/"));
            cost[i]=costStr.trim();
        }
        try {
            COSTO_MOTO=Double.parseDouble(cost[0]);
            COSTO_TAXI=Double.parseDouble(cost[1]);
            COSTO_ENVOLTORIOS=Double.parseDouble(cost[2]);
            PRECIO_DE_CORTE=Double.parseDouble(cost[3]);
        }catch (Exception e){
            String msg="No se pudo convertir a numero la inforamcion en el archivo de costos. ";
            Logger.log(msg);
            System.out.println(msg);
            Logger.log(e);
            e.printStackTrace();
        }
    }


    protected static Price getPrice(String usuario, String itemID, double costoSinIva) {

        if (COSTO_MOTO==null || COSTO_TAXI==null || COSTO_ENVOLTORIOS==null){
            loadCostsFromFile();
            if (COSTO_MOTO==null || COSTO_TAXI==null || COSTO_ENVOLTORIOS==null){
                String msg="No se saben los costos. No es posible continuar";
                Logger.log(msg);
                System.out.println(msg);
                return null;
            }
        }

        Price item = new Price();

        String msg = "";
        if (usuario == null || usuario.isEmpty()) {
            msg = "getPrice. El usuario no se ingreso. No es posible continuar";
            System.out.println(msg);
            Logger.log(msg);
            return null;
        }
        if (usuario.equals("A")) {
            usuario = SData.getAcaciaYLenga();
        } else {
            if (usuario.equals("S")) {
                usuario = SData.getSomosMas();
            }
        }
        if (!isValidUser(usuario)) {
            msg = "getPrice. El usuario '" + usuario + "' no es valido. No es posible continuar";
            System.out.println(msg);
            Logger.log(msg);
            return null;
        }

        if (!isValidId(itemID)) {
            msg = "getPrice. El item no se ingreso. No es posible continuar";
            System.out.println(msg);
            Logger.log(msg);
            return null;
        }
        itemID = itemID.trim();
        if (!itemID.startsWith("MLA")) {
            itemID = "MLA" + itemID;
        }

        if (!isValidPrice(costoSinIva)) {
            msg = "getPrice. El costo sin iva  " + costoSinIva + " no es valido. No es posible continuar";
            System.out.println(msg);
            Logger.log(msg);
            return null;
        }

        if (!itemID.startsWith("MLA") || itemID.length() < 11) {
            msg = "getPrice. El jsonItem " + itemID + " no es valido. No es posible continuar";
            System.out.println(msg);
            Logger.log(msg);
            return null;
        }

        item.usuario = usuario;
        item.itemID = itemID;
        item.costoSinIva = costoSinIva;

        //TODO y si el taxi hay que facturarlo?

        System.out.println("Aguarde...  Wait....");

        CloseableHttpClient client = HttpUtils.buildHttpClient();

        String itemUrl = "https://api.mercadolibre.com/items/" + itemID;
        JSONObject jsonItem = HttpUtils.getJsonObjectUsingToken(itemUrl, client, usuario, false);

        if (jsonItem == null) {
            System.out.println("No se pudo encontrar el jsonItem: " + itemID + " del usuario " + usuario);
            return null;
        }

        item.price = 0.0; //todo manejar descuentos
        if (jsonItem.has("price") && !jsonItem.isNull("price")) {
            item.price = jsonItem.getDouble("price");
        }

        //ACACIA: comercio electronico 3% (se deduce) - iibb minimo 1.5%
        if (usuario.equals(SData.getAcaciaYLenga())){
            item.percepcionIibbComercioElectronico=0.0;
            item.retencionIibbMinimoGeneral=item.price*0.015;
        }else {//SOMOS: comercio electronico 4% - iibb minimo 3%
            item.percepcionIibbComercioElectronico=item.price*0.04;
            item.retencionIibbMinimoGeneral=item.price*0.03;
        }

        item.descripcion = "N/A";
        if (jsonItem.has("title") && !jsonItem.isNull("title")) {
            item.descripcion = jsonItem.getString("title");
        }

        item.permalink = "N/A";
        if (jsonItem.has("permalink") && !jsonItem.isNull("permalink")) {
            item.permalink = jsonItem.getString("permalink");
        }

        String categoryId = null;
        if (jsonItem.has("category_id") && !jsonItem.isNull("category_id")) {
            categoryId = jsonItem.getString("category_id");
        }
        if (categoryId == null) {
            System.out.println("No se pudo encontrar la categoria del item: " + itemID + " del usuario " + usuario);
            return null;
        }

        String listingTypeId = null;
        if (jsonItem.has("listing_type_id") && !jsonItem.isNull("listing_type_id")) {
            listingTypeId = jsonItem.getString("listing_type_id");
        }
        if (listingTypeId == null) {
            System.out.println("No se pudo encontrar el tipo de publicación del item: " + itemID + " del usuario " + usuario);
            return null;
        }

        item.costoEvoltorios = COSTO_ENVOLTORIOS;
        item.saleType = "N/A";
        item.saleCost = 0.0;
        String saleCostUrl = "https://api.mercadolibre.com/sites/MLA/listing_prices?category_id=" + categoryId + "&price=" + item.price;
        JSONObject saleCostArray = HttpUtils.getJsonObjectUsingToken(saleCostUrl, client, usuario, true);
        if (saleCostArray != null) {
            JSONArray costArray = saleCostArray.getJSONArray("elArray");
            for (int i = 0; i < costArray.length(); i++) {
                JSONObject costObj = costArray.getJSONObject(i);
                if (costObj != null && costObj.has("listing_type_id") && !costObj.isNull("listing_type_id")) {
                    String listingTypeId2 = costObj.getString("listing_type_id");
                    if (listingTypeId2.equals(listingTypeId)) {
                        if (costObj.has("sale_fee_amount") && !costObj.isNull("sale_fee_amount")) {
                            item.saleCost = costObj.getDouble("sale_fee_amount");
                        }
                        if (costObj.has("listing_type_name") && !costObj.isNull("listing_type_name")) {
                            item.saleType = costObj.getString("listing_type_name");
                            if (item.saleType.contains("sica")) { //clásica o clasica
                                item.saleType += "/standard";
                            }
                        }
                    }
                }
            }
            if (item.saleCost == 0.0) {
                System.out.println("No se pudo encontrar el costo de la comision para el item: " + itemID + " del usuario " + usuario);
                return null;
            }
        }

        item.freeShipping = false;
        item.customShipping = false;
        item.mercadoEnvios = 0.0;
        if (jsonItem.has("shipping") && !jsonItem.isNull("shipping")) {
            JSONObject shippingObj = jsonItem.getJSONObject("shipping");
            if (shippingObj != null) {
                if (shippingObj.has("free_shipping") && !shippingObj.isNull("free_shipping")) {
                    item.freeShipping = shippingObj.getBoolean("free_shipping");
                }
            }
            if (shippingObj.has("mode") && !shippingObj.isNull("mode")
                    && shippingObj.getString("mode").equals("custom")) {
                item.customShipping = true;
            }
        }

        if (item.freeShipping) {
            item.mercadoEnvios=getMEShippingCostForZipCode(5545,"agency","list_cost",usuario, itemID, client);
        }

        if (item.customShipping) {
            JSONObject shippingOptionsObj = getShippingOptions(usuario, itemID, client, 1424);
            if (shippingOptionsObj != null && shippingOptionsObj.has("options") && !shippingOptionsObj.isNull("options")) {
                JSONArray optionsObj = shippingOptionsObj.getJSONArray("options");
                for (int i = 0; i < optionsObj.length(); i++) {
                    JSONObject theOptionObj = optionsObj.getJSONObject(i);
                    if (theOptionObj != null && theOptionObj.has("name") && !theOptionObj.isNull("name")) {
                        String shippingName = theOptionObj.getString("name");
                        if (shippingName != null && shippingName.contains("xpreso")) {//expreso
                            continue;
                        }
                    }
                    if (theOptionObj != null && theOptionObj.has("cost") && !theOptionObj.isNull("cost")) {
                        item.customShippingPrice = theOptionObj.getDouble("cost");
                        item.diferencialCustomShipping = item.customShippingPrice - COSTO_TAXI;
                    }
                }
            }
        }

        item.flex = false;
        String flexUrl = "https://api.mercadolibre.com/sites/MLA/shipping/selfservice/items/" + itemID;
        int flexStatusCode = HttpUtils.getStatusCode(flexUrl, client, TokenUtils.getToken(usuario));
        if (flexStatusCode == 204) {
            item.flex = true;
        } else {
            if (flexStatusCode == 403 || flexStatusCode == 404) {
                item.flex = false;
            } else {
                System.out.println("status code = " + flexStatusCode + " buscando envio flex en el jsonItem " + itemID
                        + "\nPor favor veriicar que el usuario " + usuario + " sea el correcto para este producto"
                        + "\n" + item.permalink);
                return null;
            }
        }
        //todo controlar otros casos

        //aca calculamos la diferencia entre lo que paga ML y lo que cobra la moto
        //deberia coincidir con lo que dice aca https://www.mercadolibre.com.ar/ayuda/costos-envios-flex_3859
        item.ayudaMotos = 0.0;
        item.diferenciaDeMotos = 0.0;
        if (item.flex) {
            item.ayudaMotos=getMEShippingCostForZipCode(1424,"address","base_cost",usuario,itemID,client);
            if (item.price >= PRECIO_DE_CORTE) {
                item.ayudaMotos = Math.ceil(item.ayudaMotos * 0.2);
            }
            item.diferenciaDeMotos = item.ayudaMotos - COSTO_MOTO;
        }


        item.ivaCompras = costoSinIva * 0.21;
        item.ivaVentas = item.price * 0.21;
        if (usuario.equals(SData.getAcaciaYLenga())) {
            item.ivaAfavorML = (item.saleCost + item.mercadoEnvios) * .21;
        }

        item.profitSF = item.price - item.costoSinIva - item.saleCost - item.mercadoEnvios + item.ivaCompras + item.ivaAfavorML
                - item.costoEvoltorios - item.percepcionIibbComercioElectronico - item.retencionIibbMinimoGeneral;
        item.profit = item.profitSF - item.ivaVentas;
        item.profitSFMoto = item.price - costoSinIva - item.saleCost + item.diferenciaDeMotos + item.ivaCompras + item.ivaAfavorML
                - item.costoEvoltorios - item.percepcionIibbComercioElectronico - item.retencionIibbMinimoGeneral;
        item.profitSFTaxi = item.price - item.costoSinIva - item.saleCost + item.diferencialCustomShipping + item.ivaCompras + item.ivaAfavorML
                - item.costoEvoltorios - item.percepcionIibbComercioElectronico - item.retencionIibbMinimoGeneral;
        item.profitMoto = item.profitSFMoto - item.ivaVentas;
        item.profitTaxi = item.profitSFTaxi - item.ivaVentas;

        item.marging = item.profit / (costoSinIva + item.ivaCompras) * 100;
        item.margingMoto = item.profitMoto / (costoSinIva + item.ivaCompras) * 100;
        item.margingTaxi = item.profitTaxi / (costoSinIva + item.ivaCompras) * 100;
        item.maringSF = item.profitSF / (costoSinIva + item.ivaCompras) * 100;
        item.margingSFMoto = item.profitSFMoto / (costoSinIva + item.ivaCompras) * 100;
        item.margingSFTaxi = item.profitSFTaxi / (costoSinIva + item.ivaCompras) * 100;

        item.profitWorstCase = item.profit;
        item.margingWorkstCase = item.marging;
        if (item.flex) {
            if (item.profitMoto < item.profitWorstCase) {
                item.profitWorstCase = item.profitMoto;
                item.margingWorkstCase = item.margingMoto;
            }
        }
        if (item.customShipping) {
            if (item.profitTaxi < item.profitWorstCase) {
                item.profitWorstCase = item.profitTaxi;
                item.margingWorkstCase = item.margingTaxi;
            }
        }


        return item;
    }

    private static double getMEShippingCostForZipCode(int zipCode, String shippingOptionType, String costType, String usuario, String itemID, CloseableHttpClient client) {
        double MEShippingCost=0.0;
        JSONObject shippingOptionsObj = getShippingOptions(usuario, itemID, client, zipCode);
        if (shippingOptionsObj != null && shippingOptionsObj.has("options") && !shippingOptionsObj.isNull("options")) {
            JSONArray optionsObj = shippingOptionsObj.getJSONArray("options");
            for (int i = 0; i < optionsObj.length(); i++) {
                JSONObject theOptionObj = optionsObj.getJSONObject(i);
                if (theOptionObj != null && theOptionObj.has("shipping_option_type") && !theOptionObj.isNull("shipping_option_type")) {
                    String shippingOptionTypeStr = theOptionObj.getString("shipping_option_type");
                    if (!shippingOptionTypeStr.equals(shippingOptionType)) {
                        continue;
                    }
                }
                if (theOptionObj != null && theOptionObj.has(costType) && !theOptionObj.isNull(costType)) {
                    MEShippingCost = theOptionObj.getDouble(costType);
                    break;
                }
            }
        }
        return MEShippingCost;
    }


    public static void main(String args[]) {


        String usuario = "";
        String itemId = "";
        double costoSinIva = 0.0;

        //checkHafele();

        while (true) {

            CloseableHttpClient client = HttpUtils.buildHttpClient();

            String userChoice = null;
            String[] data;
            boolean repeaInput = true;
            String msg = null;
            while (repeaInput) {

                System.out.println("Ingrese inicial_de_usuario  numero_de_publicacion  costo_sin_iva");
                System.out.println("Insert user_first_initial publication_number cost_before_taxes");
                System.out.println("ej./ie.: S 831772965 1479.44");
                try {
                    Scanner in;
                    in = new Scanner(System.in);
                    userChoice = in.nextLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (userChoice == null || userChoice.isEmpty()) {
                    msg = "No se ingreso informacion. No es posible continuar\nThere is no info. We cannot process your request";
                    System.out.println(msg);
                    Logger.log(msg);
                    continue;
                }
                userChoice = userChoice.trim();
                data = userChoice.split("\\s+");
                if (data.length != 3) {
                    msg = "Cantidad de parametros incorrecta/Wrong parameters quantity '" + userChoice.trim() + "' No es posible continuar/We cannot proceed";
                    System.out.println(msg);
                    Logger.log(msg);
                    continue;
                }

                usuario = data[0];
                itemId = data[1];
                String costoSinIvaStr = data[2];

                if (!isValidUser(usuario)) {
                    msg = "El usuario no es valido. No es posible continuar/User is not valid. We cannot proceed";
                    System.out.println(msg);
                    Logger.log(msg);
                    continue;
                }
                if (usuario.equals("A")) {
                    usuario = SData.getAcaciaYLenga();
                } else {
                    if (usuario.equals("S")) {
                        usuario = SData.getSomosMas();
                    }
                }

                itemId = itemId.replaceAll("-", "");
                if (!itemId.startsWith("MLA")) {
                    itemId = "MLA" + itemId;
                }
                if (!isValidId(itemId)) {
                    msg = "El numero de publicacion no es valido/Invalid publication number " + itemId + " No es posible continuar/We cannot proceed";
                    System.out.println(msg);
                    Logger.log(msg);
                    continue;
                }

                costoSinIvaStr = costoSinIvaStr.replaceAll(",", ".");
                if (!isValidPrice(costoSinIvaStr)) {
                    msg = "El precio sin iva no es valido/Invalid price before taxes " + costoSinIvaStr + " No es posible continuar/We cannot proceed";
                    System.out.println(msg);
                    Logger.log(msg);
                    continue;
                }

                try {
                    costoSinIva = Double.parseDouble(costoSinIvaStr);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Logger.log(e);
                }
                repeaInput = false;
            }


            //note:  no necesita token pero se lo dejamos
            String productUrl = "https://api.mercadolibre.com/items/" + itemId;
            JSONObject itemObj = HttpUtils.getJsonObjectUsingToken(productUrl, client, usuario, false);

            boolean active = false;
            if (itemObj != null && itemObj.has("status") && !itemObj.isNull("status")) {
                String status = itemObj.getString("status");
                if (status.equals("active")) {
                    active = true;
                }
            }

/*
        if (active){
            HttpUtils.updatePublication(itemId,client,usuario,"paused",null);
        }else {
            HttpUtils.updatePublication(itemId,client,usuario,"active",null);
        }
*/
            //HttpUtils.updatePublication(itemId,client,usuario,"paused",null);

            //HttpUtils.updatePublication(itemId,client,usuario,"active",null);


            Price price = getPrice(usuario, itemId, costoSinIva);

            if (price != null) {
                price.print(true);
            }


        /*
        CloseableHttpClient client= HttpUtils.buildHttpClient();

        //esto es otra cosa
        String url = "https://api.mercadolibre.com/questions/search?seller_id="+TokenUtils.getIdCliente(usuario);
        JSONObject jsonArray = HttpUtils.getJsonObjectUsingToken(url,client,usuario,true);
        long questionId=6766690180L;
        String url2 = "https://api.mercadolibre.com/questions/"+questionId;
        JSONObject questionObj = HttpUtils.getJsonObjectUsingToken(url2,client,usuario,false);

        //prueba de firstName
            String url3="https://api.mercadolibre.com/users/92882311";
        JSONObject object3 = HttpUtils.getJsonObjectUsingToken(url3,client,usuario,false);
        */
        }
    }

    private static JSONObject getShippingOptions(String usuario, String itemID, CloseableHttpClient client, int zipCode) {
        String shippingOptionsUrl = "https://api.mercadolibre.com/items/" + itemID + "/shipping_options?zip_code=" + zipCode + "&quantity=1";
        return HttpUtils.getJsonObjectUsingToken(shippingOptionsUrl, client, usuario, false);
    }


    public static boolean isValidUser(String usuario) {
        if (usuario == null || usuario.isEmpty()) {
            return false;
        }
        if (usuario.equals(SData.getAcaciaYLenga()) || usuario.equals(SData.getSomosMas())) {
            return true;
        }
        if (usuario.equals("A") || usuario.equals("S")) {
            return true;
        }
        return false;
    }

    public static boolean isValidId(String id) {
        if (id == null) {
            return false;
        }
        id = id.trim();
        if (!id.startsWith("MLA")) {
            id = "MLA" + id;
        }
        if (id.length() < 11) {
            return false;
        }
        String str = id.substring(3);
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPrice(String price) {
        try {
            Double.parseDouble(price);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPrice(double price) {
        if (price > 0.0) {
            return true;
        } else {
            return false;
        }
    }


    public static class Price {
        String usuario;
        String itemID;
        double costoSinIva;

        boolean freeShipping;
        boolean flex;
        boolean customShipping;

        String descripcion;
        String permalink;
        double price;
        double saleCost;
        String saleType;
        double mercadoEnvios;

        double ayudaMotos;
        double diferenciaDeMotos;

        double customShippingPrice;
        double diferencialCustomShipping;

        double ivaCompras;
        double ivaVentas;
        double ivaAfavorML;
        double costoEvoltorios;
        double profitMoto;
        double margingMoto;
        double profitTaxi;
        double margingTaxi;
        double percepcionIibbComercioElectronico; //acacia 3% - somos 4%
        double retencionIibbMinimoGeneral; //acacia 1.5% - somos 3%

        double profit;
        double marging;
        double profitSF;
        double maringSF;
        double profitSFMoto;
        double margingSFMoto;
        double profitSFTaxi;
        double margingSFTaxi;

        public double profitWorstCase;
        public double margingWorkstCase;

        public String print(boolean showDetails) {

            String result="";
            DecimalFormat decimalFormat = new DecimalFormat("#####.00");

            System.out.println(descripcion);
            result+=descripcion+"<br/>";
            System.out.println(permalink);
            result+="<a ref=\""+permalink+"\">"+permalink+"</a><br/>";
            String priceStr="Precio/Price: " + price;
            System.out.println(priceStr);
            result+=priceStr+"<br/>";
            String costBeforeTaxesStr="Costo sin Iva/Cost before taxes: " + costoSinIva;
            System.out.println(costBeforeTaxesStr);
            result+=costBeforeTaxesStr+"<br/>";
            String feeStr="Comision/Fee: " + saleCost + " / " + saleType;
            System.out.println(feeStr);
            result+=feeStr+"<br/>";
            if (freeShipping) {
                String shippingStr="Mercadoenvios/Shipping: " + mercadoEnvios;
                System.out.println(shippingStr);
                result+=shippingStr+"<br/>";
            }
            if (flex) {
                String ayudaMotoStr="Ayuda moto/Help for delivery: " + ayudaMotos;
                System.out.println(ayudaMotoStr);
                result+=ayudaMotoStr+"<br/>";
                if (showDetails) {
                    String diffMotoStr="Diff moto: " + diferenciaDeMotos;
                    System.out.println(diffMotoStr);
                    result+=diffMotoStr+"<br/>";
                }
            }
            if (customShipping) {
                String customShippingStr="Env. taxi/Custom shipping: " + customShippingPrice;
                System.out.println(customShippingStr);
                result+=customShippingStr+"<br/>";
                if (showDetails) {
                    String diffTaxiStr="Diff taxi: " + diferencialCustomShipping;
                    System.out.println(diffTaxiStr);
                    result+=diffTaxiStr+"<br/>";
                }
            }
            if (showDetails) {
                String ivaComprasStr="Iva Compras: " + decimalFormat.format(ivaCompras);
                System.out.println(ivaComprasStr);
                result+=ivaComprasStr+"<br/>";
                String ivaVentasStr="Iva Ventas: " + decimalFormat.format(ivaVentas);
                System.out.println(ivaVentasStr);
                result+=ivaVentasStr+"<br/>";
                if (usuario.equals(SData.getAcaciaYLenga())) {
                    String ivaDeMl = "Iva de ML: " + decimalFormat.format(ivaAfavorML);
                    System.out.println(ivaDeMl);
                    result += ivaDeMl + "<br/>";
                }

                String iiBB1Str="Percepcion IIBB com. elect. : " + this.percepcionIibbComercioElectronico;
                System.out.println(iiBB1Str);
                result+=iiBB1Str+"<br/>";
                String iiBB2Str="Retencion IIBB minima : " + this.retencionIibbMinimoGeneral;
                System.out.println(iiBB2Str);
                result+=iiBB2Str+"<br/>";

                String envoltoriosStr="Envoltorios: " + costoEvoltorios;
                System.out.println(envoltoriosStr);
                result+=envoltoriosStr+"<br/>";
            }

            if (showDetails) {
                String gananciaRetiraStr="\nGanancia retira        " + decimalFormat.format(profit) + "  " + decimalFormat.format(marging) + " %";
                System.out.println(gananciaRetiraStr);
                result+=gananciaRetiraStr+"<br/>";
                if (flex) {
                    String gananciaConMotoStr=("Ganancia con Moto     " + decimalFormat.format(profitMoto) + "  " + decimalFormat.format(margingMoto) + " %");
                    System.out.println(gananciaConMotoStr);
                    result+=gananciaConMotoStr+"<br/>";
                }
                if (customShipping) {
                    String customShippingStr="Ganancia con Taxi     " + decimalFormat.format(profitTaxi) + "  " + decimalFormat.format(margingTaxi) + " %";
                    System.out.println(customShippingStr);
                    result+=customShippingStr+"<br/>";
                }
                String gananciaSFStr="\nGananciaSF            " + decimalFormat.format(profitSF) + "  " + decimalFormat.format(maringSF) + " %";
                System.out.println(gananciaSFStr);
                result+=gananciaSFStr+"<br/>";
                if (flex) {
                    String gananciaSFConMoto="GananciaSF con Moto   " + decimalFormat.format(profitSFMoto) + "  " + decimalFormat.format(margingSFMoto) + " %";
                    System.out.println(gananciaSFConMoto);
                    result+=gananciaSFConMoto+"<br/>";
                }
                if (customShipping) {
                    String gananciaSFconTaxiStr="GananciaSF con Taxi   " + decimalFormat.format(profitSFTaxi) + "  " + decimalFormat.format(margingSFTaxi) + " %";
                    System.out.println(gananciaSFconTaxiStr);
                    result+=gananciaSFconTaxiStr+"<br/>";
                }
            }

            String profitStr="\nGanancia/Profit        " + decimalFormat.format(profitWorstCase) + "  " + decimalFormat.format(margingWorkstCase) + " %";
            System.out.println(profitStr);
            result+=profitStr+"<br/>";
            String version="Ver. "+getVersion();
            System.out.println(version);
            result+=version+"<br/>";

            System.out.println("\n\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

            return result;
        }
    }

    static public int getVersion(){
        return version;
    }

}
