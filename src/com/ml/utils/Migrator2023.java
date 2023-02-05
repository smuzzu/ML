package com.ml.utils;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.HashMap;




public class Migrator2023 extends Thread{
    static String DATABASE="ML2";
    static String currentDateStr = "02-02-2023";

    static PreparedStatement ps;
    static HashMap<String, Item> apiProductMap=new HashMap<String, Item>();
    static HashMap<String, Item> databaseProductMap=new HashMap<String, Item>();
    static long globalCount=0;

    String productId=null;

    public Migrator2023(String productId){
        this.productId=productId;
    }

    static int MAX_THREADS = 20;//todo corregir 35
    static final boolean DEBUG = false;

    static Date globalDate = null;


    private synchronized static Date getGlobalDate() {
        return globalDate;
    }

    private static long getGlobalCount() {
        globalCount++;
        return globalCount;
    }

        //migradores
    public static void main(String[] args) {


        Date runDate = Counters.parseDate(currentDateStr);
        globalDate = runDate;

        databaseProductMap =
                DatabaseHelper.fetchItemsBeyondRadar(DATABASE, new HashMap<String, Item>(), runDate);

        String itemsIds = "";
        int count = 0;

        long totalSoldCountBorrar=0;

        for (Item databaseItem : databaseProductMap.values()) {
            itemsIds += databaseItem.id + ",";
            count++;

            if (count == 20) {
                itemsIds = itemsIds.substring(0, itemsIds.length() - 1);
                String itemsUrl = "https://api.mercadolibre.com/items?ids=" + itemsIds;
                JSONObject jsonObject = HttpUtils.getJsonObjectWithoutToken(itemsUrl, HttpUtils.buildHttpClient(), true);
                if (jsonObject != null) {
                    JSONArray jsonArray = jsonObject.getJSONArray("elArray");
                    for (int j = 0; j < jsonArray.length(); j++) {
                        JSONObject itemObject2 = jsonArray.getJSONObject(j);
                        int code = itemObject2.getInt("code");
                        JSONObject productObj = itemObject2.getJSONObject("body");

                        if (code == 200 && productObj != null) {

                            Item apiItem = null;
                            if (productObj.has("id") && !productObj.isNull("id")) {
                                String id = productObj.getString("id");
                                Item dbitem = databaseProductMap.get(id);
                                apiItem = (Item) copy(dbitem);
                            } else {
                                String msg1 = "FATAL !!!!!!!!!!!!! no se recuperó ID " + databaseItem.id;
                                System.out.println(msg1);
                                Logger.log(msg1);
                                continue;
                            }

                            if (productObj.has("status") && !productObj.isNull("status")) {
                                apiItem.status = productObj.getString("status");
                            }
                            if (productObj.has("last_updated") && !productObj.isNull("last_updated")) {
                                String lastUpdatedStr = productObj.getString("last_updated");
                                if (lastUpdatedStr != null && lastUpdatedStr.length() > 9) {
                                    lastUpdatedStr = lastUpdatedStr.substring(0, 10);
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                    try {
                                        java.util.Date dt = sdf.parse(lastUpdatedStr);
                                        apiItem.lastUpdate = new Date(dt.getTime());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        System.out.println("Error parsing date " + lastUpdatedStr);
                                    }
                                }
                            }

                            if (productObj.has("sold_quantity") && !productObj.isNull("sold_quantity")) {
                                apiItem.totalSold = productObj.getInt("sold_quantity");
                            }

                            if (productObj.has("permalink") && !productObj.isNull("permalink")) {
                                apiItem.permalink = productObj.getString("permalink");
                            }

                            if (productObj.has("seller") && !productObj.isNull("seller")) {
                                JSONObject sellerObject = productObj.getJSONObject("seller");
                                if (sellerObject.has("nickname") && !sellerObject.isNull("nickname")) {
                                    apiItem.sellerName = sellerObject.getString("nickname");
                                }
                                apiItem.sellerId = sellerObject.getInt("id");
                            } else {
                                if (productObj.has("seller_id") && !productObj.isNull("seller_id")) {
                                    apiItem.sellerId = productObj.getInt("seller_id");
                                }
                            }
                            apiProductMap.put(apiItem.id, apiItem);
                        }
                    }
                }
                count = 0;
                itemsIds = "";
            }
        }

        Connection connection = DatabaseHelper.getAddRemoveIdConnection(DATABASE);
        try {
            ps = connection.prepareStatement("update productos set idproveedor=?, totalvendidos=?, stock=?," +
                    " lastquestion=? where id=?");
        } catch (SQLException e) {
            e.printStackTrace();
        }


        ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
        ArrayList<Thread> removeList = new ArrayList<Thread>();
        int localCount=0;

        for (String productId : apiProductMap.keySet()) {

            localCount++;
            if (localCount>10000){
                localCount=0;
                System.gc();
            }

            Migrator2023 migrator2023 = new Migrator2023(productId);
            threadArrayList.add(migrator2023);
            migrator2023.start();

            while (threadArrayList.size() >= MAX_THREADS) {
                removeList.clear();
                for (Thread t : threadArrayList) {
                    if (!t.isAlive()) {
                        removeList.add(t);
                    }
                }
                for (Thread t : removeList) {
                    threadArrayList.remove(t);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Logger.log(e);
                }
            }
        }

        for (Thread thread : threadArrayList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    public void run(){
        long t1=System.nanoTime();
        long runnerCount=getGlobalCount();
        Item item = apiProductMap.get(productId);
        String formattedId = HTMLParseUtils.getFormatedId(productId);
        boolean disable=false;
        if (item.status.equals("under_review")) {//no se procesa
            long oneDayinMiliseconds = 86400000;
            Date allowedDate = new Date(getGlobalDate().getTime() - oneDayinMiliseconds * 30);
            if (item.lastUpdate.before(allowedDate)) {
                disable=true;
            }
        }
        if (item.status.equals("paused")) {//no se procesa
            long oneDayinMiliseconds = 86400000;
            Date allowedDate = new Date(getGlobalDate().getTime() - oneDayinMiliseconds * 60);
            if (item.lastUpdate.before(allowedDate)) {
                disable=true;
            }
        }
        if (item.status.equals("inactive") || item.status.equals("closed")) {
            disable=true;
        }
        if (disable) {
            long t2=System.nanoTime();
            long t3=(t2-t1)/1000000;
            String msg = runnerCount + " Deshabilitando item " + item.status + " " + item.permalink+" "+t3;
            System.out.println(msg);
            Logger.log(msg);
            DatabaseHelper.disableProduct(formattedId, DATABASE);
            return;
        }
        //desabilitado hace poco
        if (item.status.equals("under_review") || item.status.equals("paused")){
            long t2=System.nanoTime();
            long t3=(t2-t1)/1000000;
            String msg = runnerCount + " No se procesara item " + item.status + " " + item.permalink+" "+t3;
            System.out.println(msg);
            Logger.log(msg);
            return;
        }

        if (item.permalink != null) {
            String page = HttpUtils.getHTMLStringFromPage(item.permalink, HttpUtils.buildHttpClient(), false, false, null);
            if (HttpUtils.isOK(page)) {
                int totalSold = HTMLParseUtils.getTotalSold(page, item.permalink);
                if (totalSold < 1) {
                    page = HttpUtils.getHTMLStringFromPage(item.permalink, HttpUtils.buildHttpClient(), false, false, null);
                    totalSold = HTMLParseUtils.getTotalSold(page, item.permalink);
                }
                int stock = ProductPageProcessor.getFetchStock("runerID", page, item.permalink, DEBUG);
                String lastQuestion = HTMLParseUtils.getLastQuestion(page);
                try {
                    ps.setLong(1, item.sellerId);
                    ps.setInt(2, totalSold);
                    ps.setInt(3, stock);
                    ps.setString(4, lastQuestion);
                    ps.setString(5, formattedId);


                    int updatedRecords = ps.executeUpdate();

                    if (updatedRecords != 1) {
                        Logger.log("Error actualizando producto " + productId + " " + item.permalink);
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
                long t2=System.nanoTime();
                long t3=(t2-t1)/1000000;
                String msg = runnerCount + " actualizando " + item.permalink+" "+t3;
                System.out.println(msg);
                Logger.log(msg);

            }
        }
    }


    private static final Object ERROR = new Object();

    /**
     * Returns a copy of the object, or null if the object cannot
     * be serialized.
     */
    public static Object copy(Object orig) {
        Object obj = null;
        try {
            // Make a connected pair of piped streams
            PipedInputStream in = new PipedInputStream();
            PipedOutputStream pos = null;
            try {
                pos = new PipedOutputStream(in);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Make a deserializer thread (see inner class below)
            Deserializer des = new Deserializer(in);

            // Write the object to the pipe
            ObjectOutputStream out = new ObjectOutputStream(pos);
            out.writeObject(orig);

            // Wait for the object to be deserialized
            obj = des.getDeserializedObject();

            // See if something went wrong
            if (obj == ERROR)
                obj = null;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return obj;
    }

    /**
     * Thread subclass that handles deserializing from a PipedInputStream.
     */
    private static class Deserializer extends Thread {
        /**
         * Object that we are deserializing
         */
        private Object obj = null;

        /**
         * Lock that we block on while deserialization is happening
         */
        private Object lock = null;

        /**
         * InputStream that the object is deserialized from.
         */
        private PipedInputStream in = null;

        public Deserializer(PipedInputStream pin) throws IOException {
            lock = new Object();
            this.in = pin;
            start();
        }

        public void run() {
            Object o = null;
            try {
                ObjectInputStream oin = new ObjectInputStream(in);
                o = oin.readObject();
            } catch (IOException e) {
                // This should never happen. If it does we make sure
                // that a the object is set to a flag that indicates
                // deserialization was not possible.
                e.printStackTrace();
            } catch (ClassNotFoundException cnfe) {
                // Same here...
                cnfe.printStackTrace();
            }

            synchronized (lock) {
                if (o == null)
                    obj = ERROR;
                else
                    obj = o;
                lock.notifyAll();
            }
        }

        /**
         * Returns the deserialized object. This method will block until
         * the object is actually available.
         */
        public Object getDeserializedObject() {
            // Wait for the object to show up
            try {
                synchronized (lock) {
                    while (obj == null) {
                        lock.wait();
                    }
                }
            } catch (InterruptedException ie) {
                // If we are interrupted we just return null
            }
            return obj;
        }
    }
}
