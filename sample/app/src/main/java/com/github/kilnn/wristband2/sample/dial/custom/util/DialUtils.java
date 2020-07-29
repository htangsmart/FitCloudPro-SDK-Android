package com.github.kilnn.wristband2.sample.dial.custom.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.dial.custom.bean.DialCustom;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public class DialUtils {

    public static Uri getUriFromDrawableResId(Context context, @DrawableRes int drawableResId) {
        String builder = ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" +
                context.getResources().getResourcePackageName(drawableResId) +
                "/" +
                context.getResources().getResourceTypeName(drawableResId) +
                "/" +
                context.getResources().getResourceEntryName(drawableResId);
        return Uri.parse(builder);
    }

    private static class SupportStyle {
        public int index;
        public int resId;

        private SupportStyle(int index, int resId) {
            this.index = index;
            this.resId = resId;
        }
    }

    private static final Map<String, SupportStyle> SUPPORTS;

    static {
        SUPPORTS = new HashMap<>(5);
        SUPPORTS.put("White", new SupportStyle(1, R.drawable.dial_style1));
        SUPPORTS.put("Black", new SupportStyle(2, R.drawable.dial_style2));
        SUPPORTS.put("Yellow", new SupportStyle(3, R.drawable.dial_style3));
        SUPPORTS.put("Green", new SupportStyle(4, R.drawable.dial_style4));
        SUPPORTS.put("Gray", new SupportStyle(5, R.drawable.dial_style5));
    }

    @NonNull
    public static List<DialCustom> filterSupportStyles(Context context, @NonNull List<DialCustom> dialCustoms) {
        for (int i = 0; i < dialCustoms.size(); i++) {
            DialCustom custom = dialCustoms.get(i);
            SupportStyle supportStyle = SUPPORTS.get(custom.getStyleName());
            if (supportStyle == null) {
                dialCustoms.remove(i);
                i--;
            } else {
                custom.setStyleUri(DialUtils.getUriFromDrawableResId(context, supportStyle.resId));
            }
        }
        try {
            Collections.sort(dialCustoms, new Comparator<DialCustom>() {
                @Override
                public int compare(DialCustom o1, DialCustom o2) {
                    int index1 = SUPPORTS.get(o1.getStyleName()).index;
                    int index2 = SUPPORTS.get(o2.getStyleName()).index;
                    return index1 - index2;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dialCustoms;
    }

}
