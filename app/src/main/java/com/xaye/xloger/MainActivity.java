package com.xaye.xloger;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.xaye.loglibrary.XLogger;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String URL = "https://www.wanandroid.com//hotkey/json"; // 示例 URL，返回 JSON 格式数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        fetchJsonData();
    }

    private void fetchJsonData() {
        // 创建 OkHttpClient 实例
        OkHttpClient client = new OkHttpClient();

        // 创建请求对象
        Request request = new Request.Builder()
                .url(URL)
                .build();

        // 发起异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 请求失败时打印错误日志
                XLogger.e("Network request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 获取响应的 JSON 字符串
                    String jsonResponse = response.body().string();

                    // 打印格式化的 JSON
                    XLogger.i("Response JSON: " + jsonResponse);

                    // 使用 JsonLogFormatter 格式化 JSON 字符

                    // 打印格式化后的 JSON
                    XLogger.iJson(jsonResponse);
                } else {
                    // 请求失败，打印响应错误信息
                    XLogger.e("Request failed with code: " + response.code());
                }
            }
        });
    }
}