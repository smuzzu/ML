package com.ml;

import com.ml.utils.DatabaseHelper;
import com.ml.utils.HttpUtils;
import com.ml.utils.Logger;
import com.ml.utils.TokenUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class SalesChecker {

    static String usuario = "ACACIAYLENGA";
    //static String usuario ="SOMOS_MAS";
    //static String usuario ="QUEFRESQUETE";

    //Estados de la Orden
    private static final String VENDIDO = "V";
    private static final String ENTREGADO = "E";
    private static final String RECLAMO = "R";
    private static final String CANCELADO = "C";

    //Tipos de Envio
    private static final String CORREO = "C";
    private static final String FLEX = "F";
    private static final String NO_HAY_ENVIO = "N"; //sin envio o "acordar"

    private static void getOrders(CloseableHttpClient httpClient){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSX");

        int totalOrders=999999;

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
                long orderId=jsonOrder.getLong("id");
                String dateCreatedStr=jsonOrder.getString("date_created");
                dateCreatedStr=dateCreatedStr.replace('T',' ');
                try {
                    java.util.Date dateCreated = dateFormat.parse(dateCreatedStr);
                    int hour = dateCreated.getHours();
                    if (dateCreatedStr.substring(11,13).equals("12") && dateCreatedStr.substring(24).equals("04:00")){
                        hour=13;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                JSONObject buyerJsonObject=jsonOrder.getJSONObject("buyer");
                String nickName = buyerJsonObject.getString("nickname");
                String firstName = buyerJsonObject.getString("first_name");
                String lastName = buyerJsonObject.getString("last_name");
                boolean fulfilled=false;
                if (!jsonOrder.isNull("fulfilled")) {
                    fulfilled=jsonOrder.getBoolean("fulfilled");
                }
                String orderStatus=jsonOrder.getString("status");


                String[] itemsArray = new String[3];

                JSONArray orderItemsArray = jsonOrder.getJSONArray("order_items");
                int itemCount = 0;
                for (Object orderItemObject : orderItemsArray) {
                    JSONObject jsonItem = (JSONObject) orderItemObject;
                    int itemQuantity = jsonItem.getInt("quantity");
                    JSONObject jsonItem2 = jsonItem.getJSONObject("item");
                    String itemId = jsonItem2.getString("id");
                    String itemTitle = jsonItem2.getString("title");
                    String itemCategoryId = jsonItem2.getString("category_id");
                    String itemVariation = null;
                    if (!jsonItem2.isNull("variation_attributes")) {
                        JSONArray variationAtributesArray=jsonItem2.getJSONArray("variation_attributes");
                        if (variationAtributesArray.length()>0) {
                            JSONObject variationMap = (JSONObject) jsonItem2.getJSONArray("variation_attributes").get(0);
                            String variationName = variationMap.getString("name");
                            String variationValue = variationMap.getString("value_name");
                            itemVariation = variationName + ": " + variationValue;
                            boolean b=false;
                        }
                    }
                    itemsArray[itemCount] = itemId + " " + itemQuantity + " " + itemTitle + " " + itemCategoryId + " " + itemVariation;
                    itemCount++;

                }

                String shippingType ="no shipping type";

                JSONObject shippingJsonObject=jsonOrder.getJSONObject("shipping");
                String shippingStatus="N/A";
                if (shippingJsonObject.has("status")) {
                    shippingStatus=shippingJsonObject.getString("status");
                }
                if (shippingJsonObject.has("shipping_option")) {
                    JSONObject shippingOptionJsonObject = shippingJsonObject.getJSONObject("shipping_option");


                    shippingType = shippingOptionJsonObject.getString("name");
                }else {
                    shippingType="Acordar con Vendedor";
                }


                System.out.println(orderId+" "+fulfilled+" "+orderStatus+" "+dateCreatedStr+" " +nickName+" "+firstName+" "+lastName+" "+shippingType+" "+shippingStatus+" "+itemsArray[0]);
                //System.out.println(orderId+" "+shippingType);
                if (itemsArray[1]!=null){
                    System.out.println("esta ordern tiene mas items");
                    for (String itemLine:itemsArray){
                        System.out.println(itemsArray);
                    }
                }


            }
        }
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
        getOrders(httpClient);


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
