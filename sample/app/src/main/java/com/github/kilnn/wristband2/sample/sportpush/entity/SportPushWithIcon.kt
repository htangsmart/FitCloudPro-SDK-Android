package com.github.kilnn.wristband2.sample.sportpush.entity

data class SportPushWithIcon(
    val sportType: Int,
    val pushEnabled: Boolean,
    val binFlag: Byte,
    val iconUrl:String
)