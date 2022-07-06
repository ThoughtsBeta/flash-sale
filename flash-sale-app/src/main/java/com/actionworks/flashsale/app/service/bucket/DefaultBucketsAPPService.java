package com.actionworks.flashsale.app.service.bucket;

import com.actionworks.flashsale.app.exception.AppException;
import com.actionworks.flashsale.app.exception.BizException;
import com.actionworks.flashsale.app.model.command.BucketsArrangementCommand;
import com.actionworks.flashsale.app.model.dto.StockBucketSummaryDTO;
import com.actionworks.flashsale.app.model.result.AppSimpleResult;
import com.actionworks.flashsale.domain.model.entity.FlashItem;
import com.actionworks.flashsale.domain.service.FlashItemDomainService;
import com.actionworks.flashsale.lock.DistributedLock;
import com.actionworks.flashsale.lock.DistributedLockFactoryService;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.actionworks.flashsale.app.exception.AppErrorCode.ARRANGE_STOCK_BUCKETS_FAILED;
import static com.actionworks.flashsale.app.exception.AppErrorCode.BUSINESS_ERROR;
import static com.actionworks.flashsale.app.exception.AppErrorCode.FREQUENTLY_ERROR;
import static com.actionworks.flashsale.app.exception.AppErrorCode.ITEM_NOT_FOUND;
import static com.actionworks.flashsale.app.exception.AppErrorCode.QUERY_STOCK_BUCKETS_FAILED;
import static com.actionworks.flashsale.util.StringUtil.link;

@Service
@ConditionalOnProperty(name = "place_order_type", havingValue = "buckets", matchIfMissing = true)
public class DefaultBucketsAPPService implements BucketsAPPService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultBucketsAPPService.class);
    private static final String STOCK_BUCKET_ARRANGEMENT_KEY = "STOCK_BUCKET_ARRANGEMENT_KEY";

    @Resource
    private FlashItemDomainService flashItemDomainService;

    @Resource
    private DistributedLockFactoryService lockFactoryService;

    @Resource
    private BucketsArrangementService bucketsArrangementService;

    @Override
    public AppSimpleResult arrangeStockBuckets(Long userId, Long itemId, BucketsArrangementCommand arrangementCommand) {
        logger.info("arrangeBuckets|编排库存分桶|{},{},{}", userId, itemId, JSON.toJSON(arrangementCommand));
        String arrangementKey = getArrangementKey(userId, itemId);
        DistributedLock arrangementLock = lockFactoryService.getDistributedLock(arrangementKey);
        try {
            boolean isLockSuccess = arrangementLock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!isLockSuccess) {
                return AppSimpleResult.failed(FREQUENTLY_ERROR.getErrCode(), FREQUENTLY_ERROR.getErrDesc());
            }
            FlashItem flashItem = flashItemDomainService.getFlashItem(itemId);
            if (flashItem == null) {
                throw new BizException(ITEM_NOT_FOUND.getErrDesc());
            }
            bucketsArrangementService.arrangeStockBuckets(itemId, arrangementCommand.getTotalStocksAmount(),
                    arrangementCommand.getBucketsQuantity(), arrangementCommand.getArrangementMode());

            logger.info("arrangeBuckets|库存编排完成|{}", itemId);
            return AppSimpleResult.ok(true);
        } catch (AppException e) {
            logger.error("arrangeBuckets|库存编排失败|{}", itemId, e);
            return AppSimpleResult.failed(BUSINESS_ERROR.getErrCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("arrangeBuckets|库存编排错误|{}", itemId, e);
            return AppSimpleResult.failed(ARRANGE_STOCK_BUCKETS_FAILED);
        } finally {
            arrangementLock.unlock();
        }
    }

    @Override
    public AppSimpleResult<StockBucketSummaryDTO> getStockBucketsSummary(Long userId, Long itemId) {
        logger.info("stockBucketsSummary|获取库存分桶数据|{},{}", userId, itemId);
        try {
            StockBucketSummaryDTO stockBucketSummaryDTO = bucketsArrangementService.queryStockBucketsSummary(itemId);
            return AppSimpleResult.ok(stockBucketSummaryDTO);
        } catch (BizException e) {
            logger.error("stockBucketsSummary|获取库存分桶数据失败|{}", itemId, e);
            return AppSimpleResult.failed(QUERY_STOCK_BUCKETS_FAILED);
        } catch (Exception e) {
            logger.error("stockBucketsSummary|获取库存分桶数据错误|{}", itemId, e);
            return AppSimpleResult.failed(QUERY_STOCK_BUCKETS_FAILED);
        }
    }

    private String getArrangementKey(Long userId, Long itemId) {
        return link(STOCK_BUCKET_ARRANGEMENT_KEY, userId, itemId);
    }
}
