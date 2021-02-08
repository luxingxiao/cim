package com.by.im.android.impl.service.impl;


import com.alibaba.fastjson.JSON;
import com.by.im.android.api.IMClient;
import com.by.im.android.api.IMClientFactory;
import com.by.im.android.api.Logger;
import com.by.im.android.impl.service.RouteRequest;
import com.by.im.android.impl.thread.ContextHolder;
import com.by.im.android.vo.req.GroupReqVO;
import com.by.im.android.vo.req.LoginReqVO;
import com.by.im.android.vo.req.P2PReqVO;
import com.by.im.android.vo.res.OnlineUsersResVO;
import com.by.im.android.vo.res.ServerResVO;
import com.by.im.common.enums.StatusEnum;
import com.by.im.common.exception.CIMException;
import com.by.im.common.proxy.ProxyManager;
import com.by.im.common.res.BaseResponse;
import com.by.im.common.route.api.RouteApi;
import com.by.im.common.route.api.vo.req.ChatReqVO;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.util.List;

public class RouteRequestImpl implements RouteRequest {

    private OkHttpClient okHttpClient ;

    private String routeUrl ;

    private Logger logger;
    public RouteRequestImpl(Logger logger){
        this.logger = logger;
    }

    @Override
    public void sendGroupMsg(GroupReqVO groupReqVO) throws Exception {
        RouteApi routeApi = new ProxyManager<>(RouteApi.class, routeUrl, okHttpClient).getInstance();
        ChatReqVO chatReqVO = new ChatReqVO(groupReqVO.getUserId(), groupReqVO.getMsg()) ;
        Response response = null;
        try {
            response = (Response)routeApi.groupRoute(chatReqVO);
        }catch (Exception e){
            throw e;
        }finally {
            response.body().close();
        }
    }

    @Override
    public void sendP2PMsg(P2PReqVO p2PReqVO) throws Exception {
        RouteApi routeApi = new ProxyManager<>(RouteApi.class, routeUrl, okHttpClient).getInstance();
        com.by.im.common.route.api.vo.req.P2PReqVO vo = new com.by.im.common.route.api.vo.req.P2PReqVO();
        vo.setMsg(p2PReqVO.getMsg());
        vo.setReceiveUserId(p2PReqVO.getReceiveUserId());
        vo.setUserId(p2PReqVO.getUserId());

        Response response = null;
        try {
            response = (Response) routeApi.p2pRoute(vo);
            String json = response.body().string() ;
            BaseResponse baseResponse = JSON.parseObject(json, BaseResponse.class);

            // account offline.
            if (baseResponse.getCode().equals(StatusEnum.OFF_LINE.getCode())){
                logger.error(p2PReqVO.getReceiveUserId() + ":" + StatusEnum.OFF_LINE.getMessage());
            }

        }catch (Exception e){
            logger.error("exception",e);
        }finally {
            response.body().close();
        }
    }

    @Override
    public ServerResVO.ServerInfo getCIMServer(LoginReqVO loginReqVO) throws Exception {

        RouteApi routeApi = new ProxyManager<>(RouteApi.class, routeUrl, okHttpClient).getInstance();
        com.by.im.common.route.api.vo.req.LoginReqVO vo = new com.by.im.common.route.api.vo.req.LoginReqVO() ;
        vo.setUserId(loginReqVO.getUserId());
        vo.setUserName(loginReqVO.getUserName());

        Response response = null;
        ServerResVO cimServerResVO = null;
        try {
            response = (Response) routeApi.login(vo);
            String json = response.body().string();
            cimServerResVO = JSON.parseObject(json, ServerResVO.class);

            //重复失败
            if (!cimServerResVO.getCode().equals(StatusEnum.SUCCESS.getCode())){

                // when client in reConnect state, could not exit.
                if (ContextHolder.getReconnect()){
                    throw new CIMException(StatusEnum.RECONNECT_FAIL);
                }

                System.exit(-1);
            }

        }catch (Exception e){
            logger.error("exception",e);
        }finally {
            response.body().close();
        }

        return cimServerResVO.getDataBody();
    }

    @Override
    public List<OnlineUsersResVO.DataBodyBean> onlineUsers() throws Exception{
        RouteApi routeApi = new ProxyManager<>(RouteApi.class, routeUrl, okHttpClient).getInstance();

        Response response = null;
        OnlineUsersResVO onlineUsersResVO = null;
        try {
            response = (Response) routeApi.onlineUser();
            String json = response.body().string() ;
            onlineUsersResVO = JSON.parseObject(json, OnlineUsersResVO.class);

        }catch (Exception e){
            throw e;
        }finally {
            response.body().close();
        }

        return onlineUsersResVO.getDataBody();
    }

    @Override
    public void offLine() {
        long userId = IMClientFactory.getIMClient().getClientInfo().get().getUserId();
        RouteApi routeApi = new ProxyManager<>(RouteApi.class, routeUrl, okHttpClient).getInstance();
        ChatReqVO vo = new ChatReqVO(userId, "offLine") ;
        Response response = null;
        try {
            response = (Response) routeApi.offLine(vo);
        } catch (Exception e) {
            throw  new RuntimeException(e);
        } finally {
            response.body().close();
        }
    }
}
