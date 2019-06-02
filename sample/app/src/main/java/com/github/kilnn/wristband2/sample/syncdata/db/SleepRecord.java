package com.github.kilnn.wristband2.sample.syncdata.db;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.github.kilnn.wristband2.sample.syncdata.db.converter.DateConverter;
import com.github.kilnn.wristband2.sample.syncdata.db.converter.SleepItemConverter;

import java.util.Date;
import java.util.List;


/**
 * Sleep data for a day
 */
@Entity(primaryKeys = {"date"})
public class SleepRecord {

    /**
     * date(yyyy-MM-dd)
     */
    @NonNull
    @TypeConverters(DateConverter.class)
    private Date date;

    /**
     * Deep sleep time(seconds)
     */
    private int deepSleep;

    /**
     * Light sleep time(seconds)
     */
    private int lightSleep;

    /**
     * Sober sleep time(seconds)
     */
    private int soberSleep;

    /**
     * details
     */
    @TypeConverters(SleepItemConverter.class)
    private List<SleepItem> detail;

    @NonNull
    public Date getDate() {
        return date;
    }

    public void setDate(@NonNull Date date) {
        this.date = date;
    }

    public int getDeepSleep() {
        return deepSleep;
    }

    public void setDeepSleep(int deepSleep) {
        this.deepSleep = deepSleep;
    }

    public int getLightSleep() {
        return lightSleep;
    }

    public void setLightSleep(int lightSleep) {
        this.lightSleep = lightSleep;
    }

    public int getSoberSleep() {
        return soberSleep;
    }

    public void setSoberSleep(int soberSleep) {
        this.soberSleep = soberSleep;
    }

    public List<SleepItem> getDetail() {
        return detail;
    }

    public void setDetail(List<SleepItem> detail) {
        this.detail = detail;
    }
}


