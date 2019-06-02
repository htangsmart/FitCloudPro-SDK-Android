package com.github.kilnn.wristband2.sample.syncdata.db.converter;

import android.arch.persistence.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeConverter {

    public static final String FORMAT_STR = "yyyy-MM-dd HH:mm:ss";

    /**
     * SimpleDateFormat is not thread-safe
     */
    private static ThreadLocal<SimpleDateFormat> HOLDER = new ThreadLocal<>();

    private static SimpleDateFormat getFormat() {
        SimpleDateFormat format = HOLDER.get();
        if (format == null) {
            format = new SimpleDateFormat(FORMAT_STR, Locale.getDefault());
            HOLDER.set(format);
        }
        return format;
    }

    @TypeConverter
    public static String fromDate(Date date) {
        return getFormat().format(date);
    }

    @TypeConverter
    public static Date fromStr(String str) {
        try {
            return getFormat().parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return DateConverter.DEFAULT_DATE;
    }

}
