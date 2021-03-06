package com.github.kilnn.wristband2.sample.dial.custom.util;

import android.text.TextUtils;

import com.github.kilnn.wristband2.sample.MyApplication;
import com.github.kilnn.wristband2.sample.dial.custom.bean.DialInfo;
import com.github.kilnn.wristband2.sample.dial.custom.bean.DialRequestParam;
import com.github.kilnn.wristband2.sample.dial.custom.bean.DialSubBinParam;
import com.github.kilnn.wristband2.sample.net.GlobalApiClient;
import com.htsmart.wristband2.WristbandApplication;
import com.htsmart.wristband2.WristbandManager;
import com.htsmart.wristband2.bean.DialBinInfo;
import com.htsmart.wristband2.bean.DialSubBinInfo;
import com.htsmart.wristband2.bean.WristbandConfig;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 在手环已经连接的情况下，获取表盘请求参数
 */
public class TaskGetDialRequestParam {

    private WristbandManager mWristbandManager = WristbandApplication.getWristbandManager();
    private GlobalApiClient mApiClient = MyApplication.getApiClient();

    public TaskGetDialRequestParam() {

    }

    public Flowable<DialRequestParam> get() {
        final WristbandConfig config = mWristbandManager.getWristbandConfig();
        if (config == null) {
            return Flowable.error(new NullPointerException());
        }
        final String hardwareInfo = config.getWristbandVersion().getRawVersion();
        if (TextUtils.isEmpty(hardwareInfo)) {
            return Flowable.error(new NullPointerException());
        }
        final DialRequestParam param = new DialRequestParam();
        param.setHardwareInfo(hardwareInfo);
        //是否支持多表盘
        final boolean isExtDialMultiple = config.getWristbandVersion().isExtDialMultiple();
//        final boolean isExtDialMultiple = true;//for test
//        param.setHardwareInfo("000000000A0600000155000027F3000010001100000000000000030919081319000000000000");//for test
//        return Single.just(mockDialBinInfo())//for test
        return mWristbandManager.requestDialBinInfo()
                .flatMapPublisher(new Function<DialBinInfo, Publisher<? extends DialRequestParam>>() {
                    @Override
                    public Publisher<? extends DialRequestParam> apply(@NonNull DialBinInfo dialBinInfo) throws Exception {
                        param.setLcd(dialBinInfo.getLcd());
                        param.setToolVersion(dialBinInfo.getToolVersion());
                        final List<DialSubBinInfo> subBinList = dialBinInfo.getSubBinList();
                        if (isExtDialMultiple && subBinList != null && subBinList.size() > 0) {
                            //支持多表盘升级
                            List<Integer> numbers = new ArrayList<>(subBinList.size());
                            for (DialSubBinInfo sub : subBinList) {
                                if (sub.getDialType() == DialSubBinInfo.TYPE_NORMAL) {//不可推送的表盘和自定义表盘的编号不去请求图片信息
                                    numbers.add(sub.getDialNum());
                                }
                            }
                            Flowable<List<DialInfo>> flowable;
                            if (numbers.size() <= 0) {
                                flowable = Flowable.just((List<DialInfo>) new ArrayList<DialInfo>(0));
                            } else {
                                flowable = mApiClient.getDialListByNumbers(numbers)
                                        .subscribeOn(Schedulers.io())
                                        .onErrorReturnItem(new ArrayList<DialInfo>(0));
                            }
                            return flowable
                                    .map(new Function<List<DialInfo>, DialRequestParam>() {
                                        @Override
                                        public DialRequestParam apply(@NonNull List<DialInfo> dialInfos) throws Exception {
                                            List<DialSubBinParam> subBinParams = new ArrayList<>(subBinList.size());
                                            for (DialSubBinInfo sub : subBinList) {
                                                if (sub.getDialType() != DialSubBinInfo.TYPE_NONE) {//外部只显示可以推送的表盘
                                                    DialSubBinParam subParam = new DialSubBinParam();
                                                    subParam.setDialType(sub.getDialType());
                                                    subParam.setDialNum(sub.getDialNum());
                                                    subParam.setBinVersion(sub.getBinVersion());
                                                    subParam.setBinFlag(sub.getBinFlag());
                                                    if (sub.getDialType() == DialSubBinInfo.TYPE_NORMAL) {//不可推送的表盘和自定义表盘的编号没有去请求图片信息
                                                        for (DialInfo dialInfo : dialInfos) {
                                                            if (dialInfo.getDialNum() == sub.getDialNum()) {
                                                                subParam.setImgUrl(dialInfo.getImgUrl());
                                                                subParam.setDeviceImgUrl(dialInfo.getDeviceImgUrl());
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    subBinParams.add(subParam);
                                                }
                                            }
                                            param.setSubBinParams(subBinParams);
                                            return param;
                                        }
                                    });
                        } else {
                            return Flowable.just(param);
                        }
                    }
                });
    }

//    private DialBinInfo mockDialBinInfo() {
//        DialBinInfo info = new DialBinInfo();
//        info.setLcd(7);
//        info.setToolVersion("1.4");
//        List<DialSubBinInfo> subBinList = new ArrayList<>();
//        {
//            DialSubBinInfo sub = new DialSubBinInfo();
//            sub.setDialNum(14002);
//            sub.setDialType(DialSubBinInfo.TYPE_NONE);
//            sub.setBinFlag((byte) 0);
//            subBinList.add(sub);
//        }
//        {
//            DialSubBinInfo sub = new DialSubBinInfo();
//            sub.setDialNum(14003);
//            sub.setDialType(DialSubBinInfo.TYPE_NORMAL);
//            sub.setBinFlag((byte) 0xA1);
//            subBinList.add(sub);
//        }
//        {
//            DialSubBinInfo sub = new DialSubBinInfo();
//            sub.setDialNum(14007);
//            sub.setDialType(DialSubBinInfo.TYPE_NORMAL);
//            sub.setBinFlag((byte) 0xA5);
//            subBinList.add(sub);
//        }
//        {
//            DialSubBinInfo sub = new DialSubBinInfo();
//            sub.setDialNum(60001);
//            sub.setDialType(DialSubBinInfo.TYPE_CUSTOM_STYLE_WHITE);
//            sub.setBinFlag((byte) 0xA6);
//            subBinList.add(sub);
//        }
//        {
//            DialSubBinInfo sub = new DialSubBinInfo();
//            sub.setDialNum(60002);
//            sub.setDialType(DialSubBinInfo.TYPE_CUSTOM_STYLE_GREEN);
//            sub.setBinFlag((byte) 0xA7);
//            subBinList.add(sub);
//        }
//        info.setSubBinList(subBinList);
//        return info;
//    }
}
