package com.xaye.loglibrary.file;

import java.io.File;

/**
 * Author xaye
 *
 * @date: 2024/11/4
 */
public abstract class Writer {
    public abstract boolean open(File file);

    public abstract boolean isOpened();

    public abstract File getOpenedFile();

    public abstract String getOpenedFileName();

    public abstract void appendLog(String log);

    public abstract boolean close();
}
