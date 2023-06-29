package com.topstep.fitcloud.sample2.ui.device.dial.component

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.topstep.fitcloud.sample2.model.dial.DialComponent
import com.topstep.fitcloud.sample2.ui.device.dial.createDefaultShape
import com.topstep.fitcloud.sdk.v2.model.settings.dial.FcShape

class DialComponentView : View {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    private var shape = createDefaultShape()

    private var components: MutableList<DialComponent>? = null
    private var previewBitmap: Bitmap? = null
    private var stylesBitmap: Array<Bitmap?>? = null
    private val stylesRequest = SparseArray<Target<*>>()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG).apply { color = 0xffededed.toInt() }
    private val xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    private val dialFrame = RectF()
    private val drawMatrix = Matrix()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        viewWidth = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        viewHeight = (viewWidth * (shape.height.toFloat() / shape.width)).toInt()
        val limitHeight = getDefaultSize(Int.MAX_VALUE, heightMeasureSpec)
        if (viewHeight > limitHeight) {
            viewHeight = limitHeight
            viewWidth = (viewHeight * (shape.width.toFloat() / shape.height)).toInt()
        }

        dialFrame.set(0F, 0F, viewWidth.toFloat(), viewHeight.toFloat())
        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate((width - viewWidth) / 2f, (height - viewHeight) / 2f)

        //1.draw dial background
        val save = canvas.saveLayer(dialFrame, paint)
        //1.draw dial background
        if (shape.isShapeCircle) {
            canvas.drawCircle(dialFrame.centerX(), dialFrame.centerY(), dialFrame.width() / 2f, paint)
        } else {
            canvas.drawRoundRect(dialFrame, shape.corners.toFloat(), shape.corners.toFloat(), paint)
        }
        previewBitmap?.let {
            if (it.isRecycled) return@let
            paint.xfermode = xfermode
            calcDialBackgroundMatrix(it, dialFrame.width(), dialFrame.height(), drawMatrix)
            canvas.drawBitmap(it, drawMatrix, paint)
            paint.xfermode = null
        }
        canvas.restoreToCount(save)

        //2.draw dial style
        components?.let {
            for (i in it.indices) {
                val bitmap = stylesBitmap?.getOrNull(i)
                if (bitmap == null || bitmap.isRecycled) {
                    continue
                }
                calcDialStyleMatrix(getStyleBaseOnWidth(), dialFrame.width(), it[i].positionX, it[i].positionY, drawMatrix)
                canvas.drawBitmap(bitmap, drawMatrix, paint)
            }
        }
    }

    private fun calcDialBackgroundMatrix(source: Bitmap, dstWidth: Float, dstHeight: Float, matrix: Matrix) {
        matrix.reset()
        val centerX = dstWidth / 2f
        val centerY = dstHeight / 2f
        matrix.setTranslate(centerX - source.width / 2f, centerY - source.height / 2f)
        val scale = (dstWidth / source.width).coerceAtLeast(dstHeight / source.height)
        matrix.postScale(scale, scale, centerX, centerY)
    }

    private fun calcDialStyleMatrix(styleBaseOnWidth: Int, dstWidth: Float, x: Int, y: Int, matrix: Matrix) {
        matrix.reset()
        val styleScale = dstWidth / styleBaseOnWidth
        matrix.setScale(styleScale, styleScale)
        //The x,y-coordinate is based on the size of the dial, so a scaling is required for the width of the corresponding View
        val xyScale = dstWidth / shape.width
        matrix.postTranslate(x * xyScale, y * xyScale)
    }

    fun init(shape: FcShape, previewUrl: String?, components: MutableList<DialComponent>) {
        this.shape = shape
        this.components = components
        stylesBitmap = arrayOfNulls(components.size)
        requestPreviewBitmap(previewUrl)
        for (i in components.indices) {
            requestComponentStyle(i)
        }
        requestLayout()
    }

    private fun requestPreviewBitmap(url: String?) {
        if (url.isNullOrEmpty()) return
        Glide.with(context).asBitmap().load(url)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    previewBitmap = resource
                    invalidate()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    private fun requestComponentStyle(position: Int) {
        stylesRequest.get(position)?.let {
            Glide.with(context).clear(it)
        }
        val url = components?.getOrNull(position)?.styleCurrentUrl ?: return
        val target = Glide.with(context).asBitmap().load(url)
            .into(object : CustomTarget<Bitmap?>() {

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    stylesBitmap?.set(position, resource)
                    invalidate()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
        stylesRequest.put(position, target)
    }

    /**
     * At present, the component style images on the FitCloud server are based on the dial being magnified twice
     */
    private fun getStyleBaseOnWidth() = shape.width * 2

    fun setComponentStyle(componentPosition: Int, stylePosition: Int) {
        val component = components?.removeAt(componentPosition) ?: return
        components?.add(componentPosition, component.copy(_styleCurrent = stylePosition))
        requestComponentStyle(componentPosition)
    }

    fun getComponents(): List<DialComponent>? {
        return components
    }

}