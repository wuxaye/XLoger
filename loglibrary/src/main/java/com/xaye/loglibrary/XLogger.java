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
    private static final String DEFAULT_TAG = "XLogger";

    //打印调试开关
    private static final boolean IS_DEBUG = true;

    //Log 单词打印的最大长度
    private static final int MAX_LENGTH = 3 * 1024;
    public static void v(String message) {
        if (IS_DEBUG) {
            String[] strings = Util.splitStr(MAX_LENGTH,message);
            for (String str : strings) {
                prepareLog(Log.VERBOSE, getTAG(), str, null, true);
            }
        }
    }

    public static void d(String message) {
        if (IS_DEBUG) {
            String[] strings = Util.splitStr(MAX_LENGTH,message);
            for (String str : strings) {
                prepareLog(Log.DEBUG, getTAG(), str, null, true);
            }
        }
    }

    public static void i(String message) {
        if (IS_DEBUG) {
            String[] strings = Util.splitStr(MAX_LENGTH,message);
            for (String str : strings) {
                prepareLog(Log.INFO, getTAG(), str, null, true);
            }
        }
    }

    public static void w(String message) {
        if (IS_DEBUG) {
            String[] strings = Util.splitStr(MAX_LENGTH,message);
            for (String str : strings) {
                prepareLog(Log.WARN, getTAG(), str, null, true);
            }
        }
    }

    public static void e(String message) {
        if (IS_DEBUG) {
            String[] strings = Util.splitStr(MAX_LENGTH,message);
            for (String str : strings) {
                prepareLog(Log.ERROR, getTAG(), str, null, true);
            }
        }
    }

    private static void prepareLog(int priority, String tag, String message, Throwable throwable, boolean saveLog) {
        String logTag = (tag != null) ? tag : DEFAULT_TAG;

        if (throwable != null) {
            // logger.logThrowable(logTag, throwable, message);
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
            XLoggerManager.getInstance().appendLog(" "+ LogLevel.getShortLevelName(priority)+" " + tag + ": " + message);
        }
    }


    private static synchronized String getTAG() {
        StringBuilder tag = new StringBuilder();
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return "";
        }
        for (StackTraceElement st : sts) {
            // 筛选获取需要打印的TAG
            if (!st.isNativeMethod() &&
                    !Thread.class.getName().equals(st.getClassName()) &&
                    !XLogger.class.getName().equals(st.getClassName())) {
                // 获取文件名以及打印的行数
                tag.append("(").append(st.getFileName()).append(":").append(st.getLineNumber()).append(")");
                return tag.toString();
            }
        }
        return "";
    }


}
