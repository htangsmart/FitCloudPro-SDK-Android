package com.github.kilnn.wristband2.sample.syncdata.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.github.kilnn.wristband2.sample.dial.entity.DialInfo;
import com.github.kilnn.wristband2.sample.dial.entity.DialInfoDao;

@Database(entities = {
        StepItem.class,
        SleepRecord.class,
        HeartRateItem.class,
        OxygenItem.class,
        BloodPressureItem.class,
        RespiratoryRateItem.class,
        EcgRecord.class,
        SportRecord.class,
        DialInfo.class,
}, version = 3)
public abstract class SyncDataDb extends RoomDatabase {

    public abstract SyncDataDao dao();

    public abstract DialInfoDao dialInfoDao();

}
