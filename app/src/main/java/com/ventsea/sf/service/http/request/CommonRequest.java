package com.ventsea.sf.service.http.request;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class CommonRequest {

    private CommonRequest() {

    }

    /**
     * 创建Get请求的Request
     */
    public static Request createGet(String url, RequestParams params) {
        StringBuilder urlBuilder;
        if (!url.contains("?")) {
            urlBuilder = new StringBuilder(url).append("?");
        } else {
            urlBuilder = new StringBuilder(url).append("&");
        }
        if (params != null) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                urlBuilder
                        .append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
        }
        return new Request.Builder().url(urlBuilder.substring(0, urlBuilder.length() - 1)).get().build();
    }

    public static Request createGet(String url) {
        return new Request.Builder().url(url).get().build();
    }

    /**
     * 创建Post请求的Request
     */
    public static Request createPost(String url, RequestParams params) {
        FormBody.Builder mFromBodyBuilder = new FormBody.Builder();

        if (params != null) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                mFromBodyBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        FormBody mFormBody = mFromBodyBuilder.build();
        return new Request.Builder().url(url).post(mFormBody).build();
    }

    public static Request createPost(String url, String json) {
        if (json == null) {
            json = "";
            new Request.Builder().url(url).get().build();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        return new Request.Builder().url(url).post(body).build();
    }

}