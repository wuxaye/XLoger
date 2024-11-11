package com.xaye.loglibrary;

import android.util.Log;

import com.xaye.loglibrary.format.JsonLogFormatter;
import com.xaye.loglibrary.format.LogFormatter;
import com.xaye.loglibrary.format.XmlLogFormatter;
import com.xaye.loglibrary.printer.ConsoleLogPrinter;
import com.xaye.loglibrary.printer.LogPrinter;
import com.xaye.loglibrary.utils.LogLevel;
import com.xaye.loglibrary.utils.Util;


/**
 * Author xaye
 *
 * @date: 2024/6/28
 */
public class XLogger {
    private static LogConfiguration config;
    private static boolean sIsInitialized;

    // Default Printer and optional LogFormatter (Global formatter is now removed)
    private static LogPrinter logPrinter = new ConsoleLogPrinter();

    // Private constructor to prevent instantiation
    private XLogger() {}

    // Initialize with default settings
    public static void init() {
        init(new LogConfiguration.Builder().build());
    }

    // Initialize with custom configuration
    public static void init(LogConfiguration configuration) {
        config = configuration;
        sIsInitialized = true;
    }

    // Generic log method with message formatting
    public static void log(int level, String message) {
        assertInitialization();

        if (config.isDebugEnabled() && config.getLogLevel() <= level) {
            // No formatter applied if null, or format if a formatter is set
            String formattedMessage = message;

            // Print the log
            logPrinter.printLog(level, getTAG(), formattedMessage, null);

            // Save the log if needed
            if (config.isSaveLogEnabled()) {
                LogToFile.getInstance(config).appendLog(formattedMessage);
            }
        }
    }

    // Convenience methods for different log levels
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

    // Methods for logging JSON formatted logs (formatting done locally)
    public static void logJson(int level, String jsonMessage) {
        assertInitialization();

        LogFormatter jsonFormatter = new JsonLogFormatter();  // Create JSON formatter locally
        String formattedMessage = jsonFormatter.formatMessage(jsonMessage);

        log(level, formattedMessage);  // Log the formatted message
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

        log(level, formattedMessage);  // Log the formatted message
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



