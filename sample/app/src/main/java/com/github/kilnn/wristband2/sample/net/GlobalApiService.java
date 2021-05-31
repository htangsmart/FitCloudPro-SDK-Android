package com.github.kilnn.wristband2.sample.net;

import com.github.kilnn.wristband2.sample.dial.entity.DialCustom;
import com.github.kilnn.wristband2.sample.dial.entity.DialInfo;
import com.github.kilnn.wristband2.sample.dial.entity.DialInfoComplex;
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

    String URL_DIAL_LIST = "/public/dial/list";//查询符合的表盘列表
    String URL_DIAL_GET = "/public/dial/get";//查询符合的表盘列表
    String URL_DIAL_CUSTOM = "/public/dial/custom";//获取自定义表盘列表
    String URL_DIAL_CUSTOM_GUI = "/public/dial/customgui";//获取GUI自定义表盘列表

    @POST(URL_DIAL_LIST)
    @FormUrlEncoded
    Flowable<ListResult<DialInfo>> getDialList(
            @Field("hardwareInfo") String hardwareInfo,
            @Field("lcd") int lcd,
            @Field("toolVersion") String toolVersion
    );

    @POST(URL_DIAL_GET)
    @FormUrlEncoded
    Flowable<ListResult<DialInfoComplex>> getDialListByNumbers(
            @Field("data") String data
    );

    @POST(URL_DIAL_CUSTOM)
    @FormUrlEncoded
    Flowable<ListResult<DialCustom>> getDialCustom(
            @Field("lcd") int lcd,
            @Field("toolVersion") String toolVersion
    );

    @POST(URL_DIAL_CUSTOM_GUI)
    @FormUrlEncoded
    Flowable<ListResult<DialInfoComplex>> getDialCustomGUI(
            @Field("lcd") int lcd,
            @Field("toolVersion") String toolVersion
    );
}
