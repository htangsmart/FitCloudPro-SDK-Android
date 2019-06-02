package com.github.kilnn.wristband2.sample;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.github.kilnn.wristband2.sample.syncdata.db.SyncDataDb;
import com.htsmart.wristband2.WristbandApplication;

public class MyApplication extends Application {

    private static SyncDataDb sSyncDataDb;

    @Override
    public void onCreate() {
        super.onCreate();
        WristbandApplication.init(this);
        WristbandApplication.setDebugEnable(true);
        sSyncDataDb = Room
                .databaseBuilder(this, SyncDataDb.class, "SyncDataDb")
                .allowMainThreadQueries()
                .build();
    }

    public static SyncDataDb getSyncDataDb() {
        return sSyncDataDb;
    }
}
