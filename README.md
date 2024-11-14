# 日志工具类

开发中...

## 功能包括

- 日志存文件、文件保留天数
- 日志打印堆栈信息
- 支持xml、json日志格式输出
- 更多功能，开发中...

## 简单使用

```
        //未指定TAG，默认展示 XLoger ,配置全局日志TAG后显示全局TAG，开启堆栈跟踪 显示日志打印文件名及具体行数
        XLogger.d("hello world");
        XLogger.i("hello world");
        XLogger.w("hello world");
        XLogger.e("hello world");

        //指定TAG，优先级最高
        XLogger.d(TAG, "hello world");
        XLogger.i(TAG, "hello world");
        XLogger.w(TAG, "hello world");
        XLogger.e(TAG, "hello world");
        
        XLogger.wtf("严重警告");
        
        XLogger.e("严重警告",new Throwable("我 是 Throwable"));
```

在Application中初始化，如果不初始化，则在第一次打印日志时使用默认值进行初始化

```
        XLogger.init(new LogConfiguration.Builder()
                .setTag("xLog") // 日志tag，如果开启堆栈跟踪，此值会被覆盖
                .setDebugEnabled(true) // 是否开启日志输出
                .setLogLevel(LogLevel.ALL) // 日志输出级别，默认为LogLevel.ALL,低于此级别的日志将不会输出
                .setStackTraceEnabled(true) //会在日志中打印出当前调用堆栈，方便定位日志
                .setLogDirectory(getApplicationContext().getExternalFilesDir("xloger").getAbsolutePath()) //日志保存目录,外部存储的应用私有目录,不需要权限，默认为/sdcard/Android/data/包名/files/xloger
                .setIsSaveLogEnabled(false) //是否开启日志文件保存功能，默认为false
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

2024-11-14 16:46:03.947 21251-21251 MainActivity            com.xaye.xloger                      I  hello world
2024-11-14 16:46:03.947 21251-21251 MainActivity            com.xaye.xloger                      W  hello world
2024-11-14 16:46:03.947 21251-21251 MainActivity            com.xaye.xloger                      E  hello world

2024-11-14 17:39:13.576 21650-21650 MainActivity.java:40    com.xaye.xloger                      E  严重警告

2024-11-14 17:37:40.698 21575-21575 MainActivity.java:40    com.xaye.xloger                      E  严重警告
                                                                                                    Exception: java.lang.Throwable
                                                                                                    Message: 我 是 Throwable
                                                                                                    	at com.xaye.xloger.MainActivity.onCreate(MainActivity.java:40)
                                                                                                    	at android.app.Activity.performCreate(Activity.java:8022)
                                                                                                    	at android.app.Activity.performCreate(Activity.java:8006)
                                                                                                    	at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1309)
                                                                                                    	at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3404)
                                                                                                    	at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3595)
                                                                                                    	at android.app.servertransaction.LaunchActivityItem.execute(LaunchActivityItem.java:85)
                                                                                                    	at android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:135)
                                                                                                    	at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:95)
                                                                                                    	at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2066)
                                                                                                    	at android.os.Handler.dispatchMessage(Handler.java:106)
                                                                                                    	at android.os.Looper.loop(Looper.java:223)
```