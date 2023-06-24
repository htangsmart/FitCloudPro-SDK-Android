package com.topstep.fitcloud.sample2.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topstep.fitcloud.sample2.ui.base.AsyncEvent.OnFail
import com.topstep.fitcloud.sample2.ui.base.AsyncEvent.OnSuccess
import com.topstep.fitcloud.sample2.utils.runCatchingWithLog
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.reflect.KProperty1

abstract class StateEventViewModel<State, Event>(initState: State) : ViewModel() {

    private val _flowState = MutableStateFlow(initState)
    val flowState = _flowState.asStateFlow()

    private val _flowEvent = Channel<Event>()
    val flowEvent = _flowEvent.receiveAsFlow()

    val state: State get() = _flowState.value

    protected suspend fun State.newState() {
        _flowState.emit(this)
    }

    protected suspend fun Event.newEvent() {
        _flowEvent.send(this)
    }
}

/**
 * Provide event for all [Async] property in State class
 * Use [OnSuccess.property] or [OnFail.property] to determine which property it belongs to
 */
sealed class AsyncEvent {
    class OnSuccess<T>(val property: KProperty1<*, Async<T>>, val value: T) : AsyncEvent()
    class OnFail(val property: KProperty1<*, Async<*>>, val error: Throwable) : AsyncEvent()
}

abstract class AsyncViewModel<S>(initState: S) : StateEventViewModel<S, AsyncEvent>(initState) {

    protected open fun <T : Any?> (suspend () -> T).execute(
        property: KProperty1<S, Async<T>>,
        reducer: S.(Async<T>) -> S
    ): Job {
        return viewModelScope.launch {
            state.reducer(Loading()).newState()
            try {
                val result = invoke()
                state.reducer(Success(result)).newState()
                OnSuccess(property, result).newEvent()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                state.reducer(Fail(e)).newState()
                OnFail(property, e).newEvent()
            }
        }
    }

}

data class SingleAsyncState<T>(
    val async: Async<T> = Uninitialized
)

abstract class SingleAsyncAction<T>(
    private val coroutineScope: CoroutineScope,
    initState: Async<T>
) {
    private val _flowState = MutableStateFlow(initState)
    val flowState = _flowState.asStateFlow()

    private var job: Job? = null

    fun isSuccess(): Boolean {
        return job?.isCompleted != false && _flowState.value !is Fail
    }

    fun cancel() {
        job?.cancel()
        coroutineScope.launch {
            _flowState.emit(Uninitialized)
        }
    }

    fun retry() {
        execute(0)
    }

    fun execute(delay: Long = 1000) {
        job?.cancel()
        job = coroutineScope.launch {
            _flowState.emit(Loading())
            //Delay by 1 second to avoid frequent modifications
            if (delay > 0) {
                delay(delay)
            }
            runCatchingWithLog {
                action()
            }.onSuccess {
                _flowState.emit(Success(it))
            }.onFailure {
                _flowState.emit(Fail(it))
            }
        }
    }

    abstract suspend fun action(): T

}