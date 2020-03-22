package com.github.kilnn.wristband2.sample.syncdata.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {
        StepItem.class,
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
