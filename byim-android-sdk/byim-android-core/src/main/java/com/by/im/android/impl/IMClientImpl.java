package com.by.im.android.impl;

import com.by.im.android.api.*;
import com.by.im.android.impl.init.CIMClientHandleInitializer;
import com.by.im.android.impl.service.ReConnectManager;
import com.by.im.android.impl.service.RouteRequest;
import com.by.im.android.impl.thread.ContextHolder;
import com.by.im.android.vo.req.LoginReqVO;
import com.by.im.android.vo.req.P2PReqVO;
import com.by.im.android.vo.res.ServerResVO;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

public class IMClientImpl implements IMClient {
    private ClientInfo clientInfo;
    private RouteRequest routeRequest;
    private IMClientConfig imClientConfig;
    private Logger logger;
    private SocketChannel channel;
    private EventLoopGroup group = new NioEventLoopGroup(0, new DefaultThreadFactory("im-work"));
    private long userId;
    private String userName;
    private ReConnectManager reConnectManager;
    private MsgCallBackListener msgCallBackListener;

    public IMClientImpl(Logger logger, MsgCallBackListener msgCallBackListener){
        this(logger, msgCallBackListener, new IMClientConfig());
    }

    public IMClientImpl(Logger logger, MsgCallBackListener msgCallBackListener, IMClientConfig imClientConfig){
        this.logger = logger;
        this.msgCallBackListener = msgCallBackListener;
        this.imClientConfig = imClientConfig;
    }

    @Override
    public void login(long userId, String userName) throws Exception {
        ServerResVO.ServerInfo cimServer = userLogin(userId, userName);
        startClient(cimServer);
        clientInfo.saveServiceInfo(cimServer.getIp() + ":" + cimServer.getCimServerPort())
                .saveUserInfo(userId, userName);
    }

    private ServerResVO.ServerInfo userLogin(long userId, String userName) throws Exception{
        this.userId = userId;
        this.userName = userName;
        //请求登录信息
        clientInfo = new ClientInfo();
        LoginReqVO loginReqVO = new LoginReqVO(userId, userName);
        ServerResVO.ServerInfo serverinfo = null;
        try {
            routeRequest = BeanFactory.getInstance().routeRequest(logger);
            serverinfo = routeRequest.getCIMServer(loginReqVO);

            //保存系统信息
            clientInfo.saveServiceInfo(serverinfo.getIp() + ":" + serverinfo.getCimServerPort())
                    .saveUserInfo(userId, userName);

        } catch (Exception e) {
            throw new Exception("客户端连接异常", e);
        }

        return serverinfo;
    }

    /**
     * 启动客户端
     *
     * @param cimServer
     * @throws Exception
     */
    private void startClient(ServerResVO.ServerInfo cimServer) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new CIMClientHandleInitializer())
        ;

        ChannelFuture future = null;
        try {
            future = bootstrap.connect(cimServer.getIp(), cimServer.getCimServerPort()).sync();
        } catch (Exception e) {
            logger.error("Connect fail!", e);
        }
        if (future.isSuccess()) {
            logger.info("启动 cim client 成功");
        }
        channel = (SocketChannel) future.channel();
    }

    @Override
    public void sendStringMsg(long toUser, StringMsgType type, String msg) {
        P2PReqVO p2PReqVO = new P2PReqVO();
        p2PReqVO.setUserId(userId);
        p2PReqVO.setReceiveUserId(toUser);
        p2PReqVO.setMsg(msg);

        try {
            routeRequest.sendP2PMsg(p2PReqVO);
        } catch (Exception e) {
            logger.error("Send msg exception", e);
        }
    }

    @Override
    public void reconnect() throws Exception {
        if (channel != null && channel.isActive()) {
            return;
        }
        //首先清除路由信息，下线
        routeRequest.offLine();

        login(userId, userName);
        reConnectManager = BeanFactory.getInstance().reConnectManager();
        reConnectManager.reConnectSuccess();
        ContextHolder.clear();
    }

    @Override
    public void close() throws InterruptedException {
        clientInfo = null;
        if (channel != null){
            channel.close();
        }
    }

    @Override
    public ClientInfo getClientInfo() throws IllegalStateException {
        if (clientInfo == null) {
            throw new IllegalStateException("客戶端未登录");
        } else {
            return clientInfo;
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void setConfig(IMClientConfig config) {
        this.imClientConfig = config;
    }

    @Override
    public MsgCallBackListener getMsgCallBackListener() {
        return msgCallBackListener;
    }

    @Override
    public void setMsgCallBackListener(MsgCallBackListener msgCallBackListener) {
        this.msgCallBackListener = msgCallBackListener;
    }

    @Override
    public IMClientConfig getConfig() {
        return imClientConfig;
    }
}
