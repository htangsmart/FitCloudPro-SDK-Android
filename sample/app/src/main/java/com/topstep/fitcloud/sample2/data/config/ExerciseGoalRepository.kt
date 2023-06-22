package com.topstep.fitcloud.sample2.data.config

import com.topstep.fitcloud.sample2.data.db.AppDatabase
import com.topstep.fitcloud.sample2.data.entity.ExerciseGoalEntity
import com.topstep.fitcloud.sample2.data.entity.toModel
import com.topstep.fitcloud.sample2.data.storage.InternalStorage
import com.topstep.fitcloud.sample2.model.config.ExerciseGoal
import com.topstep.fitcloud.sample2.utils.launchWithLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

interface ExerciseGoalRepository {

    /**
     * Flow current exercise goal config.
     *
     * If there is currently an Authentic user and there is a local config, then return it.
     *
     * Otherwise, return a default config.
     */
    val flowCurrent: StateFlow<ExerciseGoal>

    /**
     * 更改某个用户的单位配置。
     * @param userId
     * @param config
     */
    fun modify(userId: Long, config: ExerciseGoal)

}

internal class ExerciseGoalRepositoryImpl constructor(
    private val applicationScope: CoroutineScope,
    internalStorage: InternalStorage,
    private val appDatabase: AppDatabase,
) : ExerciseGoalRepository {

    override val flowCurrent: StateFlow<ExerciseGoal> = internalStorage.flowAuthedUserId.flatMapLatest {
        if (it == null) {
            flowOf(null)
        } else {
            appDatabase.configDao().flowExerciseGoal(it)
        }
    }.map {
        it.toModel()
    }.stateIn(applicationScope, SharingStarted.Eagerly, ExerciseGoal.defaultInstance())

    override fun modify(userId: Long, config: ExerciseGoal) {
        applicationScope.launchWithLog {
            val entity = ExerciseGoalEntity(
                userId = userId,
                step = config.step,
                distance = config.distance,
                calorie = config.calorie,
            )
            appDatabase.configDao().insertExerciseGoal(entity)
        }
    }

}