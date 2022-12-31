package com.ml;

import com.ml.utils.*;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.ArrayList;

import static com.ml.utils.DatabaseHelper.cancellAlreadyStoredInDB;




public class CancelChecker {

    private static void processCancelled(String user, CloseableHttpClient httpClient) {

        System.out.println("Chequeando canceladas de "+user);

        ArrayList<Order> orderArrayList = MessagesAndSalesHelper.requestOrdersAndMessages(false,false, MessagesAndSalesHelper.LAST_TWO_MONTHS, user,httpClient, false);

        for (Order order : orderArrayList) {
            if (!order.paymentStatus.equals("paid")) {
                if (cancellAlreadyStoredInDB(order.id)) {
                    continue;
                }

                String userLetter="S";
                if (user.equals(SData.getAcaciaYLenga())){
                    userLetter="A";
                }

                String mailTitle = "VENTA CANCELADA/CANCELLED SALE " + userLetter + order.id + " " + order.productId + " " + order.productTitle;
                String mailBody = order.id+" "+order.creationTimestamp + "<br/><br/>"
                        + "<b>Producto:</b><br/>"
                        + order.productTitle + "<br/>";

                if (order.productVariationText != null && !order.productVariationText.isEmpty()
                        && !order.productVariationText.equals("N/A")) {
                    mailBody += order.productVariationText + "<br/>";
                }
                if (order.productKeyAttributes != null && !order.productKeyAttributes.isEmpty()
                        && !order.productKeyAttributes.equals("N/A")) {
                    mailBody += order.productKeyAttributes + "<br/>";
                }
                mailBody += "total: "+order.paymentAmount + "<br/>";
                System.out.println(mailTitle);
                System.out.println(order.getPrintableCSVValues());
                String destinationAddress3=SData.getMailAddressList3();
                boolean mailSent=GoogleMailSenderUtil.sendMail(mailTitle,mailBody,destinationAddress3,null);
                if (mailSent) {// mail sent check
                    DatabaseHelper.insertCancelled(order.id, order.creationTimestamp);
                }
            }
        }
    }

    public static void main(String[] args){

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();

        String user = SData.getAcaciaYLenga();
        processCancelled(user,httpClient);

        user = SData.getSomosMas();
        processCancelled(user,httpClient);



    }
}
