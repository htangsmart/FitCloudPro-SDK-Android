package com.github.kilnn.wristband2.sample.widget;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.AttrRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.github.kilnn.wristband2.sample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kilnn on 2017/11/17.
 */

public class EditGroupLayout extends LinearLayout {

    private List<EditText> mEditTexts = new ArrayList<>(2);

    public EditGroupLayout(Context context) {
        super(context);
        setOrientation(VERTICAL);
        int padding = getDimensionPixelOffset(context, R.attr.dialogPreferredPadding);
        setPadding(padding, padding, padding, padding);
        setDividerDrawable(ContextCompat.getDrawable(context, R.drawable.custom_alert_shape_item_divider));
        setShowDividers(SHOW_DIVIDER_MIDDLE);
    }

    private int getDimensionPixelOffset(Context context, @AttrRes int attr) {
        int[] attrsArray = new int[]{attr};
        TypedArray typedArray = context.obtainStyledAttributes(attrsArray);
        int dimension = typedArray.getDimensionPixelOffset(0, 0);
        typedArray.recycle();
        return dimension;
    }

    public void setEditCount(int count) {
        mEditTexts.clear();
        removeAllViews();
        for (int i = 0; i < count; i++) {
            createAndAddEdit();
        }
    }

    private void createAndAddEdit() {
        EditText editText = new EditText(getContext());
        editText.setBackgroundResource(R.drawable.custom_alert_shape_light_edit_bg);
        addView(editText);
        mEditTexts.add(editText);
    }

    public void setEdits(String[] texts, String[] hints) {
        if (texts == null || hints == null) return;
        for (int i = 0; i < mEditTexts.size(); i++) {
            mEditTexts.get(i).setText(texts[i]);
            mEditTexts.get(i).setHint(hints[i]);
        }
    }

    public void setEdits(int count, String[] texts, String[] hints) {
        setEditCount(count);
        setEdits(texts, hints);
    }

    @Nullable
    public String[] getEdits() {
        if (mEditTexts.size() <= 0) return null;
        String[] texts = new String[mEditTexts.size()];
        for (int i = 0; i < mEditTexts.size(); i++) {
            texts[i] = mEditTexts.get(i).getText().toString();
        }
        return texts;
    }

    public EditText getEditView(int index) {
        return mEditTexts.get(index);
    }

}
