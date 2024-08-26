package com.topstep.fitcloud.sample2.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.BuildConfig
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentCombineBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.model.wh.MenstruationResult
import com.topstep.fitcloud.sample2.model.wh.WomenHealthConfig
import com.topstep.fitcloud.sample2.model.wh.WomenHealthMode
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.ui.combine.LogShareDialogFragment
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.FcConnector
import com.topstep.fitcloud.sdk.v2.features.FcLogACKType
import com.topstep.fitcloud.sdk.v2.features.FcSettingsFeature
import com.topstep.fitcloud.sdk.v2.model.config.FcWomenHealthConfig
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import timber.log.Timber
import java.util.*

/**
 * This page mainly shows the functions that need to be combined with the "FitCloud" device and APP/mobile data
 *
 * <p>
 * 1.User info(id, height, weight, sex, etc.)
 * Almost every APP has an account system, and the "FitCloud" device needs to use a unique id to distinguish different users.
 * ToNote: When using a different user id to bind the device, the device will clear the data of the previous user. This point is very important.
 * When using [FcConnector.connect]to connect to the device, you need to pass in these information.
 * When editing user information, such as changing height, you need to set these changes to the device
 *
 * <p>
 * 2.Women Health Function
 * Due to historical legacy issues, when reading this config from the device, only partial data may be returned.
 * Therefore, it is recommended not to read this config from the device, but always follow the config in your APP.
 *
 * <p>
 * 3. Exercise Goal
 * Most devices can only [FcSettingsFeature.setExerciseGoal] and cannot read them from the device.
 * So this part of the data needs to be stored in the APP, such as the database.
 *
 */
class CombineFragment : BaseFragment(R.layout.fragment_combine) {

