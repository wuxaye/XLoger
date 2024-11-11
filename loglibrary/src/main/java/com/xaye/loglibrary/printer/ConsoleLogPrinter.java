package com.xaye.loglibrary.printer;

import android.util.Log;

/**
 * Author xaye
 *
 * @date: 2024/11/11
 */
public class ConsoleLogPrinter implements LogPrinter{
    @Override
    public void printLog(int priority, String tag, String message, Throwable throwable) {
        if (throwable != null) {
            Log.e(tag, message, throwable);  // Log to console (Android Log)
        } else {
            switch (priority) {
                case Log.VERBOSE:
                    Log.v(tag, message);
                    break;
                case Log.DEBUG:
                    Log.d(tag, message);
                    break;
                case Log.INFO:
                    Log.i(tag, message);
                    break;
                case Log.WARN:
                    Log.w(tag, message);
                    break;
                case Log.ERROR:
                    Log.e(tag, message);
                    break;
                default:
                    Log.v(tag, message);
                    break;
            }
        }
    }
}
