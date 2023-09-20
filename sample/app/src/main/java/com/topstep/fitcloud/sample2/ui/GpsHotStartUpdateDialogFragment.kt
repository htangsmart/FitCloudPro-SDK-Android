package com.topstep.fitcloud.sample2.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.DialogGpsHotStartUpdateBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import kotlinx.coroutines.rx3.collect

class GpsHotStartUpdateDialogFragment : AppCompatDialogFragment() {

    private var _viewBind: DialogGpsHotStartUpdateBinding? = null
    private val viewBind get() = _viewBind!!
    private val deviceManager = Injector.getDeviceManager()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBind = DialogGpsHotStartUpdateBinding.inflate(LayoutInflater.from(context))

        isCancelable = false

        lifecycle.launchRepeatOnStarted {
            deviceManager.settingsFeature.observerGpsHotStartUpdateState().collect {
                if (it >= 0) {
                    viewBind.progressBar.progress = it
                } else {
                    dismiss()
                }
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(viewBind.root)
            .setTitle(R.string.gps_hot_start_updating)
            .setCancelable(false)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBind = null
    }
}