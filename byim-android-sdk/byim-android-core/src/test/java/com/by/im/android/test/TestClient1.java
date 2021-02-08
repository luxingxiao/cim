package com.by.im.android.test;

import com.by.im.android.api.*;


public class TestClient1 {
    public static void main(String[] args) throws InterruptedException {
        Logger logger = new TestLogger();
        MsgCallBackListener msgCallBackListener = new TestMessageCallBackListener();
        IMClientConfig imClientConfig = new IMClientConfig();
        imClientConfig.setServerUrl("http://192.168.99.152:8083/");

        IMClient imClient = IMClientFactory.initIMClient(logger, msgCallBackListener, imClientConfig);

        try {
            int i = 0;
            imClient.login(100001, "test100001");
            Thread.sleep(10 * 1000);

            while (true) {
                Thread.sleep(500);
                imClient.sendStringMsg(100002, StringMsgType.TEXT, "From 100001: MSG-" + i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            imClient.close();
        }
    }
}
