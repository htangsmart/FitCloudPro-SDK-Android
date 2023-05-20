package com.topstep.fitcloud.sample2.ui.widget;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.res.ResourcesCompat;

import com.topstep.fitcloud.sample2.R;

public class BatteryImageView extends AppCompatImageView {

    private static final float BATTERY_ICON_LEFT_PART = 0.03f;//电池图标的左边占了图片3%，不能计入电量百分比
    private static final float BATTERY_ICON_RIGHT_PART = 0.13f;//电池图标的右边占了图片13%，不能计入电量百分比

    private boolean mUnknown = true;
    private boolean mCharging;//是否在充电
    private int mPercentage;//0-100

    private Drawable mUnknownDrawable;
    private Drawable mChargingDrawable;

    public BatteryImageView(Context context) {
        super(context);
        init();
    }

    public BatteryImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BatteryImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mUnknownDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_battery_unknown, getContext().getTheme());
        mChargingDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_battery_charging, getContext().getTheme());
        mUnknownDrawable.setBounds(0, 0, mUnknownDrawable.getIntrinsicWidth(), mUnknownDrawable.getIntrinsicHeight());
        mChargingDrawable.setBounds(0, 0, mChargingDrawable.getIntrinsicWidth(), mChargingDrawable.getIntrinsicHeight());
        setImageDrawable(mUnknownDrawable);
    }

    public void setBatteryUnknown() {
        mUnknown = true;
        setImageDrawable(mUnknownDrawable);
    }

    public boolean isBatteryUnknown() {
        return mUnknown;
    }

    public void setBatteryStatus(boolean charging, int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException();
        }
        if (!mUnknown && mCharging == charging && mPercentage == percentage) {
            //和上次状态一模一样，那么久不再次更新了
            return;
        }
        mUnknown = false;
        mCharging = charging;
        mPercentage = percentage;

        if (mCharging) {
            setImageDrawable(mChargingDrawable);
        } else {
            final int showPercentage = Math.max(mPercentage, 5);//防止电量太低时，显示完全的空格子
            boolean warn = percentage < 10;//是否要闪烁提醒
            if (warn) {
                setImageDrawable(createWarnDrawable(showPercentage));
            } else {
                setImageDrawable(createPercentageDrawable(false, showPercentage));
            }
        }
    }

    private Drawable createPercentageDrawable(boolean warn, int percentage) {
        Drawable bgDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_battery_zero, getContext().getTheme());
        ClipDrawable percentageDrawable;
        if (warn) {
            percentageDrawable = new ClipDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_battery_low, getContext().getTheme()), Gravity.START, ClipDrawable.HORIZONTAL);
        } else {
            percentageDrawable = new ClipDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_battery_full, getContext().getTheme()), Gravity.START, ClipDrawable.HORIZONTAL);
        }
        percentageDrawable.setLevel(percentage2Level(percentage));

        Drawable[] drawables = new Drawable[]{bgDrawable, percentageDrawable};
        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        layerDrawable.setBounds(0, 0, layerDrawable.getIntrinsicWidth(), layerDrawable.getIntrinsicHeight());
        return layerDrawable;
    }

    private Drawable createWarnDrawable(int percentage) {
        AnimationDrawable animationDrawable = new AnimationDrawable();
        animationDrawable.addFrame(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_battery_zero, getContext().getTheme()), 500);
        animationDrawable.addFrame(createPercentageDrawable(true, percentage), 500);
        animationDrawable.setBounds(0, 0, animationDrawable.getIntrinsicWidth(), animationDrawable.getIntrinsicHeight());
        animationDrawable.start();
        return animationDrawable;
    }


    private int percentage2Level(int percentage) {
        float level = (percentage / 100.0f) * (1 - BATTERY_ICON_LEFT_PART - BATTERY_ICON_RIGHT_PART) * 10000;
        return (int) (level + BATTERY_ICON_LEFT_PART * 10000);
    }
}
