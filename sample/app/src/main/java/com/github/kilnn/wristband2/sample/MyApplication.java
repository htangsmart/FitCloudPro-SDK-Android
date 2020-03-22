package com.github.kilnn.wristband2.sample;

import android.app.Application;
import androidx.room.Room;

import com.github.kilnn.wristband2.sample.syncdata.db.SyncDataDb;
import com.htsmart.wristband2.WristbandApplication;

public class MyApplication extends Application {

    private static SyncDataDb sSyncDataDb;

    private static MyApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        WristbandApplication.init(this);
        WristbandApplication.setDebugEnable(true);
        sInstance = this;
        sSyncDataDb = Room
                .databaseBuilder(this, SyncDataDb.class, "SyncDataDb")
                .allowMainThreadQueries()
                .build();
    }

    public static MyApplication getInstance() {
        return sInstance;
    }

    public static SyncDataDb getSyncDataDb() {
        return sSyncDataDb;
    }
}
