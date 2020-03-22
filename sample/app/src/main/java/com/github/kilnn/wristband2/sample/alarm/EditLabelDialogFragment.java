package com.github.kilnn.wristband2.sample.alarm;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import android.text.InputFilter;
import android.text.Spanned;

import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.widget.EditGroupLayout;

public class EditLabelDialogFragment extends AppCompatDialogFragment {

    public interface EditLabelDialogFragmentHolder {
        String getAlarmLabel();

        void setAlarmLabel(String label);
    }

    private EditGroupLayout mEditGroupLayout;
    private EditLabelDialogFragmentHolder mHolder;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EditLabelDialogFragmentHolder) {
            mHolder = (EditLabelDialogFragmentHolder) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.ds_alarm_label);

        String label = null;
        if (mHolder != null) {
            label = mHolder.getAlarmLabel();
        }
        mEditGroupLayout = new EditGroupLayout(builder.getContext());
        mEditGroupLayout.setEdits(1, new String[]{label}, new String[]{null});

        InputFilter filter = new InputFilter() {
            final int maxLen = 32;

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                int keep = maxLen - (dest.toString().getBytes().length - dest.subSequence(dstart, dend).toString().getBytes().length);
                if (keep <= 0) {
                    return "";
                } else if (keep >= source.subSequence(start, end).toString().getBytes().length) {
                    return null; // keep original
                } else {
                    char[] tempChars = new char[1];
                    for (int i = start; i < end; i++) {
                        tempChars[0] = source.charAt(i);
                        keep -= new String(tempChars).getBytes().length;
                        if (keep <= 0) {
                            if (keep == 0) {//长度正好
                                return source.subSequence(start, i + 1);//截取start到i，包括i，所有+1
                            } else {//长度超了，也就是要去掉一个
                                if (i == start) {
                                    return "";//没有可以去掉的，返回 ""
                                } else {
                                    return source.subSequence(start, i);//截取start到i，不包括i
                                }
                            }
                        }
                    }
                    return null;//长度始终没超过，keep original
                }
            }
        };
        mEditGroupLayout.getEditView(0).setFilters(new InputFilter[]{filter});
        builder.setView(mEditGroupLayout);
        builder.setNegativeButton(R.string.action_cancel, null);
        builder.setPositiveButton(R.string.action_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mEditGroupLayout == null) return;
                String[] texts = mEditGroupLayout.getEdits();
                if (texts == null || texts.length <= 0) return;
                String label = texts[0];
                if (mHolder != null) {
                    mHolder.setAlarmLabel(label);
                }
            }
        });
        return builder.create();
    }

}
