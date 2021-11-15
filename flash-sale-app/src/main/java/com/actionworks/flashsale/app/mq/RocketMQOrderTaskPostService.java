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
            logger.info("initOrderTaskProducer|下单任务生产者初始化成功|{},{},{}", nameServer, producerGroup, placeOrderTopic);
        } catch (Exception e) {
            logger.error("initOrderTaskProducer|下单任务生产者初始化失败|{},{},{}", nameServer, producerGroup, placeOrderTopic, e);
        }
    }

    public boolean post(PlaceOrderTask placeOrderTask) {
        logger.info("postOrderTask|投递下单任务|{}", JSON.toJSONString(placeOrderTask));
        if (placeOrderTask == null) {
            logger.info("postOrderTask|投递下单任务参数错误");
            return false;
        }
        String placeOrderTaskString = JSON.toJSONString(placeOrderTask);
        Message message = new Message();
        message.setTopic(placeOrderTopic);
        message.setBody(placeOrderTaskString.getBytes());
        try {
            SendResult sendResult = placeOrderMQProducer.send(message);
            logger.info("postOrderTask|下单任务投递完成|{}", placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(sendResult));
            if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                logger.info("postOrderTask|下单任务投递成功|{}", placeOrderTask.getPlaceOrderTaskId());
                return true;
            } else {
                logger.info("postOrderTask|下单任务投递失败|{}", placeOrderTask.getPlaceOrderTaskId());
                return false;
            }
        } catch (Exception e) {
            logger.error("postOrderTask|下单任务投递错误|{}", placeOrderTask.getPlaceOrderTaskId(), e);
            return false;
        }
    }
}
