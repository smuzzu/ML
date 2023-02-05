package com.ml.utils;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Counters {

    static int globalRequestCount = 0;
    static int globalProductCount = 0;
    static int globalNewsCount = 0;
    static int globalRunnerCount;
    static int globalDisableCount=0;

    static Calendar globalCalendar1 = null;
    static Calendar globalCalendar2 = null;


    synchronized public static void incrementGlobalRequestCount() {
        globalRequestCount++;
    }

    synchronized public static void incrementGlobalNewsCount() {
        globalNewsCount++;
    }

    synchronized public static void setGlobalProductCount(int count) {
        globalProductCount=count;
    }

    synchronized public static void incrementGlobalDisableCount() {
        globalDisableCount++;
    }

    synchronized public static int getGlobalRunnerCount() {
        return ++globalRunnerCount;
    }

    synchronized public static void initGlobalRunnerCount() {
        globalRunnerCount = 0;
    }

    public static int getGlobalRequestCountCount() {
        return globalRequestCount;
    }


    public static int getGlobalNewsCount() {
        return globalNewsCount;
    }

    public static int getGlobalProductCount() {
        return globalProductCount;
    }

    public static int getGlobalDisableCount() {
        return globalDisableCount;
    }

    public static synchronized boolean isSameDate(Date date1, Date date2){
        if (globalCalendar1==null){
            globalCalendar1 = Calendar.getInstance();
        }
        if (globalCalendar2==null){
            globalCalendar2 = Calendar.getInstance();
        }

        globalCalendar1.setTime(date1);
        globalCalendar2.setTime(date2);
        boolean sameDay = globalCalendar1.get(Calendar.YEAR) == globalCalendar2.get(Calendar.YEAR) &&
                globalCalendar1.get(Calendar.DAY_OF_YEAR) == globalCalendar2.get(Calendar.DAY_OF_YEAR);
        return sameDay;
    }

    public static Date parseDate(String dateStr) {
        Date runDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            java.util.Date dt =sdf.parse(dateStr);
            runDate=new Date(dt.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("Error parsing date "+dateStr);
        }
        return runDate;
    }

}
