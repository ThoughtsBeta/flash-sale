package com.actionworks.flashsale.app.mq;

import com.actionworks.flashsale.app.model.PlaceOrderTask;
import com.actionworks.flashsale.app.service.placeorder.queued.QueuedPlaceOrderService;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@RocketMQMessageListener(topic = "PLACE_ORDER_TASK_TOPIC", consumerGroup = "PLACE_ORDER_TASK_TOPIC_CONSUMER_GROUP")
@ConditionalOnProperty(name = "place_order_type", havingValue = "queued")
public class RocketMQOrderTaskConsumerService implements RocketMQListener<String> {
    private static final Logger logger = LoggerFactory.getLogger(RocketMQOrderTaskConsumerService.class);

    @Resource
    private QueuedPlaceOrderService queuedPlaceOrderService;

    @Override
    public void onMessage(String message) {
        logger.info("handleOrderTask|接收下单任务消息|{}", message);
        if (StringUtils.isEmpty(message)) {
            logger.info("handleOrderTask|接收下单任务消息为空|{}", message);
            return;
        }
        try {
            PlaceOrderTask placeOrderTask = JSON.parseObject(message, PlaceOrderTask.class);
            queuedPlaceOrderService.handlePlaceOrderTask(placeOrderTask);
            logger.info("handleOrderTask|下单任务消息处理完成|{}", placeOrderTask.getPlaceOrderTaskId());
        } catch (Exception e) {
            logger.error("handleOrderTask|下单任务消息处理失败|{}", message);
        }
    }
}
