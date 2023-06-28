package com.topstep.fitcloud.sample2.model.sport.push

import com.topstep.fitcloud.sdk.v2.model.settings.sport.FcSportSpace

data class SportPushParams(
    val packets: List<SportPacket>,
    val pushableSpaces: List<FcSportSpace>
)