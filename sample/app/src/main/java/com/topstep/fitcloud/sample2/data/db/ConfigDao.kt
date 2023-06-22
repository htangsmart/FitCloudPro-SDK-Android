package com.topstep.fitcloud.sample2.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.topstep.fitcloud.sample2.data.entity.DeviceBindEntity
import com.topstep.fitcloud.sample2.data.entity.ExerciseGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ConfigDao {

    @Query("SELECT * FROM DeviceBindEntity WHERE userId=:userId")
    abstract fun flowDeviceBind(userId: Long): Flow<DeviceBindEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDeviceBind(vararg deviceBind: DeviceBindEntity)

    @Query("DELETE FROM DeviceBindEntity WHERE userId=:userId")
    abstract suspend fun clearDeviceBind(userId: Long)

    @Query("SELECT * FROM ExerciseGoalEntity WHERE userId=:userId")
    abstract fun flowExerciseGoal(userId: Long): Flow<ExerciseGoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertExerciseGoal(vararg configs: ExerciseGoalEntity)
}