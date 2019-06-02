package com.github.kilnn.wristband2.sample.syncdata.db;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.github.kilnn.wristband2.sample.syncdata.db.converter.ListIntegerConverter;
import com.github.kilnn.wristband2.sample.syncdata.db.converter.TimeConverter;
import com.github.kilnn.wristband2.sample.syncdata.db.converter.UUIDConverter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class EcgRecord {

    /**
     * id
     */
    @NonNull
    @PrimaryKey
    @TypeConverters(UUIDConverter.class)
    private UUID ecgId;

    /**
     * time(yyyy-MM-dd HH:mm:ss)
     */
    @TypeConverters(TimeConverter.class)
    private Date time;//该行数据的时间，yyyy-MM-dd HH:mm:ss日期格式

    @TypeConverters(ListIntegerConverter.class)
    private List<Integer> detail;//心电值详情

    private int sample;

    @NonNull
    public UUID getEcgId() {
        return ecgId;
    }

    public void setEcgId(@NonNull UUID ecgId) {
        this.ecgId = ecgId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public List<Integer> getDetail() {
        return detail;
    }

    public void setDetail(List<Integer> detail) {
        this.detail = detail;
    }

    public int getSample() {
        return sample;
    }

    public void setSample(int sample) {
        this.sample = sample;
    }

    public int[] getIntArrays() {
        if (detail == null || detail.size() == 0) return null;
        int[] datas = new int[detail.size()];
        for (int i = 0; i < detail.size(); i++) {
            datas[i] = detail.get(i);
        }
        return datas;
    }

}
