package com.github.kilnn.wristband2.sample.syncdata.db;

import com.alibaba.fastjson.annotation.JSONField;
import com.github.kilnn.wristband2.sample.syncdata.db.converter.TimeConverter;

import java.util.Date;

/**
 * 睡眠Item数据。代表某段时间的睡眠状态
 */
public class SleepItem {
    /**
     * 该时间段睡眠状态：1深睡，2浅睡，3清醒
     */
    private int status;

    /**
     * 该时间段的起始时间，yyyy-MM-dd HH:mm:ss日期格式
     */
    @JSONField(format = TimeConverter.FORMAT_STR)
    private Date startTime;

    /**
     * 该时间段的结束时间，yyyy-MM-dd HH:mm:ss日期格式
     */
    @JSONField(format = TimeConverter.FORMAT_STR)
    private Date endTime;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
