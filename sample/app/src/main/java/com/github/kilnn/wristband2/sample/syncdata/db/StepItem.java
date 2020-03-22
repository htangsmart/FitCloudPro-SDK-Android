package com.github.kilnn.wristband2.sample.syncdata.db;

import androidx.room.Entity;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;

import com.github.kilnn.wristband2.sample.syncdata.db.converter.TimeConverter;

import java.util.Date;

/**
 * Step time point data
 */
@Entity(primaryKeys = {"time"})
public class StepItem {

    /**
     * time(yyyy-MM-dd HH:mm:ss)
     */
    @NonNull
    @TypeConverters(TimeConverter.class)
    private Date time;

    private int step;

    @NonNull
    public Date getTime() {
        return time;
    }

    public void setTime(@NonNull Date time) {
        this.time = time;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void plus(@NonNull StepItem item) {
        step += item.step;
    }

}
