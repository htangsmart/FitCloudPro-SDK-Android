package com.topstep.fitcloud.sample2.ui.combine.wh

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentWhDetailBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.model.wh.MenstruationResult
import com.topstep.fitcloud.sample2.model.wh.WomenHealthConfig
import com.topstep.fitcloud.sample2.model.wh.WomenHealthMode
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.dialog.DatePickerDialogFragment
import com.topstep.fitcloud.sample2.utils.DateTimeUtils
import com.topstep.fitcloud.sample2.utils.FormatterUtil
import com.topstep.fitcloud.sample2.utils.WomenHealthUtils
import com.topstep.fitcloud.sample2.utils.showFailed
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcWomenHealthConfig
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.*

class WhDetailFragment : BaseFragment(R.layout.fragment_wh_detail), CompoundButton.OnCheckedChangeListener, DatePickerDialogFragment.Listener {

    private val viewBind: FragmentWhDetailBinding by viewBinding()
    private val args: WhDetailFragmentArgs by navArgs()

    private val dateFormat = FormatterUtil.getFormatterYYYYMMMdd()
    private val monthFormat = FormatterUtil.getFormatterYYYYMMM()

    private val configRepository = Injector.getWomenHealthRepository()

    private lateinit var config: WomenHealthConfig

    private val calendar = Calendar.getInstance()
    private val todayDate = Date()
    private lateinit var dueDate: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = runBlocking { configRepository.getConfigByMode(args.mode) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleResId = when (args.mode) {
            WomenHealthMode.MENSTRUATION -> R.string.wh_mode_menstruation
            WomenHealthMode.PREGNANCY_PREPARE -> R.string.wh_mode_pregnancy_prepare
            else -> R.string.wh_mode_pregnancy
        }

        (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(titleResId)

        viewBind.calendarView.setOnDateSelectListener(listener)
        viewBind.calendarView.setDataHolder(holder)

        viewBind.tvYearMonth.text = monthFormat.format(viewBind.calendarView.yearMonth)

        if (args.mode != WomenHealthMode.PREGNANCY) {
            viewBind.layoutLegendPregnancy.visibility = View.GONE
            viewBind.itemPregnancyDueDate.visibility = View.GONE

            updateLatestUI()
        } else {
            viewBind.layoutLegendMenstruation.visibility = View.GONE
            viewBind.itemBeginOperation.visibility = View.GONE
            viewBind.itemEndOperation.visibility = View.GONE
            viewBind.itemNoOperation.visibility = View.GONE
            viewBind.itemLatest.visibility = View.GONE

            dueDate = WomenHealthUtils.calculateDueDate(calendar, config.latest, config.cycle)
            updatePregnancyDueDateUI()
        }
        viewBind.calendarView.gotoTodayForce()
        viewBind.calendarView.invalidate()

        viewBind.tvGotoToday.clickTrigger(block = blockClick)
        viewBind.imgArrowLeft.clickTrigger(block = blockClick)
        viewBind.tvYearMonth.clickTrigger(block = blockClick)
        viewBind.imgArrowRight.clickTrigger(block = blockClick)
        viewBind.itemPregnancyDueDate.clickTrigger(block = blockClick)
        viewBind.itemLatest.clickTrigger(block = blockClick)

        viewBind.itemBeginOperation.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemEndOperation.getSwitchView().setOnCheckedChangeListener(this)
    }

    private val listener = object : WhCalendarView.OnDateSelectListener {
        override fun onDateSelect(date: Date?) {
            if (date == null) return
            viewBind.tvSelectDate.text = dateFormat.format(date)
            val week = date.day + 1
            viewBind.tvSelectWeek.setText(WhCalendarView.mWeekDaysRes.get(week))

            if (holder.isPregnancyMode) {
                if (config.remindType == FcWomenHealthConfig.RemindType.PREGNANCY_DAYS) {
                    val pregnancyDays = WomenHealthUtils.getPregnancyDays(calendar, config.latest, config.cycle, date)
                    if (pregnancyDays == null) {
                        viewBind.tvSelectInfo1.text = null
                    } else {
                        val weeks = pregnancyDays / 7
                        val days = pregnancyDays % 7
                        viewBind.tvSelectInfo1.text = getString(R.string.wh_pregnancy_remind_info2, weeks, days)
                    }
                } else {
                    val dueDays = WomenHealthUtils.getDueDays(calendar, config.latest, config.cycle, date)
                    if (dueDays == null) {
                        viewBind.tvSelectInfo1.text = null
                    } else {
                        viewBind.tvSelectInfo1.text = getString(R.string.wh_pregnancy_remind_info1, dueDays)
                    }
                }
            } else {
                viewBind.itemBeginOperation.visibility = View.GONE
                viewBind.itemEndOperation.visibility = View.GONE
                viewBind.itemNoOperation.visibility = View.GONE

                val result = runBlocking { configRepository.getMenstruationResult(calendar, date) }
                if (result == null) {
                    viewBind.tvSelectInfo1.text = null
                    viewBind.tvSelectInfo2.text = null
                    viewBind.itemNoOperation.visibility = View.VISIBLE
                    viewBind.itemNoOperation.setText(R.string.wh_no_operation_past)
                } else {
                    if (result.dateType == MenstruationResult.DateType.MENSTRUATION) {
                        viewBind.tvSelectInfo1.text = getString(R.string.wh_menstruation_remind_menstruation_days, result.dayInCycle)
                    } else {
                        if (result.remindNext != null) {
                            viewBind.tvSelectInfo1.text = getString(R.string.wh_menstruation_remind_next, result.remindNext)
                        } else {
                            when (result.dateType) {
                                MenstruationResult.DateType.OVULATION -> {
                                    viewBind.tvSelectInfo1.setText(R.string.wh_menstruation_remind_ovulation)
                                }
                                MenstruationResult.DateType.OVULATION_DAY -> {
                                    viewBind.tvSelectInfo1.setText(R.string.wh_menstruation_remind_ovulation_day)
                                }
                                else -> {
                                    viewBind.tvSelectInfo1.setText(R.string.wh_menstruation_remind_safe)
                                }
                            }
                        }
                    }

                    if (args.mode == WomenHealthMode.PREGNANCY_PREPARE) {
                        viewBind.tvSelectInfo2.text = getString(R.string.wh_menstruation_rate, result.pregnancyRate)
                    }

                    if (result.isInCycle(todayDate)) {
                        if (!DateTimeUtils.isSameDay(date, todayDate) && date.after(todayDate)) {
                            viewBind.itemNoOperation.visibility = View.VISIBLE
                            viewBind.itemNoOperation.setText(R.string.wh_no_operation_future)
                        } else {
                            when (result.operationType) {
                                MenstruationResult.OperationType.END -> {
                                    //可设置结束日期
                                    viewBind.itemEndOperation.visibility = View.VISIBLE
                                    viewBind.itemEndOperation.getSwitchView().isChecked = result.hasSetEndDate
                                }
                                MenstruationResult.OperationType.BEGIN -> {
                                    //可设置开始日期
                                    viewBind.itemBeginOperation.visibility = View.VISIBLE
                                    viewBind.itemBeginOperation.getSwitchView().isChecked = false
                                }
                                else -> {
                                    viewBind.itemNoOperation.visibility = View.VISIBLE
                                    viewBind.itemNoOperation.setText(R.string.wh_no_operation_start)
                                }
                            }
                        }
                    } else {
                        viewBind.itemNoOperation.visibility = View.VISIBLE
                        if (date.before(todayDate)) {
                            viewBind.itemNoOperation.setText(R.string.wh_no_operation_past)
                        } else {
                            viewBind.itemNoOperation.setText(R.string.wh_no_operation_future)
                        }
                    }
                }
            }
        }

        override fun onMonthChanged(isCurrentMonthNow: Boolean) {
            viewBind.tvYearMonth.text = monthFormat.format(viewBind.calendarView.yearMonth)
            viewBind.tvGotoToday.isVisible = !isCurrentMonthNow
        }
    }

    private val holder = object : WhCalendarView.DateHolder {
        override fun isPregnancyMode(): Boolean {
            return args.mode == WomenHealthMode.PREGNANCY
        }

        override fun getPregnancyDateType(date: Date): Int? {
            return WomenHealthUtils.getPregnancyDateType(calendar, config.latest, config.cycle, date)
        }

        override fun getMenstruationDateType(date: Date): Int? {
            return runBlocking { configRepository.getMenstruationResult(calendar, date) }?.dateType
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.tvGotoToday -> {
                viewBind.calendarView.gotoToday()
            }
            viewBind.imgArrowLeft -> {
                viewBind.calendarView.previousMonth()
            }
            viewBind.tvYearMonth -> {
                calendar.time = todayDate
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 3)
                val start: Date = calendar.time
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 6)
                val end: Date = calendar.time
                DatePickerDialogFragment.newInstance(
                    start = start,
                    end = end,
                    value = viewBind.calendarView.yearMonth,
                    title = getString(R.string.wh_set_calendar_title),
                    hideDay = true
                ).show(childFragmentManager, tagSetCalendar)
            }
            viewBind.imgArrowRight -> {
                viewBind.calendarView.nextMonth()
            }
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
            viewBind.itemLatest -> {
                val end = Date()
                val start = DateTimeUtils.getDateBetween(Calendar.getInstance(), end, -280)
                DatePickerDialogFragment.newInstance(
                    start = start,
                    end = end,
                    value = config.latest,
                    getString(R.string.wh_menstruation_latest),
                ).show(childFragmentManager, tagLatest)
            }
        }
    }

    private val tagSetCalendar = "set_calendar"
    private val tagPregnancyDueDate = "pregnancy_due_date"
    private val tagLatest = "latest"

    override fun onDialogDatePicker(tag: String?, date: Date) {
        when (tag) {
            tagSetCalendar -> {
                viewBind.calendarView.yearMonth = date
            }
            tagPregnancyDueDate -> {
                dueDate = date
                //设置了预产期，那要反推最后一次的经期
                config = config.copy(latest = WomenHealthUtils.calculateLatestMenstruation(calendar, dueDate, config.cycle))
                saveConfig()
                updatePregnancyDueDateUI()
            }
            tagLatest -> {
                config = config.copy(latest = date)
                saveConfig()
                updateLatestUI()
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (buttonView == null || !buttonView.isPressed) return
        if (buttonView == viewBind.itemBeginOperation.getSwitchView()) {
            val date = viewBind.calendarView.selectDate ?: return
            if (isChecked) {
                config = config.copy(latest = date)
                saveConfig()
            }
        } else if (buttonView == viewBind.itemEndOperation.getSwitchView()) {
            val date = viewBind.calendarView.selectDate ?: return
            try {
                runBlocking { configRepository.changeMenstruationEndDate(calendar, date, isChecked) }
            } catch (e: Exception) {
                Timber.w(e)
                promptToast.showFailed(e)
            }
            viewBind.calendarView.invalidate()
            listener.onDateSelect(date)
        }
    }

    private fun saveConfig() {
        try {
            runBlocking { configRepository.setConfig(config) }
        } catch (e: Exception) {
            Timber.w(e)
            promptToast.showFailed(e)
        }
        viewBind.calendarView.invalidate()
        val date = viewBind.calendarView.selectDate ?: return
        listener.onDateSelect(date)
    }

    private fun updateLatestUI() {
        val result = runBlocking { configRepository.getMenstruationResult(calendar, todayDate) }
        if (result == null) {
            viewBind.itemLatest.getSummaryView().text = dateFormat.format(config.latest)
        } else {
            //显示推算的最近一次的经期开始时间
            viewBind.itemLatest.getSummaryView().text = dateFormat.format(result.cycleBegin)
        }
    }

    private fun updatePregnancyDueDateUI() {
        viewBind.itemPregnancyDueDate.getSummaryView().text = dateFormat.format(dueDate)
    }

    override fun onDestroy() {
        super.onDestroy()
        configRepository.clearCache()
    }
}