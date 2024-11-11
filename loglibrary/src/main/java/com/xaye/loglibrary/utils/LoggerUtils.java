package com.xaye.loglibrary.utils;

import android.util.Log;

public class LoggerUtils {

    // 默认的日志标签
    private static final String TAG = "XLogger";

    /**
     * 输出警告日志
     * @param message 要输出的警告信息
     */
    public static void warn(String message) {
        Log.w(TAG, message);  // 使用 Log.w 输出警告日志
    }

    /**
     * 输出错误日志
     * @param message 要输出的错误信息
     */
    public static void error(String message) {
        Log.e(TAG, message);  // 使用 Log.e 输出错误日志
    }

    /**
     * 输出错误日志，并打印异常堆栈信息
     * @param message 错误信息
     * @param throwable 要打印的异常
     */
    public static void error(String message, Throwable throwable) {
        Log.e(TAG, message, throwable);  // 使用 Log.e 输出带异常的错误日志
    }

    /**
     * 输出警告日志，并打印异常堆栈信息
     * @param message 警告信息
     * @param throwable 要打印的异常
     */
    public static void warn(String message, Throwable throwable) {
        Log.w(TAG, message, throwable);  // 使用 Log.w 输出带异常的警告日志
    }
}

