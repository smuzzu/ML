package com.ml;

import com.ml.utils.DatabaseHelper;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.TokenUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class SalesChecker {

    static String usuario = "ACACIAYLENGA";

    private static HashMap<Integer,Integer> getOrders(CloseableHttpClient httpClient){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSX");

        HashMap<Integer,Integer> statistics = new HashMap<Integer, Integer>();
        for (int i=0;i<32;i++){
            statistics.put(i,0);
        }
        int totalOrders=999999;
        int count=0;


        for (int offset=0; offset<=totalOrders; offset+=50) {
            String ordersUrl = "https://api.mercadolibre.com/orders/search?seller=" + TokenUtils.getIdCliente(usuario) + "&order.status%20ne%20cancelled&order.status%20ne%20invalid&offset="+offset;
            JSONObject jsonOrders = HttpUtils.getJsonObjectUsingToken(ordersUrl, httpClient, usuario);
            if (offset==0){
                JSONObject pagingObj = jsonOrders.getJSONObject("paging");
                totalOrders=pagingObj.getInt("total");
                String msg="total ventas registradas: "+totalOrders;
                System.out.println(totalOrders);
                Logger.log(msg);
            }
            JSONArray jsonOrdersArray = (JSONArray) jsonOrders.get("results");
            for (Object orderObjectArray : jsonOrdersArray) {
                JSONObject jsonOrder = (JSONObject) orderObjectArray;
                String dateCreatedStr=jsonOrder.getString("date_created");
                dateCreatedStr=dateCreatedStr.replace('T',' ');
                try {
                    java.util.Date dateCreated = dateFormat.parse(dateCreatedStr);
                    int hour = dateCreated.getHours();
                    if (dateCreatedStr.substring(11,13).equals("12") && dateCreatedStr.substring(24).equals("04:00")){
                        hour=13;
                    }
                    int day = dateCreated.getDay();
                    int dayOfMonth=dateCreated.getDate();

                    int sales = statistics.get(dayOfMonth);
                    sales++;
                    statistics.put(dayOfMonth, sales);

/*                    if (day>0 && day<6) {//S a D
                        count++;
                        int sales = statistics.get(hour);
                        sales++;
                        statistics.put(hour, sales);
                    }*/
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("count: "+count);
        return statistics;
    }

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

        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();



        Timestamp creationDate = new Timestamp(Calendar.getInstance().getTimeInMillis());
        int id = 3;
        String state="K";
        updateSale(id,state);

        getAllOrders();

    }

    private static void updateSale(int id, String newState) {
        Connection updateConnection = DatabaseHelper.getCloudUpdateConnection();
        PreparedStatement ps = null;
        Timestamp lastUpdate = new Timestamp(Calendar.getInstance().getTimeInMillis());

        try{
            if (ps ==null) {
                ps = updateConnection.prepareStatement("update public.ventas set estado=?, fechaactualizacion=? where id=?");
            }

            ps.setString(1,newState);
            ps.setTimestamp(2,lastUpdate);
            ps.setInt(3,id);

            int updatedRecords = ps.executeUpdate();
            if (updatedRecords!=1){
                Logger.log("Couln't update a record in sales table id="+id);
            }
            }catch(SQLException e){
                Logger.log("update a record in sales table II id="+id);
                Logger.log(e);
            }
    }

    private static void insertSale(int id, Timestamp saleDate, String state) {
        Connection updateConnection = DatabaseHelper.getCloudUpdateConnection();
        PreparedStatement ps = null;
        Timestamp lastUpdate = new Timestamp(Calendar.getInstance().getTimeInMillis());

        try{
            if (ps ==null) {
                ps = updateConnection.prepareStatement("insert into public.ventas(id,fechaventa,fechaactualizacion,estado) values (?,?,?,?)");
            }

            ps.setInt(1,id);
            ps.setTimestamp(2,saleDate);
            ps.setTimestamp(3,lastUpdate);
            ps.setString(4,state);

            int insertedRecords = ps.executeUpdate();
            if (insertedRecords!=1){
                Logger.log("Couln't insert a record in sales table id="+id);
            }

        }catch(SQLException e){
            Logger.log("Cannot insert a record in sales table II id="+id);
            Logger.log(e);
        }
    }


    private static void deleteSale(int id) {
        Connection updateConnection = DatabaseHelper.getCloudUpdateConnection();
        PreparedStatement ps = null;
        Timestamp lastUpdate = new Timestamp(Calendar.getInstance().getTimeInMillis());

        try{
            if (ps ==null) {
                ps = updateConnection.prepareStatement("delete from public.ventas where id=?");
            }

            ps.setInt(1,id);

            int removedRecords = ps.executeUpdate();
            if (removedRecords!=1){
                Logger.log("Couln't delete a record in sales table id="+id);
            }

        }catch(SQLException e){
            Logger.log("Cannot delete a record in sales table II id="+id);
            Logger.log(e);
        }
    }

    private static void getAllOrders() {
        //recuperar las ordenes

        Connection selectConnection = DatabaseHelper.getCloudSelectConnection();
        PreparedStatement ps = null;

        try{
            if (ps ==null) {
                ps = selectConnection.prepareStatement("SELECT id,fechaventa,fechaactualizacion,estado FROM public.ventas order by id");
            }

            ResultSet rs = ps.executeQuery();
            if (rs==null){
                Logger.log("Couldn't get sales");
            }
            while (rs.next()){
                int id=rs.getInt(1);
                Timestamp creationDate = rs.getTimestamp(2);
                Timestamp updateDate = rs.getTimestamp(3);
                String state = rs.getString(4);
                System.out.println("id "+id+" "+creationDate+" "+updateDate+" "+state);
            }
            }catch(SQLException e){
                Logger.log("Couldn't get last sales II");
                Logger.log(e);
            }
    }


}
