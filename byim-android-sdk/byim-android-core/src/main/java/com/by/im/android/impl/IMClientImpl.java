package com.by.im.android.impl;

import com.by.im.android.api.ClientInfo;
import com.by.im.android.api.IMClient;
import com.by.im.android.api.StringMsgType;
import com.by.im.android.vo.req.LoginReqVO;
import com.by.im.android.vo.res.ServerResVO;

public class IMClientImpl implements IMClient {
    private ClientInfo clientInfo;

    @Override
    public void login(long userId, String userName) throws Exception {

    }

    private ServerResVO.ServerInfo userLogin(long userId, String userName) throws Exception{
        //请求登录信息
        clientInfo = new ClientInfo();
        LoginReqVO loginReqVO = new LoginReqVO(userId, userName);
        ServerResVO.ServerInfo serverinfo = null;
        try {
//            serverinfo = routeRequest.getCIMServer(loginReqVO);

            //保存系统信息
            clientInfo.saveServiceInfo(serverinfo.getIp() + ":" + serverinfo.getCimServerPort())
                    .saveUserInfo(userId, userName);

        } catch (Exception e) {
            throw new Exception("客户端连接异常", e);
        }

        return serverinfo;
    }

    @Override
    public void sendStringMsg(StringMsgType type, String msg, long userId) {

    }

    @Override
    public void reconnect() throws Exception {

    }

    @Override
    public void close() throws InterruptedException {
        clientInfo = null;
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
    public IMClient getInstance() {
        return null;
    }
}
