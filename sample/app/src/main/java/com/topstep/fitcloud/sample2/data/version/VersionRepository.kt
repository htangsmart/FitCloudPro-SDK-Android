package com.topstep.fitcloud.sample2.data.version

import com.topstep.fitcloud.sample2.data.device.DeviceManager
import com.topstep.fitcloud.sample2.data.net.ApiService
import com.topstep.fitcloud.sample2.model.version.HardwareUpgradeInfo
import kotlinx.coroutines.rx3.await
import timber.log.Timber
import java.util.*

interface VersionRepository {

    /**
     * Detect for version updates。
     */
    suspend fun checkUpgrade(): HardwareUpgradeInfo?

}

internal class VersionRepositoryImpl constructor(
    private val deviceManager: DeviceManager,
    private val apiService: ApiService,
) : VersionRepository {

    override suspend fun checkUpgrade(): HardwareUpgradeInfo? {
        val uiVersion = try {
            val uiInfo = deviceManager.settingsFeature.requestUiInfo().await()
            String.format(Locale.US, "%d-%02d", uiInfo.uiNum, uiInfo.uiSerial)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e)
            null
        }

        val deviceInfo = deviceManager.configFeature.getDeviceInfo()
        if (deviceInfo.isSimulated()) return null
        val versionBean = apiService.checkVersion(
            deviceInfo.toString(), uiVersion
        ).data ?: return null

        return versionBean.getHardwareUpgradeable(deviceInfo, uiVersion)
    }

    companion object {
        private const val TAG = "VersionRepository"
    }

}