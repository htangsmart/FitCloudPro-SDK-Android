package com.topstep.fitcloud.sample2.ui.device.dial.component

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.ui.base.Loading
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.promptProgress
import com.topstep.fitcloud.sample2.utils.promptToast
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import kotlinx.coroutines.launch

/**
 * Dial Component Main Fragment
 */
class DialComponentFragment : NavHostFragment() {

    private val toast by promptToast()
    private val progress by promptProgress()
    private val viewModel: DialComponentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController.setGraph(R.navigation.dial_component_nav_graph)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /**
         * Because [DialComponentViewModel.setComponents] is called on multiple sub fragment,
         * so the state is processed on the parent fragment
         */
        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect {
                    when (it.setComponents) {
                        is Loading -> progress.showProgress(R.string.tip_please_wait)
                        else -> progress.dismiss()
                    }
                }
            }
        }
    }

}