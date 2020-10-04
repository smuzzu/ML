package com.ml.utils;

public class Item {

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
        public int page;
        public int totalSold;
        public String sellerName;
        public long sellerId;

        public Item () {
            this.advertised=false;
            this.ranking=10000;
            this.totalSold=-1;
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
