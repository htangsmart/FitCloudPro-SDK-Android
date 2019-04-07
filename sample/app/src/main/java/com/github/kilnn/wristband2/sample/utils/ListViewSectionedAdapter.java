package com.github.kilnn.wristband2.sample.utils;

import android.widget.BaseAdapter;

/**
 * Created by Kilnn on 16-4-1.
 * 支持分组数据显示的Adapter
 * List<SectionData>
 * <p/>
 * ---sectionData1---
 * --  itemData --
 * --  itemData --
 * --  itemData --
 * --  itemData --
 * ---sectionData2---
 * --  itemData --
 * --  itemData --
 * --  itemData --
 * ---sectionData1---
 * ---sectionData1---
 * ---sectionData3---
 * <p/>
 * Section不只是分组,你可以把任意占据一整行的布局看做是一个Section
 * <p/>
 * 需要注意ViewType类型
 */
public abstract class ListViewSectionedAdapter extends BaseAdapter {

    private static final int HEADER = -1;
    private static final int FOOTER = -2;

    protected final static int VIEW_TYPE_HEADER = 0;
    protected final static int VIEW_TYPE_ITEM = 1;
    protected final static int VIEW_TYPE_FOOTER = 2;

    private int mCount = 0;
    /**
     * 数组下标为Item Position,值为属于哪个Section
     */
    private int[] mSectionForPosition;

    /**
     * 数组下标为Item Position,
     * 如果Index>=0,表示为VIEW_TYPE_ITEM，值为处于Section中的Index,
     * 如果Index==-1,表示为VIEW_TYPE_HEADER
     * 如果Index==-2,VIEW_TYPE_FOOTER
     */
    private int[] mIndexWithinSection;

    private boolean mInit = false;

    public boolean isSectionHeader(int position) {
        if (position < 0 || position >= mCount) {
            return false;
        }
        int index = mIndexWithinSection[position];
        return index == HEADER;
    }

    public boolean isSectionFooter(int position) {
        if (position < 0 || position >= mCount) {
            return false;
        }
        int index = mIndexWithinSection[position];
        return index == FOOTER;
    }

    public int positionToSection(int position) {
        return mSectionForPosition[position];
    }

    /**
     * position对应Section里的index,如果值>=0,表示1个有意义的index,否则为头部或者底部
     *
     * @param position
     * @return
     */
    public int positionToSectionIndex(int position) {
        return mIndexWithinSection[position];
    }

    @Override
    public void notifyDataSetChanged() {
        mInit = false;
        init();
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        mInit = false;
        init();
        super.notifyDataSetInvalidated();
    }

    /**
     * 初始化:
     */
    private void init() {
        if (mInit) {
            return;
        }
        /* 1.计算Item的总数量*/
        mCount = 0;
        for (int s = 0; s < getSectionCount(); s++) {
            mCount += 1 + getItemCountInSection(s) + (hasFooterInSection(s) ? 1 : 0);//加的1是Section的位置
        }

        /*初始化数组*/
        mSectionForPosition = new int[mCount];
        mIndexWithinSection = new int[mCount];

        /*初始化位置信息的值*/
        int sectionCount = getSectionCount();
        int position = 0;//item position
        for (int i = 0; i < sectionCount; i++) {
            setPrecomputedItem(position, i, HEADER);//设置这个Section,Section项设置为index -1
            position++;

            for (int j = 0; j < getItemCountInSection(i); j++) {
                setPrecomputedItem(position, i, j);//设置Section里面的Item,Item项设置index 为j,对应如List或数组的实际index
                position++;
            }

            if (hasFooterInSection(i)) {
                setPrecomputedItem(position, i, FOOTER);
                position++;
            }
        }

        mInit = true;
    }

    private void setPrecomputedItem(int position, int section, int index) {
        mSectionForPosition[position] = section;
        mIndexWithinSection[position] = index;
    }

    @Override
    public final int getCount() {
        init();
        return mCount;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public final int getItemViewType(int position) {
        int section = mSectionForPosition[position];
        int index = mIndexWithinSection[position];
        if (index == HEADER) {//这是一个Section
            return getHeaderViewType(position, section);
        } else if (index == FOOTER) {
            return getFooterViewType(position, section);
        } else {
            return getItemViewType(position, section, index);
        }
    }


    /**
     * 获取一共有几组元素
     *
     * @return
     */
    protected abstract int getSectionCount();

    /**
     * Returns true if a given section should have a footer
     */
    protected abstract boolean hasFooterInSection(int section);

    /**
     * 获取某组元素下有几个Item
     *
     * @param section
     * @return
     */
    protected abstract int getItemCountInSection(int section);


    /**
     * 获取一个SectionView的视图类型
     * 和{@link #getItemViewType(int, int, int)}配合使用,必须返回大于0且不重复的视图类型
     *
     * @param position 实际视图的位置
     * @param section  它是哪一个Section
     * @return
     */
    protected int getHeaderViewType(int position, int section) {
        return VIEW_TYPE_HEADER;
    }

    /**
     * 获取一个SectionView的视图类型
     * 和{@link #getItemViewType(int, int, int)}配合使用,必须返回大于0且不重复的视图类型
     *
     * @param position 实际视图的位置
     * @param section  它是哪一个Section
     * @return
     */
    protected int getFooterViewType(int position, int section) {
        return VIEW_TYPE_FOOTER;
    }


    /**
     * 获取一个ItemView的视图类型
     * {@link #getHeaderViewType(int, int)},{@link #getFooterViewType(int, int)}
     *
     * @param position 实际视图的位置
     * @param section  它是哪一个Section
     * @param index    在Section中的Index
     * @return
     */
    public int getItemViewType(int position, int section, int index) {
        //noinspection ResourceType
        return VIEW_TYPE_ITEM;
    }


}
