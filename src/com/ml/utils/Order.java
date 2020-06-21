package com.ml.utils;


import java.sql.Timestamp;
import java.util.ArrayList;

public class Order {
    public long id;
    public Timestamp creationTimestamp;
    public Timestamp updateTimestamp;
    public String paymentStatus;

    public long   shippingId;
    public String shippingReceiverName;
    public String shippingStatus;
    public String shippingTrackingNumber;
    public String shippingCurrier;
    public String shippingAddressLine1;
    public String shippingAddressLine2;
    public String shippingAddressLine3;

    public String  sellerName;
    public int     sellerId;
    public char    orderStatus;
    public char    shippingType;
    public boolean delivered;
    public boolean waitingForWithdrawal;
    public boolean cancelled;
    public boolean returned;
    public boolean refunded;
    public boolean timeoutFulfilled;
    public boolean finished;
    public boolean fulfilled;
    public boolean multiItem;

    public String userNickName;
    public String userCity;
    public String userState;

    public String buyerFirstName;
    public String buyerLastName;
    public String buyerEmail;
    public String buyerPhone;
    public String buyerDocNumber;

    public String billingDniCuit;
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

    public String paymentMethod;
    public String paymentAmount;
    public boolean paymentInstallments;

    public String productId;
    public String productCategoryId;
    public String productTitle;
    public int    productQuantity;
    public String productPictureURL;
    public long   productVariationId;
    public String productVariationText;
    public String productVariationName1;
    public String productVariationValue1;
    public String productVariationName2;
    public String productVariationValue2;
    public String publicationURL;


    public boolean notified;

    public ArrayList<Message> messageArrayList;
    public long packId; //for messages

    //shippingType
    public static final char ACORDAR='A';
    public static final char CORREO_A_DOMICILIO='D';
    public static final char CORREO_RETIRA='R';
    public static final char FLEX='F';
    public static final char UNKNOWN=' ';

    //orderStatus
    public static final char VENDIDO='V';
    public static final char ENTREGADO='E';
    public static final char RECLAMO='R';
    public static final char CANCELADO='C';

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

}
