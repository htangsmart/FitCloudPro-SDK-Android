package com.github.kilnn.wristband2.sample.net.result;

import java.util.List;

/**
 * Created by Kilnn on 2017/3/16.
 * 通用对象列表的返回类型
 */
public class ListResult<T> extends BaseResult {

    private List<T> data;

    public List<T> getData() {
        return this.data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}