package com.xaye.loglibrary.utils.zip;

import java.io.File;

/**
 * 压缩/解压操作的进度回调监听器。
 */
public interface ZipListener {

    /**
     * 压缩/解压任务开始时调用。
     */
    void onStart();

    /**
     * 压缩/解压进度更新时调用。
     *
     * @param currentFileName 当前正在处理的文件/条目名称。
     * @param progress 当前文件/条目的处理进度 (0-100)。
     * @param totalItems 总文件/条目数 (包括空文件夹，但进度主要针对文件)。
     * @param currentItemIndex 当前处理的文件/条目索引 (从1开始)。
     * @param totalBytesProcessed 已经处理的总字节数 (当前文件/条目已处理的字节数)。
     * @param totalBytesToProcess 需要处理的总字节数 (当前文件/条目总字节数)。
     */
    void onProgress(String currentFileName, int progress, int totalItems, int currentItemIndex,
                    long totalBytesProcessed, long totalBytesToProcess);

    /**
     * 压缩/解压成功时调用。
     *
     * @param zipFile 生成的压缩文件或解压操作所针对的源压缩文件。
     */
    void onSuccess(File zipFile);

    /**
     * 压缩/解压失败时调用。
     *
     * @param e 发生的异常。
     * @param message 失败的描述信息。
     */
    void onFailure(Exception e, String message);
}
