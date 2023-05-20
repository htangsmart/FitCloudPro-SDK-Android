package com.topstep.fitcloud.sample2.domain

import timber.log.Timber


abstract class NormalUseCase<in P, R> {

    operator fun invoke(params: P): R {
        return try {
            execute(params)
        } catch (e: Exception) {
            Timber.tag(this.javaClass.simpleName).d(e)
            throw e
        }
    }

    /**
     * Override this to set the code to be executed.
     */
    protected abstract fun execute(params: P): R
}
