package com.ml.utils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.Properties;

public class DatabaseHelper {

    private static Connection globalSelectConnection = null;

    private static Connection globalDisableProductConnection = null;
    private static Connection globalVisitUpadteConnection = null;
    private static Connection globalAddProductConnection = null;
    private static Connection globalAddDailyConnection = null;
    private static Connection globalAddWeeklyConnection = null;
    private static Connection globalAddMonthlyConnection = null;
    private static Connection globalAddActivityConnection = null;
    private static Connection globalCloudSalesUpdateConnection = null;

    private static PreparedStatement globalSelectProduct = null;
    private static PreparedStatement globalSelectTotalSold = null;
    private static PreparedStatement globalSelectLastQuestion = null;
    private static PreparedStatement globalSelectAllDaily = null;
    private static PreparedStatement globalSelectValidProductsOnDates = null;
    private static PreparedStatement globalSelectLastDaily = null;
    private static PreparedStatement globalSelectLastWeekly = null;
    private static PreparedStatement globalSelectLastMonthly = null;
    private static PreparedStatement globalSelectSales = null;

    private static PreparedStatement globalInsertProduct = null;
    private static PreparedStatement globalInsertDaily = null;
    private static PreparedStatement globalInsertWeekly = null;
    private static PreparedStatement globalInsertMonthly = null;
    private static PreparedStatement globalInsertActivity = null;
    private static PreparedStatement globalInsertSale = null;
    private static PreparedStatement globalRemoveActivity = null;
    private static PreparedStatement globalRemoveSale = null;
    private static PreparedStatement globalUpdateProduct = null;
    private static PreparedStatement globalDisableProduct=null;            ;
    private static PreparedStatement globalUpdateVisits = null;

    private static String globalCloudUrl = "jdbc:postgresql://rajje.db.elephantsql.com:5432/bivpmkkk";
    private static String globalCloudUser = "bivpmkkk";
    private static String globalCouldPassword = "TMpI5v0Gja7W974bDtDiF1RYenKW8-6f";

