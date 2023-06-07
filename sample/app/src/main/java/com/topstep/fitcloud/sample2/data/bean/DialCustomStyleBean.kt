package com.topstep.fitcloud.sample2.data.bean

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DialCustomStyleBean(
    /**
     * Bin file download url
     */
    val binUrl: String,

    /**
     * Style name
     */
    val styleName: String,

    /**
     * File size of [binUrl]
     */
    val binSize: Long = 0
)