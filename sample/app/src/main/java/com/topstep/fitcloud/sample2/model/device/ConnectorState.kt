package com.topstep.fitcloud.sample2.model.device

/**
 * Simplify FcConnectorState and add additional states required by the application layer
 */
enum class ConnectorState {
    /**
     * No device set
     */
    NO_DEVICE,

    /**
     * Bluetooth adapter is disabled
     */
    BT_DISABLED,

    /**
     * Disconnect
     */
    DISCONNECTED,

    /**
     * Although the device is not connected at this time, it will make the next connection after waiting for a certain period of time.
     */
    PRE_CONNECTING,

    /**
     * Connecting
     */
    CONNECTING,

    /**
     * Connected
     */
    CONNECTED;
}