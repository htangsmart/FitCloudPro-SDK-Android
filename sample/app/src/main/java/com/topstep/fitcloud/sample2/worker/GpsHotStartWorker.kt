package com.topstep.fitcloud.sample2.worker

import android.content.Context
import androidx.work.*
import com.topstep.fitcloud.sample2.fcSDK
import com.topstep.fitcloud.sdk.v2.exception.FcGpsHotStartException
import kotlinx.coroutines.rx3.await
import timber.log.Timber
import java.util.concurrent.TimeUnit

class GpsHotStartWorker constructor(
    context: Context,
    private val workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag(TAG).i("doWork")
        val force = workerParams.inputData.getBoolean(EXTRA_FORCE, false)

        val settingsFeature = applicationContext.fcSDK.connector.settingsFeature()
        return try {
            settingsFeature.updateGpsHotStart(force).await()
            Result.success()
        } catch (e: Exception) {
            Timber.w(e)
            if (e is FcGpsHotStartException &&
                (e.errorCode == FcGpsHotStartException.ERROR_UPDATING
                        || e.errorCode == FcGpsHotStartException.ERROR_NOT_SUPPORT
                        || e.errorCode == FcGpsHotStartException.ERROR_NOT_NECESSARY
                        || e.errorCode == FcGpsHotStartException.ERROR_REFRESH_TIMEOUT
                        )
            ) {
                Result.success()
            } else {
                Result.retry()
            }
        }
    }

    companion object {
        private const val TAG = "GpsHotStartWorker"
        private const val EXTRA_FORCE = "force"
        private const val NAME_ONCE = "gps_hot_start_once"
        private const val NAME_PERIODIC = "gps_hot_start_periodic"

        fun executeTestImmediately(context: Context) {
            Timber.tag(TAG).i("executeTestImmediately")
            WorkManager.getInstance(context).enqueueUniqueWork(
                NAME_ONCE, ExistingWorkPolicy.REPLACE,
                getWorkBuilder(false)
                    .build()
            )
        }

        fun executeTestDelay1(context: Context) {
            Timber.tag(TAG).i("executeDelay1")
            WorkManager.getInstance(context).enqueueUniqueWork(
                NAME_ONCE, ExistingWorkPolicy.REPLACE,
                getWorkBuilder()
                    .setInitialDelay(1, TimeUnit.MINUTES)
                    .build()
            )
        }

        fun executeTestDelay3(context: Context) {
            Timber.tag(TAG).i("executeDelay1")
            WorkManager.getInstance(context).enqueueUniqueWork(
                NAME_ONCE, ExistingWorkPolicy.REPLACE,
                getWorkBuilder(false)
                    .setInitialDelay(3, TimeUnit.MINUTES)
                    .build()
            )
        }

        fun executeTestDelay10(context: Context) {
            Timber.tag(TAG).i("executeDelay1")
            WorkManager.getInstance(context).enqueueUniqueWork(
                NAME_ONCE, ExistingWorkPolicy.REPLACE,
                getWorkBuilder()
                    .setInitialDelay(10, TimeUnit.MINUTES)
                    .build()
            )
        }

        private fun getWorkBuilder(force: Boolean = true): OneTimeWorkRequest.Builder {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build()
            return OneTimeWorkRequest.Builder(GpsHotStartWorker::class.java)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInputData(Data.Builder().putBoolean(EXTRA_FORCE, force).build())
        }

        fun cancelPeriodic(context: Context) {
            Timber.tag(TAG).i("cancelPeriodic")
            WorkManager.getInstance(context).cancelUniqueWork(NAME_PERIODIC)
        }

        fun executePeriodic(context: Context) {
            Timber.tag(TAG).i("executePeriodic")
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build()
            val builder = PeriodicWorkRequest.Builder(GpsHotStartWorker::class.java, 3, TimeUnit.HOURS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInputData(Data.Builder().putBoolean(EXTRA_FORCE, false).build())
                .setInitialDelay(120, TimeUnit.SECONDS)
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(NAME_PERIODIC, ExistingPeriodicWorkPolicy.KEEP, builder.build())
        }

    }
}