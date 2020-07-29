package com.github.kilnn.wristband2.sample.util;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import android.os.StatFs;
import android.util.SparseArray;
import android.view.View;

import java.io.File;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;


public class Utils {

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
     * @param height     height(cm)
     * @param man        gender，True for male, false for female
     * @return step size(m)
     */
    public static float getStepLength(float height,boolean man) {
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

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
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
}
