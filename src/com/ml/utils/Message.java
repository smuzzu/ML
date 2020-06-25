package com.ml.utils;

public class Message {
    public String id;
    public String text;
    public char direction; // E=Enviado   R=Recibido
    public String buyerEmail;

    public String toStringForReport(){
        return this.direction+":"+this.text;
    }
}
