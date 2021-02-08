package com.by.im.android.api;


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
     * @param toUser 消息发送目标用户Id
     */
    void sendStringMsg(long toUser, StringMsgType type, String msg);

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

    Logger getLogger();

    void setLogger(Logger logger);

    IMClientConfig getConfig();

    void setConfig(IMClientConfig config);

    MsgCallBackListener getMsgCallBackListener();

    void setMsgCallBackListener(MsgCallBackListener msgCallBackListener);

}