    public static synchronized Connection getSelectConnection(String database){

        boolean resetConnection=globalSelectConnection==null;
        if (!resetConnection){
            try {
                resetConnection=globalSelectConnection.isClosed();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (resetConnection) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/" + database;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "password");
            try {
                globalSelectConnection = DriverManager.getConnection(url, props);
            } catch (SQLException e) {
                Logger.log("I couldn't make a select connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalSelectConnection;
    }

    public static synchronized Connection getCloudSalesSelectConnection(){

        boolean resetConnection=globalSelectConnection==null;
        if (!resetConnection){
            try {
                resetConnection=globalSelectConnection.isClosed();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (resetConnection) {
            Properties props = new Properties();
            props.setProperty("user", globalCloudUser);
            props.setProperty("password", globalCouldPassword);

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            try {
                globalSelectConnection = DriverManager.getConnection(globalCloudUrl,props);
            } catch (SQLException e) {
                Logger.log("I couldn't make a selectCloud connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalSelectConnection;
    }


    public static synchronized Connection getCloudSalesUpdateConnection(){

        boolean resetConnection= globalCloudSalesUpdateConnection ==null;
        if (!resetConnection){
            try {
                resetConnection= globalCloudSalesUpdateConnection.isClosed();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (resetConnection) {

            Properties props = new Properties();
            props.setProperty("user", globalCloudUser);
            props.setProperty("password", globalCouldPassword);

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                globalCloudSalesUpdateConnection = DriverManager.getConnection(globalCloudUrl,props);
                globalCloudSalesUpdateConnection.setAutoCommit(true);
            } catch (SQLException e) {
                Logger.log("I couldn't make a globalCloudUpdateConnection connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalCloudSalesUpdateConnection;
    }



    public static synchronized Connection getDisableProductConnection(String database){

        boolean resetConnection=globalDisableProductConnection==null;
        if (!resetConnection){
            try {
                resetConnection=globalDisableProductConnection.isClosed();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (resetConnection) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/"+database;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "password");
            try {
                globalDisableProductConnection = DriverManager.getConnection(url, props);
                globalDisableProductConnection.setAutoCommit(true);
            } catch (SQLException e) {
                Logger.log("I couldn't make a globalDisableProductConnection connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalDisableProductConnection;
    }


    public static synchronized Connection getVisitUpdateConnection(String database){

        boolean resetConnection=globalVisitUpadteConnection==null;
        if (!resetConnection){
            try {
                resetConnection=globalVisitUpadteConnection.isClosed();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (resetConnection) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/"+database;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "password");
            try {
                globalVisitUpadteConnection = DriverManager.getConnection(url, props);
                globalVisitUpadteConnection.setAutoCommit(true);
            } catch (SQLException e) {
                Logger.log("I couldn't make an update connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalVisitUpadteConnection;
    }

    public static synchronized Connection getAddProductConnection(String database){

        boolean resetConnection=globalAddProductConnection==null;
        if (!resetConnection){
            try {
                resetConnection=globalAddProductConnection.isClosed();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (resetConnection) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/"+database;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "password");
            try {
                globalAddProductConnection = DriverManager.getConnection(url, props);
            } catch (SQLException e) {
                Logger.log("I couldn't make addproduct connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalAddProductConnection;
    }

    public static synchronized Connection getAddDailyConnection(String database){

        boolean resetConnection=globalInsertDaily==null;
        if (!resetConnection){
            try {
                resetConnection=globalInsertDaily.isClosed();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (resetConnection) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/"+database;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "password");
            try {
                globalAddDailyConnection = DriverManager.getConnection(url, props);
            } catch (SQLException e) {
                Logger.log("I couldn't make add daily connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalAddDailyConnection;
    }

    public static synchronized Connection getAddWeeklyConnection(String database){

        boolean resetConnection=globalInsertWeekly==null;
        if (!resetConnection){
            try {
                resetConnection=globalInsertWeekly.isClosed();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (resetConnection) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/"+database;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "password");
            try {
                globalAddWeeklyConnection = DriverManager.getConnection(url, props);
            } catch (SQLException e) {
                Logger.log("I couldn't make add Weekly connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalAddWeeklyConnection;
    }

    public static synchronized Connection getAddMonthlyConnection(String database){

        boolean resetConnection=globalInsertMonthly==null;
        if (!resetConnection){
            try {
                resetConnection=globalInsertMonthly.isClosed();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (resetConnection) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/"+database;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "password");
            try {
                globalAddMonthlyConnection = DriverManager.getConnection(url, props);
            } catch (SQLException e) {
                Logger.log("I couldn't make add Monthly connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalAddMonthlyConnection;
    }

    public static synchronized Connection getAddActivityConnection(String database){
        boolean resetConnection=globalAddActivityConnection==null;
        if (!resetConnection){
            try {
                resetConnection=globalAddActivityConnection.isClosed();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (resetConnection) {

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:postgresql://localhost:5432/"+database;
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "password");
            try {
                globalAddActivityConnection = DriverManager.getConnection(url, props);
                globalAddActivityConnection.setAutoCommit(false);
            } catch (SQLException e) {
                Logger.log("I couldn't make add activity connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalAddActivityConnection;
    }

/***************************************************************************************************/

    public static synchronized void updateProductAddActivity(String database, boolean overrideTodaysRun, Date globalDate, String productId, String seller, boolean officialStore, int totalSold, int newSold, String title, String url, int feedbacksTotal, double feedbacksAverage, double price, int newQuestions, String lastQuestion, int pagina, int shipping, int discount, boolean premium) {
        Connection connection = getAddActivityConnection(database);
        try{
            if (globalUpdateProduct ==null) {

                globalUpdateProduct = connection.prepareStatement("UPDATE public.productos SET totalvendidos = ?, lastupdate=?, url=?, lastquestion=?, proveedor=?, tiendaoficial=?, deshabilitado=false WHERE id = ?;");
            }

            globalUpdateProduct.setInt(1,totalSold);
            globalUpdateProduct.setDate(2,globalDate);
            globalUpdateProduct.setString(3,url);
            globalUpdateProduct.setString(4,lastQuestion);
            globalUpdateProduct.setString(5,seller);
            globalUpdateProduct.setBoolean(6,officialStore);
            globalUpdateProduct.setString(7,productId);

            int insertedRecords = globalUpdateProduct.executeUpdate();
            if (insertedRecords!=1){
                Logger.log("Couldn't update product "+productId);
            }

            if (overrideTodaysRun){//no sabemos si hay registro pero por las dudas removemos
                if (globalRemoveActivity==null){
                    globalRemoveActivity=connection.prepareStatement("DELETE FROM public.movimientos WHERE idproducto=? and fecha=?");
                }
                globalRemoveActivity.setString(1,productId);
                globalRemoveActivity.setDate(2,globalDate);
                int removedRecords=globalRemoveActivity.executeUpdate();
                if (removedRecords>=1){
                    Logger.log("Record removed on activity table date: "+globalDate+" productId: "+productId);
                }
            }

            if (globalInsertActivity ==null){
                globalInsertActivity =connection.prepareStatement("INSERT INTO public.movimientos(fecha, idproducto, titulo, url, opinionestotal, opinionespromedio, precio, vendidos, totalvendidos, nuevaspreguntas, pagina, proveedor, tiendaoficial, envio, descuento, premium) VALUES (?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
            }

            globalInsertActivity.setDate(1,globalDate);
            globalInsertActivity.setString(2,productId);
            globalInsertActivity.setString(3,title);
            globalInsertActivity.setString(4,url);
            globalInsertActivity.setInt(5,feedbacksTotal);
            globalInsertActivity.setDouble(6, feedbacksAverage);
            globalInsertActivity.setDouble(7, price);
            globalInsertActivity.setInt(8,newSold);
            globalInsertActivity.setInt(9,totalSold);
            globalInsertActivity.setInt(10,newQuestions);
            globalInsertActivity.setInt(11,pagina);
            globalInsertActivity.setString(12,seller);
            globalInsertActivity.setBoolean(13,officialStore);

            globalInsertActivity.setInt(14,shipping);
            globalInsertActivity.setInt(15,discount);
            globalInsertActivity.setBoolean(16,premium);

            insertedRecords = globalInsertActivity.executeUpdate();
            if (insertedRecords!=1){
                Logger.log("Couln't insert a record in activity table "+productId);
            }

            connection.commit();

        }catch(SQLException e){
            Logger.log("I couldn't add activity due to SQLException "+url);
            Logger.log(e);
            if (connection!=null){
                try {
                    //connection reset
                    connection.close();
                    connection=null;
                    connection=getAddActivityConnection(database);

                    //prepared statement's reset
                    globalInsertActivity=null;
                    globalRemoveActivity=null;
                    globalUpdateProduct=null;
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static synchronized void insertProduct(String database, boolean overrideTodaysRun, Date globalDate,String idProduct, String seller, int totalSold, String latestquestion, String url, boolean officialStore) {

        try{
            if (globalInsertProduct ==null) {
                Connection connection= DatabaseHelper.getAddProductConnection(database);
                globalInsertProduct = connection.prepareStatement("INSERT INTO public.productos(id, proveedor, ingreso, lastupdate, lastquestion, totalvendidos, url, tiendaoficial) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
            }

            globalInsertProduct.setString(1,idProduct);
            globalInsertProduct.setString(2,seller);
            globalInsertProduct.setDate(3,globalDate);
            globalInsertProduct.setDate(4,globalDate);
            globalInsertProduct.setString(5,latestquestion);
            globalInsertProduct.setInt(6,totalSold);
            globalInsertProduct.setString(7,url);
            globalInsertProduct.setBoolean(8,officialStore);

            int registrosInsertados = globalInsertProduct.executeUpdate();

            if (registrosInsertados!=1){
                Logger.log("Couldn't insert product I");
            }
        }catch(SQLException e){
            Logger.log("Couldn't insert product II");
            Logger.log(e);
        }
    }

    public static synchronized void insertDaily(String database, Date date,String idProduct, long orders, long visits, long questions, boolean active, double price, String title, int ranking, String user) {

        try{
            if (globalInsertDaily ==null) {
                Connection connection= getAddDailyConnection(database);
                globalInsertDaily = connection.prepareStatement("INSERT INTO public.diario(fecha, idproducto, visitas, preguntas, ventas, activo, precio, titulo, ranking, usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?);");
            }

            globalInsertDaily.setDate(1,date);
            globalInsertDaily.setString(2,idProduct);
            globalInsertDaily.setLong(3,visits);
            globalInsertDaily.setLong(4,questions);
            globalInsertDaily.setLong(5,orders);
            globalInsertDaily.setBoolean(6,active);
            globalInsertDaily.setDouble(7,price);
            globalInsertDaily.setString(8,title);
            globalInsertDaily.setInt(9,ranking);
            globalInsertDaily.setString(10,user);

            int registrosInsertados = globalInsertDaily.executeUpdate();

            if (registrosInsertados!=1){
                Logger.log("Couldn't insert daily record I");
            }
        }catch(SQLException e){
            Logger.log("Couldn't insert daily record II");
            Logger.log(e);
        }
    }

    public static synchronized void insertWeekly(String database, Date date, Date date2, String idProduct, long orders, long visits, long questions, int ranking, long pauseDays, double price, String title, String user) {

        try{
            if (globalInsertWeekly ==null) {
                Connection connection= getAddWeeklyConnection(database);
                globalInsertWeekly = connection.prepareStatement("INSERT INTO public.semanal(fecha, fecha2, idproducto, visitas, preguntas, ventas, ranking, diasenpausa, precio, titulo, usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?,?,?);");
            }

            globalInsertWeekly.setDate(1,date);
            globalInsertWeekly.setDate(2,date2);
            globalInsertWeekly.setString(3,idProduct);
            globalInsertWeekly.setLong(4,visits);
            globalInsertWeekly.setLong(5,questions);
            globalInsertWeekly.setLong(6,orders);
            globalInsertWeekly.setInt(7, ranking);
            globalInsertWeekly.setLong(8,pauseDays);
            globalInsertWeekly.setDouble(9,price);
            globalInsertWeekly.setString(10,title);
            globalInsertWeekly.setString(11,user);

            int registrosInsertados = globalInsertWeekly.executeUpdate();

            if (registrosInsertados!=1){
                Logger.log("Couldn't insert weekly record I");
            }
        }catch(SQLException e){
            Logger.log("Couldn't insert weekly record II");
            Logger.log(e);
        }
    }

    public static synchronized void insertMonthly(String database, Date date, Date date2, String idProduct, long orders, long visits, long questions, int ranking, long pauseDays, double price, String title, String user) {

        try{
            if (globalInsertMonthly ==null) {
                Connection connection= getAddMonthlyConnection(database);
                globalInsertMonthly = connection.prepareStatement("INSERT INTO public.mensual(fecha, fecha2, idproducto, visitas, preguntas, ventas, ranking, diasenpausa, precio, titulo, usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?,?,?);");
            }

            globalInsertMonthly.setDate(1,date);
            globalInsertMonthly.setDate(2,date2);
            globalInsertMonthly.setString(3,idProduct);
            globalInsertMonthly.setLong(4,visits);
            globalInsertMonthly.setLong(5,questions);
            globalInsertMonthly.setLong(6,orders);
            globalInsertMonthly.setInt(7,ranking);
            globalInsertMonthly.setLong(8,pauseDays);
            globalInsertMonthly.setDouble(9,price);
            globalInsertMonthly.setString(10,title);
            globalInsertMonthly.setString(11,user);

            int registrosInsertados = globalInsertMonthly.executeUpdate();

            if (registrosInsertados!=1){
                Logger.log("Couldn't insert monthly record I");
            }
        }catch(SQLException e){
            Logger.log("Couldn't insert monthly record II");
            Logger.log(e);
        }
    }

    public static void insertSale(int id, Timestamp saleDate, String state, String shippingType, boolean notified, int user) {
        Connection updateConnection = getCloudSalesUpdateConnection();

        Timestamp lastUpdate = new Timestamp(Calendar.getInstance().getTimeInMillis());

        try{
            if (globalInsertSale ==null) {
                globalInsertSale = updateConnection.prepareStatement("insert into public.ventas(id,fechaventa,fechaactualizacion,estado,tipoenvio,notificado,usuario) values (?,?,?,?,?,?,?)");
            }

            globalInsertSale.setInt(1,id);
            globalInsertSale.setTimestamp(2,saleDate);
            globalInsertSale.setTimestamp(3,lastUpdate);
            globalInsertSale.setString(4,state);
            globalInsertSale.setString(5,shippingType);
            globalInsertSale.setBoolean(6,notified);
            globalInsertSale.setInt(7,user);

            int insertedRecords = globalInsertSale.executeUpdate();
            if (insertedRecords!=1){
                Logger.log("Couln't insert a record in sales table id="+id);
            }

        }catch(SQLException e){
            e.printStackTrace();
            Logger.log("Cannot insert a record in sales table II id="+id);
            Logger.log(e);
        }
    }

    public static void updateSale(int id, String state, String shippingType, Boolean notified) {
        Connection updateConnection = DatabaseHelper.getCloudSalesUpdateConnection();

        Timestamp lastUpdate = new Timestamp(Calendar.getInstance().getTimeInMillis());

        if (notified==null){
            notified=false;
        }

        String queryStr="";
        if (state!=null){
            queryStr+=" estado='"+state+"',";
        }
        if (shippingType!=null){
            queryStr+=" tipoenvio='"+shippingType+"',";
        }

        try{
            PreparedStatement ps = updateConnection.prepareStatement("update public.ventas set " +queryStr+
                    "notificado=?, fechaactualizacion=? where id=?");

            ps.setBoolean(1,notified);
            ps.setTimestamp(2,lastUpdate);
            ps.setInt(3,id);

            int updatedRecords = ps.executeUpdate();
            if (updatedRecords!=1){
                Logger.log("Couln't update a record in sales table id="+id);
            }
        }catch(SQLException e){
            e.printStackTrace();
            Logger.log("update a record in sales table II id="+id);
            Logger.log(e);
        }
    }


    public static void removeSale(int id) {
        Connection updateConnection = getCloudSalesUpdateConnection();


        try{
            if (globalRemoveSale ==null) {
                globalRemoveSale = updateConnection.prepareStatement("delete from public.ventas where id=?");
            }

            globalRemoveSale.setInt(1,id);

            int removedRecords = globalRemoveSale.executeUpdate();
            if (removedRecords!=1){
                Logger.log("Couln't delete a record in sales table id="+id);
            }

        }catch(SQLException e){
            e.printStackTrace();
            Logger.log("Cannot delete a record in sales table II id="+id);
            Logger.log(e);
        }
    }

    public static synchronized void disableProduct(String productId,String database){
        int registrosModificados=0;
        try {
            if (globalDisableProduct==null){
                Connection disableProductConnection=getDisableProductConnection(database);
                globalDisableProduct=disableProductConnection.prepareStatement("update productos set deshabilitado = true where id = ?");
            }
            globalDisableProduct.setString(1, productId);
            registrosModificados = globalDisableProduct.executeUpdate();
        } catch (SQLException e) {
            Logger.log("Error deshabilitando producto "+productId);
            Logger.log(e);
        }

        if (registrosModificados < 1) {
            Logger.log("Couldn't no pudo deshabilitar ningun producto de empresa " + productId);
        }

    }

    public static synchronized void updateVisitOnDatabase(String productId, int quantity, Date date, String database){

        if (globalUpdateVisits ==null) {
            Connection connection = DatabaseHelper.getVisitUpdateConnection(database);
            try {
                globalUpdateVisits = connection.prepareStatement("update public.movimientos set visitas=? where idproducto=? and fecha =?");
                globalUpdateVisits.setDate(3,date);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            globalUpdateVisits.setInt(1,quantity);
            globalUpdateVisits.setString(2,productId);


            int updatedRecords=globalUpdateVisits.executeUpdate();

            if (updatedRecords!=1){
                Logger.log("Error updating visits "+productId+" "+ quantity + " " +date);
            }



        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static ResultSet fetchSales() {

        ResultSet resultSet = null;
        Connection selectConnection = getCloudSalesSelectConnection();

        try{
            if (globalSelectSales ==null) {
                globalSelectSales = selectConnection.prepareStatement("SELECT id,fechaventa,fechaactualizacion,estado,tipoenvio,notificado,usuario FROM public.ventas order by id");
            }

            resultSet = globalSelectSales.executeQuery();
            if (resultSet==null){
                Logger.log("Couldn't get sales");
            }
        }catch(SQLException e){
            e.printStackTrace();
            Logger.log("Couldn't get last sales II");
            Logger.log(e);
        }
        return resultSet;
    }



    public static synchronized Date fetchLastUpdate(String productId, String database) {
        Date lastUpdate=null;
        Connection connection=DatabaseHelper.getSelectConnection(database);
        try{
            if (globalSelectProduct ==null) {
                globalSelectProduct = connection.prepareStatement("SELECT lastUpdate FROM public.productos WHERE id=?;");
            }

            globalSelectProduct.setString(1,productId);

            ResultSet rs = globalSelectProduct.executeQuery();
            if (rs==null){
                Logger.log("Couldn't get last update I "+productId);
            }
            if (rs.next()){
                lastUpdate=rs.getDate(1);
            }
        }catch(SQLException e){
            Logger.log("Couldn't get last update II "+productId);
            Logger.log(e);
        }
        return lastUpdate;
    }

    public static ArrayList<String> fetchProductsLike(String searchString, String database) {
        String productId=null;
        ArrayList<String> resultArrayList = new ArrayList<String>();
        globalSelectConnection=null; //necesito resetear para cambiar de bases de datos
        Connection connection=DatabaseHelper.getSelectConnection(database);
        try{
            globalSelectProduct = connection.prepareStatement("SELECT id FROM public.productos WHERE url like '%"+searchString+"%'");

            ResultSet rs = globalSelectProduct.executeQuery();
            if (rs==null){
                Logger.log("Couldn't get searchString "+searchString);
            }
            while (rs.next()){
                productId=rs.getString(1);
                resultArrayList.add(productId);
            }
        }catch(SQLException e){
            Logger.log("Couldn't searchString II "+searchString);
            Logger.log(e);
        }
        return resultArrayList;
    }



    public static synchronized int fetchTotalSold(String productId, String database) {
        int totalSold=0;
        Connection connection = DatabaseHelper.getSelectConnection(database);
        try{
            if (globalSelectTotalSold ==null) {
                globalSelectTotalSold = connection.prepareStatement("SELECT totalvendidos FROM public.productos WHERE id=?;");
            }

            globalSelectTotalSold.setString(1,productId);

            ResultSet rs = globalSelectTotalSold.executeQuery();
            if (rs==null){
                Logger.log("Couldn't get total sold i"+productId);
                return 0;
            }

            if (rs.next()){
                totalSold=rs.getInt(1);
            }
        }catch(SQLException e){
            e.printStackTrace();
            Logger.log("Couldn't get total sold ii"+productId);
        }
        return totalSold;
    }

    public static synchronized String fetchLastQuestion(String productId, String database) {
        String lastQuestion=null;
        Connection connection=DatabaseHelper.getSelectConnection(database);
        try{
            if (globalSelectLastQuestion==null) {
                globalSelectLastQuestion = connection.prepareStatement("SELECT lastQuestion FROM public.productos WHERE id=?;");
            }

            globalSelectLastQuestion.setString(1,productId);

            ResultSet rs = globalSelectLastQuestion.executeQuery();
            if (rs==null){
                Logger.log("Couldn't get last question i "+productId);
                return null;
            }

            if (rs.next()){
                lastQuestion=rs.getString(1);
            }
        }catch(SQLException e){
            Logger.log("Couldn't get last question ii "+productId);
            Logger.log(e);
        }
        return lastQuestion;
    }

    public static synchronized Date fetchLasLastDailyUpdate(String database, String user) {
        Date lastDaily=null;
        Connection connection=DatabaseHelper.getSelectConnection(database);
        try{
            if (globalSelectLastDaily ==null) {
                globalSelectLastDaily = connection.prepareStatement("SELECT fecha FROM public.diario where usuario = '"+user+"' order by fecha desc limit 1");
            }

            ResultSet rs = globalSelectLastDaily.executeQuery();
            if (rs==null){
                Logger.log("Couldn't get last daily record on database "+database);
                return null;
            }

            if (rs.next()){
                lastDaily=rs.getDate(1);
            }
        }catch(SQLException e){
            Logger.log("Couldn't get last daily record on database II "+database);
            Logger.log(e);
        }
        return lastDaily;
    }

    public static synchronized Date fetchLasLastWeeklyUpdate(String database, String user) {
        Date lastWeeklyDate=null;
        Connection connection=DatabaseHelper.getSelectConnection(database);
        try{
            if (globalSelectLastWeekly ==null) {
                globalSelectLastWeekly = connection.prepareStatement("SELECT fecha FROM public.semanal where usuario = '"+user+"' order by fecha desc limit 1");
            }

            ResultSet rs = globalSelectLastWeekly.executeQuery();
            if (rs==null){
                Logger.log("Couldn't get last Weekly record on database "+database);
                return null;
            }

            if (rs.next()){
                lastWeeklyDate=rs.getDate(1);
            }
        }catch(SQLException e){
            Logger.log("Couldn't get last Weekly record on database II "+database);
            Logger.log(e);
        }
        return lastWeeklyDate;
    }

    public static synchronized Date fetchLasLastMonthlyUpdate(String database, String user) {
        Date lastmonthlyDate=null;
        Connection connection=DatabaseHelper.getSelectConnection(database);
        try{
            if (globalSelectLastMonthly ==null) {
                globalSelectLastMonthly = connection.prepareStatement("SELECT fecha FROM public.mensual where usuario = '"+user+"' order by fecha desc limit 1");
            }

            ResultSet rs = globalSelectLastMonthly.executeQuery();
            if (rs==null){
                Logger.log("Couldn't get last Weekly record on database "+database);
                return null;
            }

            if (rs.next()){
                lastmonthlyDate=rs.getDate(1);
            }
        }catch(SQLException e){
            Logger.log("Couldn't get last Weekly record on database II "+database);
            Logger.log(e);
        }
        return lastmonthlyDate;
    }

    public static synchronized ArrayList<String> fetchValidProductsBetweenDates(String database, Date date1, Date date2, int minimunDays, String user) {
        ArrayList<String> result=new ArrayList<String>();
        Connection connection=DatabaseHelper.getSelectConnection(database);
        try{
            if (globalSelectValidProductsOnDates ==null) {
                globalSelectValidProductsOnDates = connection.prepareStatement("select idproducto from diario where fecha >= ? and fecha <= ? and usuario = ? group by idproducto having count(*) >= ?");
            }
            globalSelectValidProductsOnDates.setDate(1,date1);
            globalSelectValidProductsOnDates.setDate(2,date2);
            globalSelectValidProductsOnDates.setString(3,user);
            globalSelectValidProductsOnDates.setLong(4,minimunDays);

            ResultSet rs = globalSelectValidProductsOnDates.executeQuery();
            if (result==null){
                Logger.log("Couldn't get all daily valid productos record on database "+database);
                return null;
            }
            while (rs.next()){
                String productId=rs.getString(1);
                result.add(productId);
            }

        }catch(SQLException e){
            Logger.log("Couldn't get all daily valid productos record on database II "+database);
            Logger.log(e);
        }
        return result;
    }


    public static synchronized ResultSet fetchAllDailyUpdatesBetweenDates(String database, Date date1, Date date2,String productId, String user) {
        ResultSet result=null;
        Connection connection=DatabaseHelper.getSelectConnection(database);
        try{
            if (globalSelectAllDaily ==null) {
                globalSelectAllDaily = connection.prepareStatement("SELECT fecha, visitas, preguntas, ventas, ranking, activo, precio, titulo FROM public.diario where fecha >=? and fecha <=? and idproducto = ? and usuario = ? order by fecha");
            }
            globalSelectAllDaily.setDate(1,date1);
            globalSelectAllDaily.setDate(2,date2);
            globalSelectAllDaily.setString(3,productId);
            globalSelectAllDaily.setString(4,user);

            result = globalSelectAllDaily.executeQuery();
            if (result==null){
                Logger.log("Couldn't get all daily record on database "+database);
                return null;
            }

        }catch(SQLException e){
            Logger.log("Couldn't get all daily record on database II "+database);
            Logger.log(e);
        }
        return result;
    }


}
