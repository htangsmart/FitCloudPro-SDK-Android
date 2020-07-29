package com.github.kilnn.wristband2.sample.net.result;

/**
 * 通用普通对象的返回类型
 *
 * @param <T>
 */
public class ObjectResult<T> extends BaseResult {

    private T data;

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }
}