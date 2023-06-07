@file:Suppress("DEPRECATION")

package com.topstep.fitcloud.sample2.utils

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

inline fun <reified T : Parcelable?> Parcel.readParcelableCompat(loader: ClassLoader?): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        readParcelable(loader, T::class.java)
    } else {
        readParcelable(loader)
    }
}

inline fun <reified T : Parcelable?> Bundle.getParcelableCompat(key: String?): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, T::class.java)
    } else {
        getParcelable(key)
    }
}

inline fun <reified T : Parcelable?> Bundle.getParcelableArrayListCompat(key: String?): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayList(key, T::class.java)
    } else {
        getParcelableArrayList(key)
    }
}

inline fun <reified T : Serializable?> Bundle.getSerializableCompat(key: String?): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, T::class.java)
    } else {
        getSerializable(key) as? T
    }
}

inline fun <reified T : Parcelable?> Intent.getParcelableExtraCompat(key: String?): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, T::class.java)
    } else {
        getParcelableExtra(key)
    }
}
