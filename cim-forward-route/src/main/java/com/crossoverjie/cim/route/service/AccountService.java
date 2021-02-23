package com.crossoverjie.cim.route.service;

import com.crossoverjie.cim.common.enums.StatusEnum;
import com.crossoverjie.cim.common.pojo.ChatMsgCache;
import com.crossoverjie.cim.route.api.vo.req.ChatReqVO;
import com.crossoverjie.cim.route.api.vo.req.LoginReqVO;
import com.crossoverjie.cim.route.api.vo.req.P2PReqVO;
import com.crossoverjie.cim.route.api.vo.res.CIMServerResVO;
import com.crossoverjie.cim.route.api.vo.res.RegisterInfoResVO;

import java.util.List;
import java.util.Map;

/**
 * Function: 账户服务
 *
 * @author crossoverJie
 *         Date: 2018/12/23 21:57
 * @since JDK 1.8
 */
public interface AccountService {

    /**
     * 注册用户
     * @param info 用户信息
     * @return
     * @throws Exception
     */
    RegisterInfoResVO register(RegisterInfoResVO info) throws Exception;

    /**
     * 登录服务
     * @param loginReqVO 登录信息
     * @return true 成功 false 失败
     * @throws Exception
     */
    StatusEnum login(LoginReqVO loginReqVO) throws Exception ;

    /**
     * 保存路由信息
     * @param msg 服务器信息
     * @param loginReqVO 用户信息
     * @throws Exception
     */
    void saveRouteInfo(LoginReqVO loginReqVO ,String msg) throws Exception ;

    /**
     * 加载所有用户的路有关系
     * @return 所有的路由关系
     */
    Map<Long, CIMServerResVO> loadRouteRelated() ;

    /**
     * 获取某个用户的路有关系
     * @param userId
     * @return 获取某个用户的路有关系
     */
    CIMServerResVO loadRouteRelatedByUserId(Long userId) ;


    /**
     * 推送消息
     * @param cimServerResVO
     * @param groupReqVO 消息
     * @param sendUserId 发送者的ID
     * @throws Exception
     */
    void pushMsg(CIMServerResVO cimServerResVO, long sendUserId , ChatReqVO groupReqVO) throws Exception;

    /**
     * 用户下线
     * @param userId 下线用户ID
     * @throws Exception
     */
    void offLine(Long userId) throws Exception;

    /**
     * 接收方缓存离线消息
     * @param p2pRequest
     */
    void receiveCacheChatMsg(P2PReqVO p2pRequest);

    /**
     * 发送方缓存个人消息记录
     * @param p2pRequest
     */
    void sendCacheChatMsg(P2PReqVO p2pRequest);

    /**
     * 分页查询个人p2p历史消息
     * @param sendUserId
     * @param receiveUserId
     * @param page
     * @param size
     * @param searchDate  查询那一天的消息 例如（20210223）
     * @return
     */
    List<ChatMsgCache> getSendCacheChatMsg(Long sendUserId,Long receiveUserId,Integer page,Integer size,String searchDate);

}
