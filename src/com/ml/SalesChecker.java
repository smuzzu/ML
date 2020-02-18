package com.ml;

import com.ml.utils.DatabaseHelper;
import com.ml.utils.Logger;
import com.ml.utils.MessagesAndSalesHelper;
import com.ml.utils.Order;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class SalesChecker {

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

        ArrayList<Order> ordersOnCloudArrayList = fetchAllOrdersOnCloud();

        ArrayList<Order> ordersOnlineArrayList = MessagesAndSalesHelper.requestOrdersAndMessages(false,true,usuario);

        for (Order onlineOrder: ordersOnlineArrayList){
            if (!ordersOnCloudArrayList.contains(onlineOrder)){

                if (onlineOrder.orderStatus==Order.VENDIDO && onlineOrder.notified==false){
                    onlineOrder.notified=true;
                    System.out.println("VENDISTE !!!!!!!!!");
                    MessagesAndSalesHelper.printOrder(onlineOrder);

                }
                boolean b=false; //agregar orden a la base
            }
        }

    }



    private static ArrayList<Order> fetchAllOrdersOnCloud() {
        //recuperar las ordenes
        ArrayList<Order> orderArrayList = new ArrayList<Order>();

        ResultSet rs = DatabaseHelper.fetchSales();

        try{
            while (rs.next()){
                Order order=new Order();
                order.id=rs.getInt(1);
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
                order.user=rs.getString(7);
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
