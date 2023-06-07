package com.topstep.fitcloud.sample2.model.user

data class UserInfo(
    val height: Int,//user height(cm)
    val weight: Int,//user weight(kg)
    val sex: Boolean,//True for male, false for female
    val age: Int
)