package com.by.im.android.api;

public class IMClientConfig {
    private int callBackThreadPoolQueueSize = 2;
    private int callBackThreadPoolSize = 2;


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
}
