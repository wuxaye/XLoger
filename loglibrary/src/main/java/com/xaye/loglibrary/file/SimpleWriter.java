package com.xaye.loglibrary.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Author xaye
 *
 * @date: 2024/11/4
 */
public class SimpleWriter extends Writer {
    private String logFileName;

    private File logFile;

    private BufferedWriter bufferedWriter;

    @Override
    public boolean open(File file) {
        logFileName = file.getName();
        logFile = file;

        boolean isNewFile = false;

        // Create log file if not exists.
        if (!logFile.exists()) {
            try {
                File parent = logFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                logFile.createNewFile();
                isNewFile = true;
            } catch (Exception e) {
                e.printStackTrace();
                close();
                return false;
            }
        }

        try {
            bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));
            if (isNewFile) {
                onNewFileCreated(logFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            close();
            return false;
        }
        return true;
    }

    @Override
    public boolean isOpened() {
        return bufferedWriter != null && logFile.exists();
    }

    @Override
    public File getOpenedFile() {
        return logFile;
    }

    @Override
    public String getOpenedFileName() {
        return logFileName;
    }


    public void onNewFileCreated(File file) {
    }

    @Override
    public void appendLog(String log) {
        try {
            bufferedWriter.write(log);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean close() {
        if (bufferedWriter != null) {
            try {
                bufferedWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bufferedWriter = null;
        logFileName = null;
        logFile = null;
        return true;
    }
}
