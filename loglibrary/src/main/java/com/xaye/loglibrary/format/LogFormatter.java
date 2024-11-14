package com.xaye.loglibrary.format;

/**
 * Author xaye
 *
 * @date: 2024/11/11
 */
public interface LogFormatter<T> {
    String formatMessage(T data);
}
