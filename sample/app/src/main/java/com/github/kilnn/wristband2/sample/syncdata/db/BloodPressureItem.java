package com.github.kilnn.wristband2.sample.syncdata.db;

import androidx.room.Entity;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;

import com.github.kilnn.wristband2.sample.syncdata.db.converter.TimeConverter;

import java.util.Date;

/**
 * BloodPressure time point data
 */
@Entity(primaryKeys = {"time"})
public class BloodPressureItem {

    /**
     * time(yyyy-MM-dd HH:mm:ss)
     */
    @NonNull
    @TypeConverters(TimeConverter.class)
    private Date time;

    /**
     * systolic pressure
     */
    private int sbp;

    /**
     * diastolic pressure
     */
    private int dbp;

    @NonNull
    public Date getTime() {
        return time;
    }

    public void setTime(@NonNull Date time) {
        this.time = time;
    }

    public int getSbp() {
        return sbp;
    }

    public void setSbp(int sbp) {
        this.sbp = sbp;
    }

    public int getDbp() {
        return dbp;
    }

    public void setDbp(int dbp) {
        this.dbp = dbp;
    }
}