    private val viewBind: FragmentCombineBinding by viewBinding()
    private val viewModel by viewModels<CombineViewModel>()
    private val womenHealthRepository = Injector.getWomenHealthRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.itemUserInfo.clickTrigger {
            findNavController().navigate(CombineFragmentDirections.toEditUserInfo())
        }
        viewBind.itemWomenHealth.clickTrigger {
            findNavController().navigate(CombineFragmentDirections.toWhHomePage())
        }
        viewBind.itemWomenHealthDetail.clickTrigger {
            val mode = womenHealthRepository.flowCurrent.value?.mode ?: return@clickTrigger
            findNavController().navigate(CombineFragmentDirections.toWhDetail(mode))
        }
        viewBind.itemExerciseGoal.clickTrigger {
            findNavController().navigate(CombineFragmentDirections.toExerciseGoal())
        }
        viewBind.itemLog.clickTrigger {
            viewLifecycleScope.launch {
                val files = AppLogger.getLogFiles(requireContext())
                if (files.isNullOrEmpty()) {
                    promptToast.showFailed(R.string.tip_current_no_data)
                    return@launch
                }
                LogShareDialogFragment.newInstance(files).show(childFragmentManager, null)
            }
        }
        viewBind.itemDeviceLog.clickTrigger {
            val disposable = viewModel.deviceManager.logFeature.execute(AppLogger.dirLog(requireContext())?.path ?: "")
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    viewBind.tvDeviceLogState.visibility = View.VISIBLE
                    viewBind.tvDeviceLogState.text = it.message
                }
                .onErrorComplete()
                .subscribe({
                    when (it.ackType) {
                        FcLogACKType.LOG_UPGRADE_ING -> {
                            viewBind.tvDeviceLogState.visibility = View.VISIBLE
                            viewBind.tvDeviceLogState.text = "获取日志，进度：${it.progress}%"
                        }
                        FcLogACKType.LOG_UPGRADE_EMPTY -> {
                            viewBind.tvDeviceLogState.visibility = View.VISIBLE
                            viewBind.tvDeviceLogState.text = "未获取到日志！"
                        }
                        FcLogACKType.LOG_UPGRADE_SUCCESS -> {
                            viewBind.tvDeviceLogState.visibility = View.VISIBLE
                            viewBind.tvDeviceLogState.text = "获取日志完成：${it.filePath}"
                        }
                        FcLogACKType.LOG_TIMEOUT_ERROR -> {
                            viewBind.tvDeviceLogState.visibility = View.VISIBLE
                            viewBind.tvDeviceLogState.text = "获取数据超时"
                        }
                        else -> {
                            viewBind.tvDeviceLogState.visibility = View.GONE
                        }
                    }
                }, {
                    Timber.tag("itemDeviceLog").w(it)
                })
        }
        viewBind.btnSignOut.clickTrigger {
            viewModel.signOut()
        }
        viewBind.tvVersion.text = "version:" + BuildConfig.VERSION_NAME
        lifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect {
                    if (it.async is Loading) {
                        promptProgress.showProgress(R.string.account_sign_out_ing)
                    } else {
                        promptProgress.dismiss()
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect {
                    when (it) {
                        is AsyncEvent.OnFail -> promptToast.showFailed(it.error)
                        is AsyncEvent.OnSuccess<*> -> {
                            startActivity(Intent(requireContext(), LaunchActivity::class.java))
                            requireActivity().finish()
                        }
                    }
                }
            }
            launch {
                womenHealthRepository.flowCurrent.collect {
                    updateWomenHealth(it)
                }
            }
            launch {
                viewModel.flowDeviceInfo.collect {
                    Log.e("Kilnn", "deviceInfo:$it")
                }
            }
        }
    }

    private suspend fun updateWomenHealth(config: WomenHealthConfig?) {
        if (config == null) {
            viewBind.itemWomenHealthDetail.visibility = View.GONE
            return
        }
        viewBind.itemWomenHealthDetail.visibility = View.VISIBLE

        val calendar = Calendar.getInstance()
        val today = Date()

        var text: String? = null
        if (config.mode == WomenHealthMode.PREGNANCY) {
            if (config.remindType == FcWomenHealthConfig.RemindType.PREGNANCY_DAYS) {
                val pregnancyDays = WomenHealthUtils.getPregnancyDays(calendar, config.latest, config.cycle, today)
                if (pregnancyDays != null) {
                    val weeks = pregnancyDays / 7
                    val days = pregnancyDays % 7
                    text = getString(R.string.wh_pregnancy_remind_info2, weeks, days)
                }
            } else {
                val dueDays = WomenHealthUtils.getDueDays(calendar, config.latest, config.cycle, today)
                if (dueDays != null) {
                    text = getString(R.string.wh_pregnancy_remind_info1, dueDays)
                }
            }
        } else {
            val result = womenHealthRepository.getMenstruationResult(calendar, today)
            if (result != null) {
                if (result.dateType == MenstruationResult.DateType.MENSTRUATION) {
                    text = getString(R.string.wh_menstruation_remind_menstruation_days, result.dayInCycle)
                } else {
                    text = if (result.remindNext != null) {
                        getString(R.string.wh_menstruation_remind_next, result.remindNext)
                    } else {
                        when (result.dateType) {
                            MenstruationResult.DateType.OVULATION -> {
                                getString(R.string.wh_menstruation_remind_ovulation)
                            }
                            MenstruationResult.DateType.OVULATION_DAY -> {
                                getString(R.string.wh_menstruation_remind_ovulation_day)
                            }
                            else -> {//安全期
                                getString(R.string.wh_menstruation_remind_safe)
                            }
                        }
                    }
                }
            }
        }

        viewBind.itemWomenHealthDetail.getTitleView().text = text
    }
}

class CombineViewModel : AsyncViewModel<SingleAsyncState<Unit>>(SingleAsyncState()) {

    private val authManager = Injector.getAuthManager()
    val deviceManager = Injector.getDeviceManager()

    //ToNote:Because convert as val parameter, so need Observable.defer{} to wrap it
    val flowDeviceInfo = Observable.defer {
        deviceManager.configFeature.observerDeviceInfo().startWithItem(deviceManager.configFeature.getDeviceInfo())
    }.asFlow().shareInView(viewModelScope)

    fun signOut() {
        suspend {
            //Delay 3 seconds. Simulate the sign out process
            delay(3000)
            authManager.signOut()
        }.execute(SingleAsyncState<Unit>::async) {
            copy(async = it)
        }
    }
}