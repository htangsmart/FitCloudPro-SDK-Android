package com.github.kilnn.wristband2.sample.net;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebSettings;

import com.github.kilnn.wristband2.sample.BuildConfig;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * Created by Kilnn on 2017/3/16.
 * API文档：https://www.zybuluo.com/htsmart/note/536277
 */

abstract class AbstractApiClient<T> {
    /**
     * 服务器url
     */
    static final String BASE_URL = "http://fitcloud.hetangsmart.com";
    static final int PLATFORM = 0;//所属平台，0安卓，1苹果，2后台

    private static final HttpLoggingInterceptor.Level DEFAULT_LOGGING_LEVEL;

    static {
        if (BuildConfig.DEBUG) {
            DEFAULT_LOGGING_LEVEL = HttpLoggingInterceptor.Level.BODY;
        } else {
            DEFAULT_LOGGING_LEVEL = HttpLoggingInterceptor.Level.NONE;
        }
    }

    private HttpLoggingInterceptor.Level mLoggingLevel;
    private volatile String mBaseUrl;
    private volatile T mService;
    private String mUserAgent;

    AbstractApiClient(Context context) {
        this(context, DEFAULT_LOGGING_LEVEL);
    }

    AbstractApiClient(Context context, HttpLoggingInterceptor.Level loggingLevel) {
        this.mLoggingLevel = loggingLevel;
        mUserAgent = getUserAgent(context);
        String appInfo = null;
        try {
            PackageManager packageManager = context.getPackageManager();

            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            String appName = context.getResources().getString(labelRes);
            String versionName = packageInfo.versionName;
            appInfo = appName + "/" + versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(appInfo)) {
            if (mUserAgent == null) {
                mUserAgent = appInfo;
            } else {
                mUserAgent = appInfo + " " + mUserAgent;
            }
        }
        Log.w("AbstractApiClient", "mUserAgent:" + mUserAgent);
    }

    /**
     * https://www.jianshu.com/p/ddbe8c637fc5
     * Okhttp设置User-Agent你可能没遇到的坑，源码解读
     *
     * @param context
     * @return
     */
    private static String getUserAgent(Context context) {
        String userAgent;
        try {
            userAgent = WebSettings.getDefaultUserAgent(context);
        } catch (Exception e) {
            e.printStackTrace();
            userAgent = System.getProperty("http.agent");
        }
        if (TextUtils.isEmpty(userAgent)) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0, length = userAgent.length(); i < length; i++) {
            char c = userAgent.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    T getService() {
        return mService;
    }

    protected abstract Class<T> getServiceClass();

    /**
     * 另外添加拦截器，如果没有，返回null
     */
    protected abstract List<Interceptor> extraInterceptors();


    protected void createService() {
        String baseUrl = BASE_URL;

        if (TextUtils.equals(mBaseUrl, baseUrl)) return;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (mLoggingLevel != HttpLoggingInterceptor.Level.NONE) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(mLoggingLevel);
            builder.interceptors().add(httpLoggingInterceptor);
        }

        if (!TextUtils.isEmpty(mUserAgent)) {
            builder.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request()
                            .newBuilder()
                            .removeHeader("User-Agent")//移除旧的
                            .addHeader("User-Agent", mUserAgent)
                            .build();
                    return chain.proceed(request);
                }
            });
        }

        List<Interceptor> extra = extraInterceptors();
        if (extra != null && extra.size() > 0) {
            builder.interceptors().addAll(extra);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(new Retrofit2ConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(builder.build())
                .build();

        /*
         * 获取泛型的Class类型
         */
//        Type genType = getClass().getGenericSuperclass();
//        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
//        @SuppressWarnings("unchecked")
//        Class<T> clazz = (Class<T>) params[0];
//        mService = retrofit.create(clazz);

        //可以使用上面来获取泛型的Class类型，但是还是用重写的方法来完成，效率更高
        mBaseUrl = baseUrl;
        mService = retrofit.create(getServiceClass());
    }
}
