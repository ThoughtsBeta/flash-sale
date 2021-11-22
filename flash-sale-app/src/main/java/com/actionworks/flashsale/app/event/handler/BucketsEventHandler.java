package com.actionworks.flashsale.app.event.handler;


import com.actionworks.flashsale.domain.event.StockBucketEvent;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventHandler
public class BucketsEventHandler implements EventHandlerI<Response, StockBucketEvent> {
    private final Logger logger = LoggerFactory.getLogger(BucketsEventHandler.class);

    @Override
    public Response execute(StockBucketEvent stockBucketEvent) {
        logger.info("execute|接收到分桶事件|{}", JSON.toJSON(stockBucketEvent));
        return Response.buildSuccess();
    }
}
