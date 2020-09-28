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
    private static Connection globalVisitUpdateConnection = null;
    private static Connection globalAddProductConnection = null;
    private static Connection globalAddDailyConnection = null;
    private static Connection globalAddWeeklyConnection = null;
    private static Connection globalAddMonthlyConnection = null;
    private static Connection globalAddActivityConnection = null;
    private static Connection globalCloudConnection = null;

    private static PreparedStatement globalSelectProduct = null;
    private static PreparedStatement globalSelectTotalSold = null;
    private static PreparedStatement globalSelectSellerName = null;
    private static PreparedStatement globalSelectLastQuestion = null;
    private static PreparedStatement globalSelectAllDaily = null;
    private static PreparedStatement globalSelectValidProductsOnDates = null;
    private static PreparedStatement globalSelectLastDaily = null;
    private static PreparedStatement globalSelectLastWeekly = null;
    private static PreparedStatement globalSelectLastMonthly = null;
    private static PreparedStatement globalSelectSales1 = null;
    private static PreparedStatement globalSelectSales2 = null;
    private static PreparedStatement globalSelectHolidays = null;
    private static PreparedStatement globalSelectProductOnCloud = null;
    private static PreparedStatement globalSelectServiceStatusOnCloud = null;
    private static PreparedStatement globalSelectToken = null;

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
    private static PreparedStatement globalUpdateToken = null;

    private static String globalCloudUrl = "jdbc:postgresql://rajje.db.elephantsql.com:5432/bivpmkkk";
    private static String globalCloudUser = "bivpmkkk";
    private static String globalCouldPassword = "TMpI5v0Gja7W974bDtDiF1RYenKW8-6f";

    private static boolean fetchTokenOnCloudFailureNotified =false;
    private static boolean updateTokenOnCloudFailureNotified =false;
    private static boolean cloudConnectionCreationFailureNotified=false;

    private static Date twoHundredSeventyDaysBefore=null;

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


    public static synchronized Connection getCloudConnection(){

        boolean resetConnection= globalCloudConnection ==null;
        if (!resetConnection){
            try {
                resetConnection= globalCloudConnection.isClosed();
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

            int retries=0;
            boolean connected=false;

            while (!connected && retries<5) {
                retries++;
                try {
                    globalCloudConnection = DriverManager.getConnection(globalCloudUrl, props);
                    globalCloudConnection.setAutoCommit(true);
                    connected=true;
                } catch (SQLException e) {
                    Logger.log("I couldn't make a globalCloudConnection connection");
                    Logger.log(e);
                    e.printStackTrace();
                }
                if (!connected){
                    try{
                        Thread.sleep(2000 * retries * retries);//aguantamos los trapos 5 segundos antes de reintentar
                    } catch (InterruptedException e) {
                        Logger.log(e);
                    }
                    String msg = "Couldn't get cloud connection, retry #"+retries;
                    System.out.println(msg);
                    Logger.log(msg);
                }
            }
            if (!connected && !cloudConnectionCreationFailureNotified){
                cloudConnectionCreationFailureNotified=true;
                GoogleMailSenderUtil.sendMail("No se pudo establecer conecion con la base de datos cloud !!!","","sebamuzzu@gmail.com");
            }
        }
        return globalCloudConnection;
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

        boolean resetConnection= globalVisitUpdateConnection ==null;
        if (!resetConnection){
            try {
                resetConnection= globalVisitUpdateConnection.isClosed();
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
                globalVisitUpdateConnection = DriverManager.getConnection(url, props);
                globalVisitUpdateConnection.setAutoCommit(true);
            } catch (SQLException e) {
                Logger.log("I couldn't make an update connection");
                Logger.log(e);
                e.printStackTrace();
            }
        }
        return globalVisitUpdateConnection;
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

    public static synchronized void updateProductAddActivity(String database, boolean overrideTodaysRun, Date globalDate, String productId, String seller, boolean officialStore, int totalSold, int newSold, String title, String url, int feedbacksTotal, double feedbacksAverage, double price, int newQuestions, String lastQuestion, int pagina, int ranking, int shipping, int discount, boolean premium) {
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
                globalInsertActivity =connection.prepareStatement("INSERT INTO public.movimientos(fecha, idproducto, titulo, url, opinionestotal, opinionespromedio, precio, vendidos, totalvendidos, nuevaspreguntas, pagina, proveedor, tiendaoficial, envio, descuento, premium, ranking) VALUES (?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?);");
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
            globalInsertActivity.setInt(17,ranking);

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

    public static synchronized void insertProduct(String database, Date globalDate,String idProduct, String seller, int sellerId, int totalSold, String latestquestion, String url, boolean officialStore) {

        try{
            if (globalInsertProduct ==null) {
                Connection connection= DatabaseHelper.getAddProductConnection(database);
                globalInsertProduct = connection.prepareStatement("INSERT INTO public.productos(id, proveedor, ingreso, lastupdate, lastquestion, totalvendidos, url, tiendaoficial, idproveedor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
            }

            globalInsertProduct.setString(1,idProduct);
            globalInsertProduct.setString(2,seller);
            globalInsertProduct.setDate(3,globalDate);
            globalInsertProduct.setDate(4,globalDate);
            globalInsertProduct.setString(5,latestquestion);
            globalInsertProduct.setInt(6,totalSold);
            globalInsertProduct.setString(7,url);
            globalInsertProduct.setBoolean(8,officialStore);
            globalInsertProduct.setInt(9,sellerId);

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

    public static void insertSale(long id, Timestamp saleDate, String state, String shippingType, boolean mailSent, int userNumber, boolean chatSent) {
        Connection updateConnection = getCloudConnection();

        Timestamp lastUpdate = new Timestamp(Calendar.getInstance().getTimeInMillis());

        try{
            if (globalInsertSale ==null) {
                globalInsertSale = updateConnection.prepareStatement("insert into public.ventas(id,fechaventa,"
                        +"fechaactualizacion,estado,tipoenvio,mailenviado,usuario,chatenviado) values (?,?,?,?,?,?,?,?)");
            }
            globalInsertSale.setLong(1,id);
            globalInsertSale.setTimestamp(2,saleDate);
            globalInsertSale.setTimestamp(3,lastUpdate);
            globalInsertSale.setString(4,state);
            globalInsertSale.setString(5,shippingType);
            globalInsertSale.setBoolean(6,mailSent);
            globalInsertSale.setInt(7,userNumber);
            globalInsertSale.setBoolean(8,chatSent);

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

    public static void updateSale(long id, Character state, Character shippingType, Boolean mailSent, Boolean chatSent) {
        Connection updateConnection =getCloudConnection();

        Timestamp lastUpdate = new Timestamp(Calendar.getInstance().getTimeInMillis());

        if (mailSent==null){
            mailSent=false;
        }

        if (chatSent==null){
            chatSent=false;
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
                    "mailEnviado=?, fechaactualizacion=?, chatEnviado=? where id=?");

            ps.setBoolean(1,mailSent);
            ps.setTimestamp(2,lastUpdate);
            ps.setBoolean(3,chatSent);
            ps.setLong(4,id);

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


    public static void removeSale(long id) {
        Connection updateConnection = getCloudConnection();


        try{
            if (globalRemoveSale ==null) {
                globalRemoveSale = updateConnection.prepareStatement("delete from public.ventas where id=?");
            }

            globalRemoveSale.setLong(1,id);

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
        Counters.incrementGlobalDisableCount();
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

    public static String fetchTokenOnCloud(String userName){
        String token=null;
        int retries=0;
        while (token==null && retries<5) {
            retries++;
            int userNumber = TokenUtils.getUserNumber(userName);
            Connection selectConnection = getCloudConnection();
            try {
                if (globalSelectToken == null) {
                    globalSelectToken = selectConnection.prepareStatement("SELECT t1 FROM public.sopa where usuario=?");
                }
                globalSelectToken.setInt(1, userNumber);
                ;

                ResultSet resultSet = globalSelectToken.executeQuery();
                if (resultSet == null) {
                    Logger.log("Couldn't get token on sopa");
                }
                if (resultSet.next()) {
                    token = resultSet.getString(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Logger.log("Couldn't get token on sopa III");
                Logger.log(e);
            }
            if (token==null){
                try{
                    Thread.sleep(2000 * retries * retries);//aguantamos los trapos 5 segundos antes de reintentar
                } catch (InterruptedException e) {
                    Logger.log(e);
                }
                String msg = "Couldn't get token con clould, retry #"+retries;
                System.out.println(msg);
                Logger.log(msg);
            }
        }
        if (token==null && !fetchTokenOnCloudFailureNotified){
            fetchTokenOnCloudFailureNotified=true;
            GoogleMailSenderUtil.sendMail("No se pudo recuperar el token de "+userName+" on cloud","","sebamuzzu@gmail.com");
        }
        return token;
    }

    public static String fetchRefreshTokenOnCloud(String userName){
        String refreshToken=null;
        int userNumber=TokenUtils.getUserNumber(userName);
        Connection selectConnection = getCloudConnection();
        try{
            PreparedStatement refreshTokenPreparedStatement = selectConnection.prepareStatement("SELECT t2 FROM public.sopa where usuario=?");
            refreshTokenPreparedStatement.setInt(1,userNumber);;

            ResultSet resultSet = refreshTokenPreparedStatement.executeQuery();
            if (resultSet==null){
                Logger.log("Couldn't get refresh token on sopa");
            }
            if (resultSet.next()){
                refreshToken=resultSet.getString(1);
            }
        }catch(SQLException e){
            e.printStackTrace();
            Logger.log("Couldn't get token on sopa III");
            Logger.log(e);
        }
        return refreshToken;
    }


    public static void addTokenOnCloud(String userName, String token, String refreshToken){

        int userNumber=TokenUtils.getUserNumber(userName);
        Connection updateConnection =  getCloudConnection();

        try{
            PreparedStatement addTokenPreparedStatement = updateConnection.prepareStatement("insert into public.sopa(usuario,t1,t2) values(?,?,?)");

            addTokenPreparedStatement.setInt(1,userNumber);
            addTokenPreparedStatement.setString(2,token);
            addTokenPreparedStatement.setString(3,refreshToken);

            int updatedRecords=addTokenPreparedStatement.executeUpdate();

            if (updatedRecords!=1){
                Logger.log("Error adding token on Could "+userNumber+" "+ token + " " +refreshToken);
            }

        }catch(SQLException e){
            e.printStackTrace();
            Logger.log("Couldn't get token on sopa III");
            Logger.log(e);
        }
    }

    public static void updateTokenOnCloud(String userName, String token, String refreshToken){
        boolean completed = false;
        int retries =0;
        while (!completed && retries<5) {
            retries++;
            int userNumber = TokenUtils.getUserNumber(userName);
            Connection updateConnection = getCloudConnection();
            try {
                if (globalUpdateToken == null) {
                    globalUpdateToken = updateConnection.prepareStatement("update public.sopa set t1=?,t2=? where usuario=?");
                }
                globalUpdateToken.setString(1, token);
                globalUpdateToken.setString(2, refreshToken);
                globalUpdateToken.setInt(3, userNumber);

                int updatedRecords = globalUpdateToken.executeUpdate();

                if (updatedRecords != 1) {
                    Logger.log("Error adding token on Could " + userNumber + " " + token + " " + refreshToken);
                }else {
                    completed=true;
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Logger.log("Couldn't get token on sopa III");
                Logger.log(e);
            }
            if (!completed){
                try{
                    Thread.sleep(2000 * retries * retries);//aguantamos los trapos 5 segundos antes de reintentar
                } catch (InterruptedException e) {
                    Logger.log(e);
                }
                String msg = "Couldn't update token con clould, retry #"+retries;
                System.out.println(msg);
                Logger.log(msg);
            }
        }
        if (!completed && updateTokenOnCloudFailureNotified){
            updateTokenOnCloudFailureNotified=true;
            GoogleMailSenderUtil.sendMail("No se pudo actualizar el token de "+userName+" on cloud","","sebamuzzu@gmail.com");
        }
    }


    public static boolean alreadyStoredInDB(long saleId) {

        ResultSet resultSet = null;
        Connection selectConnection = getCloudConnection();
        boolean result=false;
        try{
            if (globalSelectSales2 ==null) {
                globalSelectSales2 = selectConnection.prepareStatement("SELECT id FROM public.ventas where id = ?");
            }
            globalSelectSales2.setLong(1,saleId);

            resultSet = globalSelectSales2.executeQuery();

            if (resultSet==null){
                Logger.log("Couldn't get sale alreadyStoredInDB");
            }

            if (resultSet.next()){
                result = true;
            }
        }catch(SQLException e){
            e.printStackTrace();
            Logger.log("Couldn't get sale alreadyStoredInDB II");
            Logger.log(e);
        }
        return result;
    }


    //sellerId = -1 selects all sellers
    public static ResultSet fetchSales(int sellerId, boolean pendingOnly) {

        ResultSet resultSet = null;
        Connection selectConnection = getCloudConnection();

        String query = "SELECT id,fechaventa,fechaactualizacion,"
                +"estado,tipoenvio,mailenviado,usuario,chatenviado FROM public.ventas";

        if (sellerId>0){
            query+=" where usuario="+sellerId;
            if (pendingOnly){
                query+=" and (mailenviado=false or chatenviado=false)";
            }
        }else {
            if (pendingOnly){
                query+=" where mailenviado=false or chatenviado=false";
            }
        }

        query+=" order by id";

        try{
            if (globalSelectSales1 ==null) {
                globalSelectSales1 = selectConnection.prepareStatement(query);
            }

            resultSet = globalSelectSales1.executeQuery();
            if (resultSet==null){
                Logger.log("Couldn't get all sales");
            }
        }catch(SQLException e){
            e.printStackTrace();
            Logger.log("Couldn't get all sales II");
            Logger.log(e);
        }
        return resultSet;
    }

    public static ArrayList<Date> fetchHolidaysFromCloud() {

        ArrayList<Date> dateArrayList = new ArrayList<Date>();
        Connection selectConnection = getCloudConnection();

        String query = "SELECT fecha FROM public.feriados";


        try{
            if (globalSelectHolidays ==null) {
                globalSelectHolidays = selectConnection.prepareStatement(query);
            }

            ResultSet resultSet = globalSelectHolidays.executeQuery();
            if (resultSet==null){
                Logger.log("Couldn't get holidays on cloud database");
            }
            while (resultSet.next()){
                Date date = resultSet.getDate(1);
                dateArrayList.add(date);
            }

        }catch(SQLException e){
            e.printStackTrace();
            Logger.log("Exception getting holidays on cloud database");
            Logger.log(e);
        }
        return dateArrayList;
    }


    public static Product getProductFromCloud(String id) {

        Product product=null;
        Connection selectConnection = getCloudConnection();

        String query = "SELECT id,usuario,titulo,multiplicador,msgpersonalizado,deshabilitado,nombreunidad"
                +" FROM public.productos where id=?";

        try{
            if (globalSelectProductOnCloud ==null) {
                globalSelectProductOnCloud = selectConnection.prepareStatement(query);
            }

            globalSelectProductOnCloud.setString(1, id);
            ResultSet resultSet = globalSelectProductOnCloud.executeQuery();
            if (resultSet==null){
                Logger.log("Couldn't get products on cloud database");
            }
            if (resultSet.next()){
                product=new Product();
                product.id = resultSet.getString(1);
                product.user = resultSet.getInt(2);
                product.title = resultSet.getString(3);
                product.multiplier = resultSet.getInt(4);
                product.customMessage = resultSet.getString(5);
                Boolean disabled = resultSet.getBoolean(6);
                if (disabled!=null && disabled){
                    product.disabled=true;
                }else {
                    product.disabled = false;
                }
                product.unitName=resultSet.getString(7);
            }

        }catch(SQLException e){
            e.printStackTrace();
            Logger.log("Exception getting products on cloud database");
            Logger.log(e);
        }
        return product;
    }

    public static boolean isServiceEnabledOnCloud() {

        boolean serviceEnabled = true;
        Connection selectConnection = getCloudConnection();
        String query = "SELECT habilitado FROM public.servicio";

        try{
            if (globalSelectServiceStatusOnCloud ==null) {
                globalSelectServiceStatusOnCloud = selectConnection.prepareStatement(query);
            }

            ResultSet resultSet = globalSelectServiceStatusOnCloud.executeQuery();
            if (resultSet==null){
                Logger.log("Couldn't get service status on cloud database");
            }
            if (resultSet.next()){
                Boolean data = resultSet.getBoolean(1);
                if (data!=null && !data){
                    serviceEnabled=false;
                }
            }

        }catch(SQLException e){
            e.printStackTrace();
            Logger.log("Exception getting service status on cloud database");
            Logger.log(e);
        }
        return serviceEnabled;
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

    public static synchronized String fetchSellerName(String productId, String database) {
        String sellerName="";
        Connection connection = DatabaseHelper.getSelectConnection(database);
        try{
            if (globalSelectSellerName ==null) {
                globalSelectSellerName = connection.prepareStatement("SELECT proveedor FROM public.productos WHERE id=?;");
            }

            globalSelectSellerName.setString(1,productId);

            ResultSet rs = globalSelectSellerName.executeQuery();
            if (rs==null){
                Logger.log("Couldn't get seller name "+productId);
                return sellerName;
            }

            if (rs.next()){
                sellerName=rs.getString(1);
            }
        }catch(SQLException e){
            e.printStackTrace();
            Logger.log("Couldn't get total sold ii"+productId);
        }
        return sellerName;
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

    public static ArrayList<String> getPossiblePausedProducts(String database, ArrayList<String> proccessedItemsArrayList, Date date) {
        ArrayList<String> possiblyPausedProductList = new ArrayList<String>();

        String productId=null;
        Date since = getTwoHundredSeventyDaysBefore();
        Connection selectConnection = DatabaseHelper.getSelectConnection(database);
        try{
            PreparedStatement globalSelectPossiblyPaused = null;
            //option 1  //TODO, PONER UN RANGO DE FECHAS A POSIBLE PAUSADOS PARA EVITAR REGISTROS MUY VIEJOS?
            globalSelectPossiblyPaused = selectConnection.prepareStatement("SELECT id FROM public.productos WHERE lastupdate>? lastupdate<? and deshabilitado=false order by lastupdate");
            globalSelectPossiblyPaused.setDate(1,since);
            globalSelectPossiblyPaused.setDate(2,date);

            String msg="Buscando pausados en databasse"+" - "+ globalSelectPossiblyPaused.toString();
            System.out.println(msg);
            Logger.log(msg);

            ResultSet rs2 = globalSelectPossiblyPaused.executeQuery();
            if (rs2==null){
                Logger.log("Couldn't get Possibly Paused Products");
                return null;
            }

            while (rs2.next()){
                productId=rs2.getString(1);
                if (!proccessedItemsArrayList.contains(productId)) {
                    possiblyPausedProductList.add(productId);
                }
            }
        }catch(SQLException e){
            Logger.log("Couldn't get Possibly Paused Products II");
            Logger.log(e);
        }
        return possiblyPausedProductList;
    }

    private static synchronized Date getTwoHundredSeventyDaysBefore(){
        if (twoHundredSeventyDaysBefore==null) {
            Date result = null;
            long oneDayInMiliseconds = 86400000L;
            long twoHundredSeventyDaysInMiliseconds = oneDayInMiliseconds * 270;
            Calendar cal = Calendar.getInstance();
            long milliseconds = cal.getTimeInMillis();
            twoHundredSeventyDaysBefore = new Date(milliseconds - twoHundredSeventyDaysInMiliseconds);
        }
        return twoHundredSeventyDaysBefore;
    }



    public static void main(String[] args) {
        Product product = getProductFromCloud("MLA-831749248");
        boolean b=false;

    }

}
