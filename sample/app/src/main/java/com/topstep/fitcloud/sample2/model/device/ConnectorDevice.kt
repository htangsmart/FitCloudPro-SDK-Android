package com.topstep.fitcloud.sample2.model.device

import com.topstep.fitcloud.sample2.data.device.DeviceManager

/**
 * ToNote:Avoid declare as a data class, because the [DeviceManager.rebind] need trigger connection, even when the device is not changed
 */
class ConnectorDevice(
    /**
     * Device mac address
     */
    val address: String,

    /**
     * Device name
     */
    val name: String,

    /**
     * Is trying to bind
     */
    val isTryingBind: Boolean
) {
    override fun toString(): String {
        return "[address:$address name:$name isTryingBind:$isTryingBind]"
    }
}