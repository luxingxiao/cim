package com.crossoverjie.cim.route.api.vo.res;

public class ChatMsgCacheResVO {

    //发送人id
    private Long sendUserId;
    //接收人id
    private Long receiveUserId;
    //消息
    private String msg;
    //发送人名称
    private String sendUserName;
    //请求时间的 时间戳 精确到毫秒
    private Long timeStamp;

    public ChatMsgCacheResVO() {
    }


    public ChatMsgCacheResVO(Long sendUserId, Long receiveUserId, String msg, String sendUserName, Long timeStamp) {
        this.sendUserId = sendUserId;
        this.receiveUserId = receiveUserId;
        this.msg = msg;
        this.sendUserName = sendUserName;
        this.timeStamp = timeStamp;
    }

    public Long getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(Long sendUserId) {
        this.sendUserId = sendUserId;
    }

    public Long getReceiveUserId() {
        return receiveUserId;
    }

    public void setReceiveUserId(Long receiveUserId) {
        this.receiveUserId = receiveUserId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSendUserName() {
        return sendUserName;
    }

    public void setSendUserName(String sendUserName) {
        this.sendUserName = sendUserName;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "ChatMsgCache{" +
                "sendUserId=" + sendUserId +
                ", receiveUserId=" + receiveUserId +
                ", msg='" + msg + '\'' +
                ", sendUserName='" + sendUserName + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
