package com.github.kilnn.wristband2.sample.sportpush

import android.util.SparseArray
import com.github.kilnn.wristband2.sample.MyApplication
import com.github.kilnn.wristband2.sample.sportpush.entity.SportBinItem
import com.github.kilnn.wristband2.sample.sportpush.entity.SportPushParam
import com.github.kilnn.wristband2.sample.sportpush.entity.SportPushWithIcon
import com.htsmart.wristband2.WristbandApplication
import com.htsmart.wristband2.bean.SportPush
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext

class TaskGetSportPushParam {

    private val manager = WristbandApplication.getWristbandManager();
    private val apiClient = MyApplication.getApiClient()

    suspend fun execute(): SportPushParam {
//        return mockData()
        val hardwareInfo = manager.wristbandConfig?.wristbandVersion?.rawVersion?.takeIf {
            it.isNotEmpty()
        } ?: throw NullPointerException()

        return withContext(Dispatchers.IO) {
            val local: List<SportPush> = manager.requestSportPush().await()
            val remote: SparseArray<SportBinItem> = apiClient.getSportBinItems(hardwareInfo).map {
                //转成SparseArray，方便下面查找
                val array = SparseArray<SportBinItem>()
                for (item in it) {
                    array.put(item.sportUiType, item)
                }
                array
            }.awaitFirst()

            //将本地的运动分成两组
            //一组是本地已经存在的，这将展示在界面上
            val listExist = ArrayList<SportPushWithIcon>(local.size)
            //一组是本地不存在的，这些就是可以被下载推送的运动
            val listNotExist = ArrayList<SportBinItem>(local.size)

            for (sportPush in local) {
                //查找是否在服务器中有对应的，避免出错。如果为null，那么就跳过
                val sportBinItem = remote.get(sportPush.sportType) ?: continue
                if (sportPush.isExist) {
                    listExist.add(SportPushWithIcon(sportPush.sportType, sportPush.isPushEnabled, sportPush.binFlag, sportBinItem.iconUrl))
                } else {
                    listNotExist.add(sportBinItem)
                }
            }

            SportPushParam(listExist, listNotExist)
        }
    }

    //模拟测试界面的
    private fun mockData(): SportPushParam {
        val listExist = ArrayList<SportPushWithIcon>(10)
        listExist.add(SportPushWithIcon(1, false, 0, "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/icon.png"))
        listExist.add(SportPushWithIcon(2, false, 0, "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/icon.png"))
        listExist.add(SportPushWithIcon(3, false, 0, "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/icon.png"))
        listExist.add(SportPushWithIcon(4, true, 0xD0.toByte(), "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/icon.png"))
        listExist.add(SportPushWithIcon(5, true, 0xD1.toByte(), "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/icon.png"))
        listExist.add(SportPushWithIcon(6, true, 0xD2.toByte(), "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/icon.png"))

        val listNotExist = ArrayList<SportBinItem>(10)
        listNotExist.add(
            SportBinItem(
                7,
                "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/icon.png",
                "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/someother_10008_16_sport.bin"
            )
        )
        listNotExist.add(
            SportBinItem(
                8,
                "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/icon.png",
                "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/someother_10008_16_sport.bin"
            )
        )
        listNotExist.add(
            SportBinItem(
                9,
                "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/icon.png",
                "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/someother_10008_16_sport.bin"
            )
        )
        listNotExist.add(
            SportBinItem(
                10,
                "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/icon.png",
                "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/someother_10008_16_sport.bin"
            )
        )
        listNotExist.add(
            SportBinItem(
                11,
                "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/icon.png",
                "http://fitcloud.hetangsmart.com/oss/fitcloud/sportbin/10008/someother_10008_16_sport.bin"
            )
        )
        return SportPushParam(listExist, listNotExist)
    }
}