package com.by.im.android.impl.service.impl;

import com.by.im.android.api.IMClient;
import com.by.im.common.kit.HeartBeatHandler;
import io.netty.channel.ChannelHandlerContext;

public class ClientHeartBeatHandlerImpl implements HeartBeatHandler {


    @Autowired
    private IMClient imClient;


    @Override
    public void process(ChannelHandlerContext ctx) throws Exception {

        //重连
        ContextHolder.setReconnect(true);
        imClient.reconnect();

    }


}
