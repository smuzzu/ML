package com.ml.utils;

public class Message {
    public String id;
    public String text;
    public char   direction; // E=Enviado   R=Recibido
    public String buyerEmail;
    public long   customerId;
    public String productId;

    public String toStringForReport(){
        return this.direction+":"+this.text;
    }
}
