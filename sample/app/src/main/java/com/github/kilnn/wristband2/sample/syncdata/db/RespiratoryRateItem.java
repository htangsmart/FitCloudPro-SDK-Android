package com.github.kilnn.wristband2.sample.syncdata.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.github.kilnn.wristband2.sample.syncdata.db.converter.TimeConverter;

import java.util.Date;

/**
 * Respiratory rate time point data
 */
@Entity(primaryKeys = {"time"})
public class RespiratoryRateItem {

    /**
     * time(yyyy-MM-dd HH:mm:ss)
     */
    @NonNull
    @TypeConverters(TimeConverter.class)
    private Date time;

    /**
     * respiratory rate
     */
    private int rate;

    @NonNull
    public Date getTime() {
        return time;
    }

    public void setTime(@NonNull Date time) {
        this.time = time;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
}
