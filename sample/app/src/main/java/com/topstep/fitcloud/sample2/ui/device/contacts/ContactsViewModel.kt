package com.topstep.fitcloud.sample2.ui.device.contacts

import androidx.lifecycle.viewModelScope
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.*
import com.topstep.fitcloud.sample2.utils.runCatchingWithLog
import com.topstep.fitcloud.sdk.v2.model.settings.FcContacts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await

data class ContactsState(
    val requestContacts: Async<ArrayList<FcContacts>> = Uninitialized,
)

sealed class ContactsEvent {
    class RequestFail(val throwable: Throwable) : ContactsEvent()

    class Inserted(val position: Int) : ContactsEvent()
    class Removed(val position: Int) : ContactsEvent()
    class Moved(val fromPosition: Int, val toPosition: Int) : ContactsEvent()

    object NavigateUp : ContactsEvent()
}

class ContactsViewModel : StateEventViewModel<ContactsState, ContactsEvent>(ContactsState()) {

    private val deviceManager = Injector.getDeviceManager()

    init {
        requestContacts()
    }

    fun requestContacts() {
        viewModelScope.launch {
            state.copy(requestContacts = Loading()).newState()
            runCatchingWithLog {
                deviceManager.settingsFeature.requestContacts().await()
            }.onSuccess {
                state.copy(requestContacts = Success(ArrayList(it))).newState()
            }.onFailure {
                state.copy(requestContacts = Fail(it)).newState()
                ContactsEvent.RequestFail(it).newEvent()
            }
        }
    }

    fun addContacts(contacts: FcContacts) {
        viewModelScope.launch {
            val list = state.requestContacts()
            if (list != null) {
                var exist = false
                for (item in list) {
                    if (item.number == contacts.number) {
                        exist = true
                        break
                    }
                }
                if (!exist) {
                    list.add(contacts)
                    ContactsEvent.Inserted(list.size).newEvent()
                    setContactsAction.execute()
                }
            }
        }
    }

    /**
     * @param position 要删除的位置
     */
    fun deleteContacts(position: Int) {
        viewModelScope.launch {
            val list = state.requestContacts()
            if (list != null && position < list.size) {
                list.removeAt(position)
                ContactsEvent.Removed(position).newEvent()
                setContactsAction.execute()
            }
        }
    }

    fun sendNavigateUpEvent() {
        viewModelScope.launch {
            delay(1000)
            ContactsEvent.NavigateUp.newEvent()
        }
    }

    val setContactsAction = object : SingleAsyncAction<Unit>(
        viewModelScope,
        Uninitialized
    ) {
        override suspend fun action() {
            deviceManager.settingsFeature.setContacts(state.requestContacts()).await()
        }
    }
}