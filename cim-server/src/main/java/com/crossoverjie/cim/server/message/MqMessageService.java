package com.crossoverjie.cim.server.message;

import com.crossoverjie.cim.server.api.vo.req.SendMsgReqVO;
import com.crossoverjie.cim.server.server.CIMServer;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author luxingxiao
 */
@Service
@RocketMQMessageListener(consumerGroup = "${im.message.consumer-group}", topic = "${im.message.topic}")
public class MqMessageService implements RocketMQListener<SendMsgReqVO> {
    @Autowired
    private CIMServer cimServer;
    @Override
    public void onMessage(SendMsgReqVO sendMsgReqVO) {
        cimServer.sendMsg(sendMsgReqVO);
    }
}
