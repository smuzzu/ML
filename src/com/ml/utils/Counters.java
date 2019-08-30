package com.ml.utils;

public class Counters {

    static int globalPageCount = 0;
    static int globalProductCount = 0;
    static int globalNewsCount = 0;
    static int globalRunnerCount;

    synchronized public static void incrementGlobalPageCount() {
        globalPageCount++;
    }

    synchronized public static void incrementGlobalNewsCount() {
        globalNewsCount++;
    }

    synchronized public static void incrementGlobalProductCount() {
        globalProductCount++;
    }

    synchronized public static int getGlobalRunnerCount() {
        return ++globalRunnerCount;
    }

    synchronized public static void initGlobalRunnerCount() {
        globalRunnerCount = 0;
    }

    public static int getGlobalPageCount() {
        return globalPageCount;
    }

    public static int getGlobalNewsCount() {
        return globalNewsCount;
    }

    public static int getGlobalProductCount() {
        return globalProductCount;
    }


}
