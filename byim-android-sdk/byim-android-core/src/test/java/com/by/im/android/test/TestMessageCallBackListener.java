package com.by.im.android.test;

import com.by.im.android.api.MsgCallBackListener;

public class TestMessageCallBackListener implements MsgCallBackListener {
    @Override
    public void handle(long userId, String message) {
        System.out.println("从用户:"+userId+"收到消息：" + message);
    }
}
