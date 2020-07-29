package com.github.kilnn.wristband2.sample.net.exception;

import java.io.IOException;

/**
 * Created by Kilnn on 2018/2/11.
 * 数据层自定义异常基类
 */
public abstract class DataLayerException extends IOException {
    @Override
    public String getMessage() {
        String message = super.getMessage();
        return message != null ? message : "Exception occurred on data layer";
    }
}
