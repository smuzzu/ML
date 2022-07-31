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


public class InvoicesSomos {

    public static void main(String[] args) {

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        String usuario = SData.getSomosMas();
        String msg="*********** Procesando facturacion semanal de usuario: "+usuario;
        Logger.log(msg);
        System.out.println(msg);

        ArrayList<Order> completeOrders = MessagesAndSalesHelper.requestOrdersAndMessages(false,false, true, usuario,httpClient);
        Collections.sort(completeOrders);

        String html = "<h1>Reporte para facturación semanal</h1><table border=1>";
        for (Order order: completeOrders) {  //viene de la base info limitada


            boolean facturado=MessagesAndSalesHelper.hasAttachmedPDFOnOrder(order.packId,SData.getSomosMas(),httpClient);
            if (order.paymentAmount>SData.LIMITE_MONO_AFIP || facturado){
                continue;
            }

            html+="<tr><td width=500px>";

            html += order.id+" "+order.userNickName;

            String fecha = order.creationTimestamp.toString();
            fecha=fecha.substring(0,fecha.indexOf(" "));

            html += "<br/>Fecha: "+fecha;

            html += "<br/>"+order.buyerDocTypeAndNumber;

            if (order.buyerFirstName == null || order.buyerFirstName.isEmpty()) {
                html += "<br/>Razón social: " + order.buyerBusinessName;
            } else {
                html += "<br/>Razón social: " + order.buyerFirstName + " " + order.buyerLastName;
            }

            html += "<br/>" + order.productQuantity;

            String product=" " + order.productTitle;
/*
            if (order.productVariationText != null && !order.productVariationText.isEmpty()
                    && !order.productVariationText.equals("N/A")) {
                product += order.productVariationText + "<br/>";
            }
            if (order.productKeyAttributes != null && !order.productKeyAttributes.isEmpty()
                    && !order.productKeyAttributes.equals("N/A")) {
                product += order.productKeyAttributes + "<br/>";
            }*/
            product=product.replace("Hafele","");
            product=product.replace("Häfele","");
            product=product.replace("Cuotas","");
            html += product;

            html += "<br/>Importe Total: " + order.paymentAmount;

        }
        html += "</table>";

        String mailTitle="Reporte para emitir facturas C";
        String destinationAdress=SData.getMailAddressList();


        GoogleMailSenderUtil.sendMail(mailTitle, html, destinationAdress,null);

    }


}
