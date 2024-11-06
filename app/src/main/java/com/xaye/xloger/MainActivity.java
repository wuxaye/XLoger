package com.xaye.xloger;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.xaye.loglibrary.XLogger;
import com.xaye.loglibrary.XLoggerManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        XLogger.d("hello world");
    }
}