package com.topstep.fitcloud.sample2.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentCombineBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.shareInView
import com.topstep.fitcloud.sample2.utils.showFailed
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.FcConnector
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow

/**
 * This page mainly shows the functions that need to be combined with the "FitCloud" device and APP/mobile data
 *
 * <p>
 * 1.User info(id, height, weight, sex, etc.)
 * Almost every APP has an account system, and the "FitCloud" device needs to use a unique id to distinguish different users.
 * ToNote: When using a different user id to bind the device, the device will clear the data of the previous user. This point is very important.
 * When using [FcConnector.connect]to connect to the device, you need to pass in these information.
 * When editing user information, such as changing height, you need to set these changes to the device
 */
class CombineFragment : BaseFragment(R.layout.fragment_combine) {

    private val viewBind: FragmentCombineBinding by viewBinding()
    private val viewModel by viewModels<CombineViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.tvEditUserInfo.setOnClickListener {
            findNavController().navigate(CombineFragmentDirections.toEditUserInfo())
        }
        viewBind.btnSignOut.setOnClickListener {
            viewModel.signOut()
        }
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
                viewModel.flowDeviceInfo.collect {
                    Log.e("Kilnn", "deviceInfo:$it")
                }
            }
        }
    }
}

class CombineViewModel : AsyncViewModel<SingleAsyncState<Unit>>(SingleAsyncState()) {

    private val authManager = Injector.getAuthManager()
    private val deviceManager = Injector.getDeviceManager()

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