package com.topstep.fitcloud.sample2.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentSyncBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding

class SyncFragment : Fragment(R.layout.fragment_sync) {

    private val viewBind: FragmentSyncBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.btnSync.clickTrigger {
            deviceManager.syncData()
        }
    }

}