package com.github.kilnn.wristband2.sample.dial.task

import com.github.kilnn.wristband2.sample.MyApplication
import com.github.kilnn.wristband2.sample.dial.entity.DialInfoComplex
import com.htsmart.wristband2.WristbandApplication
import com.htsmart.wristband2.bean.DialBinInfo
import com.htsmart.wristband2.bean.DialComponent
import com.htsmart.wristband2.bean.DialSubBinInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 不支持的LCD类型
 */
class UnSupportLcdException : Exception()

class TaskGetDialParam {

    private val manager = WristbandApplication.getWristbandManager();
    private val apiClient = MyApplication.getApiClient()

    suspend fun execute(): DialParam {
        val config = manager.wristbandConfig ?: throw NullPointerException()

        //硬件信息
        val hardwareInfo = config.wristbandVersion.rawVersion
//        val hardwareInfo = "000000000A0600000155000027F3000010001100000000000000030919081319000000000000"//for test
        if (hardwareInfo.isNullOrEmpty()) {
            throw NullPointerException()
        }

        //是否支持多表盘
        val isExtDialMultiple = config.wristbandVersion.isExtDialMultiple
//        val isExtDialMultiple = true//for test

        //是否是GUI协议
        val isGUI = config.wristbandVersion.isExtGUI
//        val isGUI = true//for test

        val supportCustom = config.wristbandVersion.isExtDialCustom

        return withContext(Dispatchers.IO) {
            val dialBinInfo = manager.requestDialBinInfo().await()
//            val dialBinInfo = mockDialBinInfo()//for test
            if (dialBinInfo.shape == null) {
                throw UnSupportLcdException()
            }

            val subBinList = dialBinInfo.subBinList

            var dialBinParamList: List<DialBinParam>? = null

            if (isExtDialMultiple && !subBinList.isNullOrEmpty()) {//支持多表盘，并且多表盘List里有数据
                //支持多表盘升级
                val dialNumbers = HashSet<Int>(subBinList.size)
                for (i in subBinList.indices) {
                    //旧协议的自定义表盘，与其他表盘不兼容，服务器也没有对应的信息，所以不去请求图片信息
                    if (subBinList[i].dialType == DialSubBinInfo.TYPE_NORMAL || subBinList[i].dialType == DialSubBinInfo.TYPE_NONE) {
                        dialNumbers.add(subBinList[i].dialNum)
                    }
                }

                val dialInfos = if (dialNumbers.size <= 0) {
                    emptyList<DialInfoComplex>()
                } else {
                    //根据表盘编号，向服务器请求表盘信息
                    apiClient.getDialListByNumbers(dialNumbers.toList()).onErrorReturnItem(emptyList<DialInfoComplex>()).awaitFirst()
                }
                dialBinParamList = combinationData(subBinList, dialInfos)
            }

            DialParam(hardwareInfo, isGUI, dialBinInfo.lcd, dialBinInfo.toolVersion, dialBinInfo.dialNum, dialBinInfo.dialPosition, dialBinParamList, dialBinInfo.shape!!, supportCustom)
        }
    }

    /**
     * 把手环返回的表盘信息和服务器的信息组合在一起。这样表盘号，组件，图片等等都齐全了
     */
    private fun combinationData(localDials: List<DialSubBinInfo>, remoteDials: List<DialInfoComplex>): List<DialBinParam> {

        val dialBinParamList = ArrayList<DialBinParam>(localDials.size)

        for (index in localDials.indices) {
            val local = localDials[index]
            //1.根据dialNum，匹配手环上的信息和服务器返回的信息
            var remote: DialInfoComplex? = null
            if (local.dialType == DialSubBinInfo.TYPE_NORMAL || local.dialType == DialSubBinInfo.TYPE_NONE) {
                //只有这两种类型的，去请求了服务器上的表盘信息。现在查找出对应的服务器表盘
                for (_remote in remoteDials) {
                    if (_remote.dialNum == local.dialNum) {
                        //查找到服务器上存在的表盘信息
                        remote = _remote
                        break
                    }
                }
            }

            //2.检测组件数据，并组合在一起
            var components: MutableList<DialComponentParam>? = null
            if (remote != null && sizeEqualsAndValid(remote.components, local.components)) {//手环上组件和服务器上组件个数一致

                val localComponents = local.components!!
                val remoteComponents = remote.components!!

                components = ArrayList(remoteComponents.size)

                for (i in localComponents.indices) {
                    if (localComponents[i].styleCount != (remoteComponents[i].urls?.size ?: 0)) {
                        //组件样式个数不一致，就不报错了，打印个日志
                        Timber.tag("TaskGetDialParam").d("dialNum %d component size is not match", local.dialNum)
                    }

                    components.add(
                        DialComponentParam(
                            localComponents[i].width,
                            localComponents[i].height,
                            localComponents[i].positionX,
                            localComponents[i].positionY,
                            localComponents[i].styleCurrent,
                            localComponents[i].styleCount,
                            remoteComponents[i].urls
                        )
                    )
                }
            }

            dialBinParamList.add(
                DialBinParam(
                    index,
                    local.dialType,
                    local.dialNum,
                    local.binVersion,
                    local.binFlag,
                    local.dialSpace,
                    remote?.imgUrl,
                    remote?.deviceImgUrl,
                    remote?.previewImgUrl,
                    components
                )
            )
        }
        return dialBinParamList
    }

