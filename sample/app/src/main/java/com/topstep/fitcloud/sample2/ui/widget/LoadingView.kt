package com.topstep.fitcloud.sample2.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.topstep.fitcloud.sample2.R

class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val progressBar: ProgressBar
    private val tvStatus: TextView
    private val btnAction: Button
    private var status = STATUS_NONE
    var listener: Listener? = null
    var associateViews: Array<View>? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.loading_view, this)
        isClickable = true //让点击事件不透传

        progressBar = findViewById(R.id.progress_bar)
        tvStatus = findViewById(R.id.tv_status)
        btnAction = findViewById(R.id.btn_action)

        tvStatus.setOnClickListener {
            if (status == STATUS_ERROR) {
                listener?.doLoading()
            }
        }
    }

    /**
     * 显示加载框
     */
    fun showLoading() {
        if (status == STATUS_LOADING) return
        status = STATUS_LOADING
        visibility = VISIBLE
        progressBar.visibility = VISIBLE
        tvStatus.visibility = GONE
        btnAction.visibility = GONE
    }

    /**
     * 隐藏自己
     */
    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == VISIBLE) {
            associateViews?.forEach {
                it.visibility = GONE
            }
        } else {
            status = STATUS_NONE
            associateViews?.forEach {
                it.visibility = VISIBLE
            }
        }
    }

    /**
     * 显示错误
     */
    fun showError(textResId: Int) {
        status = STATUS_ERROR
        visibility = VISIBLE
        progressBar.visibility = GONE
        tvStatus.visibility = VISIBLE
        tvStatus.setText(textResId)
        btnAction.visibility = GONE
    }

    fun showInfo(textResId: Int) {
        status = STATUS_INFO
        visibility = VISIBLE
        progressBar.visibility = GONE
        tvStatus.visibility = VISIBLE
        tvStatus.setText(textResId)
        btnAction.visibility = GONE
    }

    fun showAction(textResId: Int, btnResId: Int, listener: OnClickListener?) {
        status = STATUS_ACTION
        visibility = VISIBLE
        progressBar.visibility = GONE
        tvStatus.visibility = VISIBLE
        tvStatus.setText(textResId)
        btnAction.visibility = VISIBLE
        btnAction.setText(btnResId)
        btnAction.setOnClickListener(listener)
    }

    fun isLoading(): Boolean {
        return status == STATUS_LOADING
    }

    fun interface Listener {
        fun doLoading()
    }

    companion object {
        private const val STATUS_NONE = 0
        private const val STATUS_LOADING = 1
        private const val STATUS_ERROR = 2
        private const val STATUS_INFO = 3
        private const val STATUS_ACTION = 4
    }
}