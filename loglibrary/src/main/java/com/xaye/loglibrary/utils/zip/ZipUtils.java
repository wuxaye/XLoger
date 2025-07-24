package com.xaye.loglibrary.utils.zip;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte 的缓冲区大小
    private static final String TAG = "ZipUtils";
    private static final Handler mainHandler = new Handler(Looper.getMainLooper()); // 用于将回调发送到主线程
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();


    /**
     * 压缩单个文件或文件夹。
     *
     * @param resFile 要压缩的文件（夹）。
     * @param zipFile 生成的压缩文件。
     * @param listener 进度回调监听器 (可选)。
     */
    public static void zipFile(File resFile, File zipFile, ZipListener listener) {
        zipFiles(Collections.singletonList(resFile), zipFile, "", listener);
    }

    /**
     * 压缩单个文件或文件夹。
     *
     * @param resFile 要压缩的文件（夹）。
     * @param zipFile 生成的压缩文件。
     * @param comment 压缩文件的注释。
     * @param listener 进度回调监听器 (可选)。
     */
    public static void zipFile(File resFile, File zipFile, String comment, ZipListener listener) {
        zipFiles(Collections.singletonList(resFile), zipFile, comment, listener);
    }

    /**
     * 批量压缩文件（夹）。
     *
     * @param resFileList 要压缩的文件（夹）列表。
     * @param zipFile 生成的压缩文件。
     * @param comment 压缩文件的注释 (可选，默认为空)。
     * @param listener 进度回调监听器 (可选)。
     */
    public static void zipFiles(Collection<File> resFileList, File zipFile, String comment, ZipListener listener) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (listener != null) {
                mainHandler.post(listener::onStart);
            }

            Map<File, String> filesToZipMap = new LinkedHashMap<>();
            Set<String> emptyDirsToZip = new HashSet<>();

            // 递归收集所有文件和空文件夹
            collectFiles(resFileList, filesToZipMap, emptyDirsToZip);

            int totalItems = filesToZipMap.size() + emptyDirsToZip.size();
            int[] currentItemIndex = {0};

            try {
                if (zipFile.getParentFile() != null) {
                    zipFile.getParentFile().mkdirs();
                }

                try (ZipOutputStream zipOut = new ZipOutputStream(
                        new BufferedOutputStream(new FileOutputStream(zipFile), BUFF_SIZE))) {
                    zipOut.setComment(comment);

                    // 先写入空目录
                    for (String dirEntry : emptyDirsToZip) {
                        currentItemIndex[0]++;
                        zipOut.putNextEntry(new ZipEntry(dirEntry));
                        zipOut.closeEntry();
                        int idx = currentItemIndex[0];
                        if (listener != null) {
                            mainHandler.post(() -> listener.onProgress(dirEntry, 100, totalItems, idx, 0L, 0L));
                        }
                    }

                    // 写入文件内容
                    for (Map.Entry<File, String> entry : filesToZipMap.entrySet()) {
                        currentItemIndex[0]++;
                        File file = entry.getKey();
                        String entryPath = entry.getValue();

                        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file), BUFF_SIZE)) {
                            zipOut.putNextEntry(new ZipEntry(entryPath));
                            byte[] buffer = new byte[BUFF_SIZE];
                            int realLength;
                            long bytesRead = 0;
                            long totalLength = file.length();

                            while ((realLength = in.read(buffer)) != -1) {
                                zipOut.write(buffer, 0, realLength);
                                bytesRead += realLength;
                                int progress = totalLength > 0 ? (int) ((bytesRead * 100.0) / totalLength) : 0;
                                int idx = currentItemIndex[0];
                                if (listener != null) {
                                    long finalBytesRead = bytesRead;
                                    mainHandler.post(() -> listener.onProgress(file.getName(), progress, totalItems, idx, finalBytesRead, totalLength));
                                }
                            }
                            zipOut.flush();
                            zipOut.closeEntry();
                        }
                    }
                }

                if (listener != null) {
                    mainHandler.post(() -> listener.onSuccess(zipFile));
                }

            } catch (Exception e) {
                Log.e(TAG, "压缩失败: " + e.getMessage(), e);
                if (listener != null) {
                    mainHandler.post(() -> listener.onFailure(e, "压缩失败: " + e.getMessage()));
                }
            }
        });
    }


    /**
     * 解压缩一个文件。
     *
     * @param zipFile    压缩文件。
     * @param folderPath 解压缩的目标目录。
     * @param listener   进度回调监听器 (可选)。
     */
    public static void unzipFile(File zipFile, String folderPath, ZipListener listener) {
        new Thread(() -> {
            if (listener != null) {
                mainHandler.post(listener::onStart);
            }

            File desDir = new File(folderPath);
            if (!desDir.exists()) {
                desDir.mkdirs(); // 如果目标目录不存在，则创建
            }

            try (ZipFile zf = new ZipFile(zipFile)) {
                Log.i(TAG, "开始解压: " + zipFile.getPath());

                // 将 Enumeration 转为 List 以统计总数
                Enumeration<? extends ZipEntry> entriesEnum = zf.entries();
                List<ZipEntry> entryList = new ArrayList<>();
                while (entriesEnum.hasMoreElements()) {
                    entryList.add(entriesEnum.nextElement());
                }

                int totalEntries = entryList.size();
                int entriesProcessed = 0;

                for (ZipEntry zipEntry : entryList) {
                    entriesProcessed++;
                    String entryName = getEntryName(zipEntry);

                    File desFile = new File(desDir, entryName);

                    if (zipEntry.isDirectory()) {
                        desFile.mkdirs();
                        if (listener != null) {
                            int finalEntriesProcessed = entriesProcessed;
                            mainHandler.post(() -> listener.onProgress(entryName, 100, totalEntries, finalEntriesProcessed, 0, 0));
                        }
                        continue;
                    }

                    if (desFile.getParentFile() != null) {
                        desFile.getParentFile().mkdirs();
                    }

                    if (!desFile.exists()) {
                        desFile.createNewFile();
                    }

                    try (InputStream in = zf.getInputStream(zipEntry);
                         FileOutputStream out = new FileOutputStream(desFile)) {

                        byte[] buffer = new byte[BUFF_SIZE];
                        int realLength;
                        long bytesReadForCurrentEntry = 0;
                        long totalEntryBytes = zipEntry.getSize();

                        while ((realLength = in.read(buffer)) > 0) {
                            out.write(buffer, 0, realLength);
                            bytesReadForCurrentEntry += realLength;

                            int progress = totalEntryBytes > 0
                                    ? (int) ((bytesReadForCurrentEntry * 100f) / totalEntryBytes)
                                    : 0;

                            if (listener != null) {
                                int finalEntriesProcessed = entriesProcessed;
                                long finalBytesRead = bytesReadForCurrentEntry;
                                long finalTotalBytes = totalEntryBytes;
                                mainHandler.post(() -> listener.onProgress(
                                        entryName, progress, totalEntries, finalEntriesProcessed,
                                        finalBytesRead, finalTotalBytes));
                            }
                        }
                    }
                }

                Log.i(TAG, "结束解压 成功: " + zipFile.getPath());
                if (listener != null) {
                    mainHandler.post(() -> listener.onSuccess(zipFile));
                }

            } catch (Exception e) {
                Log.e(TAG, "解压失败: " + e.getMessage(), e);
                if (listener != null) {
                    mainHandler.post(() -> listener.onFailure(e, "解压失败: " + e.getMessage()));
                }
            }
        }).start();
    }

    /**
     * 解压文件名包含传入文字的文件。
     *
     * @param zipFile       压缩文件。
     * @param folderPath    目标文件夹。
     * @param nameContains  传入的文件匹配名。
     * @param listener      进度回调监听器 (可选)。
     * @return 解压出的文件列表（注意：由于是异步执行，返回列表可能为空，建议通过 listener 获取结果）。
     */
    public static List<File> upZipSelectedFile(File zipFile, String folderPath, String nameContains, ZipListener listener) {
        List<File> fileList = new ArrayList<>(); // 用于存储解压出的文件列表

        executor.execute(() -> {
            if (listener != null) {
                mainHandler.post(listener::onStart);
            }

            File desDir = new File(folderPath);
            if (!desDir.exists()) {
                desDir.mkdirs(); // 如果目标目录不存在，则创建
            }

            try (ZipFile zf = new ZipFile(zipFile)) {
                Enumeration<? extends ZipEntry> entries = zf.entries();
                List<ZipEntry> filteredEntries = new ArrayList<>();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.getName().contains(nameContains)) {
                        filteredEntries.add(entry);
                    }
                }

                int totalEntries = filteredEntries.size(); // 过滤后的总条目数
                int entriesProcessed = 0;

                for (ZipEntry zipEntry : filteredEntries) {
                    entriesProcessed++;
                    String entryName = getEntryName(zipEntry); // 获取处理编码后的文件名

                    File desFile = new File(desDir, entryName);

                    // 如果是目录，直接创建
                    if (zipEntry.isDirectory()) {
                        desFile.mkdirs();
                        if (listener != null) {
                            final int finalEntriesProcessed = entriesProcessed;
                            final long size = zipEntry.getSize(); // 目录大小通常为0，但为了符合接口规范传入
                            mainHandler.post(() -> listener.onProgress(
                                    entryName,                  // 当前处理的文件名
                                    100,                        // 进度100%
                                    totalEntries,              // 总条目数
                                    finalEntriesProcessed,     // 当前索引
                                    size,                      // 已处理字节数
                                    size                       // 总字节数
                            ));
                        }
                        continue;
                    }

                    // 创建父目录
                    File parent = desFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    // 写入文件
                    try (
                            InputStream in = zf.getInputStream(zipEntry);
                            FileOutputStream out = new FileOutputStream(desFile)
                    ) {
                        byte[] buffer = new byte[BUFF_SIZE];
                        int len;
                        long bytesRead = 0L;
                        long totalSize = zipEntry.getSize();

                        while ((len = in.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                            bytesRead += len;

                            int progress = (totalSize > 0)
                                    ? (int) ((bytesRead * 100) / totalSize)
                                    : 0;

                            if (listener != null) {
                                final int finalProgress = progress;
                                final long finalBytesRead = bytesRead;
                                final int finalEntriesProcessed = entriesProcessed;
                                mainHandler.post(() -> listener.onProgress(entryName, finalProgress, totalEntries, finalEntriesProcessed, finalBytesRead, totalSize));
                            }
                        }
                    }

                    fileList.add(desFile);
                }

                if (listener != null) {
                    mainHandler.post(() -> listener.onSuccess(zipFile));
                }
            } catch (Exception e) {
                Log.e(TAG, "解压选定文件失败: " + e.getMessage(), e);
                if (listener != null) {
                    mainHandler.post(() -> listener.onFailure(e, "解压选定文件失败: " + e.getMessage()));
                }
            }
        });

        return fileList; // 注意：主线程返回的是空列表，使用 listener.onSuccess 获取真正解压结果
    }

    private static void collectFiles(Collection<File> resFileList, Map<File, String> filesToZipMap, Set<String> emptyDirsToZip) {
        for (File rootFile : resFileList) {
            collect(rootFile, "", filesToZipMap, emptyDirsToZip);
        }
    }

    private static void collect(File file, String parentPath, Map<File, String> filesMap, Set<String> emptyDirs) {
        String currentPath = parentPath.isEmpty() ? file.getName() : parentPath + File.separator + file.getName();

        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles == null || subFiles.length == 0) {
                emptyDirs.add(currentPath + "/");
            } else {
                for (File sub : subFiles) {
                    collect(sub, currentPath, filesMap, emptyDirs);
                }
            }
        } else {
            filesMap.put(file, currentPath);
        }
    }


    /**
     * 获得压缩文件内文件列表。
     *
     * @param zipFile 压缩文件。
     * @return 压缩文件内文件名称列表。
     * @throws ZipException 压缩文件格式有误时抛出。
     * @throws IOException  IO错误时抛出。
     */
    public static ArrayList<String> getEntriesNames(File zipFile) throws ZipException, IOException {
        ArrayList<String> entryNames = new ArrayList<>();
        List<ZipEntry> entries = getEntriesList(zipFile); // 使用 getEntriesList 方法获取条目列表，确保 ZipFile 被正确关闭
        for (ZipEntry entry : entries) {
            // 保持原始的编码转换逻辑，以兼容特定环境下的文件名编码问题
            // 如果文件名出现乱码，可能需要调整这里的编码方式（例如尝试 UTF-8 或 GBK）
            entryNames.add(new String(getEntryName(entry).getBytes("GB2312"), "8859_1"));
        }
        return entryNames;
    }

    /**
     * 获得压缩文件内压缩文件对象以取得其属性。
     * 此方法内部会关闭 ZipFile。
     *
     * @param zipFile 压缩文件。
     * @return 返回一个压缩文件条目列表。
     * @throws ZipException 压缩文件格式有误时抛出。
     * @throws IOException  IO操作有误时抛出。
     */
    public static List<ZipEntry> getEntriesList(File zipFile) throws ZipException, IOException {
        List<ZipEntry> entryList = new ArrayList<>();
        try (ZipFile zf = new ZipFile(zipFile)) { // try-with-resources 确保 ZipFile 在操作完成后关闭
            Enumeration<? extends ZipEntry> entriesEnum = zf.entries();
            while (entriesEnum.hasMoreElements()) {
                entryList.add(entriesEnum.nextElement()); // 将 Enumeration 转换为 List 并返回
            }
        }
        return entryList;
    }

    /**
     * 取得压缩文件对象的注释。
     *
     * @param entry 压缩文件对象。
     * @return 压缩文件对象的注释。
     * @throws UnsupportedEncodingException 字符集转换异常。
     */
    public static String getEntryComment(ZipEntry entry) throws UnsupportedEncodingException {
        // 保持原始的编码转换逻辑
        if (entry.getComment() == null) return "";
        return new String(entry.getComment().getBytes("GB2312"), "8859_1");
    }

    /**
     * 取得压缩文件对象的名称。
     *
     * @param entry 压缩文件对象。
     * @return 压缩文件对象的名称。
     * @throws UnsupportedEncodingException 字符集转换异常。
     */
    public static String getEntryName(ZipEntry entry) throws UnsupportedEncodingException {
        // 保持原始的编码转换逻辑
        return new String(entry.getName().getBytes("GB2312"), "8859_1");
    }
}
