package com.ml;

import com.ml.utils.HttpUtils;
import com.ml.utils.MessagesAndSalesHelper;
import com.ml.utils.Order;
import com.ml.utils.SData;
import com.ml.utils.TokenUtils;

import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;


public class BuyerInfo {

    public static void main(String args[]) {

        String user = SData.getSomosMas();
        long oderId=2000002682888449L;


        CloseableHttpClient httpClient= HttpUtils.buildHttpClient();
        Order order = null;

        try {
            order = MessagesAndSalesHelper.getOrderDetails(httpClient, user, oderId);
        }catch (NullPointerException e){
            //ignore
        }

        if (order==null) {
            //alternativa
            String searchOrdersURL = "https://api.mercadolibre.com/orders/search?seller=" + TokenUtils.getIdCliente(user) + "&tags=pack_order&sort=date_desc";
            JSONObject ordersObject = HttpUtils.getJsonObjectUsingToken(searchOrdersURL, httpClient, user, true);
            JSONArray resultsArray = ordersObject.getJSONArray("results");
            for (int i=0; i<resultsArray.length(); i++){
                JSONObject orderObject=resultsArray.getJSONObject(i);
                if (orderObject.has("pack_id") && !orderObject.isNull("pack_id")){
                    long pack_id=orderObject.getLong("pack_id");
                    if (pack_id==oderId){
                        System.out.println("Pack Order de carrito");
                        oderId=orderObject.getLong("id");
                        order = MessagesAndSalesHelper.getOrderDetails(httpClient, user, oderId);
                    }
                }
            }

        }

        System.out.println(order.getPrintableCSVValues());
        System.out.println(order.billingName);
        System.out.println(order.buyerFirstName + " " + order.buyerLastName);
        System.out.println(order.buyerDocTypeAndNumber);
        System.out.println(order.buyerEmail);
        System.out.println("telefono "+order.buyerPhone);




    }
}