package com.by.im.android.api;

public class IMClientConfig {
    private int callBackThreadPoolQueueSize = 2;
    private int callBackThreadPoolSize = 2;
    private int okHttpConnectTimeout = 30;
    private int okHttpReadTimeout = 10;
    private int okHttpWriteTimeout = 10;
    private String serverUrl = "http://localhost:8080";

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public int getCallBackThreadPoolQueueSize() {
        return callBackThreadPoolQueueSize;
    }

    public void setCallBackThreadPoolQueueSize(int callBackThreadPoolQueueSize) {
        this.callBackThreadPoolQueueSize = callBackThreadPoolQueueSize;
    }

    public int getCallBackThreadPoolSize() {
        return callBackThreadPoolSize;
    }

    public void setCallBackThreadPoolSize(int callBackThreadPoolSize) {
        this.callBackThreadPoolSize = callBackThreadPoolSize;
    }

    public int getOkHttpConnectTimeout() {
        return okHttpConnectTimeout;
    }

    public void setOkHttpConnectTimeout(int okHttpConnectTimeout) {
        this.okHttpConnectTimeout = okHttpConnectTimeout;
    }

    public int getOkHttpReadTimeout() {
        return okHttpReadTimeout;
    }

    public void setOkHttpReadTimeout(int okHttpReadTimeout) {
        this.okHttpReadTimeout = okHttpReadTimeout;
    }

    public int getOkHttpWriteTimeout() {
        return okHttpWriteTimeout;
    }

    public void setOkHttpWriteTimeout(int okHttpWriteTimeout) {
        this.okHttpWriteTimeout = okHttpWriteTimeout;
    }
}
