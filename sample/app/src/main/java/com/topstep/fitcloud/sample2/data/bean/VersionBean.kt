package com.topstep.fitcloud.sample2.data.bean

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.topstep.fitcloud.sample2.model.version.HardwareType
import com.topstep.fitcloud.sample2.model.version.HardwareUpgradeInfo
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo

@JsonClass(generateAdapter = true)
data class VersionBean(
    /**
     * New version of hardwareInfo
     * Only return if the server has a new version, otherwise return null
     */
    val hardwareInfo: String?,

    /**
     * Update information
     */
    val hardwareRemark: String?,

    /**
     * Download url
     */
    val hardwareUrl: String?,

    /**
     * Hardware upgrade type
     */
    val hardwareType: String?,

    /**
     * [hardwareUrl] file size(unit bytes)。
     */
    val hardwareSize: Long = 0,

    /**
     * Whether to force upgrade
     */
    @Json(name = "forceUpgrade")
    val isHardwareForce: Boolean = false,

    /**
     * UI version that can be overwritten
     */
    val uiVersionScope: String? = null,

    /**
     * Firmware version that can be overwritten
     */
    val appNumScope: String? = null
) {

    /**
     * Determine whether a device upgrade is necessary based on the local device version
     */
    fun getHardwareUpgradeable(deviceInfo: FcDeviceInfo, localUiVersion: String?): HardwareUpgradeInfo? {
        if (deviceInfo.isSimulated() || hardwareInfo.isNullOrEmpty() || hardwareUrl.isNullOrEmpty()) {
            return null
        }
        val type = HardwareType.fromCode(hardwareType) ?: return null
        val localVersion = type.getVersion(deviceInfo)
        val remoteVersion = type.getVersion(hardwareInfo)

        if (type == HardwareType.APP) {
            if (!uiVersionScope.isNullOrEmpty()) {
                if (localUiVersion == null) {
                    return null
                }
                if (localUiVersion != uiVersionScope) {
                    //If the local UI version is inconsistent with the server's version
                    return null
                }
                if (appNumScope.isNullOrEmpty()) {
                    return null
                }
                if (!appNumScope.contains(localVersion)) {
                    return null
                }
            }
        }

        return if (remoteVersion > localVersion) {
            HardwareUpgradeInfo(
                hardwareInfo = hardwareInfo,
                remark = hardwareRemark,
                url = hardwareUrl,
                type = type,
                size = hardwareSize,
                isForce = isHardwareForce
            )
        } else {
            null
        }
    }

}