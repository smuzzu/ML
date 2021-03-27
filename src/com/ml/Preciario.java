package com.ml;

import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.TokenUtils;

import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Scanner;


public class Preciario {

    //Alan   400
    //Gio    350
    //Charly 500
    static double COSTO_MOTO =400; //aca va lo que cobra Alan
    static double COSTO_TAXI =500; //aca va lo que cobra Charly
    static double COSTO_ENVOLTORIOS=10.0; //todo estimar bien


    public static Price getPrice(String usuario, String itemID, double costoSinIva) {
        Price item = new Price();

        String msg="";
        if (usuario==null || usuario.isEmpty()){
            msg="getPrice. El usuario no se ingreso. No es posible continuar";
            System.out.println(msg);
            Logger.log(msg);
            return null;
        }
        if (usuario.equals("A")){
            usuario="ACACIAYLENGA";
        } else {
            if (usuario.equals("S")){
                usuario="SOMOS_MAS";
            }
        }
        if (!isValidUser(usuario)){
            msg="getPrice. El usuario '"+usuario+ "' no es valido. No es posible continuar";
            System.out.println(msg);
            Logger.log(msg);
            return null;
        }

        if (!isValidId(itemID)){
            msg="getPrice. El jsonItem no se ingreso. No es posible continuar";
            System.out.println(msg);
            Logger.log(msg);
            return null;
        }

        if (!isValidPrice(costoSinIva)){
            msg="getPrice. El costo sin iva  "+costoSinIva+" no es valido. No es posible continuar";
            System.out.println(msg);
            Logger.log(msg);
            return null;
        }

        if (!itemID.startsWith("MLA") || itemID.length()<11){
            msg="getPrice. El jsonItem "+itemID+" no es valido. No es posible continuar";
            System.out.println(msg);
            Logger.log(msg);
            return null;
        }

        item.usuario=usuario;
        item.itemID=itemID;
        item.costoSinIva=costoSinIva;

        //TODO ingresos brutos?
        //TODO y si el taxi hay que facturarlo?

        System.out.println("Aguarde...");

        CloseableHttpClient client= HttpUtils.buildHttpClient();

        String itemUrl="https://api.mercadolibre.com/items/"+itemID;
        JSONObject jsonItem = HttpUtils.getJsonObjectUsingToken(itemUrl,client,usuario,false);

        if (jsonItem==null){
            System.out.println("No se pudo encontrar el jsonItem: "+itemID+" del usuario "+usuario);
            System.exit(0);
        }

        item.price=0.0; //todo manejar descuentos
        if (jsonItem.has("price") && !jsonItem.isNull("price")) {
            item.price = jsonItem.getDouble("price");
        }


        item.descripcion="N/A";
        if (jsonItem.has("title") && !jsonItem.isNull("title")) {
            item.descripcion = jsonItem.getString("title");
        }

        item.permalink="N/A";
        if (jsonItem.has("permalink") && !jsonItem.isNull("permalink")) {
            item.permalink = jsonItem.getString("permalink");
        }

        String categoryId=null;
        if (jsonItem.has("category_id") && !jsonItem.isNull("category_id")) {
            categoryId = jsonItem.getString("category_id");
        }
        if (categoryId==null){
            System.out.println("No se pudo encontrar la categoria del jsonItem: "+itemID+" del usuario "+usuario);
            System.exit(0);
        }

        String listingTypeId=null;
        if (jsonItem.has("listing_type_id") && !jsonItem.isNull("listing_type_id")) {
            listingTypeId = jsonItem.getString("listing_type_id");
        }
        if (listingTypeId==null){
            System.out.println("No se pudo encontrar el tipo de publicación del jsonItem: "+itemID+" del usuario "+usuario);
            System.exit(0);
        }

        item.saleType="N/A";
        item.saleCost=0.0;
        String saleCostUrl="https://api.mercadolibre.com/sites/MLA/listing_prices?category_id="+categoryId+"&price="+item.price;
        JSONObject saleCostArray = HttpUtils.getJsonObjectUsingToken(saleCostUrl,client,usuario,true);
        if (saleCostArray!=null){
            JSONArray costArray = saleCostArray.getJSONArray("elArray");
            for (int i=0; i<costArray.length(); i++){
                JSONObject costObj=costArray.getJSONObject(i);
                if (costObj!=null && costObj.has("listing_type_id") && !costObj.isNull("listing_type_id")){
                    String listingTypeId2=costObj.getString("listing_type_id");
                    if (listingTypeId2.equals(listingTypeId)){
                        if (costObj.has("sale_fee_amount") && !costObj.isNull("sale_fee_amount")) {
                            item.saleCost = costObj.getDouble("sale_fee_amount");
                        }
                        if (costObj.has("listing_type_name") && !costObj.isNull("listing_type_name")){
                            item.saleType=costObj.getString("listing_type_name");
                        }
                    }
                }
            }
            if (item.saleCost==0.0){
                System.out.println("No se pudo encontrar el costo de la comision para el jsonItem: "+itemID+" del usuario "+usuario);
                System.exit(0);
            }
        }

        item.freeShipping=false;
        item.customShipping=false;
        item.mercadoEnvios=0.0;
        if (jsonItem.has("shipping") && !jsonItem.isNull("shipping")){
            JSONObject shippingObj=jsonItem.getJSONObject("shipping");
            if (shippingObj!=null){
                if (shippingObj.has("free_shipping") && !shippingObj.isNull("free_shipping")) {
                    item.freeShipping = shippingObj.getBoolean("free_shipping");
                }
            }
            if (shippingObj.has("mode") && !shippingObj.isNull("mode")
                    && shippingObj.getString("mode").equals("custom")){
                item.customShipping=true;
            }
        }

        if (item.freeShipping){
            JSONObject shippingOptionsObj = getShippingOptions(usuario, itemID, client,5545 );
            if (shippingOptionsObj!=null && shippingOptionsObj.has("options") && !shippingOptionsObj.isNull("options")){
                JSONArray optionsObj=shippingOptionsObj.getJSONArray("options");
                for (int i=0; i<optionsObj.length(); i++){
                    JSONObject theOptionObj=optionsObj.getJSONObject(i);
                    if (theOptionObj!=null && theOptionObj.has("shipping_option_type") && !theOptionObj.isNull("shipping_option_type")){
                        String shippingOptionType = theOptionObj.getString("shipping_option_type");
                        if (!shippingOptionType.equals("agency")){
                            continue;
                        }
                    }
                    if (theOptionObj!=null && theOptionObj.has("list_cost") && !theOptionObj.isNull("list_cost")){
                        item.mercadoEnvios= theOptionObj.getDouble("list_cost");
                    }
                }
            }
        }

        double customShippingPrice=0.0;
        double diferencialCustomShipping=0.0;
        if (item.customShipping){
            JSONObject shippingOptionsObj = getShippingOptions(usuario, itemID, client,1424);
            if (shippingOptionsObj!=null && shippingOptionsObj.has("options") && !shippingOptionsObj.isNull("options")){
                JSONArray optionsObj=shippingOptionsObj.getJSONArray("options");
                for (int i=0; i<optionsObj.length(); i++) {
                    JSONObject theOptionObj = optionsObj.getJSONObject(i);
                    if (theOptionObj!=null && theOptionObj.has("name") && !theOptionObj.isNull("name")){
                        String shippingName=theOptionObj.getString("name");
                        if (shippingName!=null && shippingName.contains("xpreso")){//expreso
                            continue;
                        }
                    }
                    if (theOptionObj!=null && theOptionObj.has("cost") && !theOptionObj.isNull("cost")){
                        customShippingPrice=theOptionObj.getDouble("cost");
                        diferencialCustomShipping=customShippingPrice- COSTO_TAXI;
                    }
                }
            }
        }

        item.flex=false;
        String flexUrl = "https://api.mercadolibre.com/sites/MLA/shipping/selfservice/items/" + itemID;
        int flexStatusCode = HttpUtils.getStatusCode(flexUrl, client, TokenUtils.getToken(usuario));
        if (flexStatusCode==204){
            item.flex=true;
        }else {
            if (flexStatusCode==403 || flexStatusCode==404){
                item.flex=false;
            }else {
                System.out.println("status code = "+flexStatusCode+" buscando envio flex en el jsonItem "+itemID
                        +"\nPor favor veriicar que el usuario "+usuario+" sea el correcto para este producto"
                        +"\n"+item.permalink);
                System.exit(0);
            }
        }
        //todo controlar otros casos

        //aca calculamos la diferencia entre lo que paga ML y lo que cobra la moto
        //deberia coincidir con lo que dice aca https://www.mercadolibre.com.ar/ayuda/costos-envios-flex_3859
        item.ayudaMotos=0.0;
        item.diferenciaDeMotos=0.0;
        if (item.flex){
            JSONObject shippingOptionsObj = getShippingOptions(usuario, itemID, client, 1424);
            if (shippingOptionsObj!=null && shippingOptionsObj.has("options") && !shippingOptionsObj.isNull("options")){
                JSONArray optionsObj=shippingOptionsObj.getJSONArray("options");
                for (int i=0; i<optionsObj.length(); i++) {
                    JSONObject theOptionObj = optionsObj.getJSONObject(i);
                    if (theOptionObj != null && theOptionObj.has("shipping_method_type") && !theOptionObj.isNull("shipping_method_type")) {
                        String shippongMethodType=theOptionObj.getString("shipping_method_type");
                        if (shippongMethodType.equals("super_express")) {//es el envio rapido en capital
                            item.ayudaMotos = theOptionObj.getDouble("list_cost");
                            item.diferenciaDeMotos=item.ayudaMotos- COSTO_MOTO;
                        }
                    }
                }
            }
        }


        item.ivaCompras=costoSinIva*0.21;
        item.ivaVentas=item.price*0.21;
        item.ivaAfavorML=(item.saleCost+item.mercadoEnvios)*.21;

        item.profitSF=item.price-item.costoSinIva-item.saleCost-item.mercadoEnvios+item.ivaCompras+item.ivaAfavorML-COSTO_ENVOLTORIOS;
        item.profit=item.profitSF-item.ivaVentas;
        item.profitSFMoto=item.price-costoSinIva-item.saleCost+item.diferenciaDeMotos+item.ivaCompras+item.ivaAfavorML-COSTO_ENVOLTORIOS;
        item.profitSFTaxi=item.price-costoSinIva-item.saleCost+diferencialCustomShipping+item.ivaCompras+item.ivaAfavorML-COSTO_ENVOLTORIOS;

        item.profitMoto=item.profitSFMoto-item.ivaVentas;
        item.profitTaxi=item.profitSFTaxi-item.ivaVentas;
        item.marging=item.profit/(costoSinIva+item.ivaCompras)*100;
        item.margingMoto=item.profitMoto/(costoSinIva+item.ivaCompras)*100;
        item.margingTaxi=item.profitTaxi/(costoSinIva+item.ivaCompras)*100;
        item.maringSF=item.profitSF/(costoSinIva+item.ivaCompras)*100;
        item.margingSFMoto=item.profitSFMoto/(costoSinIva+item.ivaCompras)*100;
        item.margingSFTaxi=item.profitSFTaxi/(costoSinIva+item.ivaCompras)*100;

        return item;
    }


