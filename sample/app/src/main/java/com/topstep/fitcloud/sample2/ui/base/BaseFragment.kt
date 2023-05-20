package com.topstep.fitcloud.sample2.ui.base

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.topstep.fitcloud.sample2.utils.promptProgress
import com.topstep.fitcloud.sample2.utils.promptToast

abstract class BaseFragment : Fragment {

    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    protected val promptToast by promptToast()
    protected val promptProgress by promptProgress()

}