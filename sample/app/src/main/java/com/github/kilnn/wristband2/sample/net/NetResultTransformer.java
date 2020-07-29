package com.github.kilnn.wristband2.sample.net;

import com.github.kilnn.wristband2.sample.net.exception.NetResultDataException;
import com.github.kilnn.wristband2.sample.net.exception.NetResultStatusException;
import com.github.kilnn.wristband2.sample.net.result.BaseResult;
import com.github.kilnn.wristband2.sample.net.result.ListResult;
import com.github.kilnn.wristband2.sample.net.result.ObjectResult;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * Created by Kilnn on 2017/7/1.
 * 转换网络请求的结果，这样更容易获取对象
 */
class NetResultTransformer {
    private NetResultTransformer() {
    }

    /**
     * 将BaseResult原始返回，但是如果出现请求状态码错误，将抛出异常
     */
    public static <T extends BaseResult> Flowable<T> mapBase(Flowable<T> flowable) {
        return flowable.map(new Function<T, T>() {
            @Override
            public T apply(T t) throws Exception {
                if (t.getErrorCode() != BaseResult.ERROR_CODE_NONE) {
                    throw new NetResultStatusException(t.getErrorCode(), t.getErrorMsg());
                }
                return t;
            }
        });
    }


    /**
     * 将BaseResult返回为RxNotification，但是如果出现请求状态码错误，将抛出异常
     */
    public static <T extends BaseResult> Completable mapCompletable(Flowable<T> flowable) {
        return flowable
                .flatMapCompletable(new Function<T, CompletableSource>() {
                    @Override
                    public CompletableSource apply(T t) throws Exception {
                        if (t.getErrorCode() != BaseResult.ERROR_CODE_NONE) {
                            throw new NetResultStatusException(t.getErrorCode(), t.getErrorMsg());
                        }
                        return Completable.complete();
                    }
                });
    }

    /**
     * 将ObjectResult中的实体取出来然后返回
     */
    public static <M, T extends ObjectResult<M>> Flowable<M> mapObject(Flowable<T> flowable) {
        return flowable
                .map(new Function<T, M>() {
                    @Override
                    public M apply(@NonNull T t) throws Exception {
                        if (t.getErrorCode() != BaseResult.ERROR_CODE_NONE) {
                            throw new NetResultStatusException(t.getErrorCode(), t.getErrorMsg());
                        }
                        if (t.getData() == null) {
                            throw new NetResultDataException(NetResultDataException.DATA_EMPTY);
                        }
                        return t.getData();
                    }
                });
    }

    /**
     * 将ListResult中的实体取出来然后返回
     */
    public static <M, T extends ListResult<M>> Flowable<List<M>> mapList(Flowable<T> flowable, final boolean notEmpty) {
        return flowable
                .map(new Function<T, List<M>>() {
                    @Override
                    public List<M> apply(@NonNull T t) throws Exception {
                        if (t.getErrorCode() != BaseResult.ERROR_CODE_NONE) {
                            throw new NetResultStatusException(t.getErrorCode(), t.getErrorMsg());
                        }
                        if (notEmpty && t.getData() == null) {
                            throw new NetResultDataException(NetResultDataException.DATA_EMPTY);
                        }
                        if (t.getData() == null) {
                            return new ArrayList<>(0);
                        } else {
                            return t.getData();
                        }
                    }
                });
    }

}
