package com.ml.utils;

import com.ml.ReportRunner;
import org.json.JSONArray;
import org.json.JSONObject;



public class TesApi {

    private static String ACACIA=SData.getAcaciaYLenga();
    private static String SOMOS=SData.getSomosMas();
    private static String QUEFRESQUETE=SData.getQuefresquete();
    private static String sampleCustomerUrl="https://api.mercadolibre.com/sites/MLA/search?nickname=modular+drawers";
    private static String baseProductUrl="https://api.mercadolibre.com/items/";
    private static String baseOrderUrl="https://api.mercadolibre.com/orders/";
    private static String baseShippingUrl="https://api.mercadolibre.com/shipments/";
    private static String mercadopagoPayment="https://api.mercadopago.com/v1/payments/search?sort=date_created&criteria=desc&external_reference=";
    private static String mercadopagoPayment2="https://api.mercadopago.com/v1/payments/";
    private static String mercadopagoOrder="https://api.mercadopago.com/merchant_orders/search?payer_id=";
    private static String mercadopagoOrder2="https://api.mercadopago.com/merchant_orders/";

    private static String mercadopagoCustomer="https://api.mercadopago.com/v1/customers/";


    private static String getCategory(String productId,String apiUser){
        String result="N/A";
        String productUrl=baseProductUrl+productId;
        JSONObject  itemObject = HttpUtils.getJsonObjectUsingToken(productUrl,HttpUtils.buildHttpClient(),apiUser,false);
        if (itemObject!=null && itemObject.has("category_id") && !itemObject.isNull("category_id")){
            result=itemObject.getString("category_id");
        }
        System.out.println(result);
        return result;
    }


    private static JSONObject getShipping(String orderId,String apiUser){
        JSONObject orderObject = getOrder(orderId,QUEFRESQUETE);
        JSONObject shippingObject = orderObject.getJSONObject("shipping");
        String shippingId=""+shippingObject.getLong("id");
        String shippingtUrl=baseShippingUrl+shippingId;
        shippingObject = HttpUtils.getJsonObjectUsingToken(shippingtUrl,HttpUtils.buildHttpClient(),apiUser,false);
        return shippingObject;
    }

    private static JSONObject getOrder(String orderId,String apiUser){
        String result="N/A";
        String productUrl=baseOrderUrl+orderId;
        JSONObject  orderObject = HttpUtils.getJsonObjectUsingToken(productUrl,HttpUtils.buildHttpClient(),apiUser,false);
        return orderObject;
    }


    public static void main(String[] args){


        String scrollIdURL="https://api.mercadolibre.com/users/"+SData.getIdClienteSomosMas()+"/items/search?search_type=scan";
        JSONObject scrollIdObject = HttpUtils.getJsonObjectUsingToken(scrollIdURL,HttpUtils.buildHttpClient(),SData.getSomosMas(),false);
        String url="";
        if (scrollIdObject.has("scroll_id") && !scrollIdObject.isNull("scroll_id")) {
            String scrollId=scrollIdObject.getString("scroll_id");
            url = "https://api.mercadolibre.com/sites/MLA/search?category=MLA411938&scroll_id="+scrollId;
            JSONObject jsonArray = HttpUtils.getJsonObjectUsingToken(url, HttpUtils.buildHttpClient(), SData.getSomosMas(), true);
            boolean b=false;
        }
        boolean C=false;


        JSONObject historyObj=HttpUtils.getJsonObjectUsingToken(url,HttpUtils.buildHttpClient(),SData.getQuefresquete(),false);
        HttpUtils.downloadFile(HttpUtils.buildHttpClient(),url,"aa",SData.getQuefresquete());


        long orderId=23750481137L;
        long customerId=50032819L;
        JSONObject object = HttpUtils.getJsonObjectUsingToken("https://api.mercadopago.com/v1/payments/search?sort=date_created&criteria=desc&external_reference=2000003807759738",HttpUtils.buildHttpClient(),ACACIA,false);


        String productId="MLA886804834";
        getCategory(productId,SOMOS);


        //unlockUser("LAMESITAELEVABLE","SOMOS_MAS");

        String oderId="4485479390";
        //JSONObject orderObject = getOrder(oderId,QUEFRESQUETE);
        JSONObject shippingObject = getShipping(oderId,QUEFRESQUETE);


        boolean b=false;
    }

}
