package com.sleepdotsdk.demo;

import com.sleepace.sdk.util.SdkLog;

import android.app.Application;


public class DemoApp extends Application {

    private static DemoApp instance;
    
    public StringBuffer logBuf = new StringBuffer();

    public static DemoApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SdkLog.init(this);
        SdkLog.setLogEnable(true);
        String logDir = getExternalFilesDir("log").getPath();
        SdkLog.setLogDir(logDir);
        SdkLog.setSaveLog(true, "log.txt");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        // 低内存的时候执行
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        // 程序在内存清理的时候执行
        super.onTrimMemory(level);

    }

}














