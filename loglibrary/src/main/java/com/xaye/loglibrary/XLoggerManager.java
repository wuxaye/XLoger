package com.xaye.loglibrary;

import android.os.Environment;

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
public final class XLoggerManager {
    private static final String LOG_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/charge_logs/";
    private static XLoggerManager instance;
    private SimpleWriter currentWriter;
    private String currentLogDate;
    private static int daysToKeep = 7; // Default log retention days
    private static String logTag = "V5 Charge"; // Default tag

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");


    private XLoggerManager() {
        openNewLogForToday();
    }

    public static synchronized XLoggerManager getInstance() {
        if (instance == null) {
            instance = new XLoggerManager();
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
            new File(LOG_DIRECTORY).mkdirs();
            File logFile = new File(LOG_DIRECTORY, currentLogDate + ".log");
            currentWriter = new SimpleWriter();
            currentWriter.open(logFile);
            cleanupOldLogs();
        }
    }

    private void cleanupOldLogs() {
        File[] files = new File(LOG_DIRECTORY).listFiles();
        if (files != null) {
            String limitDate = new SimpleDateFormat("yyyy-MM-dd").format(getDateDaysAgo(daysToKeep));
            for (File file : files) {
                if (file.getName().replace(".log", "").compareTo(limitDate) < 0) {
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
            currentWriter.appendLog(timeStamp + " [" + logTag + "] " + log);
        }
    }

    public void close() {
        if (currentWriter != null) {
            currentWriter.close();
        }
    }

    public static void setDaysToKeep(int days) {
        daysToKeep = days;
    }

    public static void setLogTag(String tag) {
        logTag = tag;
    }
}

