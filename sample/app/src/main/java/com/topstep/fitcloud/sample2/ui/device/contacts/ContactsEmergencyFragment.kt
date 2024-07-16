package com.topstep.fitcloud.sample2.ui.device.contacts

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentContactsEmergencyBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.PermissionHelper
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.settings.FcContacts
import com.topstep.fitcloud.sdk.v2.model.settings.FcContactsEmergency
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber

class ContactsEmergencyFragment : BaseFragment(R.layout.fragment_contacts_emergency) {

    private val viewBind: FragmentContactsEmergencyBinding by viewBinding()
    private val deviceManager = Injector.getDeviceManager()
    private var cache: FcContacts? = null
    private var requestDisposable: Disposable? = null
    private var setDisposable: Disposable? = null

    private val pickContact = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val contacts = ContactsFragment.onPickContacts(requireContext(), result)
        if (contacts != null) {
            viewBind.tvName.text = contacts.name
            viewBind.tvNumber.text = contacts.number
            cache = contacts
            setEmergencyContact(viewBind.switchEmergency.isChecked, contacts)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.switchEmergency.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                setEmergencyContact(isChecked, cache)
            }
        }

        // add
        viewBind.btnAdd.clickTrigger {
            PermissionHelper.requestContacts(this) { granted ->
                if (granted) {
                    pickContact.launch(Intent(Intent.ACTION_PICK).apply {
                        type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                    })
                }
            }
        }

        //delete
        viewBind.btnDelete.clickTrigger {
            cache = null
            setEmergencyContact(viewBind.switchEmergency.isChecked, null)
        }

        requestDisposable = deviceManager.fcSDK.contactsAbility.requestContactsEmergency()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                viewBind.switchEmergency.isChecked = it.isEnabled
                it.list?.firstOrNull()?.let { c ->
                    cache = c
                    viewBind.tvName.text = c.name
                    viewBind.tvNumber.text = c.number
                }
            }, {
                Timber.w(it)
                promptToast.showFailed(R.string.tip_failed)
            })
    }

    private fun setEmergencyContact(isSwitch: Boolean, contacts: FcContacts?) {
        setDisposable = deviceManager.fcSDK.contactsAbility.setContactsEmergency(FcContactsEmergency(isSwitch, if (contacts == null) emptyList() else listOf(contacts)))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                promptToast.showSuccess(R.string.tip_success)
            }, {
                Timber.w(it)
                promptToast.showFailed(R.string.tip_failed)
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requestDisposable?.dispose()
        setDisposable?.dispose()
    }

}