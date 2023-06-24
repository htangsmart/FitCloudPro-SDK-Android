package com.topstep.fitcloud.sample2.ui.device.alarm

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.github.kilnn.wheellayout.WheelIntConfig
import com.github.kilnn.wheellayout.WheelIntFormatter
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentAlarmDetailBinding
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.FormatterUtil
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.settings.FcAlarm
import java.util.*

class AlarmDetailFragment : BaseFragment(R.layout.fragment_alarm_detail),
    AlarmLabelDialogFragment.Listener, AlarmRepeatDialogFragment.Listener {

    private val viewBind: FragmentAlarmDetailBinding by viewBinding()
    private val viewModel: AlarmViewModel by viewModels({ requireParentFragment() })
    private val args: AlarmDetailFragmentArgs by navArgs()
    private val formatter = FormatterUtil.get02dWheelIntFormatter()
    private val calendar = Calendar.getInstance()
    private val is24HourFormat by lazy { viewModel.helper.is24HourFormat(requireContext()) }

    private lateinit var alarm: FcAlarm

    /**
     * Is edit alarm or add a new alarm
     */
    private var isEditMode: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == android.R.id.home) {
                    findNavController().navigateUp()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)

        val alarms = viewModel.state.requestAlarms()
        if (alarms != null && args.position >= 0 && args.position < alarms.size) {
            //Edit Mode
            alarm = alarms[args.position].clone()
            isEditMode = true
        } else {
            //Add Mode
            alarm = FcAlarm(FcAlarm.findNewAlarmId(alarms))
            alarm.label = getString(R.string.ds_alarm_label_default)
            alarm.hour = calendar.get(Calendar.HOUR_OF_DAY)
            alarm.minute = calendar.get(Calendar.MINUTE)
            isEditMode = false
        }

        if (isEditMode) {
            (requireActivity() as AppCompatActivity?)?.supportActionBar?.setTitle(R.string.ds_alarm_edit)
        } else {
            (requireActivity() as AppCompatActivity?)?.supportActionBar?.setTitle(R.string.ds_alarm_add)
        }

        //Add mode does not display delete button
        viewBind.btnDelete.isVisible = isEditMode

        if (is24HourFormat) {
            viewBind.wheelAmPm.visibility = View.GONE
            viewBind.wheelHour.setConfig(WheelIntConfig(0, 23, true, getString(R.string.unit_hour), formatter))
        } else {
            viewBind.wheelAmPm.setConfig(WheelIntConfig(0, 1, false, null, object : WheelIntFormatter {
                override fun format(index: Int, value: Int): String {
                    return if (index == 0) {
                        requireContext().getString(R.string.ds_alarm_am)
                    } else {
                        requireContext().getString(R.string.ds_alarm_pm)
                    }
                }
            }))
            viewBind.wheelHour.setConfig(WheelIntConfig(1, 12, true, getString(R.string.unit_hour), formatter))
        }
        viewBind.wheelMinute.setConfig(WheelIntConfig(0, 59, true, getString(R.string.unit_minute), formatter))

        viewBind.btnSave.clickTrigger(block = blockClick)
        viewBind.btnDelete.clickTrigger(block = blockClick)
        viewBind.itemRepeat.clickTrigger(block = blockClick)
        viewBind.itemLabel.clickTrigger(block = blockClick)
        updateUI()
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.btnSave -> {
                var hour = viewBind.wheelHour.getValue()
                if (!is24HourFormat) { //12 Hour format
                    if (viewBind.wheelAmPm.getValue() == 0) { //AM
                        if (hour == 12) {
                            hour = 0
                        }
                    } else {
                        if (hour < 12) {
                            hour += 12
                        }
                    }
                }

                alarm.hour = hour
                alarm.minute = viewBind.wheelMinute.getValue()
                alarm.isEnabled = true
                alarm.adjust()
                if (isEditMode) {
                    viewModel.modifyAlarm(args.position, alarm)
                } else {
                    viewModel.addAlarm(alarm)
                }
                findNavController().navigateUp()
            }
            viewBind.btnDelete -> {
                viewModel.deleteAlarm(args.position)
                findNavController().navigateUp()
            }
            viewBind.itemRepeat -> {
                AlarmRepeatDialogFragment().show(childFragmentManager, null)
            }
            viewBind.itemLabel -> {
                AlarmLabelDialogFragment().show(childFragmentManager, null)
            }
        }
    }

    private fun updateUI() {
        var hour: Int = alarm.hour
        val minute: Int = alarm.minute
        if (hour == 24 && minute == 0) {
            if (is24HourFormat) {
                viewBind.wheelHour.setValue(23)
                viewBind.wheelMinute.setValue(59)
            } else {
                viewBind.wheelAmPm.setValue(0)
                viewBind.wheelHour.setValue(12)
                viewBind.wheelMinute.setValue(0)
            }
        } else {
            if (is24HourFormat) {
                viewBind.wheelHour.setValue(hour)
                viewBind.wheelMinute.setValue(minute)
            } else {
                if (hour < 12) { //AM
                    viewBind.wheelAmPm.setValue(0)
                    if (hour == 0) {
                        hour = 12
                    }
                } else {
                    viewBind.wheelAmPm.setValue(1)
                    if (hour > 12) {
                        hour -= 12
                    }
                }
                viewBind.wheelHour.setValue(hour)
                viewBind.wheelMinute.setValue(minute)
            }
        }
        viewBind.itemLabel.getTextView().text = getAlarmLabel()
        viewBind.itemRepeat.getTextView().text = viewModel.helper.repeatToSimpleStr(requireContext(), alarm.repeat)
    }

    private fun getAlarmLabel(): String? {
        return if (alarm.label.isNullOrEmpty()) {
            getString(R.string.ds_alarm_label_default)
        } else {
            alarm.label
        }
    }

    override fun dialogGetAlarmLabel(): String? {
        return getAlarmLabel()
    }

    override fun dialogSetAlarmLabel(label: String?) {
        alarm.label = label
        viewBind.itemLabel.getTextView().text = label
    }

    override fun dialogGetAlarmRepeat(): Int {
        return alarm.repeat
    }

    override fun dialogSetAlarmRepeat(repeat: Int) {
        alarm.repeat = repeat
        viewBind.itemRepeat.getTextView().text = viewModel.helper.repeatToSimpleStr(requireContext(), alarm.repeat)
    }

}
