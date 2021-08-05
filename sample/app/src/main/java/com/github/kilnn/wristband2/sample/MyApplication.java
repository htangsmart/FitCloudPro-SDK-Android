package com.github.kilnn.wristband2.sample;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.github.kilnn.wristband2.sample.net.GlobalApiClient;
import com.github.kilnn.wristband2.sample.syncdata.db.SyncDataDb;
import com.htsmart.wristband2.WristbandApplication;

public class MyApplication extends Application {

    private static SyncDataDb sSyncDataDb;
    private static GlobalApiClient sApiClient;

    private static MyApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        WristbandApplication.init(this);
        WristbandApplication.setDebugEnable(true);
        sInstance = this;
        sSyncDataDb = Room
                .databaseBuilder(this, SyncDataDb.class, "SyncDataDb")
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .allowMainThreadQueries()
                .build();
        sApiClient = new GlobalApiClient(this);
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `DialInfo` (`projectNum` TEXT NOT NULL, `lcd` INTEGER NOT NULL, `toolVersion` TEXT, `dialNum` INTEGER NOT NULL, `binVersion` INTEGER NOT NULL, `imgUrl` TEXT, `deviceImgUrl` TEXT, `binUrl` TEXT, `name` TEXT, `downloadCount` INTEGER NOT NULL, PRIMARY KEY(`projectNum`, `dialNum`))");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `DialInfo` ADD COLUMN `binSize` INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static MyApplication getInstance() {
        return sInstance;
    }

    public static SyncDataDb getSyncDataDb() {
        return sSyncDataDb;
    }

    public static GlobalApiClient getApiClient() {
        return sApiClient;
    }
}
