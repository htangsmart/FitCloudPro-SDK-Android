package com.github.kilnn.wristband2.sample.syncdata.db;

import androidx.room.Entity;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;

import com.github.kilnn.wristband2.sample.syncdata.db.converter.TimeConverter;

import java.util.Date;


/**
 * Heart rate time point data
 */
@Entity(primaryKeys = {"time"})
public class HeartRateItem {

    /**
     * time(yyyy-MM-dd HH:mm:ss)
     */
    @NonNull
    @TypeConverters(TimeConverter.class)
    private Date time;

    /**
     * heart rate
     */
    private int heartRate;

    @NonNull
    public Date getTime() {
        return time;
    }

    public void setTime(@NonNull Date time) {
        this.time = time;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }
}