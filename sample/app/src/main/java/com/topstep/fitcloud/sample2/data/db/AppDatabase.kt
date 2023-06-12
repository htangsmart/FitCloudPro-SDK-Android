package com.topstep.fitcloud.sample2.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.topstep.fitcloud.sample2.data.entity.DeviceBindEntity
import com.topstep.fitcloud.sample2.data.entity.MenstruationTimelineEntity
import com.topstep.fitcloud.sample2.data.entity.UserEntity
import com.topstep.fitcloud.sample2.data.entity.WomenHealthConfigEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor

@Database(
    version = 2,
    entities = [
        UserEntity::class,
        DeviceBindEntity::class,
        WomenHealthConfigEntity::class, MenstruationTimelineEntity::class
    ],
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun configDao(): ConfigDao

    abstract fun womenHealthDao(): WomenHealthDao

    companion object {
        private const val DB_NAME = "db_sample2"

        fun build(context: Context, ioDispatcher: CoroutineDispatcher): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .setQueryExecutor(ioDispatcher.asExecutor())
                //Because this is a sample, version migration is not necessary. So use destructive recreate to avoid crash.
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}