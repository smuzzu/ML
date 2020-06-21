package com.ml.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class MessagesAndSalesHelper {


    //static final String usuario="ACACIAYLENGA";
    //static final String usuario="QUEFRESQUETE";
    static long tooMuchTime=10000l;

    public static ArrayList<Order>  requestOrdersAndMessages(boolean messagesOnly, boolean ordersOnly, boolean fiftyOnly,  String user, CloseableHttpClient httpClient){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSX");

        int totalOrders=Integer.MAX_VALUE;

        ArrayList<Order> orderArrayList = new ArrayList<Order>();
        long startTime=0;
        long elapsedTime=0;
        long startTime2=0;
        long elapsedTime2=0;

        HashMap<String,String> stateHashMap = new HashMap<String, String>();
        stateHashMap.put("AR-A","Salta");
        stateHashMap.put("AR-B","Buenos Aires");
        stateHashMap.put("AR-C","Capital Federal");
        stateHashMap.put("AR-D","San Luis");
        stateHashMap.put("AR-E","Entre Ríos");
        stateHashMap.put("AR-F","La Rioja");
        stateHashMap.put("AR-G","Santiago del Estero");
        stateHashMap.put("AR-H","Chaco");
        stateHashMap.put("AR-J","San Juan");
        stateHashMap.put("AR-K","Catamarca");
        stateHashMap.put("AR-L","La Pampa");
        stateHashMap.put("AR-M","Mendoza");
        stateHashMap.put("AR-N","Misiones");
        stateHashMap.put("AR-P","Formosa");
        stateHashMap.put("AR-Q","Neuquén");
        stateHashMap.put("AR-R","Río Negro");
        stateHashMap.put("AR-S","Santa Fe");
        stateHashMap.put("AR-T","Tucuman");
        stateHashMap.put("AR-U","Chubut");
        stateHashMap.put("AR-V","Tierra del Fuego");
        stateHashMap.put("AR-W","Corrientes");
        stateHashMap.put("AR-X","Córdoba");
        stateHashMap.put("AR-Y","Jujuy");
        stateHashMap.put("AR-Z","Santa Cruz");

        HashMap<String,Integer> usersThatLeftMessage = new HashMap<String,Integer>();
        HashMap<String,String> userFeedbackMessages = new HashMap<String,String>();
        HashMap<String,Integer> usersInOrders = new HashMap<String,Integer>();

        if (!messagesOnly){
            boolean finished=false;
            int offset=0;
            while (!finished) {
                String feedbackUrl = "https://www.mercadolibre.com.ar/perfil/api/feedback/askForFeedback?userIdentifier=nickname%3D" + user + "&rating=all&limit=200&offset=" + offset + "&role=seller";
                JSONObject feedbacksObj = HttpUtils.getJsonObjectWithoutToken(feedbackUrl, httpClient);
                JSONArray feedbacksArray = feedbacksObj.getJSONArray("feedbacks");
                for (Object feedbackObject : feedbacksArray){
                    JSONObject feedbackJSONObject = (JSONObject)feedbackObject;
                    String message = feedbackJSONObject.getString("message");
                    if (message.length()>0){
                        String nickName = feedbackJSONObject.getJSONObject("user").getString("nickname");
                        if (usersThatLeftMessage.containsKey(nickName)){
                            int numberOfMessages = usersThatLeftMessage.get(nickName);
                            numberOfMessages++;
                            usersThatLeftMessage.replace(nickName,numberOfMessages);
                        } else {
                            usersThatLeftMessage.put(nickName,1);
                            userFeedbackMessages.put(nickName,message);
                        }
                    }
                }
                offset+=200;
                if (!feedbacksObj.has("subtitle")){
                    finished=true;
                }
            }
            for (String nickname: usersThatLeftMessage.keySet()){
                if (usersThatLeftMessage.get(nickname)>1){
                    userFeedbackMessages.remove(nickname);
                }
            }
            usersThatLeftMessage.clear();
            usersThatLeftMessage=null;
        }


        for (int ordersOffset=0; ordersOffset<=totalOrders; ordersOffset+=50) {

            String ordersUrl = "https://api.mercadolibre.com/orders/search?seller=" + TokenUtils.getIdCliente(user) + "&sort=date_desc&offset="+ordersOffset;

            JSONObject jsonOrders = HttpUtils.getJsonObjectUsingToken(ordersUrl, httpClient, user);
            if (ordersOffset==0){
                JSONObject pagingObj = jsonOrders.getJSONObject("paging");
                totalOrders=pagingObj.getInt("total");
                String msg="total ventas registradas: "+totalOrders;
                System.out.println(totalOrders);
                Logger.log(msg);
            }
            JSONArray jsonOrdersArray = jsonOrders.getJSONArray("results");
            for (Object orderObject : jsonOrdersArray) {
                startTime=System.currentTimeMillis();
                Order order = new Order();
                order.sellerName =user;
                order.delivered=false;
                order.waitingForWithdrawal=false;
                order.cancelled=false;
                order.refunded=false;
                order.finished=false;
                order.timeoutFulfilled =false;
                order.multiItem=false;
                order.messageArrayList=new ArrayList<Message>();
                order.paymentMethod ="";
                order.paymentInstallments=false;

                order.productVariationText ="N/A";

                order.buyerEmail="N/A";
                order.buyerAddressState="N/A";
                order.buyerAddressCity="N/A";
                order.buyerAddressZip="N/A";
                order.buyerAddressStreet="N/A";

                order.receivedFeedbackRating="";
                order.receivedFeedbackComment="";

                String buyerId=null;

                JSONObject jsonOrder = (JSONObject) orderObject;

                String dateCreatedStr=jsonOrder.getString("date_created");
                dateCreatedStr=dateCreatedStr.replace('T',' ');

                String dateUpdatedStr=jsonOrder.getString("last_updated");
                dateUpdatedStr=dateUpdatedStr.replace('T',' ');



                Date dateCreated=null;
                try {
                    java.util.Date dateCreated2 = dateFormat.parse(dateCreatedStr);
                    order.creationTimestamp =new Timestamp(dateCreated2.getTime());

                    java.util.Date dateUpdated2 = dateFormat.parse(dateUpdatedStr);
                    order.updateTimestamp =new Timestamp(dateUpdated2.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                order.id=jsonOrder.getLong("id");
                order.paymentAmount=""+jsonOrder.getDouble("total_amount");


                order.paymentStatus = jsonOrder.getString("status");
                if (order.paymentStatus.equals("cancelled") ){
                    order.cancelled=true;
                }
                JSONArray mediationsJSONArray = jsonOrder.getJSONArray("mediations");
                if (mediationsJSONArray.length()>0){
                    for (int i=0; i<mediationsJSONArray.length(); i++){
                        JSONObject mediationObject = mediationsJSONArray.getJSONObject(i);
                        String mediationStatus=mediationObject.getString("status");
                        //todo profundizar tema reclamos recien abiertos aca aca
                        if (mediationStatus.equals("return_closed")){
                            order.returned=true;
                        }
                    }
                }

                if (jsonOrder.has("buyer")) {
                    JSONObject buyerObject = jsonOrder.getJSONObject("buyer");
                    order.userNickName =buyerObject.getString("nickname");
                    if (usersInOrders.containsKey(order.userNickName)){
                        int orderCount = usersInOrders.get(order.userNickName);
                        orderCount++;
                        usersInOrders.replace(order.userNickName,orderCount);
                    }else {
                        usersInOrders.put(order.userNickName,1);
                    }
                    order.buyerFirstName=humanNameFormater(buyerObject.getString("first_name"));
                    order.buyerLastName=humanNameFormater(buyerObject.getString("last_name"));
                    if (buyerObject.has("phone")) {
                        JSONObject phoneObj = buyerObject.getJSONObject("phone");
                        if (!phoneObj.isNull("number")) {
                            order.buyerPhone = phoneObj.getString("number");
                        }
                    }
                    JSONObject billingInfoObj = buyerObject.getJSONObject("billing_info");
                    if (!billingInfoObj.isNull("doc_number")) {
                        order.buyerDocNumber = "" + billingInfoObj.getLong("doc_number");
                    }
                    buyerId=""+buyerObject.getInt("id");
                }

                order.fulfilled=false;
                if (jsonOrder.has("fulfilled")){
                    if (!jsonOrder.isNull("fulfilled")){
                        order.fulfilled=jsonOrder.getBoolean("fulfilled");
                    }
                }
                order.finished=order.fulfilled;

                if (jsonOrder.has("permalink")) {
                    order.publicationURL = jsonOrder.getString("permalink");
                }

                JSONArray itemsArray = jsonOrder.getJSONArray("order_items");
                order.multiItem=itemsArray.length()>1;
                JSONObject itemObject = itemsArray.getJSONObject(0);
                order.productQuantity=itemObject.getInt("quantity");
                JSONObject itemObject2 = itemObject.getJSONObject("item");
                order.productId=itemObject2.getString("id");
                order.productCategoryId=itemObject2.getString("category_id");
                order.productTitle=itemObject2.getString("title");
                order.productVariationId=0;//no variation
                if (!itemObject2.isNull("variation_id")) {
                    order.productVariationId = itemObject2.getLong("variation_id");
                }

                JSONArray variationsArray = itemObject2.getJSONArray("variation_attributes");
                if (variationsArray.length()>0){
                    JSONObject variationObject = variationsArray.getJSONObject(0);
                    variationObject = variationsArray.getJSONObject(0);
                    order.productVariationName1 =variationObject.getString("name");
                    order.productVariationValue1 =variationObject.getString("value_name");
                    if (order.productVariationValue1.charAt(1)=='.'){
                        //cocinamos esto de 1.Negro 2.Gris etc
                        order.productVariationValue1 =order.productVariationValue1.substring(2);
                    }

                    if (variationsArray.length()==2){
                        variationObject = variationsArray.getJSONObject(1);
                        order.productVariationName2 =variationObject.getString("name");
                        order.productVariationValue2 =variationObject.getString("value_name");
                        if (order.productVariationValue2.charAt(1)=='.'){
                            //cocinamos esto de 1.Negro 2.Gris etc
                            order.productVariationValue2 =order.productVariationValue2.substring(2);
                        }

                    }

                    order.productVariationText =order.productVariationName1 +" "+order.productVariationValue1;
                    if (order.productVariationName2!=null && !order.productVariationValue2.isEmpty()) {
                        order.productVariationText +=" " + order.productVariationName2 + " " + order.productVariationValue2;
                    }
                }

                String url = "https://api.mercadolibre.com/items/"+order.productId;
                JSONObject publicationJsonObject = HttpUtils.getJsonObjectWithoutToken(url,httpClient);
                order.productPictureURL= getOrderPictureUrl(publicationJsonObject,order.productVariationId);

                JSONArray paymentsArray = jsonOrder.getJSONArray("payments");
                if (paymentsArray.length()>0) {
                    for (int i = 0; i < paymentsArray.length(); i++) {
                        JSONObject paymentObj = paymentsArray.getJSONObject(i);
                        String paymentStatus = paymentObj.getString("status");
                        if (paymentStatus.equals("refunded")) {
                            order.refunded = true;
                            break;
                        }
                    }
                }

                JSONObject orderShippingObj = jsonOrder.getJSONObject("shipping");
                order.shippingType=Order.UNKNOWN;
                if (orderShippingObj.has("id") && !orderShippingObj.isNull("id")){
                    order.shippingId=orderShippingObj.getLong("id");
                   String shippingUrl="https://api.mercadolibre.com/shipments/"+order.shippingId+"?";
                    JSONObject shippingObj = HttpUtils.getJsonObjectUsingToken(shippingUrl, httpClient, user);

                    if (shippingObj!=null) {
                        if (shippingObj.has("tracking_number") && !shippingObj.isNull("tracking_number")) {
                            order.shippingTrackingNumber = shippingObj.getString("tracking_number");
                        }
                        if (shippingObj.has("tracking_method") && !shippingObj.isNull("tracking_method")) {
                            order.shippingCurrier = shippingObj.getString("tracking_method");
                        }


                        if (shippingObj.has("status") && !shippingObj.isNull("status")) {
                            order.shippingStatus = shippingObj.getString("status");
                            if (order.shippingStatus.equals("to_be_agreed")) {
                                order.shippingType = Order.ACORDAR;
                            } else {
                                order.shippingId = shippingObj.getLong("id");
                                if (order.shippingStatus.equals("shipped") || order.shippingStatus.equals("delivered")) {
                                    order.delivered = true;
                                }
                            }
                            if (shippingObj.has("substatus") && !shippingObj.isNull("substatus")) {
                                String subStatus = shippingObj.getString("substatus");
                                if (subStatus.equals("waiting_for_withdrawal")) {
                                    order.waitingForWithdrawal = true;
                                }
                            }
                            if (shippingObj.has("shipping_option")) {
                                JSONObject shippingOptionObj = shippingObj.getJSONObject("shipping_option");
                                String shippingOptionStr = shippingOptionObj.getString("name");
                                if (shippingOptionStr.contains("pido a domicilio")) {//Rapido a domicilio
                                    order.shippingType = Order.FLEX;
                                } else {
                                    if (shippingOptionStr.contains("ormal a domicilio")) { //Normal a domicilio
                                        order.shippingType = Order.CORREO_A_DOMICILIO;
                                    } else {
                                        if (shippingOptionStr.startsWith("Retiro en")) { //Retiro en Correo Argentino
                                            order.shippingType = Order.CORREO_RETIRA;
                                        } else {
                                            order.shippingType = '?';
                                        }
                                    }
                                }
                            }
                            if (shippingObj.has("receiver_address")) {
                                if (!shippingObj.isNull("receiver_address")) {
                                    JSONObject receiverAddressObj = shippingObj.getJSONObject("receiver_address");
                                    order.shippingReceiverName=receiverAddressObj.getString("receiver_name");
                                    order.buyerAddressState = receiverAddressObj.getJSONObject("state").getString("name");
                                    order.buyerAddressCity = receiverAddressObj.getJSONObject("city").getString("name");
                                    order.buyerAddressZip = receiverAddressObj.getString("zip_code");
                                    order.buyerAddressStreet = receiverAddressObj.getString("address_line");
                                    if (receiverAddressObj.has("comment") && !receiverAddressObj.isNull("comment")) {
                                        order.buyerAddressComments = receiverAddressObj.getString("comment");
                                    }
                                    if (order.buyerPhone == null) {
                                        if (receiverAddressObj.has("receiver_phone")) {
                                            if (!receiverAddressObj.isNull("receiver_phone")) {
                                                order.buyerPhone = receiverAddressObj.getString("receiver_phone");
                                            }
                                        }
                                    }
                                }
                                order.shippingAddressLine1 = humanNameFormater(order.buyerAddressStreet);
                                order.shippingAddressLine2 = "CP " + order.buyerAddressZip + " - " + humanNameFormater(order.buyerAddressCity + ", " + order.buyerAddressState);
                                order.shippingAddressLine3 = order.buyerAddressComments;
                            }
                        }
                    }
                }

                String billingInfoUrl="https://api.mercadolibre.com/orders/"+order.id+"/billing_info?";
                JSONObject billingInfoObject=HttpUtils.getJsonObjectUsingToken(billingInfoUrl,httpClient,user);
                if (billingInfoObject!=null) {
                    JSONObject billingInfoObject2 = billingInfoObject.getJSONObject("billing_info");
                    String billingDocType = "";
                    String billingDocNUmber = "";
                    String billingFirstName = "";
                    String billingLastName = "";
                    String billingStreetName = "";
                    String billingStreetNumber = "";
                    String billingZipCode = "";
                    String billingCity = "";
                    String billingState = "";
                    String billingComments = "";
                    if (billingInfoObject2 != null) {
                        billingDocType = billingInfoObject2.getString("doc_type");
                        billingDocNUmber = billingInfoObject2.getString("doc_number");
                        JSONArray additionalInfo = billingInfoObject2.getJSONArray("additional_info");
                        if (additionalInfo != null) {
                            for (int i = 0; i < additionalInfo.length(); i++) {
                                JSONObject infoObject = additionalInfo.getJSONObject(i);
                                if (infoObject != null) {
                                    String type = infoObject.getString("type");
                                    String value = infoObject.getString("value");
                                    if (type != null && !type.isEmpty()) {
                                        if (value != null && !value.isEmpty()) {

                                            if (type.equals("FIRST_NAME")) {
                                                billingFirstName = value;
                                            } else if (type.equals("LAST_NAME")) {
                                                billingLastName = value;
                                            } else if (type.equals("STREET_NAME")) {
                                                billingStreetName = value;
                                            } else if (type.equals("STREET_NUMBER")) {
                                                billingStreetNumber = value;
                                            } else if (type.equals("ZIP_CODE")) {
                                                billingZipCode = value;
                                            } else if (type.equals("CITY_NAME")) {
                                                billingCity = value;
                                            } else if (type.equals("STATE_NAME")) {
                                                billingState = value;
                                            } else if (type.equals("COMMENT")) {
                                                billingComments = value;
                                            }

                                        }
                                    }
                                }
                            }
                            order.billingDniCuit = billingDocType + " " + billingDocNUmber;
                            order.billingName = humanNameFormater(billingFirstName + " " + billingLastName);
                            order.billingAddressLine1 = humanNameFormater(billingStreetName) + " " + billingStreetNumber;
                            order.billingAddressLine2 = "CP " + billingZipCode + " - " + humanNameFormater(billingCity + ", " + billingState);
                            order.billingAddressLine3 = billingComments;
                        }
                    }
                }

                order.orderStatus=Order.VENDIDO;
                if (order.fulfilled || order.delivered){
                    order.orderStatus=Order.ENTREGADO;
                }else {
                    if (order.cancelled || order.returned){
                        order.orderStatus=Order.CANCELADO;
                    }
                    //todo RECLAMO abierto
                }

                if (!messagesOnly) {
                    String buyerUrl = "https://api.mercadolibre.com/users/" + buyerId;
                    JSONObject buyerObj = HttpUtils.getJsonObjectWithoutToken(buyerUrl, httpClient);
                    JSONObject addressObj = buyerObj.getJSONObject("address");

                    String stateId = null;
                    if (!addressObj.isNull("state")) {
                        stateId = addressObj.getString("state");
                        if (!stateHashMap.containsKey(stateId)) {
                            String stateUrl = "https://api.mercadolibre.com/classified_locations/states/" + stateId;
                            JSONObject stateObj = HttpUtils.getJsonObjectWithoutToken(stateUrl, httpClient);
                            String stateName = stateObj.getString("name");
                            stateHashMap.put(stateId, stateName);
                        }
                        order.userState = stateHashMap.get(stateId);
                    }
                    if (!addressObj.isNull("city")) {
                        order.userCity = addressObj.getString("city");
                    }
                }

                //todo esto sirve?
                if ((order.buyerAddressState == null || order.buyerAddressCity == null)) { //todo es necesario?
                    order.buyerAddressState=order.userState;
                    order.buyerAddressCity=order.userCity;
                }


                JSONObject feedbackObj = jsonOrder.getJSONObject("feedback");
                if (!feedbackObj.isNull("sale")) {
                    JSONObject saleObject = feedbackObj.getJSONObject("sale");
                    if (saleObject.getBoolean("fulfilled")) {
                        order.delivered = true;
                    }
                }
                if (!feedbackObj.isNull("purchase")) {
                    JSONObject purchaseObject = feedbackObj.getJSONObject("purchase");
                    if (purchaseObject.has("rating")) {
                        if (!purchaseObject.isNull("rating")) {
                            order.receivedFeedbackRating = purchaseObject.getString("rating");
                        }
                    }
                    if (purchaseObject.has("message")){
                        if (!purchaseObject.isNull("message")) {
                            order.receivedFeedbackComment = purchaseObject.getString("message");
                        }
                    }
                }


                for (Object paymentObj: paymentsArray) {
                    JSONObject paymentJSONObject = (JSONObject)paymentObj;
                    order.paymentMethod += paymentJSONObject.getString("payment_method_id")+" / ";
                    if (paymentJSONObject.getInt("installments")>1){
                        order.paymentInstallments=true;
                    }
                }
                if (order.paymentMethod.endsWith(" / ")) {
                    order.paymentMethod = order.paymentMethod.substring(0, order.paymentMethod.length() - 3);
                }

                //messages
                order.packId = order.id;
                if (!jsonOrder.isNull("pack_id")) {
                    order.packId = jsonOrder.getLong("pack_id");
                }
                if (!ordersOnly) {
                    order.messageArrayList=getAllMessagesOnOrder(order.packId,user,httpClient);
                    if (order.messageArrayList.size()>0){
                        order.buyerEmail=order.messageArrayList.get(0).buyerEmail;
                    }
                }
                orderArrayList.add(order);

                System.out.println(order.creationTimestamp +" "+ order.orderStatus+" "+ order.userNickName + " "+order.buyerAddressState+","+order.buyerAddressCity);
                elapsedTime=System.currentTimeMillis()-startTime;
                if (elapsedTime>=tooMuchTime){
                    httpClient=HttpUtils.buildHttpClient();
                }
            }
            if (fiftyOnly){ //la cortamos aca
                String msg="cortamos en 50 ordenes ";
                Logger.log(msg);
                break;
            }

        }

        //adding feedback messages
        for (Order order:orderArrayList) {
            int numberOfOrders = usersInOrders.get(order.userNickName);
            if (numberOfOrders == 1 && userFeedbackMessages.containsKey(order.userNickName)) {
                order.receivedFeedbackComment = userFeedbackMessages.get(order.userNickName);
            }
        }

        return orderArrayList;
    }

    public static String getOrderPictureUrl(JSONObject publicationJsonObject, long onlineOrderVariationId){
        String pictureId=null;
        String url=null;
        if (publicationJsonObject!=null) {
            if (onlineOrderVariationId != 0) { //si eligió una variante tenemos que poner esa foto, else foto general
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
                JSONArray picturesArray = publicationJsonObject.getJSONArray("pictures");

                for (int i = 0; i < picturesArray.length(); i++) {
                    JSONObject pictureObject = picturesArray.getJSONObject(i);
                    String id = pictureObject.getString("id");
                    if (id.equals(pictureId)) {
                        url = pictureObject.getString("url");
                        break;
                    }
                }
                if (url != null && url.endsWith("-O.jpg")) {//reemplazando por el thumbnail
                    url = url.substring(0, url.indexOf("-O.jpg")) + "-I.jpg";
                }
            } else {
                url = publicationJsonObject.getString("thumbnail");
            }
        }else {
            boolean b=false;
        }
        return url;

    }

    public static ArrayList<Message> getAllMessagesOnOrder(long packId, String user, CloseableHttpClient httpClient){
        ArrayList<Message> result = new ArrayList<Message>();
        int totalMessages = Integer.MAX_VALUE;
        for (int messagesOffset = 0; messagesOffset <= totalMessages; messagesOffset += 10) {
            String messagesUrl = "https://api.mercadolibre.com/messages/packs/" + packId + "/sellers/" + TokenUtils.getIdCliente(user) + "?offset=" + messagesOffset;
            long startTime2 = System.currentTimeMillis();
            JSONObject jsonMessages = HttpUtils.getJsonObjectUsingToken(messagesUrl, httpClient, user);
            long elapsedTime2 = System.currentTimeMillis() - startTime2;
            if (elapsedTime2 >= tooMuchTime) {
                httpClient = HttpUtils.buildHttpClient();
            }
            if (messagesOffset == 0) {
                JSONObject pagingObj = jsonMessages.getJSONObject("paging");
                totalMessages = pagingObj.getInt("total");
            }
            JSONArray jsonMessagesArray = jsonMessages.getJSONArray("messages");
            for (Object messageObject : jsonMessagesArray) {
                JSONObject messageJsonObject = (JSONObject) messageObject;
                Message message = new Message();
                message.id = messageJsonObject.getString("id");
                message.text = messageJsonObject.getString("text");
                String fromId = messageJsonObject.getJSONObject("from").getString("user_id");
                if (fromId.equals(TokenUtils.getIdCliente(user))) {
                    message.direction = 'E';
                    message.buyerEmail = messageJsonObject.getJSONObject("to").getString("email");
                } else {
                    message.direction = 'R';
                    message.buyerEmail = messageJsonObject.getJSONObject("from").getString("email");
                }
                result.add(message);
            }
        }
        return result;
    }


    public static void printOrder(Order order){

        String shippingTypeDesc="N/A";
        if (order.shippingType==Order.ACORDAR){
            shippingTypeDesc="Acordar";
        }
        if (order.shippingType==Order.CORREO_A_DOMICILIO){
            shippingTypeDesc="Correo a Domicilio";
        }
        if (order.shippingType==Order.CORREO_RETIRA){
            shippingTypeDesc="Retira en Correo";
        }
        if (order.shippingType==Order.FLEX){
            shippingTypeDesc="Flex";
        }
        boolean paid=false;
        if (order.paymentStatus.equals("paid")){
            paid=true;
        }

        String orderRecord=
                "\""+order.id+"\",\""+order.creationTimestamp +"\",\""+order.updateTimestamp +"\",\""+order.orderStatus+"\",\""+paid+"\",\""+order.shippingStatus+"\",\""+shippingTypeDesc+"\",\""+
                        order.delivered+"\",\""+order.waitingForWithdrawal+"\",\""+order.cancelled+"\",\""+order.refunded+"\",\""+order.timeoutFulfilled+"\",\""+order.finished+"\",\""+order.multiItem+"\",\""+
                        order.userNickName +"\",\""+order.buyerFirstName+"\",\""+order.buyerLastName+"\",\""+order.buyerEmail+"\",\""+order.buyerPhone+"\",\""+order.buyerDocNumber+"\",\""+
                        order.buyerAddressState+"\",\""+order.buyerAddressCity+"\",\""+order.buyerAddressZip+"\",\""+order.buyerAddressStreet+"\",\""+
                        order.receivedFeedbackRating+"\",\""+order.receivedFeedbackComment+"\",\""+
                        order.paymentMethod+"\",\""+order.paymentAmount+"\",\""+order.paymentInstallments+"\",\""+
                        order.productId+"\",\""+order.productTitle+"\",\""+order.productVariationText +"\",\""+order.productCategoryId+"\",\"";
        for (int i = order.messageArrayList.size(); i-- > 0; ) {
            Message message = order.messageArrayList.get(i);
            orderRecord+=message.direction+":"+message.text+"\n";
        }
        if (order.messageArrayList.size()>0) {
            orderRecord = orderRecord.substring(0, orderRecord.length() - 1);
        }
        orderRecord+="\",";
        System.out.println(orderRecord);
        Logger.log(orderRecord);
    }

    private static void printOrderHeading(){


        String orderRecord=
                "\"Order Id\",\"Creacion\",\"Actualizacion\",\"Estado\",\"Pagado\",\"Estado Envio\",\"Tipo de Envio\",\""+
                        "Entregado\",\"Esperando en el Correo\",\"Cancelado\",\"Reembolsado\",\"Completada por Vencimiento\",\"Completada\",\"Varios Items\",\""+
                        "Apodo\",\"Nombre\",\"Apellido\",\"Email\",\"Telefono\",\"Dni\",\""+
                        "Provincia\",\"Ciudad\",\"Codigo Postal\",\"Calle\",\""+
                        "Opinion\",\"Comentario de Opinion\",\""+
                        "Medio de Pago\",\"Importe\",\"Cuotas\",\""+
                        "Codigo Producto\",\"Titulo de Publicacion\",\"Variante de Producto\",\"Id Categoria\",\"Mensajes\";";
        System.out.println(orderRecord);
        //Logger.log(orderRecord);
    }

    public static void main(String args[]){
        CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
        ArrayList<Order> orderArrayList = requestOrdersAndMessages(false,false, false, "ACACIAYLENGA",httpClient);
        for (Order order:orderArrayList){
            printOrder(order);
        }
    }

    private static Date sixDaysBefore(){
        Calendar calendar = Calendar.getInstance();
        long oneDayInMiliseconds=86400000l;
        Date resultDate = new Date(calendar.getTimeInMillis()-(oneDayInMiliseconds*6));
        return resultDate;
    }

    private static String humanNameFormater(String inputString)
    {
        String result="";

        try {
            if (StringUtils.isBlank(inputString)) {
                return "";
            }

            if (StringUtils.length(inputString) == 1) {
                return inputString.toUpperCase();
            }


            String[] strings = inputString.split(" ");

            for (String name:strings) {
                name=name.trim();
                if (name.length()==0){
                    continue;
                }
                char[] nameCharArray=name.toCharArray();
                for (int i = 0; i < nameCharArray.length; i++) {
                    if (i==0){
                        nameCharArray[0]=Character.toUpperCase(nameCharArray[0]);
                    }else {
                        nameCharArray[i]=Character.toLowerCase(nameCharArray[i]);
                    }
                }
                name=new String(nameCharArray);
                result+=name+" ";
            }
            result=result.trim();

        }catch (Exception x){
            boolean b=false;
        }
        //return StringUtils.trim(resultPlaceHolder.toString());
        return result;
    }
}


