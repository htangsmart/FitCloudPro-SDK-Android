package com.github.kilnn.wristband2.sample.dial

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.htsmart.wristband2.dial.DialView
import com.htsmart.wristband2.dial.DialViewEngine

class MyDialViewEngine private constructor() : DialViewEngine {

    override fun loadDialBackground(context: Context, view: DialView, uri: Uri) {
        Glide.with(context)
            .asBitmap()
            .load(uri)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    view.clearBackgroundBitmap()
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    view.backgroundBitmap = resource
                }
            })
    }

    override fun loadDialStyle(context: Context, view: DialView, uri: Uri) {
        throw IllegalAccessException("不要使用这个方法，即不要使用DialView#setStyleSource(Uri)这个过时的方法")
    }

    override fun loadDialStyle(context: Context, view: DialView, uri: Uri, styleBaseOnWidth: Int) {
        Glide.with(context)
            .asBitmap()
            .load(uri)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    view.setStyleBitmap(resource, styleBaseOnWidth)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    view.clearStyleBitmap()
                }
            })
    }

    companion object {
        val INSTANCE = MyDialViewEngine()
    }
}