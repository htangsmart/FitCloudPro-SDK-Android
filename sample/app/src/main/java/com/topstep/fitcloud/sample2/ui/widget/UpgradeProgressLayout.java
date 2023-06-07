package com.topstep.fitcloud.sample2.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.kilnn.tool.ui.DisplayUtil;
import com.google.android.material.color.MaterialColors;
import com.topstep.fitcloud.sample2.R;

public class UpgradeProgressLayout extends RelativeLayout {

    private int mArcWidth;
    private int mArcBgColor;
    private int mArcColor;

    private Paint mPaint;
    private RectF mDrawRectF;

    private int mProgress = 0;

    private ImageView mImgProgress;
    private TextView mTvStatus;

    public UpgradeProgressLayout(Context context) {
        super(context);
        init();
    }

    public UpgradeProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UpgradeProgressLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public UpgradeProgressLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_upgrade_progress, this);
        mImgProgress = findViewById(R.id.img_progress);
        mTvStatus = findViewById(R.id.tv_status);
        setWillNotDraw(false);

        mArcWidth = DisplayUtil.dip2px(getContext(), 8f);
        mArcBgColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary);
        mArcColor = Color.GREEN;
        mPaint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setStrokeWidth(mArcWidth);

        mDrawRectF = new RectF();
        mDrawRectF.left = mArcWidth / 2.0f;
        mDrawRectF.top = mArcWidth / 2.0f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mDrawRectF.right = getMeasuredWidth() - mArcWidth / 2.0f;
        mDrawRectF.bottom = getMeasuredHeight() - mArcWidth / 2.0f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(mArcBgColor);
        canvas.drawArc(mDrawRectF, -90, 360, false, mPaint);

        mPaint.setColor(mArcColor);
        canvas.drawArc(mDrawRectF, -90, mProgress / 100.0f * 360.0f, false, mPaint);
    }

    public void setStateProgress(int progress) {
        if (progress < 0) {
            progress = 0;
        }
        mProgress = progress;
        mTvStatus.setText(getContext().getString(R.string.percent_param, progress));
        Animation animation = mImgProgress.getAnimation();
        if (animation == null) {
            mImgProgress.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.upgrade_icon_flip));
        }
        invalidate();
    }

    public void setStatePrepare() {
        mProgress = 0;
        mTvStatus.setText("...");
        Animation animation = mImgProgress.getAnimation();
        if (animation == null) {
            mImgProgress.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.upgrade_icon_flip));
        }
        invalidate();
    }

    public void setStateStop(boolean success) {
        mImgProgress.clearAnimation();
        if (success) {
            mTvStatus.setText(R.string.tip_success);
        } else {
            mTvStatus.setText(R.string.tip_failed);
        }
    }

    public void setStateNone() {
        mImgProgress.clearAnimation();
        mTvStatus.setText(null);
    }
}
