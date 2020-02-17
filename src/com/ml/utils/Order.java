package com.ml.utils;


import java.sql.Timestamp;
import java.util.ArrayList;

public class Order {
    public long id;
    public Timestamp creationTimestamp;
    public Timestamp updateTimestamp;
    public String paymentStatus;
    public String shippingStatus;
    public String user;
    public char orderStatus;
    public char shippingType;
    public boolean delivered;
    public boolean waitingForWithdrawal;
    public boolean cancelled;
    public boolean refunded;
    public boolean timeoutFulfilled;
    public boolean finished;
    public boolean fulfilled;
    public boolean multiItem;

    public String buyerNickName;
    public String buyerFirstName;
    public String buyerLastName;
    public String buyerEmail;
    public String buyerPhone;
    public String buyerDocNumber;

    public String buyerAddressState;
    public String buyerAddressCity;
    public String buyerAddressZip;
    public String buyerAddressStreet;

    public String receivedFeedbackRating;
    public String receivedFeedbackComment;

    public String paymentMethod;
    public String paymentAmount;
    public boolean paymentInstallments;

    public String productId;
    public String productTitle;
    public String productVariation;
    public String productVariationName;
    public String productVariationValue;

    public ArrayList<Message> messageArrayList;

    //shippingType
    public static final char ACORDAR='A';
    public static final char CORREO_A_DOMICILIO='D';
    public static final char CORREO_RETIRA='R';
    public static final char FLEX='F';

    //orderStatus
    public static final char VENDIDO='V';
    public static final char ENTREGADO='E';
    public static final char RECLAMO='R';
    public static final char CANCELADO='C';

}
