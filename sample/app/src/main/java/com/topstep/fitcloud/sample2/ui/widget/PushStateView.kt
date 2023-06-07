package com.topstep.fitcloud.sample2.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.color.MaterialColors

class PushStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    var progress = 100
    private val paint: Paint = Paint()
    private val rect: RectF = RectF()
    private val enabledColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary)
    private val disabledColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface).let {
        Color.argb((255 * 0.12f).toInt(), Color.red(it), Color.green(it), Color.blue(it))
    }

    override fun onDraw(canvas: Canvas) {
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        if (isEnabled) {
            paint.color = enabledColor
            if (progress > 0) {
                paint.alpha = 0x99
                canvas.drawRect(rect, paint)
                rect.set(0f, 0f, progress / 100.0f * width, height.toFloat())
            }
            paint.alpha = 0xFF
            canvas.drawRect(rect, paint)
        } else {
            paint.color = disabledColor
            canvas.drawRect(rect, paint)
        }
        super.onDraw(canvas)
    }
}