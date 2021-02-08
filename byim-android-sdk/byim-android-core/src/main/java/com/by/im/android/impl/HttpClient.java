package com.by.im.android.impl;

import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class HttpClient {
    private HttpClient instance;
    private OkHttpClient okHttpClient;
    private static final long CONNECTION_TIMEOUT = 30;
    private static final long READ_TIMEOUT = 10;
    private static final long WRITE_TIMEOUT = 10;

    private HttpClient() {
    }

    public HttpClient getInstance() {
        if (instance == null) {
            instance = new HttpClient();
            initOkHttpClient();
        }
        return instance;
    }

    private void initOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
        okHttpClient = builder.build();
    }

}
