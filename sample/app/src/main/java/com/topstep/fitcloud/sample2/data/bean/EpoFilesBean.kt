package com.topstep.fitcloud.sample2.data.bean

import com.squareup.moshi.JsonClass
import com.topstep.fitcloud.sample2.data.net.json.StringNotBlank

@JsonClass(generateAdapter = true)
data class EpoFilesBean(
    @StringNotBlank
    val url_1: String,
    @StringNotBlank
    val url_2: String,
)