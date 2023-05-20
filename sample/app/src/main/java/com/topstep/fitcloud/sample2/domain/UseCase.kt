package com.topstep.fitcloud.sample2.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Executes business logic synchronously or asynchronously using Coroutines.
 */
abstract class UseCase<in P, R>(private val coroutineDispatcher: CoroutineDispatcher) {

    /** Executes the use case asynchronously
     *
     * @return result
     *
     * @param params the input parameters to run the use case with
     */
    suspend operator fun invoke(params: P): R {
        return try {
            // Moving all use case's executions to the injected dispatcher
            // In production code, this is usually the Default dispatcher (background thread)
            // In tests, this becomes a TestCoroutineDispatcher
            withContext(coroutineDispatcher) {
                execute(params)
            }
        } catch (e: Exception) {
            Timber.tag(this.javaClass.simpleName).d(e)
            throw e
        }
    }

    /**
     * Override this to set the code to be executed.
     */
    protected abstract suspend fun execute(params: P): R
}