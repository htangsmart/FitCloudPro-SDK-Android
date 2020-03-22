package com.github.kilnn.wristband2.sample.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.utils.Utils;

/**
 * Created by Kilnn on 2017/7/29.
 * 可以对Divider设置Start padding和 End padding
 */
public class SectionGroup extends LinearLayout {

    private int mDividerWidth;
    private int mDividerHeight;

    /**
     * 如果是getOrientation() == VERTICAL，那么代表left padding，否则代表top padding
     */
    private int mDividerPaddingStart;
    /**
     * 如果是getOrientation() == VERTICAL，那么代表right padding，否则代表bottom padding
     */
    private int mDividerPaddingEnd;

    public SectionGroup(Context context) {
        this(context, null);
    }

    public SectionGroup(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SectionGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SectionGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // Styleables from XML
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SectionGroup, defStyleAttr, defStyleRes);
        mDividerPaddingStart = a.getDimensionPixelSize(R.styleable.SectionGroup_sectionGroupDividerPaddingStart, 0);
        mDividerPaddingEnd = a.getDimensionPixelSize(R.styleable.SectionGroup_sectionGroupDividerPaddingEnd, 0);
        a.recycle();
    }

    /**
     * Set a drawable to be used as a divider between items.
     *
     * @param divider Drawable that will divide each item.
     * @attr ref android.R.styleable#LinearLayout_divider
     * @see #setShowDividers(int)
     */
    public void setDividerDrawable(Drawable divider) {
        if (divider == getDividerDrawable()) {
            return;
        }
        if (divider != null) {
            mDividerWidth = divider.getIntrinsicWidth();
            mDividerHeight = divider.getIntrinsicHeight();
        } else {
            mDividerWidth = 0;
            mDividerHeight = 0;
        }
        super.setDividerDrawable(divider);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable divider = getDividerDrawable();
        if (divider == null) {
            return;
        }

        if (getOrientation() == VERTICAL) {
            _drawDividersVertical(canvas);
        } else {
            _drawDividersHorizontal(canvas);
        }
    }

    void _drawDividersVertical(Canvas canvas) {
        final int count = getVirtualChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getVirtualChildAt(i);
            if (child != null && child.getVisibility() != GONE) {
                if (hasDividerBeforeChildAt(i)) {
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    final int top = child.getTop() - lp.topMargin - mDividerHeight;
                    _drawHorizontalDivider(canvas, top);
                }
            }
        }

        if (hasDividerBeforeChildAt(count)) {
            final View child = getLastNonGoneChild();
            int bottom = 0;
            if (child == null) {
                bottom = getHeight() - getPaddingBottom() - mDividerHeight;
            } else {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                bottom = child.getBottom() + lp.bottomMargin;
            }
            _drawHorizontalDivider(canvas, bottom);
        }
    }

    void _drawDividersHorizontal(Canvas canvas) {
        final int count = getVirtualChildCount();
        final boolean isLayoutRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
        for (int i = 0; i < count; i++) {
            final View child = getVirtualChildAt(i);
            if (child != null && child.getVisibility() != GONE) {
                if (hasDividerBeforeChildAt(i)) {
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    final int position;
                    if (isLayoutRtl) {
                        position = child.getRight() + lp.rightMargin;
                    } else {
                        position = child.getLeft() - lp.leftMargin - mDividerWidth;
                    }
                    _drawVerticalDivider(canvas, position);
                }
            }
        }

        if (hasDividerBeforeChildAt(count)) {
            final View child = getLastNonGoneChild();
            int position;
            if (child == null) {
                if (isLayoutRtl) {
                    position = getPaddingLeft();
                } else {
                    position = getWidth() - getPaddingRight() - mDividerWidth;
                }
            } else {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (isLayoutRtl) {
                    position = child.getLeft() - lp.leftMargin - mDividerWidth;
                } else {
                    position = child.getRight() + lp.rightMargin;
                }
            }
            _drawVerticalDivider(canvas, position);
        }
    }

    void _drawHorizontalDivider(Canvas canvas, int top) {
        Drawable divider = getDividerDrawable();
        divider.setBounds(getPaddingLeft() + mDividerPaddingStart, top,
                getWidth() - getPaddingRight() - mDividerPaddingEnd, top + mDividerHeight);
        divider.draw(canvas);
    }

    void _drawVerticalDivider(Canvas canvas, int left) {
        Drawable divider = getDividerDrawable();
        divider.setBounds(left, getPaddingTop() + mDividerPaddingStart,
                left + mDividerWidth, getHeight() - getPaddingBottom() - mDividerPaddingEnd);
        divider.draw(canvas);
    }

    /**
     * <p>Returns the virtual number of children. This number might be different
     * than the actual number of children if the layout can hold virtual
     * children. Refer to
     * {@link android.widget.TableLayout} and {@link android.widget.TableRow}
     * for an example.</p>
     *
     * @return the virtual number of children
     */
    int getVirtualChildCount() {
        return getChildCount();
    }

    /**
     * <p>Returns the view at the specified index. This method can be overriden
     * to take into account virtual children. Refer to
     * {@link android.widget.TableLayout} and {@link android.widget.TableRow}
     * for an example.</p>
     *
     * @param index the child's index
     * @return the child at the specified index, may be {@code null}
     */
    @Nullable
    View getVirtualChildAt(int index) {
        return getChildAt(index);
    }


    /**
     * Finds the last child that is not gone. The last child will be used as the reference for
     * where the end divider should be drawn.
     */
    private View getLastNonGoneChild() {
        for (int i = getVirtualChildCount() - 1; i >= 0; i--) {
            final View child = getVirtualChildAt(i);
            if (child != null && child.getVisibility() != GONE) {
                return child;
            }
        }
        return null;
    }

    /**
     * Determines where to position dividers between children.
     *
     * @param childIndex Index of child to check for preceding divider
     * @return true if there should be a divider before the child at childIndex
     * @hide Pending API consideration. Currently only used internally by the system.
     */
    protected boolean hasDividerBeforeChildAt(int childIndex) {
        int mShowDividers = getShowDividers();
        if (childIndex == getVirtualChildCount()) {
            // Check whether the end divider should draw.
            return (mShowDividers & SHOW_DIVIDER_END) != 0;
        }
        boolean allViewsAreGoneBefore = allViewsAreGoneBefore(childIndex);
        if (allViewsAreGoneBefore) {
            // This is the first view that's not gone, check if beginning divider is enabled.
            return (mShowDividers & SHOW_DIVIDER_BEGINNING) != 0;
        } else {
            return (mShowDividers & SHOW_DIVIDER_MIDDLE) != 0;
        }
    }

    /**
     * Checks whether all (virtual) child views before the given index are gone.
     */
    private boolean allViewsAreGoneBefore(int childIndex) {
        for (int i = childIndex - 1; i >= 0; i--) {
            final View child = getVirtualChildAt(i);
            if (child != null && child.getVisibility() != GONE) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Utils.setAllChildEnabled(this, enabled);
    }

}