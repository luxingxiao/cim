package com.by.im.android.api;


import com.by.im.android.vo.req.LoginReqVO;

public interface IMClient {
    /**
     * 客户端登录
     * @param userId 用户id
     * @param userName 用户名
     * @throws Exception
     */
    void login(long userId, String userName) throws Exception;

    /**
     * 向特定的用户发送文本消息
     * @param msg 文本消息内容
     * @param userId
     */
    void sendStringMsg(StringMsgType type, String msg, long userId);

    /**
     * 客戶端
     * @throws Exception
     */
    void reconnect() throws Exception;


    /**
     * 关闭客户端
     */
    void close() throws InterruptedException;

    /**
     * 获取客户端信息
     * @return
     */
    ClientInfo getClientInfo() throws IllegalStateException;

    /**
     * 单例模式
     * @return 获取单例客户端
     */
    IMClient getInstance();
}
