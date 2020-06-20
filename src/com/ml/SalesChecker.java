package com.ml;

import com.ml.utils.DatabaseHelper;
import com.ml.utils.GoogleMailSenderUtil;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.MessagesAndSalesHelper;
import com.ml.utils.Order;
import com.ml.utils.TokenUtils;
import org.apache.http.impl.client.CloseableHttpClient;

import org.json.JSONArray;  //todo mover a http
import org.json.JSONObject; //todo mover a http

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
//                        labelFileName=downloadLabel(httpClient, onlineOrder.shippingId);
                    }

                    String saleDetails="https://www.mercadolibre.com.ar/ventas/"+onlineOrder.id+"/detalle";
                    JSONObject publicationJsonObject=MessagesAndSalesHelper.getPublication(onlineOrder.productId,httpClient);

                    String productLink =publicationJsonObject.getString("permalink");

                    String productVariationPictureUrl=getPictureUrl(publicationJsonObject,onlineOrder.productVariationId);
                    String photoFilePath=downloadPhoto(httpClient,productVariationPictureUrl);

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
                    String mailBody=onlineOrder.creationTimestamp+" "+saleDetails+"\n\n"+"Envío: "+shipping+"\n\n"+
                            productLink+"\n\n"+onlineOrder.productVariationText;
                    if (usuario.equals(ACACIA)){//facturar
                        mailBody+="\n\nDatos para la Factura:\n"+
                                onlineOrder.billingName+"\n"+
                                onlineOrder.billingDniCuit+"\n"+
                                onlineOrder.billingAddressLine1+"\n"+
                                onlineOrder.billingAddressLine2+"\n"+
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

    private static void markLabelAsPrinted(CloseableHttpClient httpClient, long shippingId){
        String shippingUrl = "https://api.mercadolibre.com/shipments/" + shippingId + "?";
        JSONObject shippingJsonObject = new JSONObject();
        shippingJsonObject.put("substatus", "printed");
        HttpUtils.putJsonOnURL(httpClient,shippingUrl,shippingJsonObject,usuario);
    }


    private static String getPictureUrl(JSONObject publicationJsonObject, long onlineOrderVariationId){
        String pictureId=null;
        String url=null;
        if (onlineOrderVariationId!=0) {
            JSONArray variationsArray = publicationJsonObject.getJSONArray("variations");
            for (int i = 0; i < variationsArray.length(); i++) {
                JSONObject variation = variationsArray.getJSONObject(i);
                long id = variation.getLong("id");
                if (id == onlineOrderVariationId) {
                    JSONArray jsonArrayPictureIds = variation.getJSONArray("picture_ids");
                    pictureId = jsonArrayPictureIds.getString(0);
                    break;
                }
            }
            JSONArray picturesArray=publicationJsonObject.getJSONArray("pictures");
            for (int i=0; i<picturesArray.length(); i++) {
                JSONObject pictureObject = picturesArray.getJSONObject(i);
                String id = pictureObject.getString("id");
                if (id.equals(pictureId)){
                    url=pictureObject.getString("url");
                    break;
                }
            }
            if (url.endsWith("-O.jpg")){//reemplazando por el thumbnail
                url=url.substring(0,url.indexOf("-O.jpg"))+"-I.jpg";
            }
        }else {
            url=publicationJsonObject.getString("thumbnail");
        }


        return url;

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
