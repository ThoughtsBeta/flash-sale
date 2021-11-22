package com.actionworks.flashsale.app.event.handler;


import com.actionworks.flashsale.app.service.activity.cache.FlashActivitiesCacheService;
import com.actionworks.flashsale.app.service.activity.cache.FlashActivityCacheService;
import com.actionworks.flashsale.config.annotion.BetaTrace;
import com.actionworks.flashsale.domain.event.FlashActivityEvent;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@EventHandler
public class FlashActivityEventHandler implements EventHandlerI<Response, FlashActivityEvent> {
    private final Logger logger = LoggerFactory.getLogger(FlashActivityEventHandler.class);

    @Resource
    private FlashActivityCacheService flashActivityCacheService;
    @Resource
    private FlashActivitiesCacheService flashActivitiesCacheService;

    @Override
    @BetaTrace
    public Response execute(FlashActivityEvent flashActivityEvent) {
        logger.info("activityEvent|接收活动事件|{}", JSON.toJSON(flashActivityEvent));
        if (flashActivityEvent.getId() == null) {
            logger.info("activityEvent|事件参数错误|{}");
            return Response.buildSuccess();
        }

        flashActivityCacheService.tryToUpdateActivityCacheByLock(flashActivityEvent.getId());
        flashActivitiesCacheService.tryToUpdateActivitiesCacheByLock(1);
        return Response.buildSuccess();
    }
}
