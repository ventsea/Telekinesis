package com.ventsea.sf.service.http;

import android.annotation.SuppressLint;

import com.ventsea.sf.service.http.https.HttpsUtils;
import com.ventsea.sf.service.http.listener.DisposeDataHandler;
import com.ventsea.sf.service.http.response.CommonDataCallback;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class AsyncHttp {

    private AsyncHttp() {
    }

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(5 * 1000, TimeUnit.MILLISECONDS)
            .readTimeout(5 * 1000, TimeUnit.MILLISECONDS)
            .followRedirects(true)
            .hostnameVerifier(new HostnameVerifier() {

                @SuppressLint("BadHostnameVerifier")
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            })
            .sslSocketFactory(HttpsUtils.initSSLSocketFactory(), HttpsUtils.initTrustManager())
            .build();

    public static Call sendRequest(Request request, DisposeDataHandler handle) {
        Call call = okHttpClient.newCall(request);
        call.enqueue(new CommonDataCallback(handle));
        return call;
    }

    public static Call sendRequest(Request request, CommonDataCallback commonCallback) {
        Call call = okHttpClient.newCall(request);
        call.enqueue(commonCallback);
        return call;
    }
}
