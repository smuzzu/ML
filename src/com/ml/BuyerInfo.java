package com.ml;

import com.ml.utils.HttpUtils;
import com.ml.utils.MessagesAndSalesHelper;
import com.ml.utils.Order;
import com.ml.utils.SData;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.Calendar;

public class BuyerInfo {

    public static void main(String args[]) {

        String user = SData.getAcaciaYLenga();
        long oderId=4698787599L;


        CloseableHttpClient httpClient= HttpUtils.buildHttpClient();
        Order order =MessagesAndSalesHelper.getOrderDetails(httpClient,user,oderId);
        System.out.println(order.getPrintableCSVValues());
        System.out.println(order.billingName);
        System.out.println(order.buyerFirstName + " " + order.buyerLastName);
        System.out.println(order.buyerDocTypeAndNumber);
        System.out.println(order.buyerEmail);
        System.out.println("telefono "+order.buyerPhone);




    }
}