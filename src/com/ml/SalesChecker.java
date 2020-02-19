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

    //static String usuario = "ACACIAYLENGA";
    //static String usuario ="SOMOS_MAS";
    static String usuario ="QUEFRESQUETE";

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
        ArrayList<Order> ordersOnlineArrayList = MessagesAndSalesHelper.requestOrdersAndMessages(false,true, false, usuario,httpClient);

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
                    String saleDetails="https://www.mercadolibre.com.ar/ventas/"+onlineOrder.id+"/detalle";
                    String productLink = MessagesAndSalesHelper.getLink(onlineOrder.productId,httpClient);
                    onlineOrder.messageArrayList=MessagesAndSalesHelper.getAllMessagesOnOrder(onlineOrder.packId,usuario,httpClient);
                    if (onlineOrder.messageArrayList.size()==0){//primer mensaje al usuario debe ser diferenciado.
                        boolean b=false;//mandar primer mensaje automatico
                    }else {
                        boolean b=false;
                        //primer mensaje al usuario debe ser diferenciado.
                    }
                    GoogleMailSenderUtil.sendMail("VENDISTE "+usuario.substring(0,1)+" "+onlineOrder.productTitle,onlineOrder.creationTimestamp+"\n\n"+saleDetails+"\n\n"+shipping+"\n\n"+productLink+"\n\n"+onlineOrder.productVariation);
                    MessagesAndSalesHelper.printOrder(onlineOrder);

                }
                DatabaseHelper.insertSale(onlineOrder.id,onlineOrder.creationTimestamp,""+onlineOrder.orderStatus,""+onlineOrder.shippingType,true,TokenUtils.getUserNumber(usuario));
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
