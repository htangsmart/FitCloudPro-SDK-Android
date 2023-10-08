package com.topstep.fitcloud.sample2.ui.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class GridSimpleSpaceDecoration extends RecyclerView.ItemDecoration {

    private final int spacingVertical; //间隔
    private final int spaceHorizontal; //间隔

    public GridSimpleSpaceDecoration(int spacingVertical, int spaceHorizontal) {
        this.spacingVertical = spacingVertical / 2;
        this.spaceHorizontal = spaceHorizontal / 2;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.left = spaceHorizontal;
        outRect.top = spacingVertical;
        outRect.right = spaceHorizontal;
        outRect.bottom = spacingVertical;
    }

}