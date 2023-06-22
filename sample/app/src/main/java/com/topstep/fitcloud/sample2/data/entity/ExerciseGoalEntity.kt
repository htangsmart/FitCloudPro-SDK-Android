package com.topstep.fitcloud.sample2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.topstep.fitcloud.sample2.model.config.ExerciseGoal

@Entity
data class ExerciseGoalEntity(
    /**
     * 用户Id
     */
    @PrimaryKey
    val userId: Long,

    val step: Int,

    val distance: Float,

    val calorie: Int,
)

fun ExerciseGoalEntity?.toModel(): ExerciseGoal {
    return if (this == null) {
        ExerciseGoal.defaultInstance()
    } else {
        ExerciseGoal(
            step = step,
            distance = distance,
            calorie = calorie
        )
    }
}