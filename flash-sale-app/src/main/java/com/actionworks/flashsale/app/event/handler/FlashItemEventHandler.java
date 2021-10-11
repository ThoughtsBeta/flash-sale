package com.actionworks.flashsale.app.event.handler;


import com.actionworks.flashsale.app.cache.FlashItemCacheService;
import com.actionworks.flashsale.app.cache.FlashItemsCacheService;
import com.actionworks.flashsale.domain.event.FlashItemEvent;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@EventHandler
public class FlashItemEventHandler implements EventHandlerI<Response, FlashItemEvent> {

    private final Logger logger = LoggerFactory.getLogger(FlashItemEventHandler.class);


    @Resource
    private FlashItemCacheService flashItemCacheService;
    @Resource
    private FlashItemsCacheService flashItemsCacheService;

    @Override
    public Response execute(FlashItemEvent flashItemEvent) {
        logger.info("Receiving item event: " + JSON.toJSON(flashItemEvent));
        if (flashItemEvent.getId() == null) {
            logger.info("Received item event params invalid: " + JSON.toJSON(flashItemEvent));
            return Response.buildSuccess();
        }

        flashItemCacheService.tryToUpdateItemCacheByLock(flashItemEvent.getId());
        flashItemsCacheService.tryToUpdateItemsCacheByLock(flashItemEvent.getFlashActivityId());
        return Response.buildSuccess();
    }
}
