package com.by.im.common.route.api.vo.req;


import com.by.im.common.req.BaseRequest;

public class RegisterInfoReqVO extends BaseRequest {

    private String userName ;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "RegisterInfoReqVO{" +
                "userName='" + userName + '\'' +
                "} " + super.toString();
    }
}
