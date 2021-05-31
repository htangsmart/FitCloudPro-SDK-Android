package com.github.kilnn.wristband2.sample.dial

import androidx.lifecycle.LiveData

sealed class State<T>(val result: T?) {
    override fun toString(): String {
        return "${javaClass.simpleName}:${result?.toString()}"
    }

    class Loading<T>(result: T? = null) : State<T>(result)
    class Failed<T>(val error: Throwable?, result: T? = null) : State<T>(result)
    class Success<T>(result: T? = null) : State<T>(result)
}

class StateLiveData<T> : LiveData<State<T>?>() {
    ///////set//////
    fun setLoading(clearData: Boolean = false) {
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