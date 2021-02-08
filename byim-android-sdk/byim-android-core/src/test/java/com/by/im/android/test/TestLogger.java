package com.by.im.android.test;

import com.by.im.android.api.Logger;

public class TestLogger implements Logger {
    @Override
    public void info(String message) {
        System.out.println(message);
    }

    @Override
    public void warn(String message) {
        System.err.println(message);
    }

    @Override
    public void error(String message) {
        System.err.println(message);
    }

    @Override
    public void error(String message, Throwable e) {
        System.err.println(message);
        e.printStackTrace();
    }
}
