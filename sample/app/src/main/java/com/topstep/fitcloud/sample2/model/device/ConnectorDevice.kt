package com.topstep.fitcloud.sample2.model.device

data class ConnectorDevice(
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
)