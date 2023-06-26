package com.topstep.fitcloud.sample2.model.user

data class UserInfo(
    val id: Long,
    val height: Int,//user height(cm)
    val weight: Int,//user weight(kg)
    val sex: Boolean,//True for male, false for female
    val age: Int
)

/**
 * Calculate step size in meters
 */
fun UserInfo?.getStepLength(): Float {
    var height = 0f
    var man = false
    if (this != null) {
        height = this.height.toFloat()
        man = this.sex
    }
    var stepLength = height * if (man) 0.415f else 0.413f
    if (stepLength < 30) {
        stepLength = 30f
    }
    if (stepLength > 100) {
        stepLength = 100f
    }
    return stepLength / 100
}

/**
 * Obtain body weight in kilograms
 */
fun UserInfo?.getWeight(): Float {
    var weight = 0f
    if (this != null) {
        weight = this.weight.toFloat()
    }
    if (weight <= 0f) {
        weight = 50f
    }
    return weight
}