package com.xaye.loglibrary.printer;

/**
 * Author xaye
 *
 * @date: 2024/11/11
 */
public interface LogPrinter {
    void printLog(int priority, String tag, String message, Throwable throwable);
}
