package com.topstep.fitcloud.sample2.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.topstep.fitcloud.sample2.data.entity.StringTypedEntity
import com.topstep.fitcloud.sample2.data.entity.TodayStepData
import com.topstep.fitcloud.sample2.utils.runCatchingWithLog
import kotlinx.coroutines.flow.Flow

@Dao
abstract class StringTypedDao(database: AppDatabase) {

    private val moshi = database.moshi

    @Query("SELECT data FROM StringTypedEntity WHERE userId=:userId AND type=:type")
    protected abstract suspend fun queryData(userId: Long, type: Int): String?

    @Query("SELECT data FROM StringTypedEntity WHERE userId=:userId AND type=:type")
    protected abstract fun flowData(userId: Long, type: Int): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insert(entity: StringTypedEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insert(entities: List<StringTypedEntity>)

    @Query("DELETE FROM StringTypedEntity WHERE userId=:userId AND type=:type")
    abstract suspend fun delete(userId: Long, type: Int)

    suspend fun getTodayStepData(userId: Long): TodayStepData? {
        val data = queryData(userId, StringTypedEntity.TODAY_STEP_DATA)
        if (data.isNullOrEmpty()) return null
        return runCatchingWithLog {
            moshi.adapter(TodayStepData::class.java).fromJson(data)
        }.getOrNull()
    }

    suspend fun setTodayStepData(userId: Long, data: TodayStepData?) {
        if (data == null) {
            delete(userId, StringTypedEntity.TODAY_STEP_DATA)
        } else {
            insert(
                StringTypedEntity(
                    userId, StringTypedEntity.TODAY_STEP_DATA,
                    moshi.adapter(TodayStepData::class.java).toJson(data)
                )
            )
        }
    }

}