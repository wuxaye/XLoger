package com.xaye.xloger;

import android.app.Application;

import com.xaye.loglibrary.LogConfiguration;
import com.xaye.loglibrary.XLogger;
import com.xaye.loglibrary.utils.LogLevel;

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
                .setTag("xLog") // 日志tag，如果开启堆栈跟踪，此值会被覆盖
                .setDebugEnabled(true) // 是否开启日志输出
                .setLogLevel(LogLevel.ALL) // 日志输出级别，默认为LogLevel.ALL,低于此级别的日志将不会输出
                .setStackTraceEnabled(true) //会在日志中打印出当前调用堆栈，方便定位日志
                .setLogDirectory(getApplicationContext().getExternalFilesDir("xloger").getAbsolutePath()) //日志保存目录,外部存储的应用私有目录,不需要权限，默认为/sdcard/Android/data/包名/files/xloger
                .setIsSaveLogEnabled(false) //是否开启日志文件保存功能，默认为false
                .setRetentionDays(3) // 日志保留天数，默认为7天
                .setMaxTotalLogSize(100 * 1024 * 1024) // x指定日志文件总大小，超过此值会自动删除最老的日志文件，默认不限制
                .setLogSizeCheckInterval(10 * 60 * 1000) //  x检查间隔，默认 5分钟，检查日志文件总大小的时间间隔,不设置 maxTotalLogSize ，此值无效
                .build());
    }
}
