package com.xaye.loglibrary.utils;

import android.util.Log;

/**
 * Author xaye
 *
 * @date: 2024/6/28
 */
public class LogLevel {
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
