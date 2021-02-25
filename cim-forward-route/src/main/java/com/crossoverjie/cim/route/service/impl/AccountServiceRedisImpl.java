package com.crossoverjie.cim.route.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.crossoverjie.cim.common.core.proxy.ProxyManager;
import com.crossoverjie.cim.common.enums.StatusEnum;
import com.crossoverjie.cim.common.exception.CIMException;
import com.crossoverjie.cim.common.pojo.CIMUserInfo;
import com.crossoverjie.cim.common.pojo.ChatMsgCache;
import com.crossoverjie.cim.common.util.RouteInfoParseUtil;
import com.crossoverjie.cim.common.util.StringUtil;
import com.crossoverjie.cim.route.api.vo.req.ChatReqVO;
import com.crossoverjie.cim.route.api.vo.req.LoginReqVO;
import com.crossoverjie.cim.route.api.vo.req.P2PReqVO;
import com.crossoverjie.cim.route.api.vo.res.CIMServerResVO;
import com.crossoverjie.cim.route.api.vo.res.RegisterInfoResVO;
import com.crossoverjie.cim.route.service.AccountService;
import com.crossoverjie.cim.route.service.UserInfoCacheService;
import com.crossoverjie.cim.server.api.ServerApi;
import com.crossoverjie.cim.server.api.vo.req.SendMsgReqVO;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.crossoverjie.cim.common.enums.StatusEnum.OFF_LINE;
import static com.crossoverjie.cim.route.constant.Constant.*;

/**
 * @author luxingxiao
 */
