package com.xaye.xloger;

import android.app.Application;
import android.os.Environment;

import com.xaye.loglibrary.LogConfiguration;
import com.xaye.loglibrary.XLogger;

/**
 * Author xaye
 *
 * @date: 2024/11/6
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        XLogger.init(new LogConfiguration.Builder()
                .setTag("xLog")
                .setDebugEnabled(true)
                .setStackTraceEnabled(true)
                .setLogDirectory(getApplicationContext().getExternalFilesDir("xloger").getAbsolutePath())
                .setRetentionDays(3)
                .build());
    }
}
