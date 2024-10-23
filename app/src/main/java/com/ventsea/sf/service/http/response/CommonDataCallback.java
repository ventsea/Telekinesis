package com.ventsea.sf.service.http.response;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;


import com.google.gson.Gson;
import com.ventsea.sf.service.http.exception.AsyncHttpException;
import com.ventsea.sf.service.http.listener.DisposeDataHandler;
import com.ventsea.sf.service.http.listener.DisposeDataListener;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

@SuppressWarnings("UnnecessaryReturnStatement")
public class CommonDataCallback implements Callback {

    public static final int NETWORK_ERROR = -1;
    public static final int JSON_ERROR = -2;
    public static final int OTHER_ERROR = -3;

    private DisposeDataListener mListener;
    private Class<?> mClass;
    private Handler mUiHandler;

    public CommonDataCallback(DisposeDataHandler handle) {
        this.mListener = handle.mListener;
        this.mClass = handle.mClass;
        this.mUiHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onFailure(@NonNull final Call call, @NonNull final IOException e) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onResponseListener(false, new AsyncHttpException(NETWORK_ERROR, e));
            }
        });
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        if (response.body() != null) {
            final String result = response.body().string();
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    handleResponse(result);
                }
            });
        } else {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onResponseListener(false, new AsyncHttpException(OTHER_ERROR, "body is null"));
                }
            });
        }
    }

    private void handleResponse(String json) {
        if (json == null || TextUtils.isEmpty(json)) {
            mListener.onResponseListener(false, new AsyncHttpException(OTHER_ERROR, "body is empty"));
            return;
        }
        try {
            if (mClass == null) {
                mListener.onResponseListener(true, json);
            } else {
                Object obj = new Gson().fromJson(json, mClass);
                if (obj != null) {
                    mListener.onResponseListener(true, obj);
                } else {
                    mListener.onResponseListener(false, new AsyncHttpException(JSON_ERROR, "fromJson is null"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            mListener.onResponseListener(false, new AsyncHttpException(JSON_ERROR, "???"));
        }
    }
}
