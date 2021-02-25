package com.crossoverjie.cim.server.server;

import com.alibaba.fastjson.JSON;
import com.crossoverjie.cim.common.constant.Constants;
import com.crossoverjie.cim.common.pojo.ChatMsgCache;
import com.crossoverjie.cim.common.protocol.CIMRequestProto;
import com.crossoverjie.cim.common.util.StringUtil;
import com.crossoverjie.cim.server.api.vo.req.SendMsgReqVO;
import com.crossoverjie.cim.server.init.CIMServerInitializer;
import com.crossoverjie.cim.server.util.SessionSocketHolder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 21/05/2018 00:30
 * @since JDK 1.8
 */
@Component
public class CIMServer {

    private final static Logger LOGGER = LoggerFactory.getLogger(CIMServer.class);

    private EventLoopGroup boss = new NioEventLoopGroup();
    private EventLoopGroup work = new NioEventLoopGroup();


    @Value("${cim.server.port}")
    private int nettyPort;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    /**
     * 启动 cim server
     *
     * @return
     * @throws InterruptedException
     */
    @PostConstruct
    public void start() throws InterruptedException {

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(boss, work)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(nettyPort))
                //保持长连接
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new CIMServerInitializer());

        ChannelFuture future = bootstrap.bind().sync();
        if (future.isSuccess()) {
            LOGGER.info("Start cim server success!!!");
        }
    }


    /**
     * 销毁
     */
    @PreDestroy
    public void destroy() {
        boss.shutdownGracefully().syncUninterruptibly();
        work.shutdownGracefully().syncUninterruptibly();
        LOGGER.info("Close cim server success!!!");
    }


    /**
     * Push msg to client.
     * @param sendMsgReqVO 消息
     */
    public void sendMsg(SendMsgReqVO sendMsgReqVO){
        NioSocketChannel socketChannel = SessionSocketHolder.get(sendMsgReqVO.getUserId());

        if (null == socketChannel) {
            LOGGER.error("client {} offline!", sendMsgReqVO.getUserId());
            return;
        }
        CIMRequestProto.CIMReqProtocol protocol = CIMRequestProto.CIMReqProtocol.newBuilder()
                .setRequestId(sendMsgReqVO.getUserId())
                .setReqMsg(sendMsgReqVO.getMsg())
                .setType(Constants.CommandType.MSG)
                .setTimeStamp(sendMsgReqVO.getSendMsgTime())
                .build();

        ChannelFuture future = socketChannel.writeAndFlush(protocol);
        future.addListener((ChannelFutureListener) channelFuture ->
                LOGGER.info("server push msg:[{}]", sendMsgReqVO.toString()));
    }

    /**
     * 发送离线消息
     * @param receiveUserId
     */
    public void sendOfflineMsg(Long receiveUserId){
        String key = "cim-receive-cache" + receiveUserId;
        List<Object> objectList = redisTemplate.opsForHash().values(key);
        if (null == objectList || objectList.isEmpty()){
//            LOGGER.info("用户id:[{}]无离线消息",receiveUserId);
            return;
        }
        for (Object o : objectList) {
            String values = (String) o;
            if (StringUtil.isEmpty(values)){
                continue;
            }
            List<ChatMsgCache> list = JSON.parseArray(values,ChatMsgCache.class);
            String hashKey = list.get(0).getSendUserId().toString();
            Iterator<ChatMsgCache> iterator = list.iterator();
            while (iterator.hasNext()){
                ChatMsgCache chatMsgCache = iterator.next();
                NioSocketChannel socketChannel = SessionSocketHolder.get(receiveUserId);
                //客户端在线
                if (null != socketChannel){
                    CIMRequestProto.CIMReqProtocol protocol = CIMRequestProto.CIMReqProtocol.newBuilder()
                            .setRequestId(receiveUserId)
                            .setReqMsg(chatMsgCache.getSendUserName() + ":" + chatMsgCache.getMsg())
                            .setType(Constants.CommandType.MSG)
                            .setTimeStamp(chatMsgCache.getTimeStamp())
                            .build();

                    ChannelFuture future = socketChannel.writeAndFlush(protocol);
                    future.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            LOGGER.info("server push offline msg:[{}]", chatMsgCache.toString());
                            iterator.remove();
                        }
                    });
                }
            }
            if (list.size() <= 0){
                //离线消息均发送成功，删除离线缓存消息
                LOGGER.info("删除离线缓存消息key[{}],hashKey[{}]",key,hashKey);
                redisTemplate.opsForHash().delete(key,hashKey);
            }else {
                //离线消息部分未发送成功
                redisTemplate.opsForHash().put(key,list.get(0).getSendUserId().toString(),JSON.toJSONString(list));
                redisTemplate.expire(key,3,TimeUnit.DAYS);
                LOGGER.info("重新保存用户id[{}]未接收成功的离线消息[{}]",receiveUserId,list);
            }
        }


    }

    private String getTodayDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMdd");
        return simpleDateFormat.format(new Date());
    }


}
