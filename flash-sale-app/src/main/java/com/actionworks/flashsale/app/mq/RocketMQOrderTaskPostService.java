package com.actionworks.flashsale.app.mq;

import com.actionworks.flashsale.app.model.PlaceOrderTask;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@ConditionalOnProperty(name = "place_order_type", havingValue = "queued")
public class RocketMQOrderTaskPostService implements OrderTaskPostService {
    private static final Logger logger = LoggerFactory.getLogger(RocketMQOrderTaskPostService.class);

    @Value("${rocketmq.name-server}")
    private String nameServer;

    @Value("${rocketmq.placeorder.producer.group}")
    private String producerGroup;

    @Value("${rocketmq.placeorder.topic}")
    private String placeOrderTopic;

    private DefaultMQProducer placeOrderMQProducer;

    @PostConstruct
    public void init() {
        try {
            placeOrderMQProducer = new DefaultMQProducer(producerGroup);
            placeOrderMQProducer.setNamesrvAddr(nameServer);
            placeOrderMQProducer.start();
            logger.info("Place order task producer started successfully.");
        } catch (Exception e) {
            logger.error("Place order task producer start failed.", e);
        }
    }

    public boolean post(PlaceOrderTask placeOrderTask) {
        if (placeOrderTask == null) {
            logger.info("PlaceOrder task message params invalid:{},{}", JSON.toJSON(placeOrderTask));
            return false;
        }
        String placeOrderTaskString = JSON.toJSONString(placeOrderTask);
        Message message = new Message();
        message.setTopic(placeOrderTopic);
        message.setBody(placeOrderTaskString.getBytes());
        try {
            SendResult sendResult = placeOrderMQProducer.send(message);
            if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                logger.info("PlaceOrder task  message sent successfully:{}", sendResult.getMsgId());
                return true;
            } else {
                logger.info("PlaceOrder task  message sent failed:{},{}", placeOrderTaskString, JSON.toJSONString(sendResult));
                return false;
            }
        } catch (Exception e) {
            logger.error("PlaceOrder task  message sent failed:{},{}", placeOrderTaskString, e);
            return false;
        }
    }
}
