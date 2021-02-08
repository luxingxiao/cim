package com.by.im.android.impl.service;

import org.springframework.stereotype.Component;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 2019-02-27 16:17
 * @since JDK 1.8
 */
@Component
public class ShutDownMsg {
    private boolean isCommand ;

    /**
     * 置为用户主动退出状态
     */
    public void shutdown(){
        isCommand = true ;
    }

    public boolean checkStatus(){
        return isCommand ;
    }
}
