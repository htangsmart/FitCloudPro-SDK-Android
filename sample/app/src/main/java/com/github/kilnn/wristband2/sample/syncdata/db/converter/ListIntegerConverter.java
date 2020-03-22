package com.github.kilnn.wristband2.sample.syncdata.db.converter;


import androidx.room.TypeConverter;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import java.util.List;

public class ListIntegerConverter {

    @TypeConverter
    public static String fromList(List<Integer> list) {
        return list == null ? null : JSON.toJSONString(list);
    }

    @TypeConverter
    public static List<Integer> fromStr(String str) {
        return TextUtils.isEmpty(str) ? null : JSON.parseArray(str, Integer.class);
    }

}

