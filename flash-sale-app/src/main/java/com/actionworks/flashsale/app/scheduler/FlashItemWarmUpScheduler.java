package com.actionworks.flashsale.app.scheduler;

import com.actionworks.flashsale.app.service.stock.ItemStockCacheService;
import com.actionworks.flashsale.config.annotion.BetaTrace;
import com.actionworks.flashsale.domain.model.PageResult;
import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashItem;
import com.actionworks.flashsale.domain.service.FlashItemDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FlashItemWarmUpScheduler {
    private static final Logger logger = LoggerFactory.getLogger(FlashItemWarmUpScheduler.class);

    @Resource
    private ItemStockCacheService itemStockCacheService;

    @Resource
    private FlashItemDomainService flashItemDomainService;

    @Scheduled(cron = "*/5 * * * * ?")
    @BetaTrace
    public void warmUpFlashItemTask() {
        logger.info("warmUpFlashItemTask|秒杀品预热调度");
        PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
        pagesQueryCondition.setStockWarmUp(0);
        PageResult<FlashItem> pageResult = flashItemDomainService.getFlashItems(pagesQueryCondition);
        pageResult.getData().forEach(flashItem -> {
            boolean initSuccess = itemStockCacheService.alignItemStocks(flashItem.getId());
            if (!initSuccess) {
                logger.info("warmUpFlashItemTask|秒杀品库存已经初始化预热失败", flashItem.getId());
                return;
            }
            flashItem.setStockWarmUp(1);
            flashItemDomainService.publishFlashItem(flashItem);
            logger.info("warmUpFlashItemTask|秒杀品库存已经初始化预热成功", flashItem.getId());
        });
    }
}

