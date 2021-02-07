package com.by.im.android.impl;

public class HttpClient {
    private HttpClient instance;
    private HttpClient() {}
    public HttpClient getInstance(){
        if(instance == null){
            instance = new HttpClient();
        }
        return instance;
    }
}
