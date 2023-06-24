package com.topstep.fitcloud.sample2.ui.device.contacts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentContactsBinding
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
import timber.log.Timber

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
class ContactsFragment : BaseFragment(R.layout.fragment_contacts) {

    private val viewBind: FragmentContactsBinding by viewBinding()
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var adapter: ContactsAdapter

    private val pickContact = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && uri != null) {
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val cursor = requireContext().contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                var number = cursor.getString(numberIndex)
                val name = cursor.getString(nameIndex)
                Timber.i("select contacts result: [$name , $number]")
                cursor.close()
                if (!name.isNullOrEmpty() && !number.isNullOrEmpty()) {
                    number = number.replace(" ".toRegex(), "")
                    val newContacts = FcContacts.create(name, number)
                    if (newContacts != null) {
                        viewModel.addContacts(newContacts)
                    }
                }
            }
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

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == android.R.id.home) {
                    onBackPressed()
                    return true
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
                if ((adapter.sources?.size ?: 0) >= 10) {
                    promptToast.showInfo(R.string.ds_contacts_tips1)
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
                            if (contacts == null || contacts.isEmpty()) {
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
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            if (adapter.itemCount <= 0) {
                viewBind.loadingView.showError(R.string.tip_current_no_data)
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
}