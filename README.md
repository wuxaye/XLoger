# 日志工具类

开发中...

## 简单使用

在Application中初始化，如果不初始化，则在第一次打印日志时使用默认值进行初始化
```

XLogger.init(new LogConfiguration.Builder()
                .setTag("xLog") // 日志tag，如果开启堆栈跟踪，此值会被覆盖
                .setDebugEnabled(true) // 是否开启日志输出
                .setStackTraceEnabled(true) //会在日志中打印出当前调用堆栈，方便定位日志
                .setLogDirectory(getApplicationContext().getExternalFilesDir("xloger").getAbsolutePath()) //日志保存目录,外部存储的应用私有目录,不需要权限，默认为/sdcard/Android/data/包名/files/xloger
                .setRetentionDays(3) // 日志保留天数，默认为7天
                .build());
```

输出示例
```
2024-11-06 14:49:55.231 18969-18969 xLog                    com.xaye.xloger                      D  hello world
2024-11-06 14:49:55.239 18969-18969 xLog                    com.xaye.xloger                      I  hello world
2024-11-06 14:49:55.241 18969-18969 xLog                    com.xaye.xloger                      W  hello world
2024-11-06 14:49:55.243 18969-18969 xLog                    com.xaye.xloger                      E  hello world

2024-11-06 14:50:16.118 19041-19041 (MainActivity.java:15)  com.xaye.xloger                      D  hello world
2024-11-06 14:50:16.126 19041-19041 (MainActivity.java:16)  com.xaye.xloger                      I  hello world
2024-11-06 14:50:16.128 19041-19041 (MainActivity.java:17)  com.xaye.xloger                      W  hello world
2024-11-06 14:50:16.131 19041-19041 (MainActivity.java:18)  com.xaye.xloger                      E  hello world
```