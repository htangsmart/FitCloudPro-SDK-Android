package com.topstep.fitcloud.sample2.data.entity

import androidx.room.Entity

/**
 * Save some data as String in database
 */
@Entity(primaryKeys = ["userId", "type"])
data class StringTypedEntity(
    val userId: Long,
    val type: Int,
    val data: String?
) {

    companion object {
        const val TODAY_STEP_DATA = 1001
    }

}