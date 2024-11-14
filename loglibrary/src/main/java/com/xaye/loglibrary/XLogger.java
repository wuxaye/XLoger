package com.xaye.loglibrary;

import android.util.Log;

import com.xaye.loglibrary.format.JsonLogFormatter;
import com.xaye.loglibrary.format.LogFormatter;
import com.xaye.loglibrary.format.ThrowableLogFormatter;
import com.xaye.loglibrary.format.XmlLogFormatter;
import com.xaye.loglibrary.printer.ConsoleLogPrinter;
import com.xaye.loglibrary.printer.LogPrinter;
import com.xaye.loglibrary.utils.LogLevel;


/**
 * Author xaye
 *
 * @date: 2024/6/28
 */
public class XLogger {
    private static LogConfiguration config;
    private static boolean sIsInitialized;

    private static LogPrinter logPrinter = new ConsoleLogPrinter();

    private XLogger() {
    }

    // Initialize with default settings
    public static void init() {
        init(new LogConfiguration.Builder().build());
    }

    public static void init(LogConfiguration configuration) {
        config = configuration;
        sIsInitialized = true;
    }

    public static void log(int level, String tag, String message) {
        assertInitialization();

        if (config.isDebugEnabled() && config.getLogLevel() <= level) {

            String formattedMessage = message;

            String logTag = (tag != null) ? tag : getTAG();

            logPrinter.printLog(level, logTag, formattedMessage, null);

            if (config.isSaveLogEnabled()) {
                LogToFile.getInstance(config).appendLog(formattedMessage);
            }
        }
    }

    public static void v(String message) {
        log(LogLevel.VERBOSE, null, message);
    }

    public static void v(String tag, String message) {
        log(LogLevel.VERBOSE, tag, message);
    }

    public static void v(String msg, Throwable tr) {
        log(LogLevel.VERBOSE, null, buildTrMessage(msg, tr));
    }

    public static void d(String message) {
        log(LogLevel.DEBUG, null, message);
    }

    public static void d(String tag, String message) {
        log(LogLevel.DEBUG, tag, message);
    }

    public static void d(String msg, Throwable tr) {
        log(LogLevel.DEBUG, null, buildTrMessage(msg, tr));
    }

    public static void i(String message) {
        log(LogLevel.INFO, null, message);
    }

    public static void i(String tag, String message) {
        log(LogLevel.INFO, tag, message);
    }

    public static void i(String message, Throwable tr) {
        log(LogLevel.INFO, null, buildTrMessage(message, tr));
    }

    public static void w(String message) {
        log(LogLevel.WARN, null, message);
    }

    public static void w(String tag, String message) {
        log(LogLevel.WARN, tag, message);
    }

    public static void w(String message, Throwable tr) {
        log(LogLevel.WARN, null, buildTrMessage(message, tr));
    }

    public static void e(String message) {
        log(LogLevel.ERROR, null, message);
    }

    public static void e(String tag, String message) {
        log(LogLevel.ERROR, tag, message);
    }

    public static void e(String message, Throwable tr) {
        log(LogLevel.ERROR, null, buildTrMessage(message, tr));
    }

    //WTF（What a Terrible Failure） Log.e()（错误）更加严重，甚至可以看作是极端的错误情况。
    public static void wtf(String message) {
        log(LogLevel.ERROR, null, message);
    }

    public static void wtf(String tag, String message) {
        log(LogLevel.ERROR, tag, message);
    }

    public static void wtf(String message, Throwable tr) {
        log(LogLevel.ERROR, null, buildTrMessage(message, tr));
    }

    /**
     * 构建 Throwable 对象的日志消息
     * @param msg
     * @param tr
     * @return
     */
    private static String buildTrMessage(String msg, Throwable tr) {
        StringBuilder formattedMessage = new StringBuilder();

        // 添加日志消息内容
        if (msg != null && !msg.isEmpty()) {
            formattedMessage.append(msg).append(System.lineSeparator());
        }

        // 如果有 Throwable 对象，格式化它
        if (tr != null) {
            formattedMessage.append(new ThrowableLogFormatter().formatMessage(tr));
        }

        return formattedMessage.toString();
    }


    // Methods for logging JSON formatted logs (formatting done locally)
    public static void logJson(int level, String jsonMessage) {
        assertInitialization();

        JsonLogFormatter jsonFormatter = new JsonLogFormatter();  // Create JSON formatter locally
        String formattedMessage = jsonFormatter.formatMessage(jsonMessage);

        log(level, null, formattedMessage);  // Log the formatted message
    }

    public static void iJson(String jsonMessage) {
        logJson(LogLevel.INFO, jsonMessage);
    }

    public static void eJson(String jsonMessage) {
        logJson(LogLevel.ERROR, jsonMessage);
    }

    // Methods for logging XML formatted logs (formatting done locally)
    public static void logXml(int level, String xmlMessage) {
        assertInitialization();

        LogFormatter xmlFormatter = new XmlLogFormatter();  // Create XML formatter locally
        String formattedMessage = xmlFormatter.formatMessage(xmlMessage);

        log(level, null, formattedMessage);  // Log the formatted message
    }

    public static void iXml(String xmlMessage) {
        logXml(LogLevel.INFO, xmlMessage);
    }

    public static void eXml(String xmlMessage) {
        logXml(LogLevel.ERROR, xmlMessage);
    }

    private static void assertInitialization() {
        if (!sIsInitialized) {
            init(); // Default initialization
        }
    }

    private static String getTAG() {
        return config.isStackTraceEnabled() ? computeTag() : config.getTag();
    }

    private static String computeTag() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (!element.isNativeMethod() &&
                    !Thread.class.getName().equals(element.getClassName()) &&
                    !XLogger.class.getName().equals(element.getClassName())) {
                return element.getFileName() + ":" + element.getLineNumber();
            }
        }
        return config.getTag();
    }

    // Allow setting custom LogPrinter
    public static void setLogPrinter(LogPrinter printer) {
        logPrinter = printer;
    }
}



