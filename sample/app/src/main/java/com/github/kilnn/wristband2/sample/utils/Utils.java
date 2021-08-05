package com.github.kilnn.wristband2.sample.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.net.exception.DataLayerException;
import com.github.kilnn.wristband2.sample.net.exception.NetResultStatusException;
import com.github.kilnn.wristband2.sample.widget.SectionGroup;
import com.github.kilnn.wristband2.sample.widget.SectionItem;
import com.htsmart.wristband2.exceptions.OperationBusyException;
import com.htsmart.wristband2.exceptions.PacketDataFormatException;
import com.htsmart.wristband2.exceptions.SyncBusyException;
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException;

import org.json.JSONException;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpRetryException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import retrofit2.HttpException;

public class Utils {

    public static String parserErrorBLE(Context context, Throwable e) {
        Log.w("ErrorHelper", "parserErrorBLE", e);
        if (e instanceof BleDisconnectedException) {
            return context.getString(R.string.action_disconnect);
        } else if (e instanceof SyncBusyException) {
            return context.getString(R.string.sync_data_ongoing);
        } else if (e instanceof TimeoutException
                || e instanceof OperationBusyException) {
            return context.getString(R.string.err_ble_time_out);
        } else if (e instanceof PacketDataFormatException) {
            return context.getString(R.string.err_ble_format);
        } else {
            return context.getString(R.string.err_unknown);
        }
    }

    /**
     * @param context context
     * @param e       error
     * @return error message
     */
    public static String parserError(Context context, Throwable e) {
        if (e == null) {
            Log.w("ErrorHelper", "Unknown Error");
            return context.getString(R.string.err_unknown);
        }
        String message = null;
        if (e instanceof DataLayerException) {
            if (e instanceof NetResultStatusException) {//返回结果的问题，弹出提示
                int errorCode = ((NetResultStatusException) e).getErrorCode();
                int res = context.getResources().getIdentifier("err_code_" + errorCode, "string", context.getPackageName());
                if (res == 0) {//没有找到对应的
                    message = ((NetResultStatusException) e).getErrorMsg();
                } else {
                    message = context.getString(res);
                }
            } /*else if (e instanceof NetResultDataException || e instanceof DbResultNoneException) {
                message = context.getString(R.string.err_data);
            }*/ else {
                message = context.getString(R.string.err_data);
            }
        } else if (e instanceof HttpException
                || e instanceof UnknownHostException
                || e instanceof UnknownServiceException
                || e instanceof SocketException
                || e instanceof SocketTimeoutException
                || e instanceof HttpRetryException
                || e instanceof MalformedURLException) {//网络异常
            message = context.getString(R.string.err_network);
        } else if (e instanceof JSONException
                || e instanceof ParseException) {//解析异常
            message = context.getString(R.string.err_data);
        } else {
            message = e.getMessage();
        }
        if (TextUtils.isEmpty(message)) {
            message = context.getString(R.string.err_unknown);
        }
        Log.w("ErrorHelper", "parserError", e);
        return message;
    }

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

