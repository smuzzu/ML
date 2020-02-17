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

        ArrayList<Order> orderArrayList = MessagesAndSalesHelper.requestOrdersAndMessages(false,true,usuario);

    }



    private static void listAllOrders() {
        //recuperar las ordenes

        ResultSet rs = DatabaseHelper.fetchSales();

        try{
            while (rs.next()){
                int id=rs.getInt(1);
                Timestamp creationDate = rs.getTimestamp(2);
                Timestamp updateDate = rs.getTimestamp(3);
                String state = rs.getString(4);
                String tipoEnvio=rs.getString(5);
                boolean notificado=rs.getBoolean(6);
                System.out.println("id "+id+" "+creationDate+" "+updateDate+" "+state+" "+tipoEnvio+" "+notificado);
            }
            }catch(SQLException e){
                e.printStackTrace();
                Logger.log("Couldn't get last sales II");
                Logger.log(e);
            }
    }


}
