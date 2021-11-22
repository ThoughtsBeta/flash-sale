package com.actionworks.flashsale.app.scheduler;

import com.actionworks.flashsale.app.service.stock.ItemStockCacheService;
import com.actionworks.flashsale.config.annotion.BetaTrace;
import com.actionworks.flashsale.domain.model.PageResult;
import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashItem;
import com.actionworks.flashsale.domain.model.enums.FlashItemStatus;
import com.actionworks.flashsale.domain.service.FlashItemDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class StocksAlignScheduler {
    private static final Logger logger = LoggerFactory.getLogger(StocksAlignScheduler.class);

    @Resource
    private ItemStockCacheService itemStockCacheService;

    @Resource
    private FlashItemDomainService flashItemDomainService;

    @Scheduled(cron = "*/2 * * * * ?")
    @BetaTrace
    public void alignStocksTask() {
        logger.info("alignStocksTask|校准库存缓存开始");
        PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
        pagesQueryCondition.setStatus(FlashItemStatus.ONLINE.getCode());
        PageResult<FlashItem> pageResult = flashItemDomainService.getFlashItems(pagesQueryCondition);
        pageResult.getData().forEach(flashItem -> {
            boolean result = itemStockCacheService.alignItemStocks(flashItem.getId());
            if (!result) {
                logger.info("alignStocksTask|库存校准失败", flashItem.getId(), flashItem.getAvailableStock());
                return;
            }
            logger.info("alignStocksTask|库存校准完成", flashItem.getId(), flashItem.getAvailableStock());
        });
        logger.info("alignStocksTask|校准库存缓存结束");
    }
}