@Service
public class AccountServiceRedisImpl implements AccountService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AccountServiceRedisImpl.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserInfoCacheService userInfoCacheService;

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public RegisterInfoResVO register(RegisterInfoResVO info) {
        String key = ACCOUNT_PREFIX + info.getUserId();

        String name = redisTemplate.opsForValue().get(info.getUserName());
        if (null == name) {
            //为了方便查询，冗余一份
            redisTemplate.opsForValue().set(key, info.getUserName());
            redisTemplate.opsForValue().set(info.getUserName(), key);
        } else {
            long userId = Long.parseLong(name.split(":")[1]);
            info.setUserId(userId);
            info.setUserName(info.getUserName());
        }

        return info;
    }

    @Override
    public StatusEnum login(LoginReqVO loginReqVO) throws Exception {
        //再去Redis里查询
        String key = ACCOUNT_PREFIX + loginReqVO.getUserId();
        String userName = redisTemplate.opsForValue().get(key);
        if (null == userName) {
            return StatusEnum.ACCOUNT_NOT_MATCH;
        }

        if (!userName.equals(loginReqVO.getUserName())) {
            return StatusEnum.ACCOUNT_NOT_MATCH;
        }

        //登录成功，保存登录状态
        boolean status = userInfoCacheService.saveAndCheckUserLoginStatus(loginReqVO.getUserId());
        if (status == false) {
            //重复登录
            return StatusEnum.REPEAT_LOGIN;
        }

        return StatusEnum.SUCCESS;
    }

    @Override
    public void saveRouteInfo(LoginReqVO loginReqVO, String msg, String topic) throws Exception {
        String key = ROUTE_PREFIX + loginReqVO.getUserId();
        redisTemplate.opsForValue().set(key, msg);
        String topicKey = TOPIC_ROUTE_PREFIX + loginReqVO.getUserId();
        redisTemplate.opsForValue().set(topicKey, topic);
    }

    @Override
    public Map<Long, CIMServerResVO> loadRouteRelated() {

        Map<Long, CIMServerResVO> routes = new HashMap<>(64);


        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        ScanOptions options = ScanOptions.scanOptions()
                .match(ROUTE_PREFIX + "*")
                .build();
        Cursor<byte[]> scan = connection.scan(options);

        while (scan.hasNext()) {
            byte[] next = scan.next();
            String key = new String(next, StandardCharsets.UTF_8);
            LOGGER.info("key={}", key);
            parseServerInfo(routes, key);

        }
        try {
            scan.close();
        } catch (IOException e) {
            LOGGER.error("IOException", e);
        }

        return routes;
    }

    @Override
    public CIMServerResVO loadRouteRelatedByUserId(Long userId) {
        String value = redisTemplate.opsForValue().get(ROUTE_PREFIX + userId);

        if (value == null) {
            return null;
//            throw new CIMException(OFF_LINE);
        }

        CIMServerResVO cimServerResVO = new CIMServerResVO(RouteInfoParseUtil.parse(value));
        return cimServerResVO;
    }

    @Override
    public String loadRouteTopicByUserId(Long userId) {
        return redisTemplate.opsForValue().get(TOPIC_ROUTE_PREFIX + userId);
    }

    private void parseServerInfo(Map<Long, CIMServerResVO> routes, String key) {
        long userId = Long.valueOf(key.split(":")[1]);
        String value = redisTemplate.opsForValue().get(key);
        CIMServerResVO cimServerResVO = new CIMServerResVO(RouteInfoParseUtil.parse(value));
        routes.put(userId, cimServerResVO);
    }


    @Override
    public void pushMsg(CIMServerResVO cimServerResVO, long sendUserId, ChatReqVO groupReqVO) throws Exception {
        CIMUserInfo cimUserInfo = userInfoCacheService.loadUserInfoByUserId(sendUserId);

        String url = "http://" + cimServerResVO.getIp() + ":" + cimServerResVO.getHttpPort();
        ServerApi serverApi = new ProxyManager<>(ServerApi.class, url, okHttpClient).getInstance();
        SendMsgReqVO vo = new SendMsgReqVO(cimUserInfo.getUserName() + ":" + groupReqVO.getMsg(), groupReqVO.getUserId());
        //先在route层设置消息发送时间,后期client层传入
        vo.setSendMsgTime(System.currentTimeMillis());
        Response response = null;
        try {
            response = (Response) serverApi.sendMsg(vo);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        } finally {
            response.body().close();
        }
    }

    @Override
    public void pushMsg(String topic, long sendUserId, ChatReqVO chatReqVO) {
        CIMUserInfo cimUserInfo = userInfoCacheService.loadUserInfoByUserId(sendUserId);
        SendMsgReqVO vo = new SendMsgReqVO(cimUserInfo.getUserName() + ":" + chatReqVO.getMsg(), chatReqVO.getUserId());
        if(topic == null){
            LOGGER.warn("用戶 {} 不在线", chatReqVO.getUserId());
            //TODO 处理离线消息
        }else {
            rocketMQTemplate.asyncSend(topic, vo, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {

                }

                @Override
                public void onException(Throwable throwable) {
                    LOGGER.error("消息推送失败", throwable);
                }
            });
        }
    }

    @Override
    public void offLine(Long userId) throws Exception {

        // TODO: 2019-01-21 改为一个原子命令，以防数据一致性

        //删除路由
        redisTemplate.delete(ROUTE_PREFIX + userId);

        //删除topic路由
        redisTemplate.delete(TOPIC_ROUTE_PREFIX + userId);

        //删除登录状态
        userInfoCacheService.removeLoginStatus(userId);
    }

    @Override
    public void receiveCacheChatMsg(P2PReqVO p2pRequest) {

        CIMUserInfo cimUserInfo = userInfoCacheService.loadUserInfoByUserId(p2pRequest.getUserId());
        String todayDate = getTodayDate();
        String key = "receive_"+todayDate+"_"+p2pRequest.getReceiveUserId();
        String values = (String)redisTemplate.opsForHash().get(key,p2pRequest.getUserId().toString());
        List<ChatMsgCache> list = new ArrayList<>();
        ChatMsgCache chatMsgCache = new ChatMsgCache(p2pRequest.getUserId(),p2pRequest.getReceiveUserId(),p2pRequest.getMsg(),cimUserInfo.getUserName(),System.currentTimeMillis());
        if (StringUtil.isEmpty(values)){
            list.add(chatMsgCache);
        }else {
            list = JSON.parseArray(values,ChatMsgCache.class);
            list.add(chatMsgCache);
        }
        redisTemplate.opsForHash().put(key,p2pRequest.getUserId().toString(),JSON.toJSONString(list));
        redisTemplate.expire(key,3,TimeUnit.DAYS);
        LOGGER.info("receiveCacheChatMsg缓存离线消息key:[{}],value增加:[{}]",key,chatMsgCache.toString());
    }

    @Override
    public void sendCacheChatMsg(P2PReqVO p2pRequest) {

        CIMUserInfo cimUserInfo = userInfoCacheService.loadUserInfoByUserId(p2pRequest.getUserId());
        String todayDate = getTodayDate();
        String key = "send_"+todayDate+"_"+p2pRequest.getUserId();
        List<ChatMsgCache> list = new ArrayList<>();
        ChatMsgCache chatMsgCache = new ChatMsgCache(p2pRequest.getUserId(),p2pRequest.getReceiveUserId(),p2pRequest.getMsg(),cimUserInfo.getUserName(),System.currentTimeMillis());
        String values = (String) redisTemplate.opsForHash().get(key, p2pRequest.getReceiveUserId().toString());
        if (StringUtil.isEmpty(values)){
            list.add(chatMsgCache);
        }else {
            list = JSON.parseArray(values,ChatMsgCache.class);
            list.add(chatMsgCache);
        }
        redisTemplate.opsForHash().put(key,p2pRequest.getReceiveUserId().toString(),JSON.toJSONString(list));
        redisTemplate.expire(key,3,TimeUnit.DAYS);
//        LOGGER.info("sendCacheChatMsg缓存个人历史消息key:[{}],value增加:[{}]",key,chatMsgCache.toString());
    }

    @Override
    public List<ChatMsgCache> getSendCacheChatMsg(Long sendUserId, Long receiveUserId, Integer page, Integer size,String searchDate) {
        if (page<=0 || size<=0){
            LOGGER.info("getSendCacheChatMsg参数错误,page[{}]size[{}]",page,size);
            return null;
        }
        String key = "send_"+searchDate+"_"+sendUserId;
        String values = (String) redisTemplate.opsForHash().get(key,receiveUserId.toString());
        if (StringUtil.isEmpty(values)){
            return null;
        }
        List<ChatMsgCache> list = JSON.parseArray(values,ChatMsgCache.class);
        //将消息倒序，看具体需求
//        Collections.reverse(list);
        int startIndex = (page-1)*size;
        if (startIndex > list.size()){
            return null;
        }
        int endIndex = page*size-1 < list.size() ? page*size-1 : list.size()-1;
        List<ChatMsgCache> resultList = new ArrayList<>();
//        System.out.println("开始index"+startIndex+",结束index"+endIndex);
        for (int i=startIndex; i<=endIndex; i++){
            resultList.add(list.get(i));
        }
        return resultList;
    }

    private String getTodayDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMdd");
        return simpleDateFormat.format(new Date());
    }



}
