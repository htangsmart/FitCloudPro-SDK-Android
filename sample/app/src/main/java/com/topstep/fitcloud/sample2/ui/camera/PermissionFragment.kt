package com.topstep.fitcloud.sample2.ui.camera

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.topstep.fitcloud.sample2.utils.PermissionHelper

class PermissionFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenResumed {
            PermissionHelper.requestAppCamera(this@PermissionFragment) { granted ->
                if (granted) {
                    navigateToCamera()
                } else {
                    requireActivity().finish()
                }
            }
        }
    }

    private fun navigateToCamera() {
        lifecycleScope.launchWhenStarted {
            findNavController().navigate(PermissionFragmentDirections.toCamera())
        }
    }
}