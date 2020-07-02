package com.ml;

import com.ml.utils.DatabaseHelper;
import com.ml.utils.GoogleMailSenderUtil;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.Message;
import com.ml.utils.MessagesAndSalesHelper;
import com.ml.utils.Order;
import com.ml.utils.TokenUtils;
import org.apache.http.impl.client.CloseableHttpClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SalesChecker {

    static String ACACIA = "ACACIAYLENGA";

    //static String usuario = "ACACIAYLENGA";
    static String usuario ="SOMOS_MAS";
    //static String usuario ="QUEFRESQUETE";

    public static void main(String[] args) {

        if (args!=null && args.length>0){
            String usuarioArg = args[0];
            if (usuarioArg!=null && usuarioArg.length()>0){
                usuario=usuarioArg;
            }
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
                if (hasLabel){ //con envio
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
                            onlineOrder.shippingAddressLine2+"<br/>"+
                            onlineOrder.shippingAddressLine3;

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

            if (!onlineOrder.chatSent){
                if (onlineOrder.messageArrayList.size()==0){//primer mensaje al usuario debe ser diferenciado.
                    // mandar mensaje aca
                    //pendingOrder.chatSent=true;
                    //statusChanged=true;
                }
            }

            if (statusChanged){
                DatabaseHelper.updateSale(pendingOrder.id,null,null,pendingOrder.mailSent,pendingOrder.chatSent);
            }

        }


    }

/*
    private static void markLabelAsPrinted(CloseableHttpClient httpClient, long shippingId){
        String shippingUrl = "https://api.mercadolibre.com/shipments/" + shippingId + "?";
        JSONObject shippingJsonObject = new JSONObject();
        shippingJsonObject.put("substatus", "printed");
        HttpUtils.putJsonOnURL(httpClient,shippingUrl,shippingJsonObject,usuario);
    }
*/


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


}
