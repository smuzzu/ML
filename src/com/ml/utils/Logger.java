package com.ml.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Logger {

    private static BufferedWriter globalLogger = null;
    private static BufferedWriter globalFile = null;
    private static DateFormat globalDateformat = null;

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

    public static synchronized void log(String string) {
        Calendar cal = Calendar.getInstance();
        long milliseconds = cal.getTimeInMillis();
        Timestamp timestamp = new Timestamp(milliseconds);
        if (globalDateformat==null){
            globalDateformat = new SimpleDateFormat("HH:mm:ss.SSS");
        }
        String timeStr = globalDateformat.format(timestamp);
        try {
            BufferedWriter log = getLogger();
            log.write(timeStr + " | " + string + "\n");
            log.newLine();
            log.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void log(Throwable throwable) {
        log(ExceptionUtils.getStackTrace(throwable));
    }


    private static BufferedWriter getFileWriter(String fileName) {
        if (globalFile == null || !globalFile.equals(fileName)) {
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
                globalFile = new BufferedWriter(fileWriter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return globalFile;
    }

    public static synchronized void writeOnFile(String fileName, String str) {
        try {
            BufferedWriter log = getFileWriter(fileName);
            log.write(str + "\n");
            log.newLine();
            log.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
