package com.by.im.android.impl;

import com.by.im.android.api.IMClientFactory;
import com.by.im.android.api.Logger;
import com.by.im.android.impl.service.ReConnectManager;
import com.by.im.android.impl.service.RouteRequest;
import com.by.im.android.impl.service.ShutDownMsg;
import com.by.im.android.impl.service.impl.ClientHeartBeatHandlerImpl;
import com.by.im.android.impl.service.impl.RouteRequestImpl;
import com.by.im.common.constant.Constants;
import com.by.im.common.construct.RingBufferWheel;
import com.by.im.common.kit.HeartBeatHandler;
import com.by.im.common.protocol.CIMRequestProto;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import okhttp3.OkHttpClient;

import java.util.concurrent.*;

public class BeanFactory {
    private static BeanFactory beanFactory = new BeanFactory();
    private HeartBeatHandler heartBeatHandler;
    private CIMRequestProto.CIMReqProtocol cimRequestProto;
    private ThreadPoolExecutor threadPoolExecutor;
    private ScheduledExecutorService scheduledExecutorService;
    private RingBufferWheel bufferWheel;
    private RouteRequest routeRequest;
    private ReConnectManager reConnectManager;
    private ShutDownMsg shutDownMsg;
    private OkHttpClient okHttpClient;

    private BeanFactory() {

    }

    public static BeanFactory getInstance() {
        return beanFactory;
    }


    public OkHttpClient okHttpClient() {
        if (okHttpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(IMClientFactory.getIMClient().getConfig().getOkHttpConnectTimeout(), TimeUnit.SECONDS)
                    .readTimeout(IMClientFactory.getIMClient().getConfig().getOkHttpReadTimeout(), TimeUnit.SECONDS)
                    .writeTimeout(IMClientFactory.getIMClient().getConfig().getOkHttpWriteTimeout(), TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true);
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }

    public ShutDownMsg shutDownMsg() {
        if (shutDownMsg == null) {
            shutDownMsg = new ShutDownMsg();
        }
        return shutDownMsg;
    }

    public ReConnectManager reConnectManager() {
        if (reConnectManager == null) {
            reConnectManager = new ReConnectManager();
        }
        return reConnectManager;
    }

    public RouteRequest routeRequest(Logger logger) {
        if (routeRequest == null) {
            routeRequest = new RouteRequestImpl(logger);
        }
        return routeRequest;
    }

    public CIMRequestProto.CIMReqProtocol heartBeatProtocol(long userId) {
        if (cimRequestProto == null) {
            cimRequestProto = CIMRequestProto.CIMReqProtocol.newBuilder()
                    .setRequestId(userId)
                    .setReqMsg("ping")
                    .setType(Constants.CommandType.PING)
                    .build();
        }
        return cimRequestProto;

    }

    public ThreadPoolExecutor threadPoolExecutor() {
        int queueSize = IMClientFactory.getIMClient().getConfig().getCallBackThreadPoolQueueSize();
        int poolSize = IMClientFactory.getIMClient().getConfig().getCallBackThreadPoolSize();
        if (threadPoolExecutor == null) {
            BlockingQueue<Runnable> queue = new LinkedBlockingQueue(queueSize);
            ThreadFactory product = new ThreadFactoryBuilder()
                    .setNameFormat("msg-callback-%d")
                    .setDaemon(true)
                    .build();
            threadPoolExecutor = new ThreadPoolExecutor(poolSize, poolSize, 1, TimeUnit.MILLISECONDS, queue, product);
        }
        return threadPoolExecutor;
    }

    public ScheduledExecutorService scheduledExecutorService() {
        if (scheduledExecutorService == null) {
            ThreadFactory sche = new ThreadFactoryBuilder()
                    .setNameFormat("reConnect-job-%d")
                    .setDaemon(true)
                    .build();
            scheduledExecutorService = new ScheduledThreadPoolExecutor(1, sche);
        }
        return scheduledExecutorService;
    }

    public RingBufferWheel bufferWheel() {
        if (bufferWheel == null) {
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            bufferWheel = new RingBufferWheel(executorService);
        }
        return bufferWheel;
    }

    public HeartBeatHandler heartBeatHandler() {
        if (heartBeatHandler == null) {
            heartBeatHandler = new ClientHeartBeatHandlerImpl();
        }
        return heartBeatHandler;
    }
}
