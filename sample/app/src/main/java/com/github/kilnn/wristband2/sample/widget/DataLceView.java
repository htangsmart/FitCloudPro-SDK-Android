package com.github.kilnn.wristband2.sample.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.kilnn.wristband2.sample.R;

public class DataLceView extends LinearLayout {

    private static final int STATUS_NONE = 0;
    private static final int STATUS_LOADING = 1;
    private static final int STATUS_FAILED = 2;

    private ProgressBar mProgressBar;
    private TextView mTvStatus;
    private int mStatus;

    public DataLceView(Context context) {
        super(context);
        initView();
    }

    public DataLceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DataLceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DataLceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_data_lce, this);
        setClickable(true);//让点击事件不透传
        mProgressBar = findViewById(R.id.progress_bar);
        mTvStatus = findViewById(R.id.tv_status);
        mStatus = STATUS_NONE;

        mTvStatus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStatus != STATUS_FAILED) {
                    return;
                }
                if (mListener != null) {
                    lceShowLoading();
                    mListener.lceLoad();
                }
            }
        });
    }

    public interface LoadingListener {
        void lceLoad();
    }

    private LoadingListener mListener;

    public void setLoadingListener(LoadingListener listener) {
        mListener = listener;
    }

    /**
     * 显示加载框
     */
    public void lceShowLoading() {
        if (mStatus == STATUS_LOADING) return;
        mStatus = STATUS_LOADING;
        setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mTvStatus.setVisibility(View.GONE);
    }

    /**
     * 显示内容，即隐藏自己
     */
    public void lceShowContent() {
        mStatus = STATUS_NONE;
        setVisibility(View.GONE);
    }

    /**
     * 显示错误
     */
    public void lceShowError(int textResId) {
        mStatus = STATUS_FAILED;
        setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mTvStatus.setVisibility(View.VISIBLE);
        mTvStatus.setText(textResId);
    }

    public boolean isShowLoading() {
        return mStatus == STATUS_LOADING;
    }
}