    public static boolean checkLocationEnabled(final Activity activity, @StringRes int msg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            //gps定位
            boolean isGpsProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            //网络定位
            boolean isNetWorkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGpsProvider && !isNetWorkProvider) {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.action_prompt)
                        .setMessage(msg)
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.action_sure, (dialog, which) -> {
                            Intent enableLocate = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            activity.startActivity(enableLocate);
                        })
                        .create().show();
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

    /**
     * Calculate the distance based on the number of steps and the step size(km)
     *
     * @param step       number of steps
     * @param stepLength step size(m)
     * @return distance(km)
     */
    public static float step2Km(int step, float stepLength) {
        return (stepLength * step) / (1000);
    }

    /**
     * Calculate calories based on distance and weight(kCal)
     *
     * @param km     distance(km)
     * @param weight weight(kg)
     * @return calories(kCal)
     */
    public static float km2Calories(float km, float weight) {
        return 0.78f * weight * km;
    }

    /**
     * Calculate the step size based on height and gender(m)
     *
     * @param height height(cm)
     * @param man    gender，True for male, false for female
     * @return step size(m)
     */
    public static float getStepLength(float height, boolean man) {
        float stepLength = height * (man ? 0.415f : 0.413f);
        if (stepLength < 30) {
            stepLength = 30.f;//30cm，Default minimum step size 30cm
        }
        if (stepLength > 100) {
            stepLength = 100.f;//100cm，Default maximum step size 100cm
        }
        return stepLength / 100;
    }

    @ColorInt
    public static int getColor(Context context, @AttrRes int attr) {
        int[] attrsArray = new int[]{attr};
        TypedArray typedArray = context.obtainStyledAttributes(attrsArray);
        int color = typedArray.getColor(0, 0);
        typedArray.recycle();
        return color;
    }

    /**
     * Get the start point of the hour
     */
    public static Date getHourStartTime(Calendar calendar, Date date) {
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Get the end point of the hour
     */
    public static Date getHourEndTime(Calendar calendar, Date date) {
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * Get day start time
     */
    public static Date getDayStartTime(Calendar calendar, Date date) {
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Get day end time
     */
    public static Date getDayEndTime(Calendar calendar, Date date) {
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public static Date getExpireLimitTime(Calendar calendar, int dayLimit) {
        Date date = new Date();
        date = getDayStartTime(calendar, date);
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - dayLimit);//设置时间到n天之前
        return calendar.getTime();
    }

    public static boolean isToday(Date date) {
        Date today = new Date();
        return date.getYear() == today.getYear()
                && date.getMonth() == today.getMonth()
                && date.getDate() == today.getDate();
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

    public static float roundDownFloat(double value, int scale) {
        return (float) round(String.valueOf(value), scale, BigDecimal.ROUND_DOWN);
    }

    private static double round(String value, int scale, int roundingMode) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(scale, roundingMode);
        return bd.doubleValue();
    }

    public static String toMD5(@NonNull String inStr) {
        StringBuilder sb = new StringBuilder();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(inStr.getBytes());
            byte b[] = md.digest();
            int i;
            for (byte aB : b) {
                i = aB;
                if (i < 0)
                    i += 256;
                if (i < 16)
                    sb.append("0");
                sb.append(Integer.toHexString(i));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return inStr;
    }

    /**
     * 获得当前目录的剩余容量，即可用大小
     *
     * @return 可用余量，单位为MB
     */
    public static double getAvailableSpace(@NonNull File file) {
        StatFs stat = null;
        try {
            stat = new StatFs(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (stat == null) {
            return 0;
        }
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize / 1024.0f / 1024.0f;//MB
    }

    public static final long KB = 1024;

    public static String fileSizeStr(long bytes) {
        final long MB = KB * KB;
        final long GB = MB * KB;
        final long TB = GB * KB;
        if (bytes <= 0) {
            return "0KB";
        } else if (bytes < KB * 0.1f) {
            return "0.1KB";
        } else if (bytes < MB) {
            return decimal1Str(bytes / (float) KB) + "KB";
        } else if (bytes < GB) {
            return decimal1Str(bytes / (float) MB) + "MB";
        } else if (bytes < TB) {
            return decimal1Str(bytes / (float) GB) + "GB";
        } else {
            return decimal1Str(bytes / (float) TB) + "TB";
        }
    }

    public static String decimal1Str(float value) {
        return DECIMAL_1_FORMAT.format(Double.parseDouble(Float.toString(value)));
    }

    private static final DecimalFormat DECIMAL_1_FORMAT;

    static {
        DECIMAL_1_FORMAT = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DECIMAL_1_FORMAT.applyPattern("0.0");
        DECIMAL_1_FORMAT.setRoundingMode(RoundingMode.DOWN);
    }
}
