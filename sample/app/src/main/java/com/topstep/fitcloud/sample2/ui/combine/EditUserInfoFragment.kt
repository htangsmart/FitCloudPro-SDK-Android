package com.topstep.fitcloud.sample2.ui.combine

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.kilnn.tool.dialog.prompt.PromptAutoCancel
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentEditUserInfoBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.viewLifecycleScope
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding

class EditUserInfoFragment : BaseFragment(R.layout.fragment_edit_user_info) {

    private val viewBind: FragmentEditUserInfoBinding by viewBinding()
    private val userInfoRepository = Injector.getUserInfoRepository()
    private val authedUserId = Injector.requireAuthedUserId()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleScope.launchWhenStarted {
            val info = userInfoRepository.getUserInfo(authedUserId)
            viewBind.editHeight.setText(info.height.toString())
            viewBind.editWeight.setText(info.weight.toString())
            viewBind.rgSex.check(
                if (info.sex) {
                    R.id.rb_sex_male
                } else {
                    R.id.rb_sex_female
                }
            )
            viewBind.editAge.setText(info.age.toString())
        }
        viewBind.btnSave.setOnClickListener {
            save()
        }
    }

    private fun save() {
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
        lifecycleScope.launchWhenStarted {
            userInfoRepository.setUserInfo(
                authedUserId, height, weight, sex, age
            )
            findNavController().popBackStack()
        }
    }
}