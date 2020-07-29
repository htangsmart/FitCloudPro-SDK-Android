package com.github.kilnn.wristband2.sample.dial.custom;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.github.kilnn.wristband2.sample.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

public class DialPagerAdapter extends PagerAdapter {

    private Context mContext;
    private View[] mViews;

    public DialPagerAdapter(DialGridView backgroundView, DialGridView styleView,
                            DialGridView positionView) {
        this.mContext = backgroundView.getContext();
        this.mViews = new View[3];
        this.mViews[0] = backgroundView;
        this.mViews[1] = styleView;
        this.mViews[2] = positionView;
    }

    @Override
    public int getCount() {
        return mViews.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        container.addView(mViews[position],
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return mViews[position];
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (object instanceof View) {
            container.removeView((View) object);
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getResources().getString(R.string.ds_dial_background);
            case 1:
                return mContext.getResources().getString(R.string.ds_dial_style);
            case 2:
            default:
                return mContext.getResources().getString(R.string.ds_dial_position);
        }
    }
}