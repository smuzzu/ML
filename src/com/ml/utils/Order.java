package com.ml.utils;


import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Order implements Comparable<Order> {
    public long id;
    public Timestamp creationTimestamp;
    public Timestamp updateTimestamp;
    public String paymentStatus;

    public long   shippingId;
    public String shippingReceiverName;
    public String shippingStatus;
    public String shippingSubStatus;
    public String shippingOptionNameDescription;
    public String shippingTrackingNumber;
    public String shippingCurrier;
    public String shippingLogisticType;
    public String shippingAddressLine1;
    public String shippingAddressLine2;
    public String shippingAddressLine3;

    public String  sellerName;
    public int     sellerId;
    public char    orderStatus;
    public char    shippingType;
    public boolean delivered;
    public boolean readyForSending;
    public boolean waitingForWithdrawal;
    public boolean cancelled;
    public boolean returned;
    public boolean refunded;
    public boolean timeoutFulfilled;
    public boolean finished;
    public boolean fulfilled;
    public boolean multiItem;
    public boolean pending;  //abierta

    public String userNickName;
    public String userCity;
    public String userState;

    public String buyerFirstName;
    public String buyerLastName;
    public String buyerBusinessName;
    public String buyerTaxerPayerType;
    public String buyerEmail;
    public String buyerPhone;
    public String buyerDocTypeAndNumber;
    public long   buyerCustId;

    public String billingName;
    public String billingAddressLine1;
    public String billingAddressLine2;
    public String billingAddressLine3;

    public String buyerAddressState;
    public String buyerAddressCity;
    public String buyerAddressZip;
    public String buyerAddressStreet;
    public String buyerAddressComments;

    public String receivedFeedbackRating;
    public String receivedFeedbackComment;
    public int    itemReviewStars;
    public String itemReviewComment;

    public String paymentMethod;
    public Double paymentAmount;
    public boolean paymentInstallments;

    public String productId;
    public String productCategoryId;
    public String productTitle;
    public int    productQuantity;
    public String productPictureURL;
    public String productPictureThumbnailURL;
    public String productKeyAttributes;
    public long   productVariationId;
    public String productVariationText;
    public String productVariationName1;
    public String productVariationValue1;
    public String productVariationName2;
    public String productVariationValue2;
    public int    productManufacturingDays;
    public String publicationURL;


    public char mailSent; //Y,N,B=buffered (venta pra el dia siguiente
    public boolean chatSent;
    public boolean superaLimiteAfip;
    public boolean buffered;

    public long packId; //for messages
    public ArrayList<Message> previousQuestionsOnItemArrayList;
    public ArrayList<Message> previousQuestionsOtherItemsArrayList;
    public ArrayList<Message> messageArrayList;


    //shippingType
    public static final char ACORDAR='A';
    public static final char PERSONALIZADO='P';
//    public static final char CORREO_A_DOMICILIO='D';
//    public static final char CORREO_RETIRA='R';
    public static final char CORREO='C';
    public static final char FLEX='F';
    public static final char FULL='U';
    public static final char UNKNOWN='?';

    //orderStatus
    public static final char VENDIDO='V';
    public static final char ENTREGADO='E';
    public static final char RECLAMO='R';
    public static final char CANCELADO='C';

    //orderStatus
    public static final char MAIL_ENVIADO='Y';
    public static final char MAIL_NO_ENVIADO='N';
    public static final char MAIL_BUFFERED='B';


    private static String[] productKeyAttributesList = new String[]
    {
        "WIDTH",
        "LENGTH",
        "HEIGHT",
        "DEPTH"
    };

    public static boolean isProductKeyAttribute(String idAttribute){
        boolean result=false;
        for (String attribute:productKeyAttributesList){
            if (attribute.equals(idAttribute)){
                result=true;
                break;
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Order orderParam = (Order) obj;
        return id == orderParam.id;
    }

    public String getPrintableCSVHeader(){
        String result = "";
        Field[] allFields = this.getClass().getDeclaredFields();
        for (Field field : allFields){
            String name=field.getName();
            if (name.equals("ACORDAR")){
                break; //aca empiezan los metadatos
            }
            result+= name+",";
        }
        result=result.substring(0,result.length()-1);
        return result;
    }

    public String getPrintableCSVValues(){
        String result = "";
        Field[] allFields = this.getClass().getDeclaredFields();
        for (Field field : allFields){
            String name=field.getName();
            if (name.equals("ACORDAR")){
                break; //aca empiezan los metadatos
            }
            Object valueObj =null;
            try {
                valueObj =field.get(this);

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            String value = "";
            if (name.equals("messageArrayList") ||
                name.equals("previousQuestionsOnItemArrayList") ||
                name.equals("previousQuestionsOtherItemsArrayList")){
                ArrayList<Message> messageArrayList = (ArrayList) valueObj;
                value = "\"";
                for (int i=0; i<messageArrayList.size(); i++){
                    value+=messageArrayList.get(i).toStringForReport();
                    if (i<messageArrayList.size()){
                        value+="\n";
                    }
                }
                value+="\"";
            }else {
                if (valueObj != null) {
                    value = valueObj.toString();
                    value=value.replaceAll("\\p{C}", "");
                    value=value.replaceAll("\"", "'");
                }
            }
            value=value.replaceAll(",", " ");
            result+=value+",";

        }
        result=result.substring(0,result.length()-1);
        return result;
    }

    @Override
    public int compareTo(Order other) {
        return Long.compare(this.id,other.id);
    }



}
