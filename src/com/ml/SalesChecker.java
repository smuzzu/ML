package com.ml;

import com.ml.utils.DatabaseHelper;
import com.ml.utils.GoogleMailSenderUtil;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.Message;
import com.ml.utils.MessagesAndSalesHelper;
import com.ml.utils.Order;
import com.ml.utils.Product;
import com.ml.utils.TokenUtils;
import org.apache.http.impl.client.CloseableHttpClient;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;

public class SalesChecker {

    static String ACACIA = "ACACIAYLENGA";

    static String usuario = "ACACIAYLENGA";
    //static String usuario ="SOMOS_MAS";
    //static String usuario ="QUEFRESQUETE";
    
    static boolean ignorarEtiquetayMail=false;

    public static void main(String[] args) {

        if (args!=null && args.length>0){
            String usuarioArg = args[0];
            if (usuarioArg!=null && usuarioArg.length()>0){
                usuario=usuarioArg;
            }
        }
        if (usuario.charAt(0)=='X'){//todo sacar !!
            usuario=usuario.substring(1,usuario.length());
            ignorarEtiquetayMail=true;
        }
        
        String msg="*********** Procesando usuario: "+usuario;
        Logger.log(msg);
        System.out.println(msg);


        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
        ArrayList<Order> pendingOrdersOnlineArrayList = MessagesAndSalesHelper.requestOrdersAndMessages(false,true, true, usuario,httpClient);

        for (Order pendingOrder: pendingOrdersOnlineArrayList) {
            boolean alreadyStoredInDB = DatabaseHelper.alreadyStoredInDB(pendingOrder.id);
            if (!alreadyStoredInDB) {
                DatabaseHelper.insertSale(pendingOrder.id, pendingOrder.creationTimestamp, "" + pendingOrder.orderStatus, "" + pendingOrder.shippingType, false, TokenUtils.getUserNumber(usuario), false);
            }
        }

        ArrayList<Order> pendingOrdersOnCloudArrayList = fetchAllOrdersOnCloud(usuario,true);
        for (Order pendingOrder: pendingOrdersOnCloudArrayList){  //viene de la base info limitada

            //if (onlineOrder.orderStatus==Order.VENDIDO) ?? hace falta
            boolean statusChanged=false;

            //viene de internet, mucha info
            Order onlineOrder=MessagesAndSalesHelper.getOrderDetails(httpClient,usuario,pendingOrder.id);
            //onlineOrder.messageArrayList=MessagesAndSalesHelper.getAllMessagesOnOrder(onlineOrder.packId,usuario,httpClient);

            if (onlineOrder.shippingType==Order.FULL){
                continue; //no hacemos nada con los full por ahora
            }

            if (!pendingOrder.mailSent){

                System.out.println("VENDISTE !!!!!!!!! "+onlineOrder.productTitle);
                boolean hasLabel=false;
                String phone="";
                String shipping="Envio indeterminado / consultar";
                if (onlineOrder.shippingType==Order.CORREO_A_DOMICILIO || pendingOrder.shippingType==Order.CORREO_RETIRA){
                    shipping="Correo";
                    hasLabel=true;
                }else {
                    if (onlineOrder.shippingType==Order.FLEX){
                        shipping="Flex";
                        hasLabel=true;
                    } else {
                        if (onlineOrder.shippingType==Order.ACORDAR){
                            shipping="Acorar";
                            phone="Teléfono: "+onlineOrder.buyerPhone;
                        }else{
                            if (onlineOrder.shippingType==Order.PERSONALIZADO){
                                shipping="Personalizado";
                                phone="Teléfono: "+onlineOrder.buyerPhone;
                            }
                        }
                    }
                }
                String letraUser = usuario.substring(0,1);

                boolean labelIsOk=true;
                String labelFileName=null;
                //if (hasLabel){ //con envio
                 if (hasLabel && !ignorarEtiquetayMail) { //TODO CAMBIAR
                    labelFileName=downloadLabel(httpClient, onlineOrder.shippingId);
                    if (labelFileName==null && labelFileName.isEmpty()){
                        labelIsOk=false;
                    }
                }

                String saleDetails="https://www.mercadolibre.com.ar/ventas/"+onlineOrder.id+"/detalle";
                String photoFilePath=downloadPhoto(httpClient,onlineOrder.productPictureURL);

                String[] attachments = new String[2];
                attachments[0]=photoFilePath;
                if (labelFileName!=null){
                    attachments[1]=labelFileName;
                }

                String previousQuestionsOnItem="";
                for (Message question: onlineOrder.previousQuestionsOnItemArrayList){
                    previousQuestionsOnItem += question.text + "<br>";
                }

                String previousQuestionsOtherItems="";
                for (Message question: onlineOrder.previousQuestionsOtherItemsArrayList){
                    previousQuestionsOnItem += question.text + "<br>";
                }


                String buyerSays="";
                if (onlineOrder.messageArrayList.size()>0){

                    //el ultimo mensaje es Recibido, osea no es respuesta nuestra
                    if (onlineOrder.messageArrayList.get(0).direction=='R') {
                        //buscamos primer mensaje que aunn no fue contestado
                        for (int i = onlineOrder.messageArrayList.size() - 1; i > +0; i--) {
                            Message message = onlineOrder.messageArrayList.get(i);
                            if (message.direction == 'E') {
                                break;
                            }
                            buyerSays += message.text + "<br>";
                        }
                    }
                }

                String mailTitle="VENDISTE "+letraUser+" "+onlineOrder.productTitle+" "+onlineOrder.id;

                String mailBody=onlineOrder.creationTimestamp+" "+saleDetails+"<br/><br/><br/>"

                        +"<b>Producto:</b><br/>"
                        +onlineOrder.productTitle+"<br/>";

                if (onlineOrder.productVariationText!=null && !onlineOrder.productVariationText.isEmpty()
                        && !onlineOrder.productVariationText.equals("N/A")) {
                    mailBody += onlineOrder.productVariationText + "<br/>";
                }
                if (onlineOrder.productKeyAttributes!=null && !onlineOrder.productKeyAttributes.isEmpty()
                        && !onlineOrder.productKeyAttributes.equals("N/A")) {
                    mailBody += onlineOrder.productKeyAttributes + "<br/>";
                }

                if (onlineOrder.productQuantity>1) {
                    mailBody += "<b>CANTIDAD: " + onlineOrder.productQuantity + "</b><br/>";
                }else {
                    mailBody += "Cantidad: " + onlineOrder.productQuantity + "<br/>";
                }
                if (onlineOrder.multiItem){
                    mailBody += "<b>ESTA PERSONA COMPRO DISTINTAS CLASES DE PRODUCTO, CONSULTAR EN INTERNET</b><br/>";
                }
                mailBody+="Comprador: "+onlineOrder.buyerFirstName+" "+onlineOrder.buyerLastName;

                if (buyerSays!=null && !buyerSays.isEmpty()){
                    mailBody+="<br/><br/><b>Mensaje del cliente:</b><br/>"+buyerSays;
                }

                if (previousQuestionsOnItem!=null && !previousQuestionsOnItem.isEmpty()){
                    mailBody+="<br/><br/><b>Preguntas antes de la compra en el item:</b><br/>"+previousQuestionsOnItem;
                }

                if (previousQuestionsOtherItems!=null && !previousQuestionsOtherItems.isEmpty()){
                    mailBody+="<br/><br/><b>Preguntas antes de la compra en otro item:</b><br/>"+previousQuestionsOtherItems;
                }

                mailBody+="<br/><br/><b>Envío:</b>  "+shipping+"<br/>";
                if (onlineOrder.shippingOptionNameDescription !=null && !onlineOrder.shippingOptionNameDescription.isEmpty()){
                    mailBody+=onlineOrder.shippingOptionNameDescription +"<br/>";
                }

                if (phone!=null && !phone.isEmpty()){
                    mailBody+=phone +"<br/>";
                }

                if (onlineOrder.shippingAddressLine1!=null && !onlineOrder.shippingAddressLine1.isEmpty()){
                    mailBody+=onlineOrder.shippingReceiverName+"<br/>"+
                            onlineOrder.shippingAddressLine1+"<br/>"+
                            onlineOrder.shippingAddressLine2;
                    if (onlineOrder.shippingAddressLine3!=null && !onlineOrder.shippingAddressLine3.isEmpty()) {
                        mailBody+="<br/>"+onlineOrder.shippingAddressLine3;
                    }

                }

                if (usuario.equals(ACACIA)){//facturar
                    mailBody+="<br/><br/><b>Factura:</b><br/>"+
                            onlineOrder.billingName+"<br/>"+
                            onlineOrder.billingDniCuit+"<br/>"+
                            "Total: $"+onlineOrder.paymentAmount+"<br/>"+
                            onlineOrder.billingAddressLine1+"<br/>"+
                            onlineOrder.billingAddressLine2+"<br/>"+
                            onlineOrder.billingAddressLine3;
                }


                boolean mailIsOk= GoogleMailSenderUtil.sendMail(mailTitle,mailBody,null,attachments);

                pendingOrder.mailSent=mailIsOk&&labelIsOk;
                if (pendingOrder.mailSent){
                    statusChanged=true;
                }
            }

            if (!onlineOrder.chatSent) {
                if (onlineOrder.messageArrayList.size() == 0) {//primer mensaje al usuario debe ser diferenciado.
                    Product product = DatabaseHelper.getProductFromCloud(onlineOrder.productId);
                    if (product==null || !product.disabled) {
                        String productTitle=buildProductTitle(product,onlineOrder);


                        String firstMsgToBuyer = null;
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
                        firstMsgToBuyer += processBuyerName(onlineOrder) + ". ";

                        if (product==null || product.customMessage==null || product.customMessage.trim().isEmpty()) {

                            if (onlineOrder.shippingType == Order.PERSONALIZADO || onlineOrder.shippingType == Order.ACORDAR) {
                                firstMsgToBuyer += "Pronto nos contactaremos con vos para coordinar el envio de"
                                        + productTitle
                                        + " Nuestro horario de atención es de lunes viernes de 9 a 17 y sábados de 10 a 13";
                            }

                            //todo controlar por si acaso que la orden este pendiente de envio
                            if (onlineOrder.shippingType == Order.CORREO_A_DOMICILIO || onlineOrder.shippingType == Order.CORREO_RETIRA) {
                                String shippingCurrier = "Correo Argentino";

                                if (hollydays == null) {
                                    hollydays = DatabaseHelper.fetchHolidaysFromCloud();
                                }

                                boolean atLeastOneHoliday = false;
                                Date nextDeliveryDate = getDate(TODAY, null);
                                boolean isHoliday = isHoliday(nextDeliveryDate, hollydays);
                                if (isHoliday) {
                                    atLeastOneHoliday = true;
                                }
                                boolean isWeekend = isWeekend(nextDeliveryDate);
                                if (!isCorreoDayTimeLimitPassed() && !isWeekend && !isHoliday) {
                                    firstMsgToBuyer += "Esta tarde te estaremos despachando por " + shippingCurrier
                                            + productTitle;
                                } else {
                                    nextDeliveryDate = getDate(TOMORROW, null);
                                    isWeekend = isWeekend(nextDeliveryDate);
                                    isHoliday = isHoliday(nextDeliveryDate, hollydays);
                                    if (isHoliday) {
                                        atLeastOneHoliday = true;
                                    }
                                    if (!isHoliday && !isWeekend) {
                                        firstMsgToBuyer += "Mañana por la tarde te estaremos despachando por "
                                                + shippingCurrier + productTitle;
                                    } else {
                                        while (isHoliday || isWeekend) {
                                            nextDeliveryDate = getDate(TOMORROW, nextDeliveryDate);
                                            isHoliday = isHoliday(nextDeliveryDate, hollydays);
                                            if (isHoliday) {
                                                atLeastOneHoliday = true;
                                            }
                                            isWeekend = isWeekend(nextDeliveryDate);
                                        }
                                        firstMsgToBuyer += "El " + getDayOfWeek(nextDeliveryDate) + " por la tarde estaremos despachando por " + shippingCurrier
                                                + productTitle;
                                    }
                                    if (atLeastOneHoliday) {
                                        firstMsgToBuyer += " (tener en cuenta que los días feriados los servicios de correo estan cerrados).";
                                    } else {
                                        firstMsgToBuyer += ".";
                                    }
                                }
                            }

                            //todo controlar por si acaso que la orden este pendiente de envio
                            if (onlineOrder.shippingType == Order.FLEX) {
                                if (hollydays == null) {
                                    hollydays = DatabaseHelper.fetchHolidaysFromCloud();
                                }

                                Date nextDeliveryDate = getDate(TODAY, null);
                                boolean atLeastOneHoliday = false;
                                boolean isHoliday = isHoliday(nextDeliveryDate, hollydays);
                                if (isHoliday) {
                                    atLeastOneHoliday = true;
                                }
                                boolean isWeekend = isWeekend(nextDeliveryDate);
                                if (!isCorreoDayTimeLimitPassed() && !isWeekend && !isHoliday) {
                                    firstMsgToBuyer += "Esta tarde de 15 a 20 hs va a llegar una moto a tu domicilio con"
                                            + productTitle;
                                } else {
                                    nextDeliveryDate = getDate(TOMORROW, null);
                                    isWeekend = isWeekend(nextDeliveryDate);
                                    isHoliday = isHoliday(nextDeliveryDate, hollydays);
                                    if (isHoliday) {
                                        atLeastOneHoliday = true;
                                    }
                                    if (!isHoliday && !isWeekend) {
                                        firstMsgToBuyer += "Mañana por la tarde de 15 a 20 hs va a llegar una moto a tu domicilio con"
                                                + productTitle;
                                    } else {
                                        while (isHoliday || isWeekend) {
                                            nextDeliveryDate = getDate(TOMORROW, nextDeliveryDate);
                                            isHoliday = isHoliday(nextDeliveryDate, hollydays);
                                            if (isHoliday) {
                                                atLeastOneHoliday = true;
                                            }
                                            isWeekend = isWeekend(nextDeliveryDate);
                                        }
                                        firstMsgToBuyer += "El " + getDayOfWeek(nextDeliveryDate) + " por la tarde de 15 a 20 hs va a llegar una moto a tu domicilio con"
                                                + productTitle;
                                    }/*
                                if (atLeastOneHoliday){
                                    firstMsgToBuyer += " (tener en cuenta que los días feriados "+onlineOrder.shippingCurrier
                                            +" esta cerrado).";
                                }else {
                                    firstMsgToBuyer +=".";
                                }*/
                                }
                            }
                        }else {//hay un mensaje custom
                            firstMsgToBuyer +=product.customMessage.trim()+".";
                        }

                        firstMsgToBuyer += " Muchas gracias por tu compra!";

                        // mandar mensaje aca
                        String saleDetails = "https://www.mercadolibre.com.ar/ventas/" + onlineOrder.id + "/detalle";
                        firstMsgToBuyer = saleDetails + "<br>" + firstMsgToBuyer;
                        String mailTitle = "primer mensaje para el cliente " + " " + onlineOrder.productTitle + " " + onlineOrder.id;
                        pendingOrder.chatSent = GoogleMailSenderUtil.sendMail(mailTitle, firstMsgToBuyer, null, null);
                        statusChanged = true;
                    }
                }else {
                    //el cliente ya nos escribió
                    String buyerSays=""; //TODO hay que notificar el caso pero buyerSays no va aca, no debe informarse
                    if (onlineOrder.messageArrayList.size()>0){
                        for (int i = onlineOrder.messageArrayList.size() - 1; i > +0; i--) {
                            Message message = onlineOrder.messageArrayList.get(i);
                            buyerSays += message.text + "<br>";
                        }
                    }
                    String saleDetails = "https://www.mercadolibre.com.ar/ventas/" + onlineOrder.id + "/detalle";
                    String msg1 = "no pudimos notificar a este cliente porque mandó mensajes post-venta, por favor notificar manualmente.<br>" + saleDetails;
                    if (buyerSays!=null && !buyerSays.isEmpty()){
                        msg1 += "<br><br>Mensajes post venta:<br>"+buyerSays;
                    }
                    String mailTitle = "primer mensaje para el cliente " + " " + onlineOrder.productTitle + " " + onlineOrder.id;
                    pendingOrder.chatSent = GoogleMailSenderUtil.sendMail(mailTitle, msg1, null, null);
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
            if (product.unitName!=null && !product.unitName.trim().isEmpty()){
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
        String token = TokenUtils.getToken(usuario);
        String urlWithToken = etiquetaUrl + "&access_token=" + token;
        boolean successfullDownload=HttpUtils.downloadFile(httpClient,urlWithToken,labelFileName);
        if (!successfullDownload){
            labelFileName=null;
        }
        return labelFileName;
    }

    private static String downloadPhoto(CloseableHttpClient httpClient, String productVariationPictureUrl){
        String photoFilePath="Foto_"+usuario.substring(0,1)+"_"+System.currentTimeMillis()+".jpg";
        boolean successfullDownload=HttpUtils.downloadFile(httpClient,productVariationPictureUrl,photoFilePath);
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
                order.sellerId =rs.getInt(7);
                order.id=rs.getLong(1);
                order.creationTimestamp = rs.getTimestamp(2);
                order.updateTimestamp = rs.getTimestamp(3);
                String status=rs.getString(4);
                if (status!=null && status.length()>0) {
                    order.orderStatus = status.charAt(0);
                }
                String tipoEnvio=rs.getString(5);
                if (tipoEnvio!=null && tipoEnvio.length()>0){
                    order.shippingType=tipoEnvio.charAt(0);
                }
                order.mailSent=rs.getBoolean(6);
                order.chatSent=rs.getBoolean(8);
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

    static final LocalTime LIMIT_HOUR_FLEX = LocalTime.of(13,0);
    static final LocalTime LIMIT_HOUR_CORREO = LocalTime.of(15,0);

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

    private static boolean isFlexDayTimeLimitPassed(){
        LocalTime now = LocalTime.now();
        boolean result=now.isAfter(LIMIT_HOUR_FLEX);
        return result;
    }

    private static boolean isCorreoDayTimeLimitPassed(){
        LocalTime now = LocalTime.now();
        boolean result=now.isAfter(LIMIT_HOUR_CORREO);
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
        if (nombresDobles==null){
            nombresDobles=new ArrayList<String>();
            nombresDobles.add("Maria");
            nombresDobles.add("María");
            nombresDobles.add("Jose");
            nombresDobles.add("José");
            nombresDobles.add("Juan");
            nombresDobles.add("Cristian");//todo sacar
        }

        String result=order.buyerFirstName.trim();
        if (result.contains(" ")){ //+ de in nombre
            boolean cutDoubleName=true;
            for (String nombreDoble:nombresDobles){
                if (result.startsWith(nombreDoble)){
                    cutDoubleName=false;
                    break;
                }
            }
            if (cutDoubleName) {
                int pos = result.indexOf(" ");
                result = result.substring(0, pos);
            }
        }

        return result;
    }

}
