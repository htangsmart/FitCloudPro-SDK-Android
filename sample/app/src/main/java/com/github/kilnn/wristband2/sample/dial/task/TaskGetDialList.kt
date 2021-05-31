package com.github.kilnn.wristband2.sample.dial.task

import com.github.kilnn.wristband2.sample.MyApplication
import com.github.kilnn.wristband2.sample.dial.entity.DialInfo
import com.htsmart.wristband2.bean.WristbandVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.withContext

class TaskGetDialList {

    private val appDatabase = MyApplication.getSyncDataDb()
    private val apiClient = MyApplication.getApiClient()

    suspend fun execute(param: DialParam, isLocal: Boolean): MutableList<DialInfo> {
        return withContext(Dispatchers.IO) {
            if (isLocal) {
                val projectNum = WristbandVersion.get_version_project(param.hardwareInfo)
                appDatabase.dialInfoDao().query(projectNum, param.lcd, param.toolVersion).awaitFirst()
            } else {
                apiClient.getDialList(param.hardwareInfo, param.lcd, param.toolVersion)
                    .map {
                        //从服务器请求下来的表盘数据没有带上projectNum，这里加上，后面有使用这个字段
                        val projectNum = WristbandVersion.get_version_project(param.hardwareInfo)
                        for (dial in it) {
                            dial.projectNum = projectNum
                        }
                        it
                    }.awaitFirst()
            }
        }
    }
}