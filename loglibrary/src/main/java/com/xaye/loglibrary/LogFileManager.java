package com.xaye.loglibrary;

import android.annotation.SuppressLint;
import android.util.Log;

import com.xaye.loglibrary.file.SimpleWriter;
import com.xaye.loglibrary.utils.zip.ZipListener;
import com.xaye.loglibrary.utils.zip.ZipUtils;

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
 * 按日期创建和切换日志文件，每天生成一个新日志文件；
 * 定时清理过期日志（按保留天数）；
 * 控制日志总大小，超出限制时删除旧文件，支持处理当前正在写入的日志文件；
 * 低优先级后台线程执行清理任务，避免影响主线程。
 * 
 * 新增功能：
 * - 自动压缩历史日志文件（除了当天的日志文件）
 * - 压缩成功后删除原始日志文件，只保留压缩文件
 * - 使用新的命名格式：2025_07_23_log.zip
 * - 压缩过程不影响当前日志写入
 */
final class LogFileManager {
    private static final String TAG = "LogFileManager";
    private static LogFileManager instance;
    private SimpleWriter currentWriter;
    private String currentLogDate;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final LogConfiguration config;
    private final ScheduledExecutorService sizeCleanupScheduler;
    private final ScheduledExecutorService compressionScheduler;

    private LogFileManager(LogConfiguration config) {
        this.config = config;
        
        // 创建大小清理的后台调度器
        this.sizeCleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "LogSizeCleanupThread");
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
        
        // 创建压缩任务的后台调度器
        this.compressionScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "LogCompressionThread");
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
        
        openNewLogForToday();
        startBackgroundSizeCleanup();
    }

    public static synchronized LogFileManager getInstance(LogConfiguration config) {
        if (instance == null) {
            instance = new LogFileManager(config);
        }
        return instance;
    }

    /**
     * 启动大小清理的后台任务
     */
    private void startBackgroundSizeCleanup() {
        if (config.getMaxTotalLogSize() > 0) {
            long interval = config.getLogSizeCheckInterval();
            sizeCleanupScheduler.scheduleWithFixedDelay(
                    this::cleanupOldLogsBySize,
                    interval,
                    interval,
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
            
            // 清理过期文件
            cleanupOldLogsByDate();
            
            // 压缩历史日志文件（异步执行，不阻塞日志写入）
            compressionScheduler.execute(this::compressHistoryLogFiles);
        }
    }

    /**
     * 压缩历史日志文件
     * 在每天第一次写入日志时执行，确保历史日志被压缩
     */
    private void compressHistoryLogFiles() {
        try {
            File logDir = new File(config.getLogDirectory());
            File[] files = logDir.listFiles(file -> 
                file.isFile() && 
                file.getName().endsWith(".log") && 
                !file.getName().equals(currentLogDate + ".log") // 排除当天的日志文件
            );
            
            if (files == null || files.length == 0) {
                return;
            }
            
            for (File logFile : files) {
                String fileName = logFile.getName();
                String dateStr = fileName.replace(".log", "");
                
                // 检查是否符合日期格式 yyyy-MM-dd
                if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    // 生成新的压缩文件名称格式：2025_07_23_log.zip
                    String zipFileName = dateStr.replace("-", "_") + "_log.zip";
                    File zipFile = new File(logDir, zipFileName);
                    
                    // 检查压缩文件是否已存在
                    if (!zipFile.exists()) {
                        compressSingleLogFile(logFile, zipFile);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "压缩历史日志文件异常", e);
        }
    }

    /**
     * 压缩单个日志文件
     */
    private void compressSingleLogFile(File logFile, File zipFile) {
        ZipUtils.zipFile(logFile, zipFile, new ZipListener() {
            @Override
            public void onStart() {
                Log.d(TAG, "开始压缩日志文件：" + logFile.getName());
            }

            @Override
            public void onProgress(String fileName, int progress, int totalItems, int currentItemIndex, long bytesRead, long totalBytes) {
                Log.d(TAG, "正在压缩日志文件：" + logFile.getName() + "，进度：" + progress + "%");
            }

            @Override
            public void onSuccess(File resultZipFile) {
                Log.d(TAG, "压缩日志文件成功：" + logFile.getName() + " -> " + resultZipFile.getName());
                // 压缩成功，删除原始日志文件
                if (logFile.exists()) {
                    if (logFile.delete()) {
                        Log.d(TAG, "已删除原始日志文件：" + logFile.getName());
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String message) {
                Log.e(TAG, "压缩日志文件失败：" + logFile.getName(), e);
                // 压缩失败，保留原始文件
            }
        });
    }

    /**
     * 清理过期的日志文件 (按日期)
     * 同时清理 .log 和 .zip 文件
     */
    private void cleanupOldLogsByDate() {
        File logDir = new File(config.getLogDirectory());
        File[] files = logDir.listFiles();

        if (files != null) {
            String limitDate = new SimpleDateFormat("yyyy-MM-dd").format(getDateDaysAgo(config.getRetentionDays()));
            for (File file : files) {
                String fileName = file.getName();
                String fileDate = null;
                
                if (fileName.endsWith(".log")) {
                    fileDate = fileName.replace(".log", "");
                } else if (fileName.endsWith(".zip")) {
                    // 处理新的命名格式：2025_07_23_log.zip
                    if (fileName.matches("\\d{4}_\\d{2}_\\d{2}_log\\.zip")) {
                        String dateStr = fileName.replace("_log.zip", "");
                        fileDate = dateStr.replace("_", "-"); // 转换为 yyyy-MM-dd 格式用于比较
                    }
                    // 兼容旧格式
                    else if (fileName.matches("\\d{4}-\\d{2}-\\d{2}\\.zip")) {
                        fileDate = fileName.replace(".zip", "");
                    } else if (fileName.matches("\\d{4}-\\d{2}-\\d{2}_\\d{4}\\.zip")) {
                        fileDate = fileName.substring(0, 10); // 提取日期部分
                    }
                }
                
                if (fileDate != null && fileDate.compareTo(limitDate) <= 0) {
                    file.delete();
                }
            }
        }
    }

    /**
     * 清理旧日志直到总大小低于限制
     * 同时处理 .log 和 .zip 文件
     */
    @SuppressLint("NewApi")
    private void cleanupOldLogsBySize() {
        long maxSize = config.getMaxTotalLogSize();
        if (maxSize <= 0) return;

        File logDir = new File(config.getLogDirectory());
        File[] files = logDir.listFiles(pathname -> pathname.isFile() && 
            (pathname.getName().endsWith(".log") || 
             pathname.getName().endsWith(".zip")));

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
            boolean isCurrentLogFile = file.getName().equals(currentLogDate + ".log");
            
            if (isCurrentLogFile && currentWriter != null) {
                currentWriter.close();
            }

            if (file.delete()) {
                totalSize -= fileSize;
            }

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
        if (compressionScheduler != null && !compressionScheduler.isShutdown()) {
            compressionScheduler.shutdown();
        }
    }
}