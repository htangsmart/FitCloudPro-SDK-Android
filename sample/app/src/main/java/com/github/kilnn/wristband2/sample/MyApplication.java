package com.github.kilnn.wristband2.sample;

import android.app.Application;

import com.htsmart.wristband2.WristbandApplication;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        WristbandApplication.init(this);
        WristbandApplication.setDebugEnable(true);
    }
}