    public static void main (String args[]){

        String usuario="";
        String itemId="";
        double costoSinIva=0.0;

        while (true) {

            CloseableHttpClient client = HttpUtils.buildHttpClient();

            String userChoice = null;
            String[] data;
            boolean repeaInput = true;
            String msg = null;
            while (repeaInput) {

                System.out.println("Ingrese la letra del usuario  codigo_de_proucto  costo_sin_iva\nej. A MLA680935542 2560.12");
                try {
                    Scanner in;
                    in = new Scanner(System.in);
                    userChoice = in.nextLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (userChoice == null || userChoice.isEmpty()) {
                    msg = "main. No se ingreso informacion. No es posible continuar";
                    System.out.println(msg);
                    Logger.log(msg);
                    continue;
                }
                userChoice = userChoice.trim();
                data = userChoice.split("\\s+");
                if (data.length != 3) {
                    msg = "main. Cantidad de parametros incorrecta. '" + userChoice.trim() + "' No es posible continuar";
                    System.out.println(msg);
                    Logger.log(msg);
                    continue;
                }

                usuario = data[0];
                itemId = data[1];
                String costoSinIvaStr = data[2];

                if (!isValidUser(usuario)) {
                    msg = "main. El usuario no se ingreso. No es posible continuar";
                    System.out.println(msg);
                    Logger.log(msg);
                    continue;
                }
                if (usuario.equals("A")) {
                    usuario = "ACACIAYLENGA";
                } else {
                    if (usuario.equals("S")) {
                        usuario = "SOMOS_MAS";
                    }
                }

                itemId = itemId.replaceAll("-", "");
                if (!isValidId(itemId)) {
                    msg = "main. El usuario no se ingreso. No es posible continuar";
                    System.out.println(msg);
                    Logger.log(msg);
                    continue;
                }

                costoSinIvaStr = costoSinIvaStr.replaceAll(",", ".");
                if (!isValidPrice(costoSinIvaStr)) {
                    msg = "main. El usuario no se ingreso. No es posible continuar";
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

            price.print();


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
        String shippingOptionsUrl = "https://api.mercadolibre.com/items/" + itemID + "/shipping_options?zip_code="+zipCode+"&quantity=1";
        return HttpUtils.getJsonObjectUsingToken(shippingOptionsUrl, client, usuario, false);
    }


    public static boolean isValidUser(String usuario){
        if (usuario==null || usuario.isEmpty()){
            return false;
        }
        if (usuario.equals("ACACIAYLENGA") || usuario.equals("SOMOS_MAS")){
            return true;
        }
        if (usuario.equals("A") || usuario.equals("S")){
            return true;
        }
        return false;
    }

    public static boolean isValidId(String id) {
        if (id==null || id.length()<11){
            return false;
        }
        if (!id.startsWith("MLA")){
            return false;
        }
        String str=id.substring(3);
        try {
            Long.parseLong(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public static boolean isValidPrice(String price) {
        try {
            Double.parseDouble(price);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public static boolean isValidPrice(double price) {
       if (price>0.0){
           return true;
       }else {
           return false;
       }
    }


    private static class Price {
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

        double profit;
        double marging;
        double profitSF;
        double maringSF;
        double profitSFMoto;
        double margingSFMoto;
        double profitSFTaxi;
        double margingSFTaxi;

        public void print(){

            DecimalFormat decimalFormat = new DecimalFormat("#####.00");

            System.out.println(descripcion);
            System.out.println(permalink);
            System.out.println("Precio: "+price);
            System.out.println("Costo sin Iva: "+costoSinIva);
            System.out.println("Comision: "+saleCost+" / "+saleType);
            if (freeShipping) {
                System.out.println("Mercadoenvios: " + mercadoEnvios);
            }
            if (flex) {
                System.out.println("Ayuda moto: " + ayudaMotos);
                System.out.println("Diff moto: " + diferenciaDeMotos);
            }
            if (customShipping){
                System.out.println("Env. taxi: " + customShippingPrice);
                System.out.println("Diff taxi: " + diferencialCustomShipping);

            }
            System.out.println("Iva Compras: "+decimalFormat.format(ivaCompras));
            System.out.println("Iva Ventas: "+decimalFormat.format(ivaVentas));
            System.out.println("Iva de ML: "+decimalFormat.format(ivaAfavorML));
            System.out.println("Envoltorios: "+costoEvoltorios);

            System.out.println("\nGanancia              "+decimalFormat.format(profit)+"  "+decimalFormat.format(marging)+" %");
            if (flex) {
                System.out.println("Ganancia con Moto     " + decimalFormat.format(profitMoto) + "  " + decimalFormat.format(margingMoto) + " %");
            }
            if (customShipping) {
                System.out.println("Ganancia con Taxi     " + decimalFormat.format(profitTaxi) + "  " + decimalFormat.format(margingTaxi) + " %");
            }
            System.out.println("\nGananciaSF            " + decimalFormat.format(profitSF) + "  " + decimalFormat.format(maringSF) + " %");
            if (flex) {
                System.out.println("GananciaSF con Moto   " + decimalFormat.format(profitSFMoto) + "  " + decimalFormat.format(margingSFMoto) + " %");
            }
            if (customShipping) {
                System.out.println("GananciaSF con Taxi   " + decimalFormat.format(profitSFTaxi) + "  " + decimalFormat.format(margingSFTaxi) + " %");
            }

            System.out.println("\n\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        }
    }

}
