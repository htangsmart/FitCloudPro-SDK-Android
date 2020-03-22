package com.github.kilnn.wristband2.sample.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.view.View;
import android.view.ViewGroup;

import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.widget.SectionGroup;
import com.github.kilnn.wristband2.sample.widget.SectionItem;

public class Utils {

    @SuppressWarnings("ConstantConditions")
    public static boolean checkLocationForBle(final FragmentActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetWorkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGpsProvider && !isNetWorkProvider) {
                new LocationFeatureDialogFragment().show(activity.getSupportFragmentManager(), null);
            }
            return isGpsProvider || isNetWorkProvider;
        }
        return true;
    }

    public static class LocationFeatureDialogFragment extends AppCompatDialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getContext())
                    .setTitle(R.string.action_prompt)
                    .setMessage(R.string.dialog_msg_location_feature)
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton(R.string.action_sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent enableLocate = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            getContext().startActivity(enableLocate);
                        }
                    })
                    .create();
        }
    }

    /**
     * 如果子视图 tag 为 "ignoreParentState"，那么不对其设置
     * 如果子视图 是SectionItem类型，那么不去设置子类型
     */
    public static void setAllChildEnabled(ViewGroup viewGroup, boolean enabled) {
        if (viewGroup.isEnabled() != enabled) {
            viewGroup.setEnabled(enabled);
        }
        for (int idx = 0; idx < viewGroup.getChildCount(); idx++) {
            View child = viewGroup.getChildAt(idx);
            Object tag = child.getTag();
            if (tag != null && tag instanceof String && ((String) tag).contains("ignoreParentState")) {
                continue;
            }
            if (child.isEnabled() != enabled) {
                child.setEnabled(enabled);
            }
            if (child instanceof ViewGroup && !(child instanceof SectionItem)
                    && !(child instanceof SectionGroup)) {
                //SectionItem不需要设置
                //SectionGroup的setEnabled里会调用setAllChildEnabled，所以也不需要设置
                setAllChildEnabled((ViewGroup) child, enabled);
            }
        }
    }
}
