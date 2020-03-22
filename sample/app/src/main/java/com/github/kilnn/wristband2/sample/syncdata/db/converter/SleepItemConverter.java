package com.github.kilnn.wristband2.sample.syncdata.db.converter;

import androidx.room.TypeConverter;

import com.alibaba.fastjson.JSON;
import com.github.kilnn.wristband2.sample.syncdata.db.SleepItem;

import java.util.List;

public class SleepItemConverter {

    @TypeConverter
    public static List<SleepItem> fromStr(String json) {
        return JSON.parseArray(json, SleepItem.class);
    }

    @TypeConverter
    public static String fromList(List<SleepItem> list) {
        return JSON.toJSONString(list);
    }
}
