package com.by.im.android.impl.thread;

import com.by.im.android.impl.IMClientConfig;
import com.by.im.common.kit.HeartBeatHandler;
import io.netty.channel.ChannelHandlerContext;

public class ReConnectJob implements Runnable {
    private ChannelHandlerContext context;
    private HeartBeatHandler heartBeatHandler;

    public ReConnectJob(ChannelHandlerContext context) {
        this.context = context;
        this.heartBeatHandler = IMClientConfig.getInstance().heartBeatHandler();
    }

    @Override
    public void run() {
        try {
            heartBeatHandler.process(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
