package com.topstep.fitcloud.sample2.model.config

data class ExerciseGoal(
    val step: Int = 8000,
    /**
     * unit km
     */
    val distance: Float = 5.0f,

    /**
     * unit kcal
     */
    val calorie: Int = 240,
) {
    companion object {
        fun defaultInstance(): ExerciseGoal {
            return ExerciseGoal()
        }
    }
}