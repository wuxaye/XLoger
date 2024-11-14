package com.xaye.loglibrary.format;

/**
 * Author xaye
 *
 * @date: 2024/11/14
 */
public class ThrowableLogFormatter implements LogFormatter<Throwable> {

    @Override
    public String formatMessage(Throwable throwable) {
        StringBuilder formattedMessage = new StringBuilder();
        formattedMessage.append("Exception: ").append(throwable.getClass().getName()).append("\n");
        formattedMessage.append("Message: ").append(throwable.getMessage()).append("\n");

        // 获取异常的堆栈信息
        for (StackTraceElement element : throwable.getStackTrace()) {
            formattedMessage.append("\tat ").append(element.toString()).append("\n");
        }

        return formattedMessage.toString();
    }
}
