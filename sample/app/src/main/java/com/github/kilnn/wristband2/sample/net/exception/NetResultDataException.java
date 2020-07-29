package com.github.kilnn.wristband2.sample.net.exception;

/**
 * Created by Kilnn on 2017/6/12.
 * 网络请求的数据异常，空数据或无效数据
 */
public class NetResultDataException extends DataLayerException {

    public static final int DATA_EMPTY = 0;//数据为空
    public static final int DATA_INVALID = 1;//数据无效，与期望数据不符合

    private int errorType;

    public NetResultDataException(int errorType) {
        super();
        this.errorType = errorType;
    }

    public int getErrorType() {
        return errorType;
    }

    @Override
    public String getMessage() {
        return "Error Type:" + errorType;
    }
}
