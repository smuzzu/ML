package com.ml;

import com.ml.utils.HTMLParseUtils;
import com.ml.utils.HttpUtils;
import org.apache.http.impl.client.CloseableHttpClient;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Calendar;
import java.util.Properties;


public class MLFixSales {

    static CloseableHttpClient globalClient = null;
    static Connection globalSelectConnection = null;
    static Connection globalUpadteConnection = null;
    static BufferedWriter globalLogger = null;

    static String DATABASE = "ML1";



    private static BufferedWriter getLogger() {
        if (globalLogger == null) {
            Calendar cal = Calendar.getInstance();
            long milliseconds = cal.getTimeInMillis();
            Timestamp timestamp = new Timestamp(milliseconds);

            String fileName = ("salida" + timestamp.getTime() / 1000 + ".txt");
            File file = new File(fileName);
            FileWriter fileWriter = null;
            if (file.exists()) {
                try {
                    fileWriter = new FileWriter(file, true);//if file exists append to file. Works fine.
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    file.createNewFile();
                    fileWriter = new FileWriter(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                globalLogger = new BufferedWriter(fileWriter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return globalLogger;
    }

    private static void log(String string) {
        Calendar cal = Calendar.getInstance();
        long milliseconds = cal.getTimeInMillis();
        Timestamp timestamp = new Timestamp(milliseconds);
        String timeStr = timestamp.getHours() + ":" + timestamp.getMinutes() + ":" + timestamp.getSeconds() + "." + timestamp.getNanos();
        try {
            BufferedWriter log = getLogger();
            log.write(timeStr + " | " + string + "\n");
            log.newLine();
            log.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static Connection getSelectConnection() {
        if (globalSelectConnection == null) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/" + DATABASE;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "postgres");
            try {
                globalSelectConnection = DriverManager.getConnection(url, props);
            } catch (SQLException e) {
                log("I couldn't make a select connection");
                log(e.toString());
                e.printStackTrace();
            }
        }
        return globalSelectConnection;
    }


    private static Connection getUpdateConnection() {
        if (globalUpadteConnection == null) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/" + DATABASE;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "postgres");
            try {
                globalUpadteConnection = DriverManager.getConnection(url, props);
                globalUpadteConnection.setAutoCommit(false);
            } catch (SQLException e) {
                log("I couldn't make an update connection");
                log(e.toString());
                e.printStackTrace();
            }
        }
        return globalUpadteConnection;
    }


    public static void main(String[] args) {



        getSelectConnection();
        getUpdateConnection();
        PreparedStatement updatePrepredStatement=null;

        try {
            PreparedStatement selectPreparedStatement = globalSelectConnection.prepareStatement("select idproducto,fecha,url from movimientos where precio = 0 and fecha = '2020-08-08'");
            updatePrepredStatement = globalUpadteConnection.prepareStatement("update movimientos set precio = ?, titulo = ? where idproducto = ? and fecha = ? and precio = 0");
            ResultSet selectResultSet = selectPreparedStatement.executeQuery();
            CloseableHttpClient httpClient = HttpUtils.buildHttpClient();
            while (selectResultSet.next()){
                String idProducto = selectResultSet.getString(1);
                Date fecha = selectResultSet.getDate(2);
                String url = selectResultSet.getString(3);
                String htmlStringFromPage = HttpUtils.getHTMLStringFromPage(url,httpClient,false, true);
                Double price = HTMLParseUtils.getPrice(htmlStringFromPage,url);
                String titulo = HTMLParseUtils.getTitle2(htmlStringFromPage);

                updatePrepredStatement.setDouble(1,price);
                updatePrepredStatement.setString(2,titulo);
                updatePrepredStatement.setString(3,idProducto);
                updatePrepredStatement.setDate(4,fecha);

                int updatedRows = updatePrepredStatement.executeUpdate();
                if (updatedRows!=1){
                    log("no pudo actualizar 1 retistro "+updatedRows);
                }else {
                    getUpdateConnection().commit();
                    log("se actualizó la venta: "+url+" con idProducto="+idProducto+" precio "+price);
                }
                System.out.println(titulo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }



    }
}
