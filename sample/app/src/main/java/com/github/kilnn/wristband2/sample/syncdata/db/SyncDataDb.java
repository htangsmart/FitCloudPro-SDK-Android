package com.github.kilnn.wristband2.sample.syncdata.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {
//        StepItem.class,
        SleepRecord.class,
        HeartRateItem.class,
        OxygenItem.class,
        BloodPressureItem.class,
        RespiratoryRateItem.class,
        EcgRecord.class,
        SportRecord.class,
}, version = 1)
public abstract class SyncDataDb extends RoomDatabase {

    public abstract SyncDataDao dao();

}
