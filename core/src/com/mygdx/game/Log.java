/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.game;

/**
 *
 * @author fazo
 */
public class Log {

    public static final int ERROR = 0;
    public static final int INFO = 1;
    public static final int DEBUG = 2;

    private static int logLevel = 0;

    public static void log(int level, String msg) {
        if (level <= logLevel) {
            System.out.println(msg);
        }
    }

    public static int getLogLevel() {
        return logLevel;
    }

    public static void setLogLevel(int logLevel) {
        Log.logLevel = logLevel;
    }

}
