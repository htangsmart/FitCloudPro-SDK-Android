package com.github.kilnn.wristband2.sample.syncdata.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.github.kilnn.wristband2.sample.syncdata.db.converter.SportHeartRateConverter;
import com.github.kilnn.wristband2.sample.syncdata.db.converter.TimeConverter;
import com.github.kilnn.wristband2.sample.syncdata.db.converter.UUIDConverter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class SportRecord {

    /**
     * id
     */
    @NonNull
    @PrimaryKey
    @TypeConverters(UUIDConverter.class)
    private UUID sportId;

    /**
     * time(yyyy-MM-dd HH:mm:ss)
     */
    @TypeConverters(TimeConverter.class)
    private Date time;//该行数据的时间，yyyy-MM-dd HH:mm:ss日期格式

    private int duration;
    private float distance;
    private float calorie;
    private int step;
    private int sportType;

    @TypeConverters(SportHeartRateConverter.class)
    private List<SportHeartRate> heartRates;//运动过程中的心率信息

    @NonNull
    public UUID getSportId() {
        return sportId;
    }

    public void setSportId(@NonNull UUID sportId) {
        this.sportId = sportId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getCalorie() {
        return calorie;
    }

    public void setCalorie(float calorie) {
        this.calorie = calorie;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getSportType() {
        return sportType;
    }

    public void setSportType(int sportType) {
        this.sportType = sportType;
    }

    public List<SportHeartRate> getHeartRates() {
        return heartRates;
    }

    public void setHeartRates(List<SportHeartRate> heartRates) {
        this.heartRates = heartRates;
    }
}
