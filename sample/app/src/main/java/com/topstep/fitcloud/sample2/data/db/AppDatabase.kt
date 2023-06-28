package com.topstep.fitcloud.sample2.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.squareup.moshi.Moshi
import com.topstep.fitcloud.sample2.data.entity.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor

@Database(
    version = 5,
    entities = [
        UserEntity::class,
        DeviceBindEntity::class, ExerciseGoalEntity::class,
        WomenHealthConfigEntity::class, MenstruationTimelineEntity::class,
        StringTypedEntity::class,
        StepItemEntity::class, SleepItemEntity::class,
        HeartRateItemEntity::class, OxygenItemEntity::class, BloodPressureItemEntity::class, TemperatureItemEntity::class, PressureItemEntity::class,
        EcgRecordEntity::class, GameRecordEntity::class,
        SportRecordEntity::class, SportGpsEntity::class
    ],
)
abstract class AppDatabase : RoomDatabase() {

    lateinit var moshi: Moshi

    abstract fun userDao(): UserDao

    abstract fun configDao(): ConfigDao

    abstract fun womenHealthDao(): WomenHealthDao

    abstract fun stringTypedDao(): StringTypedDao

    abstract fun syncDataDao(): SyncDataDao

    companion object {
        private const val DB_NAME = "db_sample2"

        fun build(context: Context, ioDispatcher: CoroutineDispatcher, moshi: Moshi): AppDatabase {
            val database = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .setQueryExecutor(ioDispatcher.asExecutor())
                //Because this is a sample, version migration is not necessary. So use destructive recreate to avoid crash.
                .fallbackToDestructiveMigration()
                .build()
            database.moshi = moshi
            return database
        }
    }
}