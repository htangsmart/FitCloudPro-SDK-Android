package com.topstep.fitcloud.sample2.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["name"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,//unique username
    val password: String,
    val height: Int,//user height(cm)
    val weight: Int,//user weight(kg)
    val sex: Boolean,//True for male, false for female
    val age: Int
)