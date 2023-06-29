package com.topstep.fitcloud.sample2.ui.realtime

import java.util.*


data class HeartRateRealtime(
    val time: Date,
    val heartRate: Int
)

data class OxygenRealtime(
    val time: Date,
    val oxygen: Int
)

data class BloodPressureRealtime(
    val time: Date,
    val sbp: Int,
    val dbp: Int,
)

data class TemperatureRealtime(
    val time: Date,
    val body: Float,
    val wrist: Float,
)

data class PressureRealtime(
    val time: Date,
    val pressure: Int,
)