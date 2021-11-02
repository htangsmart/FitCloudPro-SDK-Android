package com.github.kilnn.wristband2.sample.sportpush.entity

data class SportPushParam(
    val listExist: ArrayList<SportPushWithIcon>,
    val listNotExist: ArrayList<SportBinItem>,
)