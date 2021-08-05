package com.github.kilnn.wristband2.sample.dial.custom

import android.net.Uri

class DialCustomCompat(
    /**
     * 默认的背景图片Uri
     */
    val defaultBackgroundUri: Uri,

    /**
     * 样式列表
     */
    val styles: List<Style>
) {

    fun enabled(): Boolean {
        return !styles.isNullOrEmpty()
    }

    data class Style(
        val styleUri: Uri,
        /**
         * 样式基于多大的宽度设计的
         */
        val styleBaseOnWidth: Int,
        val binUrl: String,
        val binSize: Long
    )
}