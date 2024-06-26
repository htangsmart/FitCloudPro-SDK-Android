package com.topstep.fitcloud.sample2

import android.app.Application
import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.polidea.rxandroidble3.LogConstants
import com.polidea.rxandroidble3.LogOptions
import com.polidea.rxandroidble3.RxBleClient
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sdk.v2.FcSDK
import com.topstep.fitcloud.sdk.v2.features.FcBuiltInFeatures
import com.topstep.fitcloud.sdk.v2.features.FcGpsHotStartProvider
import com.topstep.fitcloud.sdk.v2.model.settings.gps.FcGpsEpoInfo
import com.topstep.fitcloud.sdk.v2.model.settings.gps.FcGpsLocationInfo
import com.topstep.wearkit.base.ProcessLifecycleManager
import com.topstep.wearkit.base.utils.Optional
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.exceptions.CompositeException
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Init "FitCloudSDK".
 * <p>
 * "FitCloudSDK" is a BLE library to scan/connect "FitCloud" device.
 * The main responsibility of this library is to interact with "FitCloud" devices in accordance with the protocol
 * <p>
 * "FitCloudSDK" is based on "RxAndroidBLE", which is a basic ble library.
 * While you are developing with "FitCloudSDK", you can also use many convenient methods in "RxAndroidBLE". You can see some usage in this sample.
 * <p>
 */
@MainThread
fun fitCloudSDKInit(application: Application) {
    //ToNote:1.Configure log
    //"FitCloudSDK" use the Timber to output log, so you need to configure the Timber
//    if (BuildConfig.DEBUG) {
//        Timber.plant(Timber.DebugTree())
//    } else {
//        Timber.plant(object : Timber.DebugTree() {
//            override fun isLoggable(tag: String?, priority: Int): Boolean {
//                return priority > Log.DEBUG
//            }
//        })
//    }
    //The BLE function of "FitCloudSDK" is based on "RxAndroidBLE". So you also need to configure the log settings of "RxAndroidBLE"
    RxBleClient.updateLogOptions(
        LogOptions.Builder()
            .setShouldLogAttributeValues(BuildConfig.DEBUG)
            .setShouldLogScannedPeripherals(false)
            .setMacAddressLogSetting(if (BuildConfig.DEBUG) LogConstants.MAC_ADDRESS_FULL else LogConstants.NONE)
            .setUuidsLogSetting(if (BuildConfig.DEBUG) LogConstants.UUIDS_FULL else LogConstants.NONE)
            .setLogLevel(if (BuildConfig.DEBUG) LogConstants.DEBUG else LogConstants.INFO)
            .build()
    )

    //ToNote:2.Init
    //Before using any function of FitCloudSDK, you must initialize it first
    //In this sample, we use kotlin delegate to maintained [FcSDK] singleton.
    //If you use other methods, you need to keep the singleton and call the constructor of [FcSDK] here
    application.fcSDK

    //ToNote:3.RxJavaPlugins.setErrorHandler
    //Because rxjava is used in the SDK, some known exceptions that cannot be distributed need to be handled to avoid app crash.
    val ignoreExceptions = HashSet<Class<out Throwable>>()
//    ignoreExceptions.add(YourAppIgnoredException::class.java)//Exceptions need to be ignored in your own app (maybe not according to your own app)
    ignoreExceptions.addAll(FcSDK.rxJavaPluginsIgnoreExceptions())//Exceptions need to be ignored in the SDK
    RxJavaPlugins.setErrorHandler(RxJavaPluginsErrorHandler(ignoreExceptions))

    //ToNote:4.The connector must be initialized by the main thread
    application.fcSDK.connector
}

class MyProcessLifecycleManager : ProcessLifecycleManager(), DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        setForeground(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        setForeground(false)
    }
}

/**
 * Keep the FcSDK singleton and call it anywhere using context
 */
val Context.fcSDK: FcSDK by FcSDKSingletonDelegate()

private class FcSDKSingletonDelegate : ReadOnlyProperty<Context, FcSDK> {
    private val lock = Any()

    @Volatile
    private var instance: FcSDK? = null
    override fun getValue(thisRef: Context, property: KProperty<*>): FcSDK {
        return instance ?: synchronized(lock) {
            if (instance == null) {
                instance = FcSDK
                    .Builder(
                        application = MyApplication.instance,
                        processLifecycleObserver = MyApplication.processLifecycleManager
                    )
                    //ToNote: If no other SDK in your app is also based on "RxAndroidBLE", don't use [RxBleClient.create], please use [FcSDK.rxBleClient] to get the [RxBleClient] instance.
//                    .setRxBleClient(RxBleClient.create(thisRef.applicationContext))
                    .setTestStrictMode(BuildConfig.DEBUG)
                    .setBuiltInFeatures(
                        FcBuiltInFeatures(
                            autoSetLanguage = true,
                            mediaControl = true,
                            musicControl = true,
                            gpsHotStartProvider = object : FcGpsHotStartProvider {
                                override val logEnabled: Boolean = true

                                override val fileDir: File? = null

                                override fun requestGpsLocationInfo(): Single<Optional<FcGpsLocationInfo>> {
                                    return Injector.getGpsHotStartRepository().requestGpsLocationInfo()
                                }

                                override fun requestGpsEpoInfo(): Single<Optional<FcGpsEpoInfo>> {
                                    return Injector.getGpsHotStartRepository().requestGpsEpoInfo()
                                }
                            },
                            sensorGameApiKey = "xxxxxxxxxxxxxxxx",
                        )
                    )
                    .build()
            }
            instance!!
        }
    }
}

private class RxJavaPluginsErrorHandler(
    /**
     * Exception types that can be ignored
     */
    private val ignores: Set<Class<out Throwable>>
) : Consumer<Throwable> {

    override fun accept(t: Throwable) {
        if (handle(t)) return
        throw RuntimeException(t)
    }

    /**
     * Handle exception
     * @param throwable
     * @return True for handled, false for not
     */
    private fun handle(throwable: Throwable): Boolean {
        val cause = if (throwable is UndeliverableException) {
            throwable.cause
        } else {
            throwable
        } ?: return true

        if (cause is CompositeException) {
            var nullCount = 0
            for (e in cause.exceptions) {
                if (e == null) {
                    nullCount++
                } else if (isIgnore(e)) {
                    return true
                }
            }

            if (nullCount == cause.exceptions.size) {
                return true
            }
        } else if (isIgnore(cause)) {
            return true
        }

        return false
    }

    private fun isIgnore(throwable: Throwable): Boolean {
        val clazz = throwable::class.java
        for (ignore in ignores) {
            if (ignore.isAssignableFrom(clazz)) {
                return true
            }
        }
        return false
    }
}