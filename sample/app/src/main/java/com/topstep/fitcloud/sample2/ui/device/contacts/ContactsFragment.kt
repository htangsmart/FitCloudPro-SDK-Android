package com.topstep.fitcloud.sample2.ui.device.contacts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentContactsBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.base.Fail
import com.topstep.fitcloud.sample2.ui.base.Loading
import com.topstep.fitcloud.sample2.ui.base.Success
import com.topstep.fitcloud.sample2.ui.widget.LoadingView
import com.topstep.fitcloud.sample2.utils.*
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.features.FcSettingsFeature
import com.topstep.fitcloud.sdk.v2.model.settings.FcContacts
import kotlinx.coroutines.launch

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/10.Other-Features#setting-contacts
 *
 * ***Description**
 * Display and modify contacts
 *
 * **Usage**
 * 1. [ContactsFragment]
 * Display and add contacts
 * [ContactsAdapter]
 *
 * And wait contacts changes saving.
 * [SetContactsDialogFragment]
 *
 * 2. [ContactsViewModel]
 * Show how to request contacts and set contacts
 * [FcSettingsFeature.requestContacts] [FcSettingsFeature.setContacts]
 *
 */
class ContactsFragment : BaseFragment(R.layout.fragment_contacts), RandomContactsDialogFragment.Listener {

    private val viewBind: FragmentContactsBinding by viewBinding()
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var adapter: ContactsAdapter
    private val deviceManager = Injector.getDeviceManager()

    private val pickContact = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val contacts = onPickContacts(requireContext(), result)
        if (contacts != null) {
            viewModel.addContacts(contacts)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ContactsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_contacts, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == android.R.id.home) {
                    onBackPressed()
                    return true
                } else if (menuItem.itemId == R.id.menu_random) {
                    val limit = deviceManager.fcSDK.contactsAbility.getContactsMaxNumber()
                    val size = adapter.sources?.size ?: 0

                    RandomContactsDialogFragment.newInstance(
                        limit - size
                    ).show(childFragmentManager, null)
                }
                return false
            }
        }, viewLifecycleOwner)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })

        viewBind.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewBind.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        adapter.listener = object : ContactsAdapter.Listener {
            override fun onItemDelete(position: Int) {
                viewModel.deleteContacts(position)
            }
        }
        adapter.registerAdapterDataObserver(adapterDataObserver)
        viewBind.recyclerView.adapter = adapter

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.requestContacts()
        }
        viewBind.loadingView.associateViews = arrayOf(viewBind.recyclerView)

        viewBind.fabAdd.setOnClickListener {
            viewLifecycleScope.launchWhenResumed {
                val limit = deviceManager.fcSDK.contactsAbility.getContactsMaxNumber()
                val size = adapter.sources?.size ?: 0
                if (size >= limit) {
                    promptToast.showInfo("Up to $limit contacts can be added.")
                } else {
                    PermissionHelper.requestContacts(this@ContactsFragment) { granted ->
                        if (granted) {
                            pickContact.launch(Intent(Intent.ACTION_PICK).apply {
                                type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                            })
                        }
                    }
                }
            }
        }

        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect { state ->
                    when (state.requestContacts) {
                        is Loading -> {
                            viewBind.loadingView.showLoading()
                            viewBind.fabAdd.hide()
                        }
                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                            viewBind.fabAdd.hide()
                        }
                        is Success -> {
                            val contacts = state.requestContacts()
                            if (contacts.isNullOrEmpty()) {
                                viewBind.loadingView.showError(R.string.tip_current_no_data)
                            } else {
                                viewBind.loadingView.visibility = View.GONE
                            }
                            adapter.sources = contacts
                            adapter.notifyDataSetChanged()

                            viewBind.fabAdd.show()
                        }
                        else -> {}
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect { event ->
                    when (event) {
                        is ContactsEvent.RequestFail -> {
                            promptToast.showFailed(event.throwable)
                        }

                        is ContactsEvent.Inserted -> {
                            adapter.notifyItemInserted(event.position)
                        }

                        is ContactsEvent.Removed -> {
                            adapter.notifyItemRemoved(event.position)
                        }

                        is ContactsEvent.Moved -> {
                            adapter.notifyItemMoved(event.fromPosition, event.toPosition)
                        }

                        is ContactsEvent.NavigateUp -> {
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            if (adapter.itemCount <= 0) {
                viewBind.loadingView.showError(R.string.tip_current_no_data)
            } else {
                viewBind.loadingView.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.unregisterAdapterDataObserver(adapterDataObserver)
    }

    private fun onBackPressed() {
        if (viewModel.setContactsAction.isSuccess()) {
            findNavController().navigateUp()
        } else {
            SetContactsDialogFragment().show(childFragmentManager, null)
        }
    }

    override fun onDialogRandom(size: Int) {
        if (size <= 0) return
        viewModel.randomContacts(size)
    }

    companion object {
        fun onPickContacts(context: Context, result: ActivityResult): FcContacts? {
            val uri = result.data?.data
            if (uri == null || result.resultCode != Activity.RESULT_OK) return null
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )
            context.contentResolver.query(uri, projection, null, null, null)?.use {
                if (it.moveToFirst()) {
                    val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val number = it.getString(numberIndex)?.replace(" ", "")
                    val name = it.getString(nameIndex)
                    if (!name.isNullOrEmpty() && !number.isNullOrEmpty()) {
                        return FcContacts.create(name, number)
                    }
                }
            }
            return null
        }
    }
}