package com.ml;

import com.ml.utils.DatabaseHelper;
import com.ml.utils.GoogleMailSenderUtil;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.Message;
import com.ml.utils.MessagesAndSalesHelper;
import com.ml.utils.Order;
import com.ml.utils.Product;
import com.ml.utils.SData;
import com.ml.utils.TokenUtils;

import org.apache.http.impl.client.CloseableHttpClient;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;

public class SalesChecker {

    static String ACACIA = SData.getAcaciaYLenga();
    static String SOMOS = SData.getSomosMas();

    //static String usuario = SData.getAcaciaYLenga();
    static String usuario =SData.getSomosMas();
    //static String usuario =SData.getQuefresquete();

    public static void main(String[] args) {

        LocalTime now = LocalTime.now();
        if (now.isAfter(SData.NON_WORKING_HOURS_FROM) && now.isBefore(SData.NON_WORKING_HOURS_TO)) {
            String msg = "Zzzzzzzz "+now.toString();
            System.out.println(msg);
            Logger.log(msg);
            System.exit(0);
        }

        if (!DatabaseHelper.isServiceEnabledOnCloud()) {
            String msg = "SalesChecker deshabilitado en cloud";
            System.out.println(msg);
            Logger.log(msg);
            System.exit(0);
        }

        if (args!=null && args.length>0){
            String usuarioArg = args[0];
            if (usuarioArg!=null && usuarioArg.length()>0){
                usuario=usuarioArg;
            }
        }


        String msg="*********** Procesando ventas de usuario: "+usuario;
        Logger.log(msg);
        System.out.println(msg);


        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
        ArrayList<Order> pendingOrdersOnlineArrayList = MessagesAndSalesHelper.requestOrdersAndMessages(false, true, false, usuario,httpClient);

        for (Order pendingOrder: pendingOrdersOnlineArrayList) {
            boolean alreadyStoredInDB = DatabaseHelper.alreadyStoredInDB(pendingOrder.id);
            if (!alreadyStoredInDB) {
                DatabaseHelper.insertSale(pendingOrder.id, pendingOrder.creationTimestamp, "" + pendingOrder.orderStatus, "" + pendingOrder.shippingType, "N", TokenUtils.getUserNumber(usuario), false);
            }
        }

        ArrayList<Order> pendingOrdersOnCloudArrayList = fetchAllOrdersOnCloud(usuario,true);

        //complete orders with all the detils
        ArrayList<Order> completePendingOrders = new ArrayList<>();
        for (Order pendingOrder: pendingOrdersOnCloudArrayList) {  //viene de la base info limitada
            Order onlineOrder=MessagesAndSalesHelper.getOrderDetails(httpClient,usuario,pendingOrder.id);
            if (onlineOrder.orderStatus==Order.CANCELADO) {
                continue; //salteamos el caso de una orden que se cargo en cloud y después se canceló
            }
            if (pendingOrder.mailSent==Order.MAIL_BUFFERED && onlineOrder.buffered){
                continue; //ya se notificó para mañana
            }
            completePendingOrders.add(onlineOrder);
        }

        //check multi items
        for (Order order: completePendingOrders) {  //viene de la base info limitada
            for (Order otherOrder: completePendingOrders) {
                if (order.shippingId==otherOrder.shippingId &&
                order.id!=otherOrder.id){
                    order.multiItem=true;
                    otherOrder.multiItem=true;
                }
            }
        }

        for (Order pendingOrder: completePendingOrders) {  //viene de la base info limitada

            boolean statusChanged = false;

            double limiteAfip=39000.0;
            pendingOrder.superaLimiteAfip = false;
            if (usuario.equals(SOMOS) && pendingOrder.paymentAmount>limiteAfip){
                pendingOrder.superaLimiteAfip=true;
            }

            Date orderCreationDate = new java.sql.Date(pendingOrder.creationTimestamp.getTime());

            if (pendingOrder.shippingType == Order.FULL) {
                continue; //no hacemos nada con los full por ahora
            }

            if (pendingOrder.mailSent!=Order.MAIL_ENVIADO) {

                System.out.println("VENDISTE !!!!!!!!! " + pendingOrder.productTitle+ " \nMail"+ pendingOrder.buyerEmail);
                boolean hasLabel = false;
                String phone = "";
                String shipping = "Envio indeterminado / consultar";
                if (pendingOrder.shippingType == Order.CORREO) {
                    shipping = "Correo";
                    hasLabel = true;
                } else {
                    if (pendingOrder.shippingType == Order.FLEX) {
                        shipping = "Flex";
                        hasLabel = true;
                    } else {
                        if (pendingOrder.shippingType == Order.ACORDAR) {
                            shipping = "Acordar";
                            phone = "Teléfono: " + pendingOrder.buyerPhone;
                        } else {
                            if (pendingOrder.shippingType == Order.PERSONALIZADO) {
                                shipping = "Personalizado";
                                phone = "Teléfono: " + pendingOrder.buyerPhone;
                            }
                        }
                    }
                }
                if (pendingOrder.buffered){
                    shipping="<b>MAÑANA</b> - "+shipping;
                }
                if (pendingOrder.superaLimiteAfip || pendingOrder.buffered){
                    hasLabel=false;
                }
                String letraUser = usuario.substring(0, 1);

                boolean labelIsOk = true;
                String labelFileName = null;

                if (hasLabel) {
                    labelFileName = downloadLabel(httpClient, pendingOrder.shippingId);
                    if (labelFileName == null || labelFileName.isEmpty()) {
                        labelIsOk = false;
                    }
                }

                String saleDetails = "https://www.mercadolibre.com.ar/ventas/" + pendingOrder.id + "/detalle";
                String photoFilePath = downloadPhoto(httpClient, pendingOrder.productPictureThumbnailURL);
                if (photoFilePath == null || photoFilePath.isEmpty()) {
                    photoFilePath = downloadPhoto(httpClient, pendingOrder.productPictureURL);
                }

                String[] attachments = new String[2];
                attachments[0] = photoFilePath;
                if (labelFileName != null) {
                    attachments[1] = labelFileName;
                }
                String[] attachments2 = new String[1];
                attachments2[0] = photoFilePath;

                String previousQuestionsOnItem = "";
                for (Message question : pendingOrder.previousQuestionsOnItemArrayList) {
                    previousQuestionsOnItem += question.text + "<br>";
                }

                String previousQuestionsOtherItems = "";
                for (Message question : pendingOrder.previousQuestionsOtherItemsArrayList) {
                    previousQuestionsOnItem += question.text + "<br>";
                }

                String buyerSays = "";
                if (pendingOrder.messageArrayList.size() > 0) {

                    //el ultimo mensaje es Recibido, osea no es respuesta nuestra
                    if (pendingOrder.messageArrayList.get(0).direction == 'R') {
                        //buscamos primer mensaje que aunn no fue contestado
                        for (int i = pendingOrder.messageArrayList.size() - 1; i > +0; i--) {
                            Message message = pendingOrder.messageArrayList.get(i);
                            if (message.direction == 'E') {
                                break;
                            }
                            buyerSays += message.text + "<br>";
                        }
                    }
                }

                String prefijoStr="";
                if (pendingOrder.superaLimiteAfip){
                    prefijoStr="CANCELAR - ";
                }

                if (pendingOrder.buffered){
                    prefijoStr="MAÑANA - ";
                }

                String mailTitle = prefijoStr + "VENDISTE " + letraUser + pendingOrder.id + " " + pendingOrder.productTitle;

                String mailTitle2 = "VENTA/SALE " + letraUser + pendingOrder.id + " " + pendingOrder.productId + " " + pendingOrder.productTitle;

                String mailBody = pendingOrder.creationTimestamp + "<br/><br/>"
                        + "<b>Producto:</b><br/>"
                        + pendingOrder.productTitle + "<br/>";

                if (pendingOrder.productVariationText != null && !pendingOrder.productVariationText.isEmpty()
                        && !pendingOrder.productVariationText.equals("N/A")) {
                    mailBody += pendingOrder.productVariationText + "<br/>";
                }
                if (pendingOrder.productKeyAttributes != null && !pendingOrder.productKeyAttributes.isEmpty()
                        && !pendingOrder.productKeyAttributes.equals("N/A")) {
                    mailBody += pendingOrder.productKeyAttributes + "<br/>";
                }

                if (pendingOrder.productQuantity > 1) {
                    mailBody += "<b>CANTIDAD/QTY: " + pendingOrder.productQuantity + "</b><br/>";
                } else {
                    mailBody += "Cantidad/Qty: " + pendingOrder.productQuantity + "<br/>";
                }
                String mailBody2=mailBody;

                if (pendingOrder.multiItem) {
                    mailBody += "<b>ESTA PERSONA COMPRO DISTINTAS CLASES DE PRODUCTO, PRESTAR ESPECIAL ATENCION LOS ITEMS NOMBRADOS EN LA SEGUNDA PAGINA DE LA ETIQUETA</b><br/>";
                    mailBody2 +="<b>Compra de varios productos, consultar | This purchase include many items, please ask</b><br/>";
                }
                if (pendingOrder.buyerFirstName==null || pendingOrder.buyerFirstName.isEmpty()){
                    mailBody += "Comprador: " + pendingOrder.buyerBusinessName;
                }else {
                    mailBody += "Comprador: " + pendingOrder.buyerFirstName + " " + pendingOrder.buyerLastName;
                }
                mailBody += "<br/>" + pendingOrder.buyerDocTypeAndNumber + " tel: " + pendingOrder.buyerPhone;
                mailBody += "<br/>email: " + pendingOrder.buyerEmail;

                mailBody +="<br/>"+saleDetails;

                if (buyerSays != null && !buyerSays.isEmpty()) {
                    mailBody += "<br/><br/><b>Mensaje del cliente:</b><br/>" + buyerSays;
                }

                if (previousQuestionsOnItem != null && !previousQuestionsOnItem.isEmpty()) {
                    mailBody += "<br/><br/><b>Preguntas antes de la compra en el item:</b><br/>" + previousQuestionsOnItem;
                }

                if (previousQuestionsOtherItems != null && !previousQuestionsOtherItems.isEmpty()) {
                    mailBody += "<br/><br/><b>Preguntas antes de la compra en otro item:</b><br/>" + previousQuestionsOtherItems;
                }

                mailBody += "<br/><br/><b>Envío:</b>  " + shipping + "<br/>";
                if (pendingOrder.shippingOptionNameDescription != null && !pendingOrder.shippingOptionNameDescription.isEmpty()) {
                    mailBody += pendingOrder.shippingOptionNameDescription + "<br/>";
                }
                if (pendingOrder.shippingType == Order.FLEX) {
                    mailBody += "Entrega: "+getWhen('F',orderCreationDate,false) + "<br/>";

                }

                if (phone != null && !phone.isEmpty()) {
                    mailBody += phone + "<br/>";
                }

                if (pendingOrder.shippingAddressLine1 != null && !pendingOrder.shippingAddressLine1.isEmpty()) {
                    mailBody += pendingOrder.shippingReceiverName + "<br/>" +
                            pendingOrder.shippingAddressLine1 + "<br/>" +
                            pendingOrder.shippingAddressLine2;
                    if (pendingOrder.shippingAddressLine3 != null && !pendingOrder.shippingAddressLine3.isEmpty()) {
                        mailBody += "<br/>" + pendingOrder.shippingAddressLine3;
                    }

                }

                if (usuario.equals(ACACIA)) {//facturar
                    mailBody += "<br/><br/><b>Factura:</b><br/>" +
                            pendingOrder.billingName + "<br/>" +
                            pendingOrder.buyerDocTypeAndNumber + "<br/>";
                    if (pendingOrder.buyerTaxerPayerType!=null){
                        mailBody += pendingOrder.buyerTaxerPayerType + "<br/>";
                    }
                    mailBody += "Total: $" + pendingOrder.paymentAmount + "<br/>" +
                            pendingOrder.billingAddressLine1 + "<br/>" +
                            pendingOrder.billingAddressLine2 + "<br/>" +
                            pendingOrder.billingAddressLine3;
                }

                mailBody2 += "<br/>Total: $" + pendingOrder.paymentAmount + "<br/>";

                String destinationAddress=SData.getMailAddressList();
                String destinationAddress2=SData.getMailAddressList2();
                String destinationAddress3=SData.getMailAddressList3();

                boolean mailIsOk = GoogleMailSenderUtil.sendMail(mailTitle, mailBody, destinationAddress, attachments);

                if (mailIsOk && letraUser!=null && letraUser.equals("S")){
                    //GoogleMailSenderUtil.sendMail(mailTitle2, mailBody2, destinationAddress2, attachments2);
                }
                if (mailIsOk && letraUser!=null && letraUser.equals("A")){
                    GoogleMailSenderUtil.sendMail(mailTitle2, mailBody2, destinationAddress3, attachments2);
                }

                pendingOrder.mailSent= Order.MAIL_NO_ENVIADO;
                if (mailIsOk && labelIsOk) {//todo ver que pasa con el buffered
                    if (pendingOrder.buffered){
                        pendingOrder.mailSent = Order.MAIL_BUFFERED;
                    }else {
                        pendingOrder.mailSent = Order.MAIL_ENVIADO;
                    }
                    statusChanged = true;
                }
            }

            if (!pendingOrder.chatSent) {
                if (pendingOrder.multiItem == false){
                    if (pendingOrder.messageArrayList.size() == 0) {//primer mensaje al usuario debe ser diferenciado.
                        Product product = DatabaseHelper.getProductFromCloud(pendingOrder.productId);
                        if (product == null || !product.disabled) {
                            String firstMsgToBuyer = null;
                            String firstMsgToBuyer2=null;
                            if (product != null && product.customMessage != null && !product.customMessage.isEmpty()) {
                                firstMsgToBuyer = product.customMessage;
                            }else{
                                firstMsgToBuyer2="Hola ";
                                String productTitle = buildProductTitle(product, pendingOrder);
                                int dayPeriod = getDayPeriod();
                                if (dayPeriod == MORNING) {
                                    firstMsgToBuyer = "Buen dia ";
                                } else {
                                    if (dayPeriod == AFTERNOON) {
                                        firstMsgToBuyer = "Buenas tardes ";
                                    } else {
                                        if (dayPeriod == EVENING) {
                                            firstMsgToBuyer = "Buenas noches ";
                                        } else {
                                            if (dayPeriod == IMPRECISE_TIME) {
                                                firstMsgToBuyer = "Hola ";
                                            }
                                        }
                                    }
                                }
                                firstMsgToBuyer += processBuyerName(pendingOrder) + ". ";
                                firstMsgToBuyer2 += processBuyerName2(pendingOrder) + ". ";

                                if (product == null || product.customMessage == null || product.customMessage.trim().isEmpty()) {

                                    if (pendingOrder.shippingType == Order.PERSONALIZADO) {
                                        firstMsgToBuyer += "Pronto nos contactaremos con vos para coordinar el envio de"
                                                + productTitle
                                                + " Nuestro horario de atención es de lunes viernes de 10:00 a 12:00 y de 15:00 a 18:00";
                                    }

                                    if (pendingOrder.shippingType == Order.ACORDAR) {
                                        if (pendingOrder.productManufacturingDays == 0) {//entrega inmediata
                                            firstMsgToBuyer += "Podes pasar a retirar ";
                                            if (pendingOrder.productQuantity == 1){
                                                firstMsgToBuyer +="tu ";
                                            }
                                            firstMsgToBuyer += productTitle + " en Av. Rivadavia 3756 CABA. "
                                                    + "Nuestro horario de atención es de lunes viernes de 10:00 a 12:00 y de 15:00 a 18:00 "
                                                    + "y nuestro teléfono es 4982-2519.  Por favor llamanos antes de venir porque estamos trabajando a puertas cerradas";
                                        } else {
                                            firstMsgToBuyer += "Ya nos podremos a preparar tu "
                                                    + productTitle
                                                    + " Y te avisaremos cuando tengamos listo.  Ante cualquier consulta no dudes en escribirnos por este chat";
                                        }
                                    }

                                    if (pendingOrder.shippingType == Order.CORREO) {
                                        String shippingCurrier = "Mercadoenvíos";
                                        if (pendingOrder.shippingCurrier!=null && !pendingOrder.shippingCurrier.isEmpty()){
                                            shippingCurrier+="/"+pendingOrder.shippingCurrier;
                                        }
                                        String when = getWhen('C',orderCreationDate,pendingOrder.buffered);
                                        firstMsgToBuyer += when + " te estaremos despachando por " + shippingCurrier
                                                + productTitle;
                                        String when2 = getWhen2('C',orderCreationDate);
                                        firstMsgToBuyer2 += when2 + " despacharemos tu compra por Mercadoenvios.";
                                    }

                                    if (pendingOrder.shippingType == Order.FLEX) {
                                        String when = getWhen('F',orderCreationDate,false);
                                        firstMsgToBuyer += when + " de 15 a 20 hs va a llegar una moto a tu domicilio con"
                                                + productTitle;
                                        String when2 = getWhen2('F',orderCreationDate);//el horario lo trae la funcion
                                        firstMsgToBuyer2 += when2 + " llegará tu compra a tu domicilio.";
                                    }
                                }

                                firstMsgToBuyer += " Muchas gracias por tu compra!";
                                firstMsgToBuyer2 += " Gracias!";
                            }
                            pendingOrder.chatSent = HttpUtils.postMessage(firstMsgToBuyer, httpClient, pendingOrder.packId, usuario, pendingOrder.buyerCustId, pendingOrder.shippingType);
                            if (pendingOrder.chatSent) {
                                String mailTitle = "primer mensaje para el cliente " + " " + pendingOrder.productTitle + " " + pendingOrder.id;
                                String text = firstMsgToBuyer+"<br/><br/>Version corta:<br/>"+firstMsgToBuyer2;
                                GoogleMailSenderUtil.sendMail(mailTitle, text, SData.getMailErrorNotification(), null); //todo sacar
                            }
                            statusChanged = true;
                        }
                    } else {
                        //el cliente ya nos escribió
                        String buyerSays = ""; //TODO hay que notificar el caso pero buyerSays no va aca, no debe informarse
                        if (pendingOrder.messageArrayList.size() > 0) {
                            for (int i = pendingOrder.messageArrayList.size() - 1; i > +0; i--) {
                                Message message = pendingOrder.messageArrayList.get(i);
                                buyerSays += message.text + "<br>";
                            }
                        }
                        String saleDetails = "https://www.mercadolibre.com.ar/ventas/" + pendingOrder.id + "/detalle";
                        String msg1 = "no pudimos notificar a este cliente porque mandó mensajes post-venta, por favor notificar manualmente.<br>" + saleDetails;
                        if (buyerSays != null && !buyerSays.isEmpty()) {
                            msg1 += "<br><br>Mensajes post venta:<br>" + buyerSays;
                        }
                        String mailTitle = "primer mensaje para el cliente " + " " + pendingOrder.productTitle + " " + pendingOrder.id;
                        pendingOrder.chatSent = GoogleMailSenderUtil.sendMail(mailTitle, msg1, SData.getMailErrorNotification(), null);
                        statusChanged = true;
                    }
                }else {
                    //es una order multi item
                    String saleDetails = "https://www.mercadolibre.com.ar/ventas/" + pendingOrder.id + "/detalle";
                    String msg1 = "No pudimos notificar a este cliente porque mandó hizo una compra multi item, por favor notificar manualmente.<br>" + saleDetails;
                    String mailTitle = "primer mensaje para el cliente " + " " + pendingOrder.productTitle + " " + pendingOrder.id;
                    pendingOrder.chatSent = GoogleMailSenderUtil.sendMail(mailTitle, msg1, SData.getMailErrorNotification(), null);
                    statusChanged = true;
                }
            }

            if (statusChanged){
                DatabaseHelper.updateSale(pendingOrder.id,null,null,pendingOrder.mailSent,pendingOrder.chatSent);
            }

        }

        msg="*********** FIN";
        Logger.log(msg);
        System.out.println(msg);
    }

/*
    private static void markLabelAsPrinted(CloseableHttpClient httpClient, long shippingId){
        String shippingUrl = "https://api.mercadolibre.com/shipments/" + shippingId + "?";
        JSONObject shippingJsonObject = new JSONObject();
        shippingJsonObject.put("substatus", "printed");
        HttpUtils.putJsonOnURL(httpClient,shippingUrl,shippingJsonObject,usuario);
    }
*/

    private static String buildProductTitle(Product product, Order order){
        String result=null;

        int quantity = order.productQuantity;
        String productName = order.productTitle;

        if (product!=null) {
            quantity = order.productQuantity * product.multiplier;
            if (product.title != null && !product.title.isEmpty()) {
                productName = product.title;
            }
        }

        String qtyPrefix=" tu ";
        if (quantity > 1) {
            String unitName="";
            if (product !=null && product.unitName!=null && !product.unitName.trim().isEmpty()){
                unitName=product.unitName.trim();
                qtyPrefix = " " + quantity + " " + unitName + " de ";
            }else {
                qtyPrefix = " " + quantity + " ";
            }
        }

        String productVariation="";
        if (order.productVariationText!=null && !order.productVariationText.isEmpty()
                && !order.productVariationText.equals("N/A")) {
            productVariation += " "+order.productVariationText;
        }

        result=qtyPrefix+productName + productVariation+ ".";
        return result;
    }


    private static String downloadLabel(CloseableHttpClient httpClient, long shippingId){
        long minutes=System.currentTimeMillis()/1000/60;
        String labelFileName="Etiqueta_"+usuario.substring(0,1)+"_"+shippingId+"_"+minutes+".pdf";
        String etiquetaUrl="https://api.mercadolibre.com/shipment_labels?shipment_ids="+shippingId+"&savePdf=Y";
        boolean successfullDownload=HttpUtils.downloadFile(httpClient,etiquetaUrl,labelFileName,usuario);
        if (!successfullDownload){
            labelFileName=null;
        }
        return labelFileName;
    }

    private static String downloadPhoto(CloseableHttpClient httpClient, String productVariationPictureUrl){
        String photoFilePath="Foto_"+usuario.substring(0,1)+"_"+System.currentTimeMillis()+".jpg";
        boolean successfullDownload=HttpUtils.downloadFile(httpClient,productVariationPictureUrl,photoFilePath,usuario);
        if (!successfullDownload){
            photoFilePath=null;
        }
        return photoFilePath;
    }




    private static ArrayList<Order> fetchAllOrdersOnCloud(String user,boolean onlyPending) {
        //recuperar las ordenes
        ArrayList<Order> orderArrayList = new ArrayList<Order>();
        int sellerId = TokenUtils.getUserNumber(user);

        ResultSet rs = null;
        if (onlyPending) {
            rs=DatabaseHelper.fetchSales(sellerId,true);
        } else {
            rs=DatabaseHelper.fetchSales(sellerId,false);
        }

        try{
            while (rs.next()){
                Order order=new Order();
                order.sellerId =rs.getInt("usuario");
                order.id=rs.getLong("id");
                order.creationTimestamp = rs.getTimestamp("fechaventa");
                order.updateTimestamp = rs.getTimestamp("fechaactualizacion");
                String status=rs.getString("estado");
                if (status!=null && status.length()>0) {
                    order.orderStatus = status.charAt(0);
                }
                String tipoEnvio=rs.getString("tipoenvio");
                if (tipoEnvio!=null && tipoEnvio.length()>0){
                    order.shippingType=tipoEnvio.charAt(0);
                }
                String mailSent=rs.getString("mailenviado");
                if (mailSent!=null && mailSent.length()>0) {
                    order.mailSent = mailSent.charAt(0);
                }
                order.chatSent=rs.getBoolean("chatenviado");
                orderArrayList.add(order);
            }
            }catch(SQLException e){
                e.printStackTrace();
                Logger.log("Couldn't get last sales II");
                Logger.log(e);
            }
        return orderArrayList;
    }


    static final int TODAY = 0;
    static final int TOMORROW = 1;
    static final int NEXT_MONDAY = 2;

    private static Date getDate(int type,Date date){
        Calendar cal = Calendar.getInstance();
        if (date!=null) {
            cal.setTime(date);
        }
        if (type!=TODAY){
            if (type==TOMORROW){
                cal.add(Calendar.DATE, 1);
            }else {
                if (type==NEXT_MONDAY){
                    cal.add(Calendar.DATE, 1);
                    while( cal.get( Calendar.DAY_OF_WEEK ) != Calendar.MONDAY ) {
                        cal.add(Calendar.DATE, 1);
                    }
                }
            }
        }
        Date result = new Date(cal.getTimeInMillis());

        return result;
    }

    private static boolean isWeekend(Date date){
        boolean result=false;
        Calendar cal = Calendar.getInstance();
        if (date!=null) {
            cal.setTime(date);
        }
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek==Calendar.SUNDAY){
            result=true;
        }
        return result;
    }

    private static boolean isHoliday(Date date,ArrayList<Date> holidays){
        boolean result=false;
        Calendar holidayDate = Calendar.getInstance();;
        int dayHoliday,monthHoliday,yearHoliday;
        Calendar calendarDate = Calendar.getInstance();
        if (date!=null) {
            calendarDate.setTime(date);
        }
        int dayDate=calendarDate.get(Calendar.DAY_OF_MONTH);
        int monthDate=calendarDate.get(Calendar.MONTH);
        int yearDate=calendarDate.get(Calendar.YEAR);
        for (Date holiday: holidays){
            if (holiday!=null) {
                holidayDate.setTime(holiday);
            }
            dayHoliday=holidayDate.get(Calendar.DAY_OF_MONTH);
            monthHoliday=holidayDate.get(Calendar.MONTH);
            yearHoliday=holidayDate.get(Calendar.YEAR);
            if (dayDate==dayHoliday && monthDate==monthHoliday && yearDate==yearHoliday){
                result = true;
                break;
            }
        }
        return result;
    }

    static final int IMPRECISE_TIME = 0;
    static final int MORNING = 1;
    static final int AFTERNOON = 2;
    static final int EVENING = 3;

    static final LocalTime LIMIT_HOUR_FLEX = LocalTime.of(0,1);
    static final LocalTime LIMIT_HOUR_CORREO = LocalTime.of(14,30);

    static LocalTime MORNING_FROM = LocalTime.of(7,00);
    static LocalTime MORNING_TO = LocalTime.of(13,00);
    static LocalTime AFTERNOON_FROM = LocalTime.of(13,01);
    static LocalTime AFTERNOON_TO = LocalTime.of(19,00);
    static LocalTime EVENING_FROM = LocalTime.of(20,30);
    static LocalTime EVENING_TO = LocalTime.of(2,00);

    static ArrayList<Date> hollydays=null;

    private static int getDayPeriod(){
        int result = IMPRECISE_TIME;
        LocalTime now = LocalTime.now();
        if (now.isAfter(MORNING_FROM) && now.isBefore(MORNING_TO)){
            result=MORNING;
        }else {
            if (now.isAfter(AFTERNOON_FROM) && now.isBefore(AFTERNOON_TO)){
                result=AFTERNOON;
            }else {
                if (EVENING_FROM.isAfter(EVENING_TO)) { //de 20 a 1 am
                    if (now.isAfter(EVENING_FROM) || now.isBefore(EVENING_TO)) {
                        result=EVENING;
                    }
                }else { //de 20 a 23 hs
                    if (now.isAfter(EVENING_FROM) && now.isBefore(EVENING_TO)) {
                        result=EVENING;
                    }
                }
            }
        }
        return result;
    }

    private static boolean isDayTimeLimitPassed(char correoOrFlex){
        LocalTime now = LocalTime.now();
        boolean result=false;
        if (correoOrFlex=='C'){ //correo
            result=now.isAfter(LIMIT_HOUR_CORREO);
        }
        else { //flex 'F'
            result=now.isAfter(LIMIT_HOUR_FLEX);
        }
        return result;
    }

    private static String getWhen(char correoOrFlex, Date orderCreationDate, boolean isBuffered){
        String result="";

        if (hollydays == null) {
            hollydays = DatabaseHelper.fetchHolidaysFromCloud();
        }

        Date nextDeliveryDate = orderCreationDate;
        boolean isHoliday = isHoliday(nextDeliveryDate, hollydays);
        boolean isWeekend = isWeekend(nextDeliveryDate);
        if (!isDayTimeLimitPassed(correoOrFlex) && !isWeekend && !isHoliday &&!isBuffered) {
            result = "Esta tarde";
        } else {
            nextDeliveryDate = getDate(TOMORROW, null);
            isWeekend = isWeekend(nextDeliveryDate);
            isHoliday = isHoliday(nextDeliveryDate, hollydays);
            if (!isHoliday && !isWeekend) {
                result += "Mañana por la tarde";
            } else {
                while (isHoliday || isWeekend) {
                    nextDeliveryDate = getDate(TOMORROW, nextDeliveryDate);
                    isHoliday = isHoliday(nextDeliveryDate, hollydays);
                    isWeekend = isWeekend(nextDeliveryDate);
                }
                result += "El " + getDayOfWeek(nextDeliveryDate) + " por la tarde";
            }
        }
        return result;

    }

    private static String getWhen2(char correoOrFlex,Date orderCreationDate){
        String result="";

        if (hollydays == null) {
            hollydays = DatabaseHelper.fetchHolidaysFromCloud();
        }

        Date nextDeliveryDate = orderCreationDate;
        boolean isHoliday = isHoliday(nextDeliveryDate, hollydays);
        boolean isWeekend = isWeekend(nextDeliveryDate);
        if (!isDayTimeLimitPassed(correoOrFlex) && !isWeekend && !isHoliday) {
            if (correoOrFlex=='C') {//correo
                result = "Esta tarde";
            }else{//flex
                result = "Hoy de 15:00 a 20:00";
            }
        } else {
            nextDeliveryDate = getDate(TOMORROW, null);
            isWeekend = isWeekend(nextDeliveryDate);
            isHoliday = isHoliday(nextDeliveryDate, hollydays);
            if (!isHoliday && !isWeekend) {
                if (correoOrFlex=='C') {//correo
                    result = "Mañana por la tarde";
                }else {//flex
                    result = "Mañana de 15:00 a 20:00";
                }
            } else {
                while (isHoliday || isWeekend) {
                    nextDeliveryDate = getDate(TOMORROW, nextDeliveryDate);
                    isHoliday = isHoliday(nextDeliveryDate, hollydays);
                    isWeekend = isWeekend(nextDeliveryDate);
                }
                if (correoOrFlex=='C') {//correo
                    result = "El " + getDayOfWeek(nextDeliveryDate) + " por la tarde";
                }else {//flex
                    result = "El " + getDayOfWeek(nextDeliveryDate) + " de 15:00 a 20:00";
                }
            }
        }
        return result;

    }

    private static String getDayOfWeek(Date date){
        String result=null;

        Calendar cal = Calendar.getInstance();
        if (date!=null) {
            cal.setTime(date);
        }
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.MONDAY){
            result="lunes";
        }else {
            if (dayOfWeek == Calendar.TUESDAY) {
                result = "martes";
            } else {
                if (dayOfWeek == Calendar.WEDNESDAY) {
                    result = "miércoles";
                } else {
                    if (dayOfWeek == Calendar.TUESDAY) {
                        result = "jueves";
                    } else {
                        if (dayOfWeek == Calendar.FRIDAY) {
                            result = "viernes";
                        } else {
                            if (dayOfWeek == Calendar.SATURDAY) {
                                result = "sábado";
                            }else {
                                if (dayOfWeek == Calendar.SUNDAY) {
                                    result = "domingo";
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private static ArrayList<String> nombresDobles=null;

    private static String processBuyerName(Order order){
        String result="";
        boolean processHumanName=true;
        if (order.buyerFirstName!=null) {
            result = order.buyerFirstName.trim();
        }else { //empresa
            if (order.buyerBusinessName!=null) {
                result = order.buyerBusinessName.trim();
                if (result.contains("SA") || result.contains("SRL")) {
                    processHumanName = false;
                }
            }else {
                Logger.log("no sabemos quien carajos compro / pago la cuenta");
            }
        }

       if (processHumanName) {
           if (nombresDobles==null){
               nombresDobles=new ArrayList<String>();
               nombresDobles.add("Maria");
               nombresDobles.add("María");
               nombresDobles.add("Jose");
               nombresDobles.add("José");
               nombresDobles.add("Juan");
           }

           if (result.contains(" ")) { //+ de un nombre
               boolean cutDoubleName = true;
               for (String nombreDoble : nombresDobles) {
                   if (result.startsWith(nombreDoble)) {
                       cutDoubleName = false;
                       break;
                   }
               }
               if (cutDoubleName) {
                   int pos = result.indexOf(" ");
                   result = result.substring(0, pos);
               }
           }
       }

        return result;
    }

    private static String processBuyerName2(Order order){
        String result=processBuyerName(order);
        if (result.length() > 9) {
            result = result.substring(0, 9);
        }
        return result;
    }


}
