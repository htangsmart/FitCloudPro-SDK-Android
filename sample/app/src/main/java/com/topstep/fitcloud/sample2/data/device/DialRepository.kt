package com.topstep.fitcloud.sample2.data.device

import android.content.Context
import android.net.Uri
import android.util.SparseArray
import com.github.kilnn.tool.util.ResourceUtil
import com.squareup.moshi.Moshi
import com.topstep.fitcloud.sample2.data.UnSupportDialCustomException
import com.topstep.fitcloud.sample2.data.UnSupportDialLcdException
import com.topstep.fitcloud.sample2.data.bean.DialPacketComplexBean
import com.topstep.fitcloud.sample2.data.net.ApiService
import com.topstep.fitcloud.sample2.data.net.findDialPacket
import com.topstep.fitcloud.sample2.model.dial.*
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.settings.dial.FcDialSpace
import com.topstep.fitcloud.sdk.v2.utils.dial.DialDrawer
import kotlinx.coroutines.rx3.await
import timber.log.Timber

interface DialRepository {

    suspend fun getDialPushParams(): DialPushParams

    suspend fun getDialPacket(dialPushParams: DialPushParams): List<DialPacket>

    suspend fun getDialCustomParams(dialPushParams: DialPushParams): DialCustomParams
}

internal class DialRepositoryImpl constructor(
    private val context: Context,
    private val moshi: Moshi,
    private val deviceManager: DeviceManager,
    private val apiService: ApiService,
) : DialRepository {

    private data class RemoteCacheKey(
        val hardwareInfo: String,
        val lcd: Int,
        val toolVersion: String,
    )

    private var remoteCacheKey: RemoteCacheKey? = null
    private var remoteCache: List<DialPacket>? = null

    override suspend fun getDialPacket(dialPushParams: DialPushParams): List<DialPacket> {
        val key = RemoteCacheKey(dialPushParams.hardwareInfo, dialPushParams.lcd, dialPushParams.toolVersion)
        val cache = remoteCache
        return if (cache != null && key == remoteCacheKey) {
            cache
        } else {
            (apiService.listDialPacket(dialPushParams.hardwareInfo, dialPushParams.lcd, dialPushParams.toolVersion).data ?: emptyList())
                .also {
                    remoteCacheKey = key
                    remoteCache = it
                }
        }
    }

    private val findCache: SparseArray<DialPacketComplexBean> = SparseArray()

    private suspend fun findDialPacket(dialNumbers: HashSet<Int>): List<DialPacketComplexBean> {
        val results = ArrayList<DialPacketComplexBean>(dialNumbers.size)
        //Find from local cache first
        if (findCache.size() > 0) {
            val iterator = dialNumbers.iterator()
            while (iterator.hasNext()) {
                val number = iterator.next()
                val find = findCache.get(number)
                if (find != null) {
                    results.add(find)
                    iterator.remove()
                }
            }
        }
        //If not found, search from the server
        if (dialNumbers.isNotEmpty()) {
            //Request dial information from the server based on the dial number
            val packets = apiService.findDialPacket(moshi, dialNumbers.toIntArray()).data ?: emptyList()
            for (packet in packets) {
                findCache.put(packet.dialNum, packet)
            }
            results.addAll(packets)
        }
        return results
    }

    override suspend fun getDialPushParams(): DialPushParams {
        val deviceInfo = deviceManager.configFeature.getDeviceInfo()

        val isSupportGUI = deviceInfo.isSupportFeature(FcDeviceInfo.Feature.GUI)

        val dialPushInfo = deviceManager.settingsFeature.requestDialPushInfo().await()
        if (dialPushInfo.shape == null) {
            throw UnSupportDialLcdException()
        }
        val dialSpaces = dialPushInfo.dialSpaces

        var dialSpacePackets: List<DialSpacePacket>? = null
        if (!dialSpaces.isNullOrEmpty()) {//Support for multiple dials
            val dialNumbers = HashSet<Int>(dialSpaces.size)
            for (space in dialSpaces) {
                //The custom dial of the 'Base dial format' does not have corresponding information on the server, so we do not request image information
                if (space.dialType == FcDialSpace.DIAL_TYPE_NORMAL || space.dialType == FcDialSpace.DIAL_TYPE_NONE) {
                    dialNumbers.add(space.dialNum)
                }
            }
            val dialPackets = if (dialNumbers.isEmpty()) {
                emptyList()
            } else {
                //Request dial information from the server based on the dial number
                findDialPacket(dialNumbers)
            }
            dialSpacePackets = combinationData(dialSpaces, dialPackets)
        }

        return DialPushParams(
            hardwareInfo = deviceInfo.toString(),
            isSupportGUI = isSupportGUI,
            lcd = dialPushInfo.lcd,
            shape = dialPushInfo.shape!!,
            toolVersion = dialPushInfo.toolVersion,
            currentDialNum = dialPushInfo.currentDialNum,
            currentPosition = dialPushInfo.currentPosition,
            dialSpacePackets = dialSpacePackets
        )
    }

    /**
     * Combine [dialSpaces] from the device and [dialPackets] from the server.
     * In this way, we can display the picture of the dial currently owned by the device on the UI.
     */
    private fun combinationData(dialSpaces: List<FcDialSpace>, dialPackets: List<DialPacketComplexBean>): List<DialSpacePacket> {
        val dialSpacePackets = ArrayList<DialSpacePacket>(dialSpaces.size)

        for (spaceIndex in dialSpaces.indices) {
            val space = dialSpaces[spaceIndex]
            //1.According to dialNum, match the information on the device with the information returned by the server
            var packet: DialPacketComplexBean? = null
            if (space.dialType == FcDialSpace.DIAL_TYPE_NORMAL || space.dialType == FcDialSpace.DIAL_TYPE_NONE) {
                for (_packet in dialPackets) {
                    if (_packet.dialNum == space.dialNum) {
                        packet = _packet
                        break
                    }
                }
            }

            //2.Detect component data and combine them together
            var components: MutableList<DialComponent>? = null
            if (packet != null && sizeEqualsAndValid(packet.components, space.components)) {//The number of components on the device is consistent with that on the server
                val spaceComponents = space.components!!
                val packetComponents = packet.components!!
                components = ArrayList(packetComponents.size)
                for (i in spaceComponents.indices) {
                    if (spaceComponents[i].styleCount != (packetComponents[i].urls?.size ?: 0)) {
                        Timber.tag("TaskGetDialParam").d("dialNum %d component size is not match", packet.dialNum)
                    }

                    components.add(
                        DialComponent(
                            spaceComponents[i].width,
                            spaceComponents[i].height,
                            spaceComponents[i].positionX,
                            spaceComponents[i].positionY,
                            spaceComponents[i].styleCurrent,
                            spaceComponents[i].styleCount,
                            packetComponents[i].urls
                        )
                    )
                }
            }

            dialSpacePackets.add(
                DialSpacePacket(
                    spaceIndex,
                    space.dialType,
                    space.dialNum,
                    space.binVersion,
                    space.binFlag,
                    space.spaceSize,
                    packet?.imgUrl,
                    packet?.deviceImgUrl,
                    packet?.previewImgUrl,
                    components
                )
            )
        }
        return dialSpacePackets
    }

    /**
     * Detect a list and data that are of equal length and contain valid data
     */
    private fun sizeEqualsAndValid(list1: List<Any>?, list2: List<Any>?): Boolean {
        if (list1 == null || list1.isEmpty()
            || list2 == null || list2.isEmpty()
        ) {
            return false
        }
        return list1.size == list2.size
    }

    override suspend fun getDialCustomParams(dialPushParams: DialPushParams): DialCustomParams {
        return if (dialPushParams.isSupportGUI) {
            val list = apiService.dialCustomGUI(dialPushParams.lcd, dialPushParams.toolVersion).data
            if (list.isNullOrEmpty()) {
                throw UnSupportDialCustomException()
            }
            val dialPacket = list[0]//Take the first object, usually there will only be one
            val components = dialPacket.components
            if (components.isNullOrEmpty()) {//At least one component
                throw UnSupportDialCustomException()
            }
            val urls = components[0].urls
            if (urls.isNullOrEmpty()) {//Need to have style
                throw UnSupportDialCustomException()
            }
            //The "GUI dial format" image on the FitCloud server is doubled in size relative to the device screen size
            // So we need shape.width * 2
            val styleBaseOnWidth = dialPushParams.shape.width * 2
            val styles = ArrayList<DialCustomParams.Style>(urls.size)
            for (styleIndex in urls.indices) {
                val url = urls[styleIndex]
                styles.add(DialCustomParams.Style(styleIndex, Uri.parse(url), styleBaseOnWidth, dialPacket.binUrl, dialPacket.binSize))
            }
            DialCustomParams(
                //Use server image as default background
                Uri.parse(dialPacket.previewImgUrl),
                styles
            )
        } else {
            val list = apiService.dialCustom(dialPushParams.lcd, dialPushParams.toolVersion).data
            if (list.isNullOrEmpty()) {
                throw UnSupportDialCustomException()
            }
            //Displayed on the UI in the following order
            val supportList = listOf(
                SupportStyle("White", com.topstep.fitcloud.sdk.v2.R.drawable.fc_dial_custom_style_white),
                SupportStyle("Black", com.topstep.fitcloud.sdk.v2.R.drawable.fc_dial_custom_style_black),
                SupportStyle("Yellow", com.topstep.fitcloud.sdk.v2.R.drawable.fc_dial_custom_style_yellow),
                SupportStyle("Green", com.topstep.fitcloud.sdk.v2.R.drawable.fc_dial_custom_style_green),
                SupportStyle("Gray", com.topstep.fitcloud.sdk.v2.R.drawable.fc_dial_custom_style_gray)
            )
            val styles: MutableList<DialCustomParams.Style> = ArrayList(supportList.size)
            for (support in supportList) {
                for (dial in list) {
                    if (support.styleName == dial.styleName) {
                        styles.add(
                            DialCustomParams.Style(
                                styles.size,
                                ResourceUtil.getUriFromDrawableResId(context, support.resId),
                                //The "Base dial format" image on the FitCloud SDK is design base on 800px
                                DialDrawer.STYLE_BASE_ON_WIDTH,
                                dial.binUrl,
                                dial.binSize
                            )
                        )
                        break
                    }
                }
            }
            if (styles.isEmpty()) throw UnSupportDialCustomException()
            DialCustomParams(
                //Use local image as default background
                ResourceUtil.getUriFromDrawableResId(context, com.topstep.fitcloud.sdk.v2.R.drawable.fc_dial_custom_default_bg),
                styles
            )
        }
    }

    private class SupportStyle constructor(var styleName: String, var resId: Int)

}