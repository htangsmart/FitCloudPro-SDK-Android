package com.topstep.fitcloud.sample2.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.github.kilnn.tool.dialog.prompt.PromptAutoCancel
import com.github.kilnn.tool.dialog.prompt.PromptDialogFragment
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentSignUpBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.MainActivity
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.showFailed
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SignUpFragment : BaseFragment(R.layout.fragment_sign_up), PromptDialogFragment.OnPromptListener {

    private val viewBind: FragmentSignUpBinding by viewBinding()
    private val viewModel by viewModels<SignUpViewMode>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.btnSignUp.setOnClickListener {
            signUp()
        }
        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect {
                    if (it.async is Loading) {
                        promptProgress.showProgress(R.string.action_loading)
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
                            promptToast.showSuccess(R.string.account_sign_up_success, intercept = true, promptId = 1)
                        }
                    }
                }
            }
        }
    }

    private fun signUp() {
        val username = viewBind.editUsername.text.trim().toString().takeIf { it.isNotEmpty() } ?: return
        val password = viewBind.editPassword.text.trim().toString().takeIf { it.isNotEmpty() } ?: return
        val height = viewBind.editHeight.text.trim().toString().toIntOrNull() ?: return
        if (height !in 50..300) {
            promptToast.showInfo(R.string.account_height_error, autoCancel = PromptAutoCancel.Duration(2500))
            return
        }
        val weight = viewBind.editWeight.text.trim().toString().toIntOrNull() ?: return
        if (weight !in 20..300) {
            promptToast.showInfo(R.string.account_weight_error)
            return
        }
        val sex = viewBind.rbSexMale.isChecked
        val age = viewBind.editAge.text.trim().toString().toIntOrNull() ?: return
        if (age !in 1..150) {
            promptToast.showInfo(R.string.account_age_error)
            return
        }
        viewModel.signUp(username, password, height, weight, sex, age)
    }

    override fun onPromptCancel(promptId: Int, cancelReason: Int, tag: String?) {
        if (promptId == 1) {
            MainActivity.start(requireContext())
            requireActivity().finish()
        }
    }

}

class SignUpViewMode : AsyncViewModel<SingleAsyncState<Unit>>(SingleAsyncState()) {

    private val authManager = Injector.getAuthManager()

    fun signUp(username: String, password: String, height: Int, weight: Int, sex: Boolean, age: Int) {
        suspend {
            //Delay 3 seconds. Simulate the sign up process
            delay(3000)
            authManager.signUp(username, password, height, weight, sex, age)
        }.execute(SingleAsyncState<Unit>::async) {
            copy(async = it)
        }
    }
}