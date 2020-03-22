package com.github.kilnn.wristband2.sample.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.kilnn.wristband2.sample.R;

/**
 * Created by Kilnn on 2017/7/28.
 * 可定制的Section Item
 */
public class SectionItem extends RelativeLayout {
    private static final int ITEM_TYPE_DEFAULT = 0;
    private static final int ITEM_TYPE_TEXT = 1;
    private static final int ITEM_TYPE_ICON = 2;
    private static final int ITEM_TYPE_SWITCH = 3;
    private static final int ITEM_TYPE_CUSTOM = 4;

    private int mItemType = ITEM_TYPE_DEFAULT;

    public SectionItem(Context context) {
        this(context, null);
    }

    public SectionItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SectionItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SectionItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private LinearLayout mTitleLayout;
    private TextView mTvTitle;
    private TextView mTvSubTitle;
    private ImageView mImgIndicator;

    private TextView mTvText;
    private ImageView mImgIcon;
    private SwitchCompat mSwitchStatus;
    private View mCustomView;

    private View mContentView;

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        LayoutInflater inflater;
        if (context instanceof Activity) {
            inflater = ((Activity) context).getLayoutInflater();
        } else {
            inflater = LayoutInflater.from(context);
        }
        inflater.inflate(R.layout.layout_section_item_default, this);
        mTitleLayout = findViewById(R.id.layout_section_item_title);
        mTvTitle = findViewById(R.id.tv_section_item_title);
        mTvSubTitle = findViewById(R.id.tv_section_item_sub_title);
        mImgIndicator = findViewById(R.id.img_section_item_indicator);

        // Styleables from XML
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SectionItem, defStyleAttr, defStyleRes);
        //Title
        int titleTextAppearance = a.getResourceId(R.styleable.SectionItem_sectionItemTitleTextAppearance, -1);
        if (titleTextAppearance != -1) {
            TextViewCompat.setTextAppearance(mTvTitle, titleTextAppearance);
        }
        ColorStateList titleColor = a.getColorStateList(R.styleable.SectionItem_sectionItemTitleColor);
        if (titleColor != null) {
            mTvTitle.setTextColor(titleColor);
        }
        int titleSize = a.getDimensionPixelSize(R.styleable.SectionItem_sectionItemTitleSize, -1);
        if (titleSize != -1) {
            mTvTitle.setTextSize(titleSize);
        }
        String title = a.getString(R.styleable.SectionItem_sectionItemTitle);
        mTvTitle.setText(title);

        Drawable titleDrawableStart = a.getDrawable(R.styleable.SectionItem_sectionItemTitleDrawableStart);
        if (titleDrawableStart != null) {
            titleDrawableStart.setBounds(0, 0, titleDrawableStart.getIntrinsicWidth(), titleDrawableStart.getIntrinsicHeight());
        }
        Drawable titleDrawableEnd = a.getDrawable(R.styleable.SectionItem_sectionItemTitleDrawableEnd);
        if (titleDrawableEnd != null) {
            titleDrawableEnd.setBounds(0, 0, titleDrawableEnd.getIntrinsicWidth(), titleDrawableEnd.getIntrinsicHeight());
        }
        int drawablePadding = a.getDimensionPixelOffset(R.styleable.SectionItem_sectionItemTitleDrawablePadding, 0);
        mTvTitle.setCompoundDrawablePadding(drawablePadding);
        mTvTitle.setCompoundDrawables(titleDrawableStart, null, titleDrawableEnd, null);

        //SubTitle
        int subTitleTextAppearance = a.getResourceId(R.styleable.SectionItem_sectionItemSubTitleTextAppearance, -1);
        if (subTitleTextAppearance != -1) {
            TextViewCompat.setTextAppearance(mTvSubTitle, subTitleTextAppearance);
        }
        ColorStateList subTitleColor = a.getColorStateList(R.styleable.SectionItem_sectionItemSubTitleColor);
        if (subTitleColor != null) {
            mTvSubTitle.setTextColor(subTitleColor);
        }
        int subTitleSize = a.getDimensionPixelSize(R.styleable.SectionItem_sectionItemSubTitleSize, -1);
        if (subTitleSize != -1) {
            mTvSubTitle.setTextSize(subTitleSize);
        }
        String subTitle = a.getString(R.styleable.SectionItem_sectionItemSubTitle);
        if (TextUtils.isEmpty(subTitle)) {
            mTvSubTitle.setVisibility(GONE);
        } else {
            mTvSubTitle.setText(subTitle);
        }

        //Indicator
        Drawable indicator = a.getDrawable(R.styleable.SectionItem_sectionItemIndicator);
        if (indicator == null) {
            mImgIndicator.setVisibility(GONE);
        } else {
            mImgIndicator.setImageDrawable(indicator);
        }

        //Add other views
        mItemType = a.getInt(R.styleable.SectionItem_sectionItemType, ITEM_TYPE_DEFAULT);
        if (mItemType == ITEM_TYPE_DEFAULT) {
            LayoutParams params = (LayoutParams) mTitleLayout.getLayoutParams();
            params.addRule(RelativeLayout.START_OF, R.id.img_section_item_indicator);
        } else {
            switch (mItemType) {
                case ITEM_TYPE_TEXT:
                    mContentView = createItemText(a);
                    break;
                case ITEM_TYPE_ICON:
                    mContentView = createItemIcon(a);
                    break;
                case ITEM_TYPE_SWITCH:
                    mContentView = createItemSwitch(a);
                    break;
                case ITEM_TYPE_CUSTOM:
                    mContentView = createItemCustom(inflater, a);
                    break;
            }
            if (mContentView != null) {
                LayoutParams contentParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                contentParams.addRule(RelativeLayout.CENTER_VERTICAL);
                contentParams.addRule(RelativeLayout.START_OF, R.id.img_section_item_indicator);
                contentParams.addRule(RelativeLayout.END_OF, R.id.layout_section_item_title);
                contentParams.alignWithParent = true;
                mContentView.setLayoutParams(contentParams);
                addView(mContentView);

//                RelativeLayout.LayoutParams titleParams = (LayoutParams) mTitleLayout.getLayoutParams();
//                titleParams.addRule(RelativeLayout.START_OF, mContentView.getId());
            }
        }
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mContentView != null) {
            if (mTitleLayout.getMeasuredWidth() == 0) {
                mTitleLayout.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int titleMeasureWidth = mTitleLayout.getMeasuredWidth();
                int contentMeasureWidth = mContentView.getMeasuredWidth();
                if (titleMeasureWidth > contentMeasureWidth / 2) {
                    mContentView.getLayoutParams().width = contentMeasureWidth / 2;
                } else {
                    mContentView.getLayoutParams().width = contentMeasureWidth - titleMeasureWidth;
                }
            }
        }
    }

    private View createItemText(TypedArray a) {
        mTvText = new TextView(getContext());
        mTvText.setDuplicateParentStateEnabled(true);
        mTvText.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        mTvText.setId(R.id.tv_section_item_text);
        int textAppearance = a.getResourceId(R.styleable.SectionItem_sectionItemTextAppearance, -1);
        if (textAppearance != -1) {
            TextViewCompat.setTextAppearance(mTvText, textAppearance);
        }
        ColorStateList textColor = a.getColorStateList(R.styleable.SectionItem_sectionItemTextColor);
        if (textColor != null) {
            mTvText.setTextColor(textColor);
        }
        int textSize = a.getDimensionPixelSize(R.styleable.SectionItem_sectionItemTextSize, -1);
        if (textSize != -1) {
            mTvText.setTextSize(textSize);
        }
        String text = a.getString(R.styleable.SectionItem_sectionItemText);
        mTvText.setText(text);

        Drawable drawableStart = a.getDrawable(R.styleable.SectionItem_sectionItemTextDrawableStart);
        if (drawableStart != null) {
            drawableStart.setBounds(0, 0, drawableStart.getIntrinsicWidth(), drawableStart.getIntrinsicHeight());
        }
        Drawable drawableEnd = a.getDrawable(R.styleable.SectionItem_sectionItemTextDrawableEnd);
        if (drawableEnd != null) {
            drawableEnd.setBounds(0, 0, drawableEnd.getIntrinsicWidth(), drawableEnd.getIntrinsicHeight());
        }
        int drawablePadding = a.getDimensionPixelOffset(R.styleable.SectionItem_sectionItemTextDrawablePadding, 0);
        mTvText.setCompoundDrawablePadding(drawablePadding);
        mTvText.setCompoundDrawables(drawableStart, null, drawableEnd, null);
        return mTvText;
    }

    private View createItemIcon(TypedArray a) {
        mImgIcon = new ImageView(getContext());
        mImgIcon.setDuplicateParentStateEnabled(true);
        mImgIcon.setId(R.id.img_section_item_icon);
        Drawable icon = a.getDrawable(R.styleable.SectionItem_sectionItemIcon);
        mImgIcon.setImageDrawable(icon);
        return mImgIcon;
    }

    private View createItemSwitch(TypedArray a) {
        mSwitchStatus = new SwitchCompat(getContext());
        mSwitchStatus.setDuplicateParentStateEnabled(true);
        mSwitchStatus.setId(R.id.switch_section_item_status);
        mSwitchStatus.setEnabled(isEnabled());
        return mSwitchStatus;
    }

    private View createItemCustom(LayoutInflater inflater, TypedArray a) {
        int layoutId = a.getResourceId(R.styleable.SectionItem_sectionItemCustomLayout, -1);
        if (layoutId != -1) {
            mCustomView = inflater.inflate(layoutId, this, false);
            mCustomView.setDuplicateParentStateEnabled(true);
            mCustomView.setId(R.id.view_section_item_custom);
        }
        return mCustomView;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mSwitchStatus != null && mSwitchStatus.isDuplicateParentStateEnabled()) {
            mSwitchStatus.setEnabled(enabled);
        }
    }

    public TextView getTitleView() {
        return mTvTitle;
    }

    public TextView getSubTitleView() {
        return mTvSubTitle;
    }

    public ImageView getIndicatorView() {
        return mImgIndicator;
    }

    public SwitchCompat getSwitchView() {
        return mSwitchStatus;
    }

    public TextView getTextView() {
        return mTvText;
    }

    public ImageView getIconView() {
        return mImgIcon;
    }

    public View getCustomView() {
        return mCustomView;
    }
}
