package com.ml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.io.UnsupportedEncodingException;

import java.net.URLDecoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.commons.lang3.exception.ExceptionUtils;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Created by Muzzu on 6/11/2018.
 */
public class MLFixProduct {

    static CloseableHttpClient globalClient = null;
    static Connection globalSelectConnection = null;
    static Connection globalUpadteConnection = null;
    static BufferedWriter globalLogger = null;

    static boolean LOAD_IMAGES = false;
    static boolean SILENT = true;
    static boolean FIREFOX = false;
    static boolean CHROME = true;
    static String DATABASE = "ML1";

    //static String SEE_SELLER_DETAILS_LABEL = "Ver más datos de este vendedor";
    static String SEE_SELLER_DETAILS_LABEL = "Ver mais dados deste vendedor";

    private static CloseableHttpClient buildHttpClient() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("random", "random"));
        CloseableHttpClient httpclient =
                HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
        return httpclient;
    }


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

    private static void log(Throwable throwable) {
        log(ExceptionUtils.getStackTrace(throwable));
    }


    public static void main(String[] args) {

        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Muzzu\\IdeaProjects\\selenium\\chromedriver_win32_2.4\\chromedriver.exe");
        System.setProperty("webdriver.gecko.driver", "C:\\Users\\Muzzu\\IdeaProjects\\selenium\\geckodriver-v0.20.0-win64\\geckodriver.exe");

        globalClient = buildHttpClient();

        WebDriver webDriver = getWebDriver();

        getSelectConnection();
        getUpdateConnection();
        PreparedStatement updatePrepredStatement=null;

        try {
            PreparedStatement selectPreparedStatement = globalSelectConnection.prepareStatement("select id, url from productos where proveedor is null or trim(proveedor) = ''");
            updatePrepredStatement = globalUpadteConnection.prepareStatement("update productos set proveedor = ? where id = ?");
            ResultSet selectResultSet = selectPreparedStatement.executeQuery();
            while (selectResultSet.next()){
                String id = selectResultSet.getString(1);
                if  (id!=null && !id.isEmpty()) {
                    String url = selectResultSet.getString(2);
                    if (url != null && !url.isEmpty()) {
                        String seller = getSeller(webDriver, url);
                        if (seller != null && !seller.isEmpty()) {
                            seller=formatSeller(seller);
                            updatePrepredStatement.setString(1,seller);
                            updatePrepredStatement.setString(2,id);
                            int updatedRows = updatePrepredStatement.executeUpdate();
                            if (updatedRows!=1){
                                log("no pudo actualizar 1 retistro "+updatedRows);
                            }else {
                                getUpdateConnection().commit();
                                log("se actualizó el producto: "+id+" con seller="+seller);
                            }
                        } else {
                            log("seller is null " + url);
                        }
                    } else {
                        log("null url !!");
                    }
                }else {
                    log("null id");
                }


            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }


        try{
            PreparedStatement selectPreparedStatement2 = globalSelectConnection.prepareStatement("select id, proveedor from productos");
            ResultSet selectResultSet = selectPreparedStatement2.executeQuery();
            while (selectResultSet.next()){
                String id = selectResultSet.getString(1);
                if  (id!=null && !id.isEmpty()) {
                    String savedSeller = selectResultSet.getString(2);
                    if (savedSeller != null && !savedSeller.isEmpty()) {
                        String seller = formatSeller(savedSeller);
                        if (seller != null && !seller.isEmpty()) {
                            seller=formatSeller(seller);
                            if (!savedSeller.equals(seller)) {
                                updatePrepredStatement.setString(1, seller);
                                updatePrepredStatement.setString(2, id);
                                int updatedRows = updatePrepredStatement.executeUpdate();
                                if (updatedRows != 1) {
                                    log("no pudo actualizar 1 retistro " + updatedRows);
                                } else {
                                    getUpdateConnection().commit();
                                    log("se actualizó el producto: " + id + " con viejo seller="+savedSeller+ "y nuevoseller=" + seller);
                                }
                            }
                        } else {
                            log("seller is null " + savedSeller);
                        }
                    } else {
                        log("null url !!");
                    }
                }else {
                    log("null id");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }

    }

    private static String formatSeller(String seller) {
        try {//decode seller url
            seller = URLDecoder.decode(seller, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log("something went wrong trying to decode the seller " + seller);
            log(e);
        }
        return seller;
    }

    private static WebDriver getWebDriver() {
        WebDriver webDriver=null;

        if (FIREFOX) {
            FirefoxBinary firefoxBinary = new FirefoxBinary();
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            if (SILENT) {
                firefoxBinary.addCommandLineOptions("--headless");
                firefoxOptions.setBinary(firefoxBinary);
            }
            if (!LOAD_IMAGES) {
                firefoxOptions.addPreference("permissions.default.image", 2);
            }
            webDriver = new FirefoxDriver(firefoxOptions);
        }

        if (CHROME) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-extensions");
            if (SILENT) {
                options.addArguments("--headless");
            }
            if (!LOAD_IMAGES) {
                HashMap<String, Object> images = new HashMap<String, Object>();
                images.put("images", 2);
                HashMap<String, Object> prefs = new HashMap<String, Object>();
                prefs.put("profile.default_content_setting_values", images);
                options.setExperimentalOption("prefs", prefs);
            }
            webDriver = new ChromeDriver(options);
        }
        return webDriver;
    }


    private static String getSeller(WebDriver webDriver, String productUrl) {
        String seller=null;
        int retries = 0;
        boolean retry = true;

        if (!webDriver.getCurrentUrl().equals(productUrl)) {
            while (retry && retries < 5) {
                retries++;
                try {
                    webDriver.navigate().to(productUrl);
                } catch (Exception e) {
                    log("something went wrong trying to reach url(i) " + productUrl);
                    log(e);
                }
                if (webDriver.getPageSource().length() > 300) {
                    retry = false;
                } else {
                    log("retry #" + retries + " on url to get seller " + productUrl);
                    try {
                        Thread.sleep(5000);//aguantamos los trapos 5 segundos antes de reintentar
                    } catch (InterruptedException e) {
                        log(e);
                    }
                }
            }
        }

        if (webDriver.getCurrentUrl().equals(productUrl)) {

            WebElement webElement = null;
            String sellerStr = null;

            boolean officialStore = false;

            try {
                webElement = webDriver.findElement(By.partialLinkText(SEE_SELLER_DETAILS_LABEL));
                sellerStr = webElement.getAttribute("href");
                if (sellerStr == null || sellerStr.length() == 0) {
                    System.out.println("Seller couldn't be found I " + productUrl);
                }
                int pos = sellerStr.lastIndexOf("/");
                if (pos == -1) {
                    log("Seller couldn't be found II " + productUrl);
                }
                pos++;
                seller = sellerStr.substring(pos);

            } catch (NoSuchElementException noSuchElementException) { //tienda oficial
                officialStore = true;
            } catch (Exception e) {
                log("something went wrong trying to get the seller (i) in " + productUrl);
                log(e);
            }

            if (officialStore) {
                try {
                    webElement = webDriver.findElement(By.className("store-header__logo-name"));
                    seller = webElement.getText();
                    if (seller == null || seller.length() == 0) {
                        log("something went wrong trying to get the seller (iii) in " + productUrl);
                    }
                } catch (Exception e) {
                    log("something went wrong trying to get the seller (iV) in " + productUrl);
                    log(e);
                }
            }
        }
        return seller;
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
                log(e);
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
                log(e);
                e.printStackTrace();
            }
        }
        return globalUpadteConnection;
    }


}
