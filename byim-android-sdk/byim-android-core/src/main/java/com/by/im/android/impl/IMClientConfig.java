package com.by.im.android.impl;

import com.by.im.android.impl.service.impl.ClientHeartBeatHandlerImpl;
import com.by.im.common.constant.Constants;
import com.by.im.common.kit.HeartBeatHandler;
import com.by.im.common.protocol.CIMRequestProto;

import java.util.concurrent.*;

public class IMClientConfig {
    private static IMClientConfig imClientConfig;
    private HeartBeatHandler heartBeatHandler;
    private CIMRequestProto.CIMReqProtocol cimRequestProto;
    private int threadQueueSize;
    private int threadPoolSize;
    private IMClientConfig() {

    }
    public static IMClientConfig getInstance(int threadQueueSize, int threadPoolSize){
        if(imClientConfig == null){
            imClientConfig = new IMClientConfig();
        }
        return imClientConfig;
    }
    public CIMRequestProto.CIMReqProtocol heartBeat(long userId) {
        if(cimRequestProto == null) {
            cimRequestProto = CIMRequestProto.CIMReqProtocol.newBuilder()
                    .setRequestId(userId)
                    .setReqMsg("ping")
                    .setType(Constants.CommandType.PING)
                    .build();
        }
        return cimRequestProto;

    }

    public ThreadPoolExecutor buildCallerThread(){
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue(queueSize);
        ThreadFactory product = new ThreadFactoryBuilder()
                .setNameFormat("msg-callback-%d")
                .setDaemon(true)
                .build();
        ThreadPoolExecutor productExecutor = new ThreadPoolExecutor(poolSize, poolSize, 1, TimeUnit.MILLISECONDS, queue,product);
        return  productExecutor ;
    }

    public ScheduledExecutorService buildSchedule(){
        ThreadFactory sche = new ThreadFactoryBuilder()
                .setNameFormat("reConnect-job-%d")
                .setDaemon(true)
                .build();
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1,sche) ;
        return scheduledExecutorService ;
    }

    public MsgHandleCaller buildCaller(){
        MsgHandleCaller caller = new MsgHandleCaller(new MsgCallBackListener()) ;

        return caller ;
    }

    public RingBufferWheel bufferWheel(){
        ExecutorService executorService = Executors.newFixedThreadPool(2) ;
        return new RingBufferWheel(executorService) ;
    }

    public HeartBeatHandler heartBeatHandler() {
        if(heartBeatHandler == null ) {
            heartBeatHandler = new ClientHeartBeatHandlerImpl();
        }
        return heartBeatHandler;
    }
}
