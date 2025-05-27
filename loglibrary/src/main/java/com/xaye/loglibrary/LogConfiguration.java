package com.xaye.loglibrary;

import android.os.Environment;

import com.xaye.loglibrary.utils.LogLevel;

import java.io.File;

/**
 * Author xaye
 *
 * @date: 2024/11/6
 */
public class LogConfiguration {
    private final String tag;
    private final String logDirectory;
    private final int retentionDays;
    private final boolean debugEnabled;
    private final int logLevel;
    private final boolean stackTraceEnabled;
    private final int maxLogLength;
    private final boolean isSaveLogEnabled;
    private final long maxTotalLogSize;
    private final long logSizeCheckInterval;

    private LogConfiguration(Builder builder) {
        this.tag = builder.tag;
        this.logDirectory = builder.logDirectory;
        this.retentionDays = builder.retentionDays;
        this.debugEnabled = builder.debugEnabled;
        this.logLevel = builder.logLevel;
        this.stackTraceEnabled = builder.stackTraceEnabled;
        this.maxLogLength = builder.maxLogLength;
        this.isSaveLogEnabled = builder.isSaveLogEnabled;
        this.maxTotalLogSize = builder.maxTotalLogSize;
        this.logSizeCheckInterval = builder.logSizeCheckInterval;
    }

    public String getTag() {
        return tag;
    }

    public String getLogDirectory() {
        return logDirectory;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public boolean isStackTraceEnabled() {
        return stackTraceEnabled;
    }

    public int getMaxLogLength() {
        return maxLogLength;
    }

    public boolean isSaveLogEnabled() {
        return isSaveLogEnabled;
    }

    public long getMaxTotalLogSize() {
        return maxTotalLogSize;
    }

    public long getLogSizeCheckInterval() {
        return logSizeCheckInterval;
    }

    public static class Builder {
        private String tag = "XLogger";
        private String logDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + tag + File.separator;
        private int retentionDays = 7;
        private boolean debugEnabled = false;
        private int logLevel = LogLevel.ALL;
        private boolean stackTraceEnabled = true;  // 堆栈跟踪日志的默认值,开启会消耗一些性能
        private int maxLogLength = 3 * 1024;  // 默认值：单词打印最大长度，超过会被分段打印
        private boolean isSaveLogEnabled = false;
        private long maxTotalLogSize = -1L;
        private long logSizeCheckInterval = 5 * 60 * 1000L; // 默认5分钟检查一次 (单位：毫秒)

        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder setLogDirectory(String logDirectory) {
            this.logDirectory = logDirectory;
            return this;
        }

        public Builder setRetentionDays(int retentionDays) {
            this.retentionDays = retentionDays;
            return this;
        }

        public Builder setDebugEnabled(boolean debugEnabled) {
            this.debugEnabled = debugEnabled;
            return this;
        }

        public Builder setLogLevel(int logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public Builder setStackTraceEnabled(boolean stackTraceEnabled) {
            this.stackTraceEnabled = stackTraceEnabled;
            return this;
        }

        public Builder setMaxLogLength(int maxLogLength) {  // 新增方法设置日志单词最大长度
            this.maxLogLength = maxLogLength;
            return this;
        }

        public Builder setIsSaveLogEnabled(boolean isSaveLogEnabled) {
            this.isSaveLogEnabled = isSaveLogEnabled;
            return this;
        }

        public Builder setMaxTotalLogSize(long maxTotalLogSize) {
            this.maxTotalLogSize = maxTotalLogSize;
            return this;
        }

        public Builder setLogSizeCheckInterval(long logSizeCheckInterval) {
            this.logSizeCheckInterval = logSizeCheckInterval;
            return this;
        }

        public LogConfiguration build() {
            return new LogConfiguration(this);
        }
    }
}

