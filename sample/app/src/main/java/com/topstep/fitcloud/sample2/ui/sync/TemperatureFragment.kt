package com.topstep.fitcloud.sample2.ui.sync

import android.content.Context
import android.os.Bundle
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.entity.TemperatureItemEntity
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.utils.FormatterUtil
import com.topstep.fitcloud.sample2.utils.celsius2Fahrenheit
import com.topstep.fitcloud.sdk.v2.model.config.FcFunctionConfig
import kotlinx.coroutines.runBlocking
import java.util.*

class TemperatureFragment : DataListFragment<TemperatureItemEntity>() {

    private val deviceManager = Injector.getDeviceManager()
    private var isUnitCentigrade = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isUnitCentigrade = !deviceManager.configFeature.getFunctionConfig().isFlagEnabled(FcFunctionConfig.Flag.TEMPERATURE_UNIT)
    }

    override val valueFormat: DataListAdapter.ValueFormat<TemperatureItemEntity> = object : DataListAdapter.ValueFormat<TemperatureItemEntity> {
        override fun format(context: Context, obj: TemperatureItemEntity): String {
            val timeStr = timeFormat.format(obj.time)
            val valueStr = if (isUnitCentigrade) {
                context.getString(
                    R.string.unit_centigrade_param,
                    "${FormatterUtil.decimal1Str(obj.body)}/${FormatterUtil.decimal1Str(obj.wrist)}"
                )
            } else {
                context.getString(
                    R.string.unit_fahrenheit_param,
                    "${FormatterUtil.decimal1Str(obj.body.celsius2Fahrenheit())}/${FormatterUtil.decimal1Str(obj.wrist.celsius2Fahrenheit())}"
                )
            }
            return timeStr + valueStr
        }
    }

    override fun queryData(date: Date): List<TemperatureItemEntity>? {
        return runBlocking { syncDataRepository.queryTemperature(authedUserId, date) }
    }
}