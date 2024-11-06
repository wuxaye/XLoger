package com.xaye.loglibrary.utils;

import android.util.Log;

/**
 * Author xaye
 *
 * @date: 2024/6/28
 */
public class LogLevel {

    public static final int VERBOSE = 2;


    public static final int DEBUG = 3;


    public static final int INFO = 4;


    public static final int WARN = 5;


    public static final int ERROR = 6;


    public static final int ALL = Integer.MIN_VALUE;

    public static final int NONE = Integer.MAX_VALUE;

    public static String getShortLevelName(int logLevel) {
        String levelName;
        switch (logLevel) {
            case Log.VERBOSE:
                levelName = "V";
                break;
            case Log.DEBUG:
                levelName = "D";
                break;
            case Log.INFO:
                levelName = "I";
                break;
            case Log.WARN:
                levelName = "W";
                break;
            case Log.ERROR:
                levelName = "E";
                break;
            default:
                if (logLevel < 2) {
                    levelName = "V-" + (2 - logLevel);
                } else {
                    levelName = "E+" + (logLevel - 6);
                }
        }

        return levelName;
    }

}
