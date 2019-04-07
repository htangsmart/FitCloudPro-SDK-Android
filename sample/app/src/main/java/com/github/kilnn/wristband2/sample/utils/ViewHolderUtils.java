package com.github.kilnn.wristband2.sample.utils;

import android.util.SparseArray;
import android.view.View;

/**
 * Created by Kilnn on 2017/8/21.
 */
public class ViewHolderUtils {
    private ViewHolderUtils() {
    }

    // I added a generic return type to reduce the casting noise in client code
    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }
}
