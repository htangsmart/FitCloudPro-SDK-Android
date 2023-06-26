package com.topstep.fitcloud.sample2.data.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TodayStepData(
    val timestamp: Long,
    val step: Int,
    val distance: Float,//km
    val calories: Float//kilocalorie
)