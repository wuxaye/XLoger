package com.xaye.loglibrary;

import android.annotation.SuppressLint;
import android.os.Build;

import com.xaye.loglibrary.file.SimpleWriter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private final ScheduledExecutorService sizeCleanupScheduler; //后台线程池

    private LogToFile(LogConfiguration config) {
        this.config = config;
        // NEW: 创建一个单线程的后台调度器
        this.sizeCleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "LogSizeCleanupThread");
            t.setPriority(Thread.MIN_PRIORITY); // 设置为低优先级，避免抢占业务线程资源
            return t;
        });
        openNewLogForToday();

        // 启动后台“大小”清理任务
        startBackgroundSizeCleanup();
    }

    public static synchronized LogToFile getInstance(LogConfiguration config) {
        if (instance == null) {
            instance = new LogToFile(config);
        }
        return instance;
    }


    /**
     * 启动大小清理的后台任务
     */
    private void startBackgroundSizeCleanup() {
        if (config.getMaxTotalLogSize() > 0) {
            long interval = config.getLogSizeCheckInterval();
            // 只定期执行大小清理任务
            sizeCleanupScheduler.scheduleWithFixedDelay(
                    this::cleanupOldLogsBySize,
                    interval,  // 初始延迟
                    interval,  // 每次任务结束到下一次开始的间隔（而非固定速率）
                    TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * 打开今天的日志文件
     */
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
            cleanupOldLogsByDate();
        }
    }


    /**
     * 清理过期的日志文件 (按日期)
     */
    private void cleanupOldLogsByDate() {
        File logDir = new File(config.getLogDirectory());
        File[] files = logDir.listFiles();

        if (files != null) {
            String limitDate = new SimpleDateFormat("yyyy-MM-dd").format(getDateDaysAgo(config.getRetentionDays()));
            for (File file : files) {
                if (file.getName().endsWith(".log") && file.getName().replace(".log", "").compareTo(limitDate) <= 0) {
                    file.delete();
                }
            }
        }
    }

    /**
     * NEW: 增加了对当前日志文件的处理逻辑
     * 清理旧日志直到总大小低于限制
     */
    @SuppressLint("NewApi")
    private void cleanupOldLogsBySize() {
        long maxSize = config.getMaxTotalLogSize();
        if (maxSize <= 0) return;

        File logDir = new File(config.getLogDirectory());
        File[] files = logDir.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".log"));

        if (files == null || files.length == 0) return;

        long totalSize = 0;
        for (File file : files) {
            totalSize += file.length();
        }

        if (totalSize <= maxSize) return;

        Arrays.sort(files, Comparator.comparing(File::getName));

        for (File file : files) {
            if (totalSize <= maxSize) break;

            long fileSize = file.length();

            // 关键修改！处理正在写入的当前日志文件
            boolean isCurrentLogFile = file.getName().equals(currentLogDate + ".log");
            if (isCurrentLogFile && currentWriter != null) {
                currentWriter.close(); // 必须先关闭文件句柄
            }

            if (file.delete()) {
                totalSize -= fileSize;
            }

            // 如果删除的是当前日志文件，需要重新打开它（会创建一个新的空文件）
            if (isCurrentLogFile) {
                File logFile = new File(logDir, currentLogDate + ".log");
                currentWriter.open(logFile);
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
            currentWriter.appendLog(timeStamp + " " + log);
        }
    }

    public synchronized void close() {
        if (currentWriter != null) {
            currentWriter.close();
        }
        if (sizeCleanupScheduler != null && !sizeCleanupScheduler.isShutdown()) {
            sizeCleanupScheduler.shutdown();
        }
    }
}