    /**
     * 检测一个list和数据，长度相等，并且都包含有效数据
     */
    private fun sizeEqualsAndValid(list: List<Any>?, array: Array<out Any>?): Boolean {
        if (list == null || list.isEmpty()
            || array == null || array.isEmpty()
        ) {
            return false
        }
        return list.size == array.size
    }

    //for test
    private fun mockDialBinInfo(): DialBinInfo {
        val info = DialBinInfo()
        info.lcd = 7
        info.toolVersion = "1.4"
        info.dialNum = 84003
        val subBinList = ArrayList<DialSubBinInfo>(5)
        info.subBinList = subBinList

        val sub1 = DialSubBinInfo()
        sub1.dialNum = 84003
        sub1.dialType = DialSubBinInfo.TYPE_NORMAL
        sub1.binFlag = 0
        sub1.components = arrayOf(
            DialComponent().apply { width = 85;height = 72;positionX = 10;positionY = 10;styleCurrent = 1;styleCount = 4 },
            DialComponent().apply { width = 85;height = 72;positionX = 10;positionY = 180;styleCurrent = 1;styleCount = 4 },
        )
        sub1.dialSpace = 256
        subBinList.add(sub1)


        val sub2 = DialSubBinInfo()
        sub2.dialNum = 84009
        sub2.dialType = DialSubBinInfo.TYPE_NORMAL
        sub2.binFlag = 0xA1.toByte()
        sub2.components = arrayOf(
            DialComponent().apply { width = 85;height = 72;positionX = 180;positionY = 10;styleCurrent = 1;styleCount = 4 },
            DialComponent().apply { width = 85;height = 72;positionX = 180;positionY = 180;styleCurrent = 1;styleCount = 4 },
        )
        sub2.dialSpace = 512
        subBinList.add(sub2)

//        val sub3 = DialSubBinInfo()
//        sub3.dialNum = 14007
//        sub3.dialType = DialSubBinInfo.TYPE_NORMAL
//        sub3.binFlag = 0xA5.toByte()
//        subBinList.add(sub3)
//
//        val sub4 = DialSubBinInfo()
//        sub4.dialNum = 60001
//        sub4.dialType = DialSubBinInfo.TYPE_CUSTOM_STYLE_WHITE
//        sub4.binFlag = 0xA6.toByte()
//        subBinList.add(sub4)
//
//
//        val sub5 = DialSubBinInfo()
//        sub5.dialNum = 60002
//        sub5.dialType = DialSubBinInfo.TYPE_CUSTOM_STYLE_GREEN
//        sub5.binFlag = 0xA7.toByte()
//        sub5.components = arrayOf(
//            DialComponent().apply { width = 1;height = 2;positionX = 10;positionY = 20;styleCurrent = 1;styleCount = 4 },
//            DialComponent().apply { width = 1;height = 2;positionX = 10;positionY = 20;styleCurrent = 1;styleCount = 4 },
//            DialComponent().apply { width = 1;height = 2;positionX = 10;positionY = 20;styleCurrent = 1;styleCount = 4 },
//            DialComponent().apply { width = 1;height = 2;positionX = 10;positionY = 20;styleCurrent = 1;styleCount = 4 },
//        )
//        subBinList.add(sub5)

        return info
    }

}