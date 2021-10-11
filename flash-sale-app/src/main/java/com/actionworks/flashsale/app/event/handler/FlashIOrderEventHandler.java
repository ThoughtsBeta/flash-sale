package com.actionworks.flashsale.app.event.handler;


import com.actionworks.flashsale.app.cache.ItemStockCacheService;
import com.actionworks.flashsale.domain.event.FlashOrderEvent;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@EventHandler
public class FlashIOrderEventHandler implements EventHandlerI<Response, FlashOrderEvent> {
    private final Logger logger = LoggerFactory.getLogger(FlashIOrderEventHandler.class);

    @Resource
    private ItemStockCacheService itemStockCacheService;

    @Override
    public Response execute(FlashOrderEvent flashOrderEvent) {
        logger.info("Receiving order event: " + JSON.toJSON(flashOrderEvent));
        if (flashOrderEvent.getOrderId() == null) {
            logger.info("Received order event params invalid: " + JSON.toJSON(flashOrderEvent));
            return Response.buildSuccess();
        }

        return Response.buildSuccess();
    }
}
