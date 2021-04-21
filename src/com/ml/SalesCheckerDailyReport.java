package com.ml;

import com.ml.utils.GoogleMailSenderUtil;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.MessagesAndSalesHelper;
import com.ml.utils.Order;
import com.ml.utils.SData;

import org.apache.http.impl.client.CloseableHttpClient;


import java.util.ArrayList;
import java.util.Collections;

public class SalesCheckerDailyReport {

    public static void main(String[] args) {


        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        String usuario = SData.getAcaciaYLenga();
        String msg="*********** Procesando usuario: "+usuario;
        Logger.log(msg);
        System.out.println(msg);
        ArrayList<Order> completePendingOrdersAcacia = MessagesAndSalesHelper.requestOrdersAndMessages(false,true, usuario,httpClient);

        usuario = SData.getSomosMas();
        msg="*********** Procesando usuario: "+usuario;
        Logger.log(msg);
        System.out.println(msg);
        ArrayList<Order> completePendingOrdersSomos = MessagesAndSalesHelper.requestOrdersAndMessages(false,true, usuario,httpClient);

        ArrayList<Order> completePendingOrders = new ArrayList<Order>();
        completePendingOrders.addAll(completePendingOrdersAcacia);
        completePendingOrders.addAll(completePendingOrdersSomos);

        Collections.sort(completePendingOrders);

        String html = "<h1>Correo</h1><table border=1>";
        for (Order pendingOrder: completePendingOrders) {  //viene de la base info limitada
            if (pendingOrder.shippingType==Order.CORREO && pendingOrder.readyForSending){
                html+="<tr><td width=500px>"+pendingOrder.productTitle+"<br/>";
                if (pendingOrder.productVariationText!=null && !pendingOrder.productVariationText.isEmpty()
                    && !pendingOrder.productVariationText.equals("N/A")) {
                    html += pendingOrder.productVariationText+"<br/>";
                }
                if (pendingOrder.productKeyAttributes!=null && !pendingOrder.productKeyAttributes.isEmpty()
                        && !pendingOrder.productKeyAttributes.equals("N/A")) {
                    html+=pendingOrder.productKeyAttributes+"<br/>";
                }
                html += "Cantidad: " + pendingOrder.productQuantity + "<br/>";
                html += "Comprador: " + pendingOrder.buyerFirstName + " " + pendingOrder.buyerLastName+"<td width=200px>&nbsp;</td></tr>";

            }
        }
        html += "</table><br/><br/><br/>";


        html += "<h1>Moto</h1><table border=1>";
        for (Order pendingOrder: completePendingOrders) {  //viene de la base info limitada
            if (pendingOrder.shippingType==Order.FLEX && pendingOrder.readyForSending){
                html+="<tr><td width=500px>"+pendingOrder.productTitle+"<br/>";
                if (pendingOrder.productVariationText!=null && !pendingOrder.productVariationText.isEmpty()
                        && !pendingOrder.productVariationText.equals("N/A")) {
                    html += pendingOrder.productVariationText+"<br/>";
                }
                if (pendingOrder.productKeyAttributes!=null && !pendingOrder.productKeyAttributes.isEmpty()
                        && !pendingOrder.productKeyAttributes.equals("N/A")) {
                    html+=pendingOrder.productKeyAttributes+"<br/>";
                }
                html += "Cantidad: " + pendingOrder.productQuantity + "<br/>";
                html += "Comprador: " + pendingOrder.buyerFirstName + " " + pendingOrder.buyerLastName+"<td width=200px>&nbsp;</td></tr>";
            }
        }
        html += "</table><br/><br/><br/>";

        html += "<h1>Envio Personalizado</h1><table border=1>";
        for (Order pendingOrder: completePendingOrders) {  //viene de la base info limitada
            if (pendingOrder.shippingType==Order.PERSONALIZADO){
                html+="<tr><td width=500px>"+pendingOrder.productTitle+"<br/>";
                if (pendingOrder.productVariationText!=null && !pendingOrder.productVariationText.isEmpty()
                        && !pendingOrder.productVariationText.equals("N/A")) {
                    html += pendingOrder.productVariationText+"<br/>";
                }
                if (pendingOrder.productKeyAttributes!=null && !pendingOrder.productKeyAttributes.isEmpty()
                        && !pendingOrder.productKeyAttributes.equals("N/A")) {
                    html+=pendingOrder.productKeyAttributes+"<br/>";
                }
                html += "Cantidad: " + pendingOrder.productQuantity + "<br/>";
                html += "Comprador: " + pendingOrder.buyerFirstName + " " + pendingOrder.buyerLastName+"<br/>";
                if (pendingOrder.buyerPhone!=null && !pendingOrder.buyerPhone.isEmpty()){
                    html += "Telefono: " + pendingOrder.buyerPhone;
                }
                html+="<td width=200px>&nbsp;</td></tr>";
            }
        }
        html += "</table><br/><br/><br/>";

        html += "<h1>Acordar / Retira</h1><table border=1>";
        for (Order pendingOrder: completePendingOrders) {  //viene de la base info limitada
            if (pendingOrder.shippingType==Order.ACORDAR){
                html+="<tr><td width=500px>"+pendingOrder.productTitle+"<br/>";
                if (pendingOrder.productVariationText!=null && !pendingOrder.productVariationText.isEmpty()
                        && !pendingOrder.productVariationText.equals("N/A")) {
                    html += pendingOrder.productVariationText+"<br/>";
                }
                if (pendingOrder.productKeyAttributes!=null && !pendingOrder.productKeyAttributes.isEmpty()
                        && !pendingOrder.productKeyAttributes.equals("N/A")) {
                    html+=pendingOrder.productKeyAttributes+"<br/>";
                }
                html += "Cantidad: " + pendingOrder.productQuantity + "<br/>";
                html += "Comprador: " + pendingOrder.buyerFirstName + " " + pendingOrder.buyerLastName+"<br/>";
                if (pendingOrder.buyerPhone!=null && !pendingOrder.buyerPhone.isEmpty()){
                    html += "Telefono: " + pendingOrder.buyerPhone;
                }
                html+="<td width=200px>&nbsp;</td></tr>";
            }
        }


        html += "</table>";

        String mailTitle="Reporte de entregas pendientes";
        String destinationAdress=SData.getMailAddressList();
        GoogleMailSenderUtil.sendMail(mailTitle, html, destinationAdress,null);

    }


}
