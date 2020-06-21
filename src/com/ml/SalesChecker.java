package com.ml;

import com.ml.utils.DatabaseHelper;
import com.ml.utils.GoogleMailSenderUtil;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.MessagesAndSalesHelper;
import com.ml.utils.Order;
import com.ml.utils.TokenUtils;
import org.apache.http.impl.client.CloseableHttpClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SalesChecker {

    static String ACACIA = "ACACIAYLENGA";

    static String usuario = "ACACIAYLENGA";
    //static String usuario ="SOMOS_MAS";
    //static String usuario ="QUEFRESQUETE";

    public static void main(String[] args) {

        if (args!=null && args.length>0){
            String usuarioArg = args[0];
            if (usuarioArg!=null && usuarioArg.length()>0){
                usuario=usuarioArg;
            }
        }
        String msg="*********** Procesando usuario: "+usuario;
        //Logger.log(msg);
        System.out.println(msg);

        ArrayList<Order> ordersOnCloudArrayList = fetchAllOrdersOnCloud(usuario);

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
        ArrayList<Order> ordersOnlineArrayList = MessagesAndSalesHelper.requestOrdersAndMessages(false,true, true, usuario,httpClient);

        for (Order onlineOrder: ordersOnlineArrayList){
            if (!ordersOnCloudArrayList.contains(onlineOrder)){

                if (onlineOrder.orderStatus==Order.VENDIDO && onlineOrder.notified==false){
                    onlineOrder.notified=true;
                    System.out.println("VENDISTE !!!!!!!!!");
                    String shipping="Retira";
                    if (onlineOrder.shippingType==Order.CORREO_A_DOMICILIO || onlineOrder.shippingType==Order.CORREO_RETIRA){
                        shipping="Correo";
                    }else {
                        if (onlineOrder.shippingType==Order.FLEX){
                            shipping="Flex";
                        }
                    }
                    String letraUser = usuario.substring(0,1);

                    String labelFileName=null;
                    if (!shipping.equals("Retira")){ //con envio
                        labelFileName=downloadLabel(httpClient, onlineOrder.shippingId);
                    }

                    String saleDetails="https://www.mercadolibre.com.ar/ventas/"+onlineOrder.id+"/detalle";
                    String photoFilePath=downloadPhoto(httpClient,onlineOrder.productPictureURL);

                    String[] attachments = new String[2];
                    attachments[0]=photoFilePath;
                    if (labelFileName!=null){
                        attachments[1]=labelFileName;
                    }

                    onlineOrder.messageArrayList=MessagesAndSalesHelper.getAllMessagesOnOrder(onlineOrder.packId,usuario,httpClient);
                    if (onlineOrder.messageArrayList.size()==0){//primer mensaje al usuario debe ser diferenciado.
                        boolean b=false;//mandar primer mensaje automatico
                    }else {
                        boolean b=false;
                        //primer mensaje al usuario debe ser diferenciado.
                    }
                    String mailTitle="VENDISTE "+letraUser+" "+onlineOrder.productTitle+" "+onlineOrder.id;

                    String mailBody=onlineOrder.creationTimestamp+" "+saleDetails+"<br/><br/><br/>"

                            +"<b>Producto:</b><br/>"
                            +onlineOrder.productTitle+"<br/>"
                            +onlineOrder.productVariationText+"<br/>";

                            if (onlineOrder.productQuantity>1) {
                                mailBody += "<b>CANTIDAD: " + onlineOrder.productQuantity + "</b><br/>";
                            }else {
                                mailBody += "Cantidad: " + onlineOrder.productQuantity + "<br/>";
                            }
                            if (onlineOrder.multiItem){
                                mailBody += "<b>ESTA PERSONA COMPRO DISTINTAS CLASES DE PRODUCTO, CONSULTAR EN INTERNET</b><br/>";
                            }
                            mailBody+="Comprador: "+onlineOrder.buyerFirstName+" "+onlineOrder.buyerLastName

                            +"<br/><br/><b>Envío:</b>  "+shipping+"<br/>";
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
                                onlineOrder.billingAddressLine1+"<br/>"+
                                onlineOrder.billingAddressLine2+"<br/>"+
                                onlineOrder.billingAddressLine3;
                    }


                    boolean mailSentOK=GoogleMailSenderUtil.sendMail(mailTitle,mailBody,null,attachments);

                    if (mailSentOK) {
                        //markLabelAsPrinted(httpClient,onlineOrder.shippingId);
                    }

                }
                //todo desbloquear
                //DatabaseHelper.insertSale(onlineOrder.id,onlineOrder.creationTimestamp,""+onlineOrder.orderStatus,""+onlineOrder.shippingType,true,TokenUtils.getUserNumber(usuario));
            }else{
                int pos=ordersOnCloudArrayList.indexOf(onlineOrder);
                Order cloudOrder=ordersOnCloudArrayList.get(pos);
                if (cloudOrder.orderStatus!=onlineOrder.orderStatus){
                    // todo cambio el estado de la ordern, tenemos que hacer algg?
                }
                boolean b=false;
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




    private static ArrayList<Order> fetchAllOrdersOnCloud(String user) {
        //recuperar las ordenes
        ArrayList<Order> orderArrayList = new ArrayList<Order>();
        int sellerId=TokenUtils.getUserNumber(user);

        ResultSet rs = DatabaseHelper.fetchSales();

        try{
            while (rs.next()){
                Order order=new Order();
                order.sellerId =rs.getInt(7);
                if (order.sellerId!=sellerId){
                    continue;
                }
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
                order.notified=rs.getBoolean(6);
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
