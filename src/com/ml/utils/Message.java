package com.ml.utils;

import java.sql.Timestamp;

public class Message implements Comparable<Message> {
    public String id;
    public Timestamp creationTimestamp;
    public String text;
    public char   direction; // E=Enviado   R=Recibido
    public String buyerEmail;
    public long   customerId;
    public String productId;
    public boolean deleted;

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Message messageParam = (Message) obj;
        return id.equals(messageParam.id) && direction==messageParam.direction;
    }


    public String toStringForReport(){
        return this.direction+":"+this.text;
    }


    @Override
    public int compareTo(Message other) {
        if (other==null || this.id==null || this.id.isEmpty()){
            return 0;
        }
        return this.id.compareTo(other.id);
    }
}
