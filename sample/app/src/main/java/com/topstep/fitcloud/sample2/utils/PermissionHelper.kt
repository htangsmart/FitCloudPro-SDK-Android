package com.topstep.fitcloud.sample2.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.permissionx.guolindev.PermissionX
import com.polidea.rxandroidble3.ClientComponent
import com.polidea.rxandroidble3.RxBleClient
import com.topstep.fitcloud.sample2.MyApplication
import com.topstep.fitcloud.sample2.R

object PermissionHelper {

    /**
     * Compatible processing:
     * If [Manifest.permission.CAMERA] is added in Manifest.xml and targetSdk>=23,
     * you must also request permission to call the system camera
     */
    fun requestSystemCamera(fragment: Fragment, grantResult: ((Boolean) -> Unit)) {
        requestPermission(
            fragment, arrayListOf(
                Manifest.permission.CAMERA,
            ), grantResult
        )
    }

    fun requestAppCamera(fragment: Fragment, grantResult: ((Boolean) -> Unit)) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            grantResult.invoke(true)
            return
        }
        val permissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            arrayListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            arrayListOf(
                Manifest.permission.CAMERA
            )
        }
        requestPermission(fragment, permissions, grantResult)
    }

    fun hasCamera(context: Context): Boolean {
        return hasPermissions(context, arrayListOf(Manifest.permission.CAMERA))
    }

    /**
     * ToNote:This is the permission group based on app settings.
     * For more detailed permissions, please refer to the Android development documentation
     * @see [ClientComponent.ClientModule.provideRecommendedScanRuntimePermissionNames]
     * @see [RxBleClient.getRecommendedScanRuntimePermissions]
     */
    private fun getBle(): ArrayList<String>? {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> {
                null
            }
            //[23,31)
            Build.VERSION.SDK_INT in Build.VERSION_CODES.M until Build.VERSION_CODES.S -> {
                arrayListOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
            //>=31
            else -> {
                arrayListOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            }
        }
    }

    fun hasBle(context: Context): Boolean {
        return hasPermissions(context, getBle())
    }

    /**
     * Request permission for ble Scan and Connect
     */
    fun requestBle(fragment: Fragment, grantResult: ((Boolean) -> Unit)) {
        requestPermission(fragment, getBle(), grantResult)
    }

    fun requestBleConnect(fragment: Fragment, grantResult: ((Boolean) -> Unit)) {
        val permissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            null
        } else {
            arrayListOf(
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        }
        requestPermission(fragment, permissions, grantResult)
    }

    fun requestWeatherLocation(fragment: Fragment, grantResult: ((Boolean) -> Unit)) {
        requestPermission(
            fragment, arrayListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ), grantResult
        )
    }

    fun requestContacts(fragment: Fragment, grantResult: ((Boolean) -> Unit)) {
        requestPermission(
            fragment, arrayListOf(
                Manifest.permission.READ_CONTACTS,
            ), grantResult
        )
    }

    /**
     * Do you have the necessary permissions to receive Telephony notifications
     */
    fun hasReceiveTelephony(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermissions(context, arrayListOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE))
        } else {
            hasPermissions(context, arrayListOf(Manifest.permission.READ_CALL_LOG))
        }
    }

    /**
     * Do you have the necessary permissions to receive SMS notifications
     */
    fun hasReceiveSms(context: Context): Boolean {
        return hasPermissions(context, arrayListOf(Manifest.permission.RECEIVE_SMS))
    }

    private fun getTelephony(): ArrayList<String> {
        val permissions = ArrayList<String>(6)
        permissions.add(Manifest.permission.READ_CALL_LOG)
        permissions.add(Manifest.permission.READ_PHONE_STATE)
        permissions.add(Manifest.permission.READ_CONTACTS)
        permissions.add(Manifest.permission.CALL_PHONE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            permissions.add(Manifest.permission.ANSWER_PHONE_CALLS)
        }
        permissions.add(Manifest.permission.SEND_SMS)
        return permissions
    }

    private fun getSms(): ArrayList<String> {
        return arrayListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS
        )
    }

    fun requestTelephony(fragment: Fragment, grantResult: ((Boolean) -> Unit)) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            grantResult.invoke(true)
            return
        }
        requestPermission(fragment, getTelephony()) {
            if (hasPermissions(fragment.requireContext(), arrayListOf(Manifest.permission.READ_PHONE_STATE))) {
                MyApplication.instance.myTelephonyControl?.checkInitialize()
            }
            grantResult.invoke(it)
        }
    }

    fun requestSms(fragment: Fragment, grantResult: ((Boolean) -> Unit)) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            grantResult.invoke(true)
            return
        }
        val context = fragment.requireContext()
        requestPermission(fragment, getSms()) {
            if (hasReceiveSms(context)) {
                PermissionTrigger.readSmsTest(context)
            }
            grantResult.invoke(it)
        }
    }

    private fun hasPermissions(context: Context, permissions: ArrayList<String>?): Boolean {
        if (permissions.isNullOrEmpty()) return true
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun requestPermission(
        fragment: Fragment,
        permissions: ArrayList<String>?,
        grantResult: ((Boolean) -> Unit)? = null,
    ) {
        if (permissions.isNullOrEmpty()) {
            grantResult?.invoke(true)
            return
        }
        val context = fragment.requireContext()
        PermissionX
            .init(fragment)
            .permissions(permissions)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    context.getString(R.string.permission_explain_msg),
                    context.getString(android.R.string.ok),
                    context.getString(android.R.string.cancel),
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    context.getString(R.string.permission_to_settings_msg),
                    context.getString(android.R.string.ok),
                    context.getString(android.R.string.cancel),
                )
            }
            .explainReasonBeforeRequest()
            .request { allGranted, _, _ ->
                grantResult?.invoke(allGranted)
            }
    }

}