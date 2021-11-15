package com.actionworks.flashsale.app.event.handler;


import com.actionworks.flashsale.config.annotion.BetaTrace;
import com.actionworks.flashsale.domain.event.FlashOrderEvent;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventHandler
public class FlashIOrderEventHandler implements EventHandlerI<Response, FlashOrderEvent> {
    private final Logger logger = LoggerFactory.getLogger(FlashIOrderEventHandler.class);

    @Override
    @BetaTrace
    public Response execute(FlashOrderEvent flashOrderEvent) {
        logger.info("orderEvent|接收订单事件|{}", JSON.toJSON(flashOrderEvent));
        if (flashOrderEvent.getOrderId() == null) {
            logger.info("orderEvent|订单事件参数错误");
            return Response.buildSuccess();
        }

        return Response.buildSuccess();
    }
}
