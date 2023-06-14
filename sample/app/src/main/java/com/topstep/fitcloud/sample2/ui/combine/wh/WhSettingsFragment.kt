package com.topstep.fitcloud.sample2.ui.combine.wh

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentWhSettingsBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.model.wh.WomenHealthConfig
import com.topstep.fitcloud.sample2.model.wh.WomenHealthMode
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.dialog.ChoiceIntDialogFragment
import com.topstep.fitcloud.sample2.ui.dialog.DatePickerDialogFragment
import com.topstep.fitcloud.sample2.ui.dialog.SelectIntDialogFragment
import com.topstep.fitcloud.sample2.utils.DateTimeUtils
import com.topstep.fitcloud.sample2.utils.FormatterUtil
import com.topstep.fitcloud.sample2.utils.WomenHealthUtils
import com.topstep.fitcloud.sample2.utils.showFailed
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import com.topstep.fitcloud.sdk.v2.model.config.FcWomenHealthConfig
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.*

class WhSettingsFragment : BaseFragment(R.layout.fragment_wh_settings),
    SelectIntDialogFragment.Listener, DatePickerDialogFragment.Listener, ChoiceIntDialogFragment.Listener {

    private val viewBind: FragmentWhSettingsBinding by viewBinding()
    private val args: WhSettingsFragmentArgs by navArgs()
    private val format by lazy { FormatterUtil.getFormatterYYYYMMMdd() }

    private val configRepository = Injector.getWomenHealthRepository()

    private val deviceManager = Injector.getDeviceManager()

    private lateinit var config: WomenHealthConfig
    private var isDeviceSupport = false

    private val calendar = Calendar.getInstance()
    private val todayDate = Date()
    private lateinit var dueDate: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = runBlocking { configRepository.getConfigByMode(args.mode) }
        isDeviceSupport = deviceManager.configFeature.getDeviceInfo().isSupportFeature(FcDeviceInfo.Feature.WOMEN_HEALTH)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (args.mode) {
            WomenHealthMode.MENSTRUATION -> {
                (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(R.string.wh_menstruation_settings)
            }
            WomenHealthMode.PREGNANCY_PREPARE -> {
                (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(R.string.wh_pregnancy_prepare_settings)
            }
            else -> {
                (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(R.string.wh_pregnancy_settings)
            }
        }

        if (args.mode != WomenHealthMode.PREGNANCY) {
            viewBind.layoutPregnancy1.visibility = View.GONE
            viewBind.layoutPregnancy2.visibility = View.GONE
            viewBind.itemRemindType.visibility = View.GONE

            viewBind.itemDuration.getTextView().text = getString(R.string.unit_day_count_param, config.duration)
            viewBind.itemDuration.clickTrigger(block = blockClick)

            viewBind.itemCycle.getTextView().text = getString(R.string.unit_day_count_param, config.cycle)
            viewBind.itemCycle.clickTrigger(block = blockClick)

            viewBind.itemLatest.getSummaryView().text = format.format(config.latest)
            viewBind.itemLatest.clickTrigger(block = blockClick)

            viewBind.itemRemindAdvance.getSummaryView().text = getString(R.string.wh_menstruation_advance_param, config.remindAdvance)
            viewBind.itemRemindAdvance.clickTrigger(block = blockClick)

        } else {
            viewBind.layoutMenstruation1.visibility = View.GONE
            viewBind.layoutMenstruation2.visibility = View.GONE
            viewBind.itemRemindAdvance.visibility = View.GONE

            dueDate = WomenHealthUtils.calculateDueDate(calendar, config.latest, config.cycle)
            viewBind.itemPregnancyDueDate.getSummaryView().text = format.format(dueDate)
            viewBind.itemPregnancyDueDate.clickTrigger(block = blockClick)

            viewBind.itemPregnancyCycle.getTextView().text = getString(R.string.unit_day_count_param, config.cycle)
            viewBind.itemPregnancyCycle.clickTrigger(block = blockClick)

            viewBind.itemPregnancyLatest.getSummaryView().text = format.format(config.latest)
            viewBind.itemPregnancyLatest.clickTrigger(block = blockClick)

            updateRemindType()
            viewBind.itemRemindType.clickTrigger(block = blockClick)
        }

        viewBind.itemRemindDevice.getSwitchView().isChecked = config.remindDevice
        viewBind.itemRemindTime.getTextView().text = FormatterUtil.minute2Duration(config.remindTime)
        viewBind.itemRemindDevice.isEnabled = isDeviceSupport
        viewBind.itemRemindTime.isEnabled = viewBind.itemRemindDevice.getSwitchView().isChecked && isDeviceSupport
        viewBind.itemRemindDevice.getSwitchView().setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                viewBind.itemRemindTime.isEnabled = isChecked && isDeviceSupport
            }
        }
        viewBind.itemRemindTime.clickTrigger(block = blockClick)

        viewBind.btnCommit.clickTrigger(block = blockClick)
    }

    private fun updateRemindType() {
        if (config.remindType == FcWomenHealthConfig.RemindType.PREGNANCY_DAYS) {
            val pregnancyDays = WomenHealthUtils.getPregnancyDays(calendar, config.latest, config.cycle, todayDate)
            if (pregnancyDays == null) {
                viewBind.itemRemindType.getSummaryView().text = null
            } else {
                val weeks = pregnancyDays / 7
                val days = pregnancyDays % 7
                viewBind.itemRemindType.getSummaryView().text = getString(R.string.wh_pregnancy_remind_info2, weeks, days)
            }
        } else {
            val dueDays = WomenHealthUtils.getDueDays(calendar, config.latest, config.cycle, todayDate)
            if (dueDays == null) {
                viewBind.itemRemindType.getSummaryView().text = null
            } else {
                viewBind.itemRemindType.getSummaryView().text = getString(R.string.wh_pregnancy_remind_info1, dueDays)
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            //Menstruation
            viewBind.itemDuration -> {
                SelectIntDialogFragment.newInstance(
                    min = 3,
                    max = 15,
                    multiples = 1,
                    value = config.duration,
                    title = requireContext().getString(R.string.wh_menstruation_duration),
                    des = requireContext().getString(R.string.unit_day_count)
                ).show(childFragmentManager, tagDuration)
            }
            viewBind.itemCycle -> {
                SelectIntDialogFragment.newInstance(
                    min = 17,
                    max = 60,
                    multiples = 1,
                    value = config.cycle,
                    title = requireContext().getString(R.string.wh_menstruation_cycle),
                    des = requireContext().getString(R.string.unit_day_count)
                ).show(childFragmentManager, tagCycle)
            }
            viewBind.itemLatest -> {
                val end = Date()
                val start = DateTimeUtils.getDateBetween(calendar, end, -280)
                DatePickerDialogFragment.newInstance(
                    start = start,
                    end = end,
                    value = config.latest,
                    getString(R.string.wh_menstruation_latest),

                    ).show(childFragmentManager, tagLatest)
            }

            //Pregnancy
            viewBind.itemPregnancyDueDate -> {
                val start = Date()
                val end = DateTimeUtils.getDateBetween(calendar, start, 280)
                DatePickerDialogFragment.newInstance(
                    start = start,
                    end = end,
                    value = dueDate,
                    getString(R.string.wh_set_due_date),
                ).show(childFragmentManager, tagPregnancyDueDate)
            }
            viewBind.itemPregnancyCycle -> {
                SelectIntDialogFragment.newInstance(
                    min = 17,
                    max = 60,
                    multiples = 1,
                    value = config.cycle,
                    title = requireContext().getString(R.string.wh_menstruation_cycle),
                    des = requireContext().getString(R.string.unit_day_count)
                ).show(childFragmentManager, tagPregnancyCycle)
            }
            viewBind.itemPregnancyLatest -> {
                val end = Date()
                val start = DateTimeUtils.getDateBetween(calendar, end, -280)
                DatePickerDialogFragment.newInstance(
                    start = start,
                    end = end,
                    value = config.latest,
                    getString(R.string.wh_menstruation_latest),

                    ).show(childFragmentManager, tagPregnancyLatest)
            }

            //Remind
            viewBind.itemRemindAdvance -> {
                SelectIntDialogFragment.newInstance(
                    min = 1,
                    max = 3,
                    multiples = 1,
                    value = config.remindAdvance,
                    title = requireContext().getString(R.string.wh_menstruation_advance),
                    des = requireContext().getString(R.string.unit_day_count)
                ).show(childFragmentManager, tagRemindAdvance)
            }
            viewBind.itemRemindType -> {
                val items = arrayOf(
                    requireContext().getString(R.string.wh_pregnancy_remind_type1),
                    requireContext().getString(R.string.wh_pregnancy_remind_type2)
                )
                val values = intArrayOf(FcWomenHealthConfig.RemindType.DUE_DAYS, FcWomenHealthConfig.RemindType.PREGNANCY_DAYS)
                ChoiceIntDialogFragment.newInstance(items, values, config.remindType, requireContext().getString(R.string.wh_pregnancy_remind_select_tips))
                    .show(childFragmentManager, tagRemindType)
            }
            viewBind.itemRemindTime -> {
                SelectIntDialogFragment.newInstance(
                    min = 0,
                    max = 23,
                    multiples = 60,
                    value = config.remindTime,
                    title = requireContext().getString(R.string.wh_remind_time),
                ).show(childFragmentManager, tagRemindTime)
            }

            //commit
            viewBind.btnCommit -> {
                saveConfig()
            }
        }
    }

    private fun saveConfig() {
        config = config.copy(
            mode = args.mode,
            remindDevice = viewBind.itemRemindDevice.getSwitchView().isChecked
        )
        val latestConfig = configRepository.flowCurrent.value
        if (latestConfig != config) {
            try {
                runBlocking { configRepository.setConfig(config) }
            } catch (e: Exception) {
                Timber.w(e)
                promptToast.showFailed(e)
                return
            }
        }
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.combineFragment, false).build()
        findNavController().navigate(WhSettingsFragmentDirections.toWhDetail(args.mode), navOptions)
    }

    //Menstruation
    private val tagDuration = "duration"
    private val tagCycle = "cycle"
    private val tagLatest = "latest"

    //Pregnancy
    private val tagPregnancyDueDate = "pregnancy_due_date"
    private val tagPregnancyCycle = "pregnancy_cycle"
    private val tagPregnancyLatest = "pregnancy_latest"

    //Remind
    private val tagRemindAdvance = "remind_advance"
    private val tagRemindType = "remind_type"
    private val tagRemindTime = "remind_time"

    override fun onDialogSelectInt(tag: String?, selectValue: Int) {
        when (tag) {
            tagDuration -> {
                config = config.copy(duration = selectValue)
                viewBind.itemDuration.getTextView().text = getString(R.string.unit_day_count_param, config.duration)
            }
            tagCycle -> {
                config = config.copy(cycle = selectValue)
                viewBind.itemCycle.getTextView().text = getString(R.string.unit_day_count_param, config.cycle)
            }
            tagPregnancyCycle -> {
                config = config.copy(cycle = selectValue)
                viewBind.itemPregnancyCycle.getTextView().text = getString(R.string.unit_day_count_param, config.cycle)
                //设置了月经周期，那么要在计算预产期
                dueDate = WomenHealthUtils.calculateDueDate(calendar, config.latest, config.cycle)
                viewBind.itemPregnancyDueDate.getSummaryView().text = format.format(dueDate)
                updateRemindType()
            }
            tagRemindAdvance -> {
                config = config.copy(remindAdvance = selectValue)
                viewBind.itemRemindAdvance.getSummaryView().text = getString(R.string.wh_menstruation_advance_param, config.remindAdvance)
            }
            tagRemindTime -> {
                config = config.copy(remindTime = selectValue)
                viewBind.itemRemindTime.getTextView().text = FormatterUtil.minute2Duration(config.remindTime)
            }
        }
    }

    override fun dialogSelectIntFormat(tag: String?, value: Int): String {
        return if (tag == tagRemindTime) {
            FormatterUtil.minute2Duration(value)
        } else {
            super.dialogSelectIntFormat(tag, value)
        }
    }

    override fun onDialogDatePicker(tag: String?, date: Date) {
        when (tag) {
            tagLatest -> {
                config = config.copy(latest = date)
                viewBind.itemLatest.getSummaryView().text = format.format(config.latest)
            }
            tagPregnancyDueDate -> {
                dueDate = date
                viewBind.itemPregnancyDueDate.getSummaryView().text = format.format(dueDate)
                //设置了预产期，那要反推最后一次的经期
                config = config.copy(latest = WomenHealthUtils.calculateLatestMenstruation(calendar, dueDate, config.cycle))
                viewBind.itemPregnancyLatest.getSummaryView().text = format.format(config.latest)
                updateRemindType()
            }
            tagPregnancyLatest -> {
                config = config.copy(latest = date)
                viewBind.itemPregnancyLatest.getSummaryView().text = format.format(config.latest)
                //设置了经期，那么要在计算预产期
                dueDate = WomenHealthUtils.calculateDueDate(calendar, config.latest, config.cycle)
                viewBind.itemPregnancyDueDate.getSummaryView().text = format.format(dueDate)
                updateRemindType()
            }
        }
    }

    override fun onDialogChoiceInt(tag: String?, selectValue: Int) {
        if (tag == tagRemindType) {
            config = config.copy(remindType = selectValue)
            updateRemindType()
        }
    }

}