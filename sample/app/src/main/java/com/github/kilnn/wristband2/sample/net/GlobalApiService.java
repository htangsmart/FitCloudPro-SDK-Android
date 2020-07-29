package com.github.kilnn.wristband2.sample.net;

import com.github.kilnn.wristband2.sample.dial.custom.bean.DialCustom;
import com.github.kilnn.wristband2.sample.net.result.ListResult;

import io.reactivex.Flowable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


/**
 * Created by Kilnn on 2017/3/16.
 * 用于全局的网络数据请求
 */
interface GlobalApiService {

    String URL_DIAL_CUSTOM = "/public/dial/custom";//获取自定义表盘列表

    @POST(URL_DIAL_CUSTOM)
    @FormUrlEncoded
    Flowable<ListResult<DialCustom>> getDialCustom(
            @Field("lcd") int lcd,
            @Field("toolVersion") String toolVersion
    );

}
