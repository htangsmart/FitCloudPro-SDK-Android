package com.topstep.fitcloud.sample2.data.bean

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DialPacketComplexBean(
    val dialNum: Int,

    val lcd: Int,

    val toolVersion: String,

    val binVersion: Int,

    /**
     * Dial image url
     */
    val imgUrl: String? = null,

    /**
     * Image url of dial with device casing
     */
    val deviceImgUrl: String? = null,

    /**
     * Bin file download url
     */
    val binUrl: String,

    /**
     * Dial name
     */
    val name: String? = null,

    /**
     * File size of [binUrl]
     */
    val binSize: Long = 0,

    /**
     * Download count
     */
    val downloadCount: Int = 0,

    /**
     * 0 No components，1 Has components
     */
    val hasComponent: Int = 0,

    /**
     * Component preview background image
     */
    val previewImgUrl: String? = null,

    /**
     * Components
     */
    val components: List<Component>? = null
) {

    @JsonClass(generateAdapter = true)
    data class Component(
        val urls: List<String>? = null
    )
}