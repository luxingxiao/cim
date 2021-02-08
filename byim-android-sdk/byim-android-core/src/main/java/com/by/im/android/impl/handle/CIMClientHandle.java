package com.by.im.android.impl.handle;

import com.by.im.android.api.IMClientFactory;
import com.by.im.android.api.Logger;
import com.by.im.android.impl.BeanFactory;
import com.by.im.android.impl.service.ReConnectManager;
import com.by.im.android.impl.service.ShutDownMsg;
import com.by.im.common.constant.Constants;
import com.by.im.common.protocol.CIMRequestProto;
import com.by.im.common.protocol.CIMResponseProto;
import com.by.im.common.util.NettyAttrUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

@ChannelHandler.Sharable
public class CIMClientHandle extends SimpleChannelInboundHandler<CIMResponseProto.CIMResProtocol> {

    private ThreadPoolExecutor threadPoolExecutor;

    private ScheduledExecutorService scheduledExecutorService;

    private ReConnectManager reConnectManager;

    private ShutDownMsg shutDownMsg;

    private Logger logger;

    public CIMClientHandle() {
        logger = IMClientFactory.getIMClient().getLogger();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;

            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                CIMRequestProto.CIMReqProtocol heartBeat = BeanFactory.getInstance().heartBeatProtocol(IMClientFactory.getIMClient().getClientInfo().get().getUserId());
                ctx.writeAndFlush(heartBeat).addListeners((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        logger.error("IO error,close Channel");
                        future.channel().close();
                    }
                });
            }

        }

        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //客户端和服务端建立连接时调用
        logger.info("cim server connect success!");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        if (shutDownMsg == null) {
            shutDownMsg = BeanFactory.getInstance().shutDownMsg();
        }

        //用户主动退出，不执行重连逻辑
        if (shutDownMsg.checkStatus()) {
            return;
        }

        if (scheduledExecutorService == null) {
            scheduledExecutorService = BeanFactory.getInstance().scheduledExecutorService();
            reConnectManager = BeanFactory.getInstance().reConnectManager();
        }
        logger.info("客户端断开了，重新连接！");
        reConnectManager.reConnect(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CIMResponseProto.CIMResProtocol msg) throws Exception {

        //心跳更新时间
        if (msg.getType() == Constants.CommandType.PING) {
            //LOGGER.info("收到服务端心跳！！！");
            NettyAttrUtil.updateReaderTime(ctx.channel(), System.currentTimeMillis());
        }

        if (msg.getType() != Constants.CommandType.PING) {
            //回调消息
            long userId = msg.getResponseId();
            String msgString = msg.getResMsg();
            IMClientFactory.getIMClient().getMsgCallBackListener().handle(userId, msgString);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //异常时断开连接
        cause.printStackTrace();
        ctx.close();
    }
}
