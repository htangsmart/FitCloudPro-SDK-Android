package com.github.kilnn.wristband2.sample.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;

import com.github.kilnn.wristband2.sample.R;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;
import com.yanzhenjie.permission.runtime.Runtime;
import com.yanzhenjie.permission.source.ContextSource;
import com.yanzhenjie.permission.source.Source;
import com.yanzhenjie.permission.source.SupportFragmentSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AndPermissionHelper {

    private static final String TAG = AndPermissionHelper.class.getSimpleName();

    public static void fileRequest(AppCompatActivity activity, AndPermissionHelperListener1 listener1) {
        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        HashMap<String, Integer> rationales = new HashMap<>(1);
        rationales.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_dialog_rationale_storage);

        AndPermissionRequest
                .with(activity)
                .permission(permissions)
                .rationales(rationales)
                .listener1(listener1)
                .start();
    }

    public static void cameraRequest(AppCompatActivity activity, AndPermissionHelperListener1 listener1) {
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        HashMap<String, Integer> rationales = new HashMap<>(1);
        rationales.put(Manifest.permission.CAMERA, R.string.permission_dialog_rationale_camera);
        rationales.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_dialog_rationale_storage);
        AndPermissionRequest
                .with(activity)
                .permission(permissions)
                .rationales(rationales)
                .listener1(listener1)
                .start();
    }

    public static void blePermissionRequest(AppCompatActivity activity, AndPermissionHelperListener1 listener1) {
        String[] permissions = new String[]{
                //LOCATION
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        HashMap<String, Integer> rationales = new HashMap<>();

        rationales.put(Manifest.permission.ACCESS_FINE_LOCATION, R.string.permission_dialog_rationale_location);
        AndPermissionRequest
                .with(activity)
                .permission(permissions)
                .rationales(rationales)
                .listener1(listener1)
                .start();
    }

    public interface AndPermissionHelperListener1 {
        void onSuccess();
    }

    /**
     * 只支持 Support v4 中的 FragmentActivity 和 Fragment
     */
    private static class AndPermissionRequest {

        public static AndPermissionRequest with(FragmentActivity activity) {
            return new AndPermissionRequest(activity);
        }

        public static AndPermissionRequest with(Fragment fragment) {
            return new AndPermissionRequest(fragment);
        }

        public static AndPermissionRequest with(Context context) {
            return new AndPermissionRequest(context);
        }

        private String[] mPermissions;
        private HashMap<String, Integer> mRationales;
        private AndPermissionHelperListener1 mListener1;

        private Runtime mAndPermission;
        private Source mSource;
        private FragmentManager mFragmentManager;

        private AndPermissionRequest(FragmentActivity activity) {
            mAndPermission = AndPermission.with(activity).runtime();
            mSource = new ContextSource(activity);
            mFragmentManager = activity.getSupportFragmentManager();
        }

        private AndPermissionRequest(Fragment fragment) {
            mAndPermission = AndPermission.with(fragment).runtime();
            mSource = new SupportFragmentSource(fragment);
            mFragmentManager = fragment.getFragmentManager();
        }

        private AndPermissionRequest(Context context) {
            mAndPermission = AndPermission.with(context).runtime();
            mSource = new ContextSource(context);
            mFragmentManager = null;
        }

        public AndPermissionRequest permission(String... permissions) {
            mPermissions = permissions;
            return this;
        }

        /**
         * 用于显示rationales信息
         *
         * @param rationales 权限为key，intres为value
         * @return
         */
        public AndPermissionRequest rationales(HashMap<String, Integer> rationales) {
            mRationales = rationales;
            return this;
        }


        public AndPermissionRequest listener1(AndPermissionHelperListener1 listener1) {
            mListener1 = listener1;
            return this;
        }

        public void start() {
            mAndPermission
                    .permission(mPermissions)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            checkResult();
                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            checkResult();
                        }
                    })
                    .rationale(new Rationale<List<String>>() {
                        @Override
                        public void showRationale(Context context, List<String> data, RequestExecutor executor) {
                            showRationaleInner(executor);
                        }
                    })
                    .start();
        }

        private void showRationaleInner(final RequestExecutor executor) {
            if (mRationales == null || mRationales.size() <= 0 || mFragmentManager == null) return;
            String message = "";
            List<Integer> addedList = new ArrayList<>(mRationales.size());//同样的ResId只添加一次
            for (String permission : mPermissions) {
                boolean shouldRationale = mSource.isShowRationalePermission(permission);
                if (shouldRationale) {
                    Integer resId = mRationales.get(permission);
                    if (resId != null && resId != 0) {
                        boolean added = false;
                        for (int i = 0; i < addedList.size(); i++) {
                            Integer v = addedList.get(i);
                            if (v.intValue() == resId.intValue()) {
                                added = true;
                                break;
                            }
                        }
                        if (!added) {
                            addedList.add(resId);
                            if (!TextUtils.isEmpty(message)) message += "\n";
                            message += (mSource.getContext().getString(resId));
                        }
                    }
                }
            }
            RationaleDialog dialog = RationaleDialog.newInstance(message);
            dialog.setPositiveListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    executor.execute();
                }
            });
            dialog.setNegativeListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    executor.cancel();
                }
            });
            dialog.show(mFragmentManager, null);
        }

        private void showSetting() {
            if (mFragmentManager == null) return;
            SettingDialog dialog = SettingDialog.newInstance();
            dialog.setPositiveListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAndPermission.setting().start();
                }
            });
            dialog.show(mFragmentManager, null);
        }

        private void checkResult() {
            List<String> deniedPermissions = checkPermissionAgain(mSource.getContext(), mPermissions);
            if (deniedPermissions == null || deniedPermissions.size() <= 0) {//success
                //Check listener and callback
                if (mListener1 != null) {
                    mListener1.onSuccess();
                }
            } else {//failed
                // 是否有不再提示并拒绝的权限。
                boolean hasAlwaysDeniedPermission = false;
                if (mSource.getContext() instanceof Activity) {
                    hasAlwaysDeniedPermission = AndPermission.hasAlwaysDeniedPermission((Activity) mSource.getContext(), deniedPermissions);
                }
                if (hasAlwaysDeniedPermission) {
                    showSetting();
                }
                //Check listener and callback
            }
        }


        /**
         * 检查这一组权限是否全部授权了
         *
         * @param context
         * @param permissions
         * @return 没有授权的权限
         */
        @Nullable
        private static List<String> checkPermissionAgain(Context context, List<String> permissions) {
            if (context == null || permissions == null || permissions.size() <= 0) return null;
            List<String> deniedPermissions = new ArrayList<>(permissions.size());
            for (String permission : permissions) {
                if (TextUtils.isEmpty(permission)) continue;
                //BUG，部分手机检测权限会报错，加个异常捕获
                try {
                    if (PermissionChecker.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permission);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return deniedPermissions;
        }

        @Nullable
        private static List<String> checkPermissionAgain(Context context, String[] permission) {
            if (permission == null || permission.length <= 0) return null;
            return checkPermissionAgain(context, Arrays.asList(permission));
        }

    }

    public static class RationaleDialog extends AppCompatDialogFragment {
        private static final String EXTRA_MESSAGE = "message";

        public static RationaleDialog newInstance(String message) {
            RationaleDialog dialog = new RationaleDialog();
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_MESSAGE, message);
            dialog.setArguments(bundle);
            return dialog;
        }

        public DialogInterface.OnClickListener mPositiveListener;
        public DialogInterface.OnClickListener mNegativeListener;

        public void setPositiveListener(DialogInterface.OnClickListener listener) {
            this.mPositiveListener = listener;
        }

        public void setNegativeListener(DialogInterface.OnClickListener listener) {
            this.mNegativeListener = listener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getContext())
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(getArguments().getString(EXTRA_MESSAGE))
                    .setPositiveButton(R.string.permission_dialog_rationale_sure, mPositiveListener)
                    .setNegativeButton(R.string.permission_dialog_rationale_cancel, mNegativeListener)
                    .create();
        }
    }

    public static class SettingDialog extends AppCompatDialogFragment {

        public static SettingDialog newInstance() {
            return new SettingDialog();
        }

        public DialogInterface.OnClickListener mPositiveListener;

        public void setPositiveListener(DialogInterface.OnClickListener listener) {
            this.mPositiveListener = listener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getContext())
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(R.string.permission_dialog_setting_message)
                    .setPositiveButton(R.string.permission_dialog_setting_sure, mPositiveListener)
                    .setNegativeButton(R.string.permission_dialog_setting_cancel, null)
                    .create();
        }
    }

}
