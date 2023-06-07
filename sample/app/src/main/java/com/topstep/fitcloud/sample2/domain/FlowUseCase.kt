package com.topstep.fitcloud.sample2.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

/**
 * Executes business logic in its execute method and keep posting updates to the result as
 * [Result<R>].
 * Handling an exception (emit [Result.failure] to the result) is the subclasses's responsibility.
 */
abstract class FlowUseCase<in P, R>(private val coroutineDispatcher: CoroutineDispatcher) {
    operator fun invoke(params: P): Flow<R> = execute(params)
        .catch { e -> Timber.tag(this.javaClass.simpleName).d(e) }
        .flowOn(coroutineDispatcher)

    protected abstract fun execute(params: P): Flow<R>
}
