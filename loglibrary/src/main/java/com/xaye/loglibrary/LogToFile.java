package com.xaye.loglibrary;

import com.xaye.loglibrary.file.SimpleWriter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Author xaye
 *
 * @date: 2024/11/4
 */
final class LogToFile {
    private static LogToFile instance;
    private SimpleWriter currentWriter;
    private String currentLogDate;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private LogConfiguration config;

    private LogToFile(LogConfiguration config) {
        this.config = config;
        openNewLogForToday();
    }

    public static synchronized LogToFile getInstance(LogConfiguration config) {
        if (instance == null) {
            instance = new LogToFile(config);
        }
        return instance;
    }

    private void openNewLogForToday() {
        String today = DATE_FORMAT.format(new Date());
        if (!today.equals(currentLogDate)) {
            if (currentWriter != null) {
                currentWriter.close();
            }
            currentLogDate = today;
            new File(config.getLogDirectory()).mkdirs();
            File logFile = new File(config.getLogDirectory(), currentLogDate + ".log");
            currentWriter = new SimpleWriter();
            currentWriter.open(logFile);
            cleanupOldLogs();
        }
    }

    private void cleanupOldLogs() {
        File[] files = new File(config.getLogDirectory()).listFiles();
        if (files != null) {
            String limitDate = new SimpleDateFormat("yyyy-MM-dd").format(getDateDaysAgo(config.getRetentionDays()));
            for (File file : files) {
                if (file.getName().replace(".log", "").compareTo(limitDate) <= 0) {
                    file.delete();
                }
            }
        }
    }

    private Date getDateDaysAgo(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        return calendar.getTime();
    }

    public void appendLog(String log) {
        openNewLogForToday();
        if (currentWriter != null && currentWriter.isOpened()) {
            String timeStamp = new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date());
            currentWriter.appendLog(timeStamp + log);
        }
    }

    public void close() {
        if (currentWriter != null) {
            currentWriter.close();
        }
    }
}

