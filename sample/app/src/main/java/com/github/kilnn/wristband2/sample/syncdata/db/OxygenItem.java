package com.github.kilnn.wristband2.sample.syncdata.db;

import androidx.room.Entity;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;

import com.github.kilnn.wristband2.sample.syncdata.db.converter.TimeConverter;

import java.util.Date;


/**
 * Oxygen time point data
 */
@Entity(primaryKeys = {"time"})
public class OxygenItem {

    /**
     * time(yyyy-MM-dd HH:mm:ss)
     */
    @NonNull
    @TypeConverters(TimeConverter.class)
    private Date time;

    /**
     * oxygen
     */
    private int oxygen;

    @NonNull
    public Date getTime() {
        return time;
    }

    public void setTime(@NonNull Date time) {
        this.time = time;
    }

    public int getOxygen() {
        return oxygen;
    }

    public void setOxygen(int oxygen) {
        this.oxygen = oxygen;
    }
}
