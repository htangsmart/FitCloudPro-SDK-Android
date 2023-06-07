package com.topstep.fitcloud.sample2.di.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus

object CoroutinesInstance {

    val defaultDispatcher = Dispatchers.Default

    val ioDispatcher = Dispatchers.IO

    val mainDispatcher = Dispatchers.Main

    val mainImmediateDispatcher = Dispatchers.Main.immediate

    val applicationScope by lazy { CoroutineScope(SupervisorJob() + defaultDispatcher) }

    val applicationIOScope by lazy { applicationScope + ioDispatcher }
}