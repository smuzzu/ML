package com.ml.utils;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;

public class Item implements Serializable {

        public String id;
        public String title;
        public String permalink;
        public double price;
        public int discount;
        public boolean advertised;
        public boolean officialStore;
        public int shipping;
        public boolean premium;
        public int ranking;
        public int totalSold;
        public int stock;
        public int reviews;
        public double stars;
        public String sellerName;
        public long sellerId;

        public boolean catalog;
        public Date lastUpdate;
        public int newQuestions;
        public String lastQuestion;
        public ArrayList<String> variations;

        public String status; //para migrador2023 todo sacar

        public Item () {
            this.advertised=false;
            this.ranking=10000;
            this.totalSold=-1;
            this.stock=-1;
            this.price=-1.0;
            this.discount=-1;
            this.sellerId=-1L;
        }

        public boolean equals(Object obj) {
            if (this.id == null || this.id.isEmpty()) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Item)) {
                return false;
            }
            Item otherItem = (Item) obj;
            if (otherItem.id == null || otherItem.id.isEmpty()) {
                return false;
            }
            return this.id.equals(otherItem.id);
        }


    }
