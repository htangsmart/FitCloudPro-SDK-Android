package com.topstep.fitcloud.sample2.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.topstep.fitcloud.sample2.databinding.FragmentRealtimeBinding

class RealtimeFragment : Fragment() {

    private var _viewBind: FragmentRealtimeBinding? = null
    private val viewBind get() = _viewBind!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _viewBind = FragmentRealtimeBinding.inflate(inflater, container, false)
        return viewBind.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBind = null
    }

}