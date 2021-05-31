package com.github.kilnn.wristband2.sample.dial

import androidx.lifecycle.MediatorLiveData


class MediatorStateLiveData<T> : MediatorLiveData<State<T>?>() {

    ///////set//////
    fun setLoading(clearData: Boolean = true) {
        value = if (clearData) {
            State.Loading()
        } else {
            State.Loading(value?.result)
        }
    }

    fun setSuccess(obj: T?, clearData: Boolean = true) {
        value = if (value == null) {
            if (clearData) {
                State.Success()
            } else {
                State.Success(value?.result)
            }
        } else {
            State.Success(obj)
        }
    }

    fun setFailed(error: Throwable? = null, clearData: Boolean = false) {
        value = if (clearData) {
            State.Failed(error)
        } else {
            State.Failed(error, value?.result)
        }
    }

    ///////post//////
    fun postLoading(clearData: Boolean = false) {
        postValue(
            if (clearData) {
                State.Loading()
            } else {
                State.Loading(value?.result)
            }
        )
    }

    fun postSuccess(obj: T?, clearData: Boolean = true) {
        postValue(
            if (value == null) {
                if (clearData) {
                    State.Success()
                } else {
                    State.Success(value?.result)
                }
            } else {
                State.Success(obj)
            }
        )
    }

    fun postFailed(error: Throwable? = null, clearData: Boolean = false) {
        postValue(
            if (clearData) {
                State.Failed(error)
            } else {
                State.Failed(error, value?.result)
            }
        )
    }

}