package com.topstep.fitcloud.sample2.ui.auth

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentSignInBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.MainActivity
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.utils.launchRepeatOnStarted
import com.topstep.fitcloud.sample2.utils.showFailed
import com.topstep.fitcloud.sample2.utils.viewLifecycle
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The sample uses a simulated user system, and the user information is stored in the local database.
 * The user system is used to show how to associate the user id, height, etc. with the "FitCloud" device.
 * You don't need to pay attention to the logic of user sign in/up itself.
 */
class SignInFragment : BaseFragment(R.layout.fragment_sign_in) {

    private val viewBind: FragmentSignInBinding by viewBinding()
    private val viewModel by viewModels<SignInViewMode>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.tvNoAccount.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        viewBind.tvNoAccount.setOnClickListener {
            findNavController().navigate(SignInFragmentDirections.toSignUp())
        }
        viewBind.btnSignIn.setOnClickListener {
            signIn()
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
                            MainActivity.start(requireContext())
                            requireActivity().finish()
                        }
                    }
                }
            }
        }
    }

    private fun signIn() {
        val username = viewBind.editUsername.text.trim().toString().takeIf { it.isNotEmpty() } ?: return
        val password = viewBind.editPassword.text.trim().toString().takeIf { it.isNotEmpty() } ?: return
        viewModel.signIn(username, password)
    }

}

class SignInViewMode : AsyncViewModel<SingleAsyncState<Unit>>(SingleAsyncState()) {

    private val authManager = Injector.getAuthManager()

    fun signIn(username: String, password: String) {
        suspend {
            //Delay 3 seconds. Simulate the sign in process
            delay(3000)
            authManager.signIn(username, password)
        }.execute(SingleAsyncState<Unit>::async) {
            copy(async = it)
        }
    }

}