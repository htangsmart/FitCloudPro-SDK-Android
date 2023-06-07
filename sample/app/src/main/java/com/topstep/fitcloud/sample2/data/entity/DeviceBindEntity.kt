package com.topstep.fitcloud.sample2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.topstep.fitcloud.sample2.model.device.ConnectorDevice

@Entity
data class DeviceBindEntity(
    /**
     * 用户Id
     */
    @PrimaryKey
    val userId: Long,

    /**
     * 设备地址
     */
    val address: String,

    /**
     * 设备名称
     */
    val name: String,
)

internal fun DeviceBindEntity?.toModel(): ConnectorDevice? {
    return if (this == null) {
        null
    } else {
        ConnectorDevice(
            address = address,
            name = name,
            isTryingBind = false
        )
    }
}