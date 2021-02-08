package com.by.im.common.route.api.vo.req;


import com.by.im.common.req.BaseRequest;

public class ChatReqVO extends BaseRequest {

    private Long userId ;

    private String msg ;
    public ChatReqVO() {
    }

    public ChatReqVO(Long userId, String msg) {
        this.userId = userId;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "GroupReqVO{" +
                "userId=" + userId +
                ", msg='" + msg + '\'' +
                "} " + super.toString();
    }
}
