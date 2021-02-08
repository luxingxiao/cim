package com.by.im.common.util;


import com.by.im.common.exception.CIMException;
import com.by.im.common.pojo.RouteInfo;

import static com.by.im.common.enums.StatusEnum.VALIDATION_FAIL;

public class RouteInfoParseUtil {

    public static RouteInfo parse(String info){
        try {
            String[] serverInfo = info.split(":");
            RouteInfo routeInfo =  new RouteInfo(serverInfo[0], Integer.parseInt(serverInfo[1]),Integer.parseInt(serverInfo[2])) ;
            return routeInfo ;
        }catch (Exception e){
            throw new CIMException(VALIDATION_FAIL) ;
        }
    }
}
