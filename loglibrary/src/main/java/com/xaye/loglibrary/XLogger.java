package com.xaye.loglibrary;

import android.util.Log;

import com.xaye.loglibrary.utils.LogLevel;
import com.xaye.loglibrary.utils.Util;


/**
 * Author xaye
 *
 * @date: 2024/6/28
 */
public class XLogger {
    private static LogConfiguration config;

    static boolean sIsInitialized;

    // Private constructor to prevent instantiation
    private XLogger() {
    }

    // Default initialization with default settings
    public static void init() {
        init(new LogConfiguration.Builder().build());
    }

    // Initialization with custom configuration
    public static void init(LogConfiguration configuration) {
        config = configuration;
        sIsInitialized = true;
    }

    public static void log(int level, String message) {
        assertInitialization();
        if (config.isDebugEnabled() && config.getLogLevel() <= level) {
            String[] strings = Util.splitStr(config.getMaxLogLength(), message);
            for (String str : strings) {
                prepareLog(level, getTAG(), str, null, true);
            }
        }
    }

    public static void v(String message) {
        log(LogLevel.VERBOSE, message);
    }

    public static void d(String message) {
        log(LogLevel.DEBUG, message);
    }

    public static void i(String message) {
        log(LogLevel.INFO, message);
    }

    public static void w(String message) {
        log(LogLevel.WARN, message);
    }

    public static void e(String message) {
        log(LogLevel.ERROR, message);
    }


    private static void prepareLog(int priority, String tag, String message, Throwable throwable, boolean saveLog) {
        String logTag = (tag != null) ? tag : config.getTag();
        if (config.getLogLevel() > priority) return;

        if (throwable != null) {
            Log.e(logTag, message, throwable); // 记录异常和信息
        } else {
            switch (priority) {
                case Log.VERBOSE:
                    Log.v(logTag, message);
                    break;
                case Log.DEBUG:
                    Log.d(logTag, message);
                    break;
                case Log.INFO:
                    Log.i(logTag, message);
                    break;
                case Log.WARN:
                    Log.w(logTag, message);
                    break;
                case Log.ERROR:
                    Log.e(logTag, message);
                    break;
                default:
                    Log.v(logTag, message);
                    break;
            }
        }
        if (saveLog) {
            LogToFile.getInstance(config).appendLog(" " + LogLevel.getShortLevelName(priority) + " " + tag + ": " + message);
        }
    }

    static void assertInitialization() {
        if (!sIsInitialized) {
            init(); // 默认初始化或抛出异常提醒初始化
        }
    }

    private static String getTAG() {
        if (config.isStackTraceEnabled()) {
            return computeTag();
        } else return config.getTag();
    }

    private static String computeTag() {
        StringBuilder tag = new StringBuilder();
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) return config.getTag();
        for (StackTraceElement st : sts) {
            if (!st.isNativeMethod() &&
                    !Thread.class.getName().equals(st.getClassName()) &&
                    !XLogger.class.getName().equals(st.getClassName())) {
                tag.append("(").append(st.getFileName()).append(":").append(st.getLineNumber()).append(")");
                return tag.toString();
            }
        }
        return config.getTag();
    }
}
