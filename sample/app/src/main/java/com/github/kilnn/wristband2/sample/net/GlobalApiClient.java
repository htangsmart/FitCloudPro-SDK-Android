package com.github.kilnn.wristband2.sample.net;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.github.kilnn.wristband2.sample.dial.custom.bean.DialCustom;
import com.github.kilnn.wristband2.sample.dial.custom.bean.DialInfo;

import java.util.List;

import io.reactivex.Flowable;
import okhttp3.Interceptor;

/**
 * Created by Kilnn on 2017/3/16.
 * 请求全局的网络数据请求的Client
 */
public class GlobalApiClient extends AbstractApiClient<GlobalApiService> {

    public GlobalApiClient(Context context) {
        super(context.getApplicationContext());
        createService();
    }

    @Override
    protected Class<GlobalApiService> getServiceClass() {
        return GlobalApiService.class;
    }

    @Override
    protected List<Interceptor> extraInterceptors() {
        return null;
    }

    public Flowable<List<DialCustom>> getDialCustom(int lcd, String toolVersion) {
        return NetResultTransformer.mapList(getService().getDialCustom(lcd, toolVersion), false);
    }

    public Flowable<List<DialInfo>> getDialListByNumbers(List<Integer> dialNumbers) {
        return NetResultTransformer.mapList(getService().getDialListByNumbers(JSON.toJSONString(dialNumbers)), false);
    }
}
