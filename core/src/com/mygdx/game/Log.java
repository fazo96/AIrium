/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.game;

import java.util.ArrayList;

/**
 *
 * @author fazo
 */
public class Log {

    public static final int ERROR = 0;
    public static final int INFO = 1;
    public static final int DEBUG = 2;
    private static ArrayList<LogListener> logListeners;

    private static int logLevel = 1;

    public static void log(int level, String msg) {
        if (level <= logLevel) {
            if (logListeners == null) {
                logListeners = new ArrayList<LogListener>();
            }
            for (LogListener l : logListeners) {
                l.onLog(level, msg);
            }
        }
    }

    public static int getLogLevel() {
        return logLevel;
    }

    public static void setLogLevel(int logLevel) {
        Log.logLevel = logLevel;
    }

    public interface LogListener {
        public abstract void onLog(int level, String msg);
    }

    public static void addListener(LogListener l) {
        if (logListeners == null) {
            logListeners = new ArrayList<LogListener>();
        }
        logListeners.add(l);
    }

}
