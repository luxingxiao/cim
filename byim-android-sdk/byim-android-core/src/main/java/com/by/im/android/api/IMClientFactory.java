package com.by.im.android.api;

import com.by.im.android.impl.IMClientImpl;

public class IMClientFactory {
    private static IMClient instance;

    public static IMClient initIMClient(Logger logger, MsgCallBackListener msgCallBackListener) {
        return initIMClient(logger, msgCallBackListener, new IMClientConfig());
    }

    public static IMClient initIMClient(Logger logger, MsgCallBackListener msgCallBackListener, IMClientConfig imClientConfig) {
        if (instance == null) {
            instance = new IMClientImpl(logger, msgCallBackListener, imClientConfig);
        }
        return instance;
    }

    public static IMClient getIMClient() {
        return instance;
    }
}
