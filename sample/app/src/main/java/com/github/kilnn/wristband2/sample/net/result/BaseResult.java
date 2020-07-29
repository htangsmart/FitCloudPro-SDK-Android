package com.github.kilnn.wristband2.sample.net.result;

/**
 * Created by Kilnn on 2017/3/16.
 * 网络请求返回通用类型
 */
public class BaseResult {

    public static final int ERROR_CODE_NONE = 0;//无错误为0
    public static final int ERROR_CODE_SYSTEM = 1001;//1001 系统错误
    public static final int ERROR_CODE_ACCESS_TOKEN_EXPIRES_IN = 1002;//1002 access_token无效或者过期，需要使用refresh_token刷新，或者重新登录
    public static final int ERROR_CODE_AUTH_CODE = 1003;//1003 验证码不正确
    public static final int ERROR_CODE_USER_EXIST = 1004;//1004 用户已经存在
    public static final int ERROR_CODE_USER_NOT_EXIST = 1005;//1005 用户不存在
    public static final int ERROR_CODE_PASSWORD = 1006;//1006 密码不正确
    public static final int ERROR_CODE_ID_ALREADY_SET = 1007;//1007 身份ID已经设置
    public static final int ERROR_CODE_REFRESH_TOKEN_EXPIRES_IN = 1008;//1008 用户在其他地方登录，access_token失效
    public static final int ERROR_CODE_ACCESS_TOKEN_INVALIDATE = 1009;//1009 refresh_token无效或者过期，需要重新登录

    /**
     * 错误码，参考API文档：https://www.zybuluo.com/htsmart/note/1254642
     */
    private int errorCode;

    /**
     * 默认显示的错误信息
     */
    private String errorMsg;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
