package com.github.kilnn.wristband2.sample.net.exception;

/**
 * Created by Kilnn on 2017/6/12.
 * 网络请求的状态码错误，附带一个errorCode和msg
 */
public class NetResultStatusException extends DataLayerException {

    private int errorCode;
    private String errorMsg;

    public NetResultStatusException(int errorCode, String errorMsg) {
        super();
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    @Override
    public String getMessage() {
        return "NetResultStatusException Exception occurred on data layer:errorCode="
                + errorCode + " , errorMsg=" + errorMsg;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
