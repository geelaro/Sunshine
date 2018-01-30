package com.geelaro.blackboard.utils;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.$Gson$Types;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by geelaro on 2017/6/23.
 */

public class OkHttpUtils {

    private static final String TAG = OkHttpUtils.class.getSimpleName(); //

    private static volatile OkHttpUtils mInstance;
    private OkHttpClient mOkHttpClient;

    private Handler mDelivery;
    private static final int CACHE_SIZE = 1024 * 1024 * 10; //cache size is 10Mb


    private OkHttpUtils() {
        File cacheFile = FileUtils.getCacheDir(BkApp.getContext(), "cache");
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }
        Cache cache = new Cache(cacheFile, CACHE_SIZE); //10Mb

        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cache(cache) //设置cache
                .build();

        mDelivery = new Handler(Looper.getMainLooper());

    }

    //单例
    public synchronized static OkHttpUtils newInstance() {
        if (mInstance == null) {
            synchronized (OkHttpUtils.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpUtils();
                }
            }
        }
        return mInstance;
    }

    //GET
    private void getRequest(String url, final ResultCallback callback) {
        Request request;
        //设置缓存时间为10m
        CacheControl cacheControl = new CacheControl.Builder()
                .maxAge(60, TimeUnit.SECONDS)
                .build();
        if (!NetworkUtils.isNetworkAvailable(BkApp.getContext())) {
            request = new Request.Builder().url(url).cacheControl(CacheControl.FORCE_CACHE).build();
        } else {
            request = new Request.Builder().url(url).cacheControl(cacheControl).build();
        }
        deliveryResult(callback, request);

    }

    //POST
    private void postRequest(String url, final ResultCallback callback, List<Param> params) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Param param : params) {
            builder.add(param.key, param.value);
        }
        FormBody body = builder.build();
        Request request = new Request.Builder().url(url).post(body).build();

        deliveryResult(callback, request);
    }


    //
    private void deliveryResult(final ResultCallback callback, Request request) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailureCallBack(callback, e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String str = response.body().string();
                    SunLog.d(TAG, "response cache:" + response.cacheResponse());
                    SunLog.d(TAG, "response networkResponse：" + response.networkResponse());

                    if (callback.mType == String.class) {
                        sendSuccessCallBack(callback, str);
                        SunLog.d(TAG, "str: " + str);
                    } else {
                        Gson gson = new GsonBuilder().create();
                        Object obj = gson.fromJson(str, callback.mType);
                        sendSuccessCallBack(callback, obj);
                        SunLog.d(TAG, "obj: " + obj);
                    }

                } catch (Exception e) {
                    SunLog.e(TAG, "服务器错误." + e);
                    sendFailureCallBack(callback, e);
                }
            }
        });
    }

    private void sendFailureCallBack(final ResultCallback callback, final Exception e) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onFailure(e);
                    ShowToast.Short("数据加载失败！");
                }
            }
        });
    }

    private void sendSuccessCallBack(final ResultCallback callback, final Object obj) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onSuccess(obj);

                }
            }
        });
    }


    //对外接口

    /**
     * GET 接口
     **/
    public void get(String url, final ResultCallback callback) {
        getRequest(url, callback);
    }

    public static abstract class ResultCallback<T> {
        Type mType;

        public ResultCallback() {
            mType = getSuperclassTypeParameter(getClass());
        }

        static Type getSuperclassTypeParameter(Class<?> subclass) {
            Type superclass = subclass.getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            ParameterizedType parameterized = (ParameterizedType) superclass;
            return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
        }

        public abstract void onFailure(Exception e);

        public abstract void onSuccess(T response);
    }

    public static class Param {
        public Param() {
        }

        public Param(String key, String value) {
            this.key = key;
            this.value = value;
        }

        String key;
        String value;
    }


}


