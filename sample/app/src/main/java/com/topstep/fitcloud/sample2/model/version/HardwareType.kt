package com.topstep.fitcloud.sample2.model.version

import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo

enum class HardwareType(val code: String, val versionLength: Int) {
    PATCH("P", 12),
    FLASH("F", 8),
    APP("A", 8);

    /**
     * Obtain the version number in [FcDeviceInfo]
     */
    fun getVersion(deviceInfo: FcDeviceInfo): String {
        return when (this) {
            PATCH -> deviceInfo.getPatch()
            APP -> deviceInfo.getApp()
            FLASH -> deviceInfo.getFlash()
        }
    }

    /**
     * Obtain the version number in hardwareInfo
     */
    fun getVersion(hardwareInfo: String): String {
        return when (this) {
            PATCH -> hardwareInfo.hardwarePatch()
            APP -> hardwareInfo.hardwareApp()
            FLASH -> hardwareInfo.hardwareFlash()
        }
    }

    companion object {
        fun fromCode(code: String?): HardwareType? {
            return when (code) {
                "P" -> PATCH
                "F" -> FLASH
                "A" -> APP
                else -> null
            }
        }
    }
}

private const val HARDWARE_INFO_MIN_LENGTH = 76

fun String.hardwareProject(): String {
    return if (this.length >= HARDWARE_INFO_MIN_LENGTH) this.substring(0, 12) else ""
}

fun String.hardwarePatch(): String {
    return if (this.length >= HARDWARE_INFO_MIN_LENGTH) this.substring(28, 40) else ""
}

fun String.hardwareApp(): String {
    return if (this.length >= HARDWARE_INFO_MIN_LENGTH) this.substring(48, 56) else ""
}

fun String.hardwareFlash(): String {
    return if (this.length >= HARDWARE_INFO_MIN_LENGTH) this.substring(40, 48) else ""
}

fun String.hardwareInfoDisplay(): String {
    if (this.length < HARDWARE_INFO_MIN_LENGTH) return "——.——"
    val project = hardwareProject()
    val patch = hardwarePatch().run {
        substring(this.length - 4)
    }//Patch number only displays 4 characters
    val app = removeFirst0(hardwareApp())

    val subProjectNum = project.substring(0, 2)
    return if (subProjectNum == "00") {
        "${removeFirst0(project)}.$patch.$app"
    } else {
        "${removeFirst0(project.substring(2))}-$subProjectNum.$patch.$app"
    }
}

fun FcDeviceInfo.hardwareInfoDisplay(): String {
    if (this.isSimulated()) return "——.——"
    return toString().hardwareInfoDisplay()
}

/**
 * Remove the 0 at the beginning of the string
 */
private fun removeFirst0(str: String): String {
    if (str.isEmpty()) return str
    var startIndex = 0
    for (i in str.indices) {
        val c = str[i]
        if (c != '0') {
            startIndex = i
            break
        }
    }
    return str.substring(startIndex, str.length)
}
