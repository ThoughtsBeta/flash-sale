package com.actionworks.flashsale.app.service.bucket;

import com.actionworks.flashsale.app.exception.StockBucketException;
import com.actionworks.flashsale.app.model.builder.StockBucketBuilder;
import com.actionworks.flashsale.app.model.dto.StockBucketDTO;
import com.actionworks.flashsale.app.model.dto.StockBucketSummaryDTO;
import com.actionworks.flashsale.app.model.enums.ArrangementMode;
import com.actionworks.flashsale.cache.DistributedCacheService;
import com.actionworks.flashsale.domain.model.Bucket;
import com.actionworks.flashsale.domain.model.enums.BucketStatus;
import com.actionworks.flashsale.domain.service.BucketsDomainService;
import com.actionworks.flashsale.lock.DistributedLock;
import com.actionworks.flashsale.lock.DistributedLockFactoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.actionworks.flashsale.app.model.constants.CacheConstants.ITEM_BUCKET_AVAILABLE_STOCKS_KEY;
import static com.actionworks.flashsale.app.model.constants.CacheConstants.ITEM_STOCK_BUCKETS_SUSPEND_KEY;
import static com.actionworks.flashsale.app.service.placeorder.buckets.cache.BucketsCacheService.getItemStockBucketsQuantityCacheKey;
import static com.actionworks.flashsale.util.StringUtil.link;
import static java.util.stream.Collectors.toList;

@Service
@ConditionalOnProperty(name = "place_order_type", havingValue = "buckets", matchIfMissing = true)
public class DefaultBucketsArrangementService implements BucketsArrangementService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultBucketsArrangementService.class);

    @Resource
    protected BucketsDomainService bucketsDomainService;
    @Resource
    private DistributedLockFactoryService lockFactoryService;
    @Resource
    private DistributedCacheService distributedCacheService;
    @Resource
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;

    @Transactional
    @Override
    public void arrangeStockBuckets(Long itemId, Integer stocksAmount, Integer bucketsQuantity, Integer assignmentMode) {
        logger.info("arrangeBuckets|准备库存分桶|{},{},{}", itemId, stocksAmount, bucketsQuantity);
        if (itemId == null || stocksAmount == null || stocksAmount < 0 || bucketsQuantity == null || bucketsQuantity <= 0) {
            throw new StockBucketException("参数错误");
        }
        DistributedLock lock = lockFactoryService.getDistributedLock(ITEM_STOCK_BUCKETS_SUSPEND_KEY + itemId);
        try {
            boolean isLockSuccess = lock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!isLockSuccess) {
                logger.info("arrangeBuckets|库存分桶时获取锁失败|{}", itemId);
                return;
            }
            TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
            try {
                boolean success = bucketsDomainService.suspendBuckets(itemId);
                if (!success) {
                    logger.info("arrangeBuckets|关闭库存分桶失败|{}", itemId);
                    throw new StockBucketException("关闭库存分桶失败");
                }
                dataSourceTransactionManager.commit(transactionStatus);
            } catch (Exception e) {
                logger.info("arrangeBuckets|关闭分桶失败回滚中|{}", itemId, e);
                dataSourceTransactionManager.rollback(transactionStatus);
            }

            List<Bucket> buckets = bucketsDomainService.getBucketsByItem(itemId);
            if (buckets.size() == 0) {
                initStockBuckets(itemId, stocksAmount, bucketsQuantity);
                return;
            }
            if (ArrangementMode.isTotalAmountMode(assignmentMode)) {
                arrangeStockBucketsBasedTotalMode(itemId, stocksAmount, bucketsQuantity, buckets);
            }
            if (ArrangementMode.isIncrementalAmountMode(assignmentMode)) {
                rearrangeStockBucketsBasedIncrementalMode(itemId, stocksAmount, bucketsQuantity, buckets);
            }
        } catch (Exception e) {
            logger.error("arrangeBuckets|库存分桶错误|", e);
            throw new StockBucketException("库存分桶错误");
        } finally {
            lock.unlock();
            boolean success = bucketsDomainService.resumeBuckets(itemId);
            if (!success) {
                logger.error("arrangeBuckets|打开库存分桶失败|");
            }
        }
    }

    @Override
    public StockBucketSummaryDTO queryStockBucketsSummary(Long itemId) {
        if (itemId == null) {
            throw new StockBucketException("参数错误");
        }
        List<Bucket> buckets = bucketsDomainService.getBucketsByItem(itemId);
        int remainAvailableStocks = buckets.stream().mapToInt(Bucket::getAvailableStocksAmount).sum();
        Optional<Bucket> primaryBucketOptional = buckets.stream().filter(Bucket::isPrimaryBucket).findFirst();
        if (!primaryBucketOptional.isPresent()) {
            return new StockBucketSummaryDTO();
        }
        List<StockBucketDTO> subBuckets = buckets.stream()
                .map(StockBucketBuilder::toStockBucketDTO)
                .collect(toList());
        return new StockBucketSummaryDTO()
                .setTotalStocksAmount(primaryBucketOptional.get().getTotalStocksAmount())
                .setAvailableStocksAmount(remainAvailableStocks)
                .setBuckets(subBuckets);
    }

    private void initStockBuckets(Long itemId, Integer totalStockAmount, Integer bucketsQuantity) {
        Bucket primaryBucket = new Bucket()
                .initPrimary()
                .setTotalStocksAmount(totalStockAmount)
                .setItemId(itemId);
        List<Bucket> presentBuckets = buildBuckets(itemId, totalStockAmount, bucketsQuantity, primaryBucket);
        submitBucketsToArrange(itemId, presentBuckets);
    }

    /**
     * 根据总量库存重新分桶
     */
    private void arrangeStockBucketsBasedTotalMode(Long itemId, Integer totalStockAmount, Integer bucketsAmount, List<Bucket> existingBuckets) {
        // 重新分桶
        int remainAvailableStocks = existingBuckets.stream()
                .filter(Bucket::isSubBucket)
                .mapToInt(Bucket::getAvailableStocksAmount).sum();
        Optional<Bucket> primaryBucketOptional = existingBuckets.stream().filter(Bucket::isPrimaryBucket).findFirst();
        if (!primaryBucketOptional.isPresent()) {
            return;
        }
        // 回收分桶库存到主桶
        Bucket primaryBucket = primaryBucketOptional.get();
        primaryBucket.addAvailableStocks(remainAvailableStocks);
        int soldStocksAmount = primaryBucket.getTotalStocksAmount() - primaryBucket.getAvailableStocksAmount();
        if (soldStocksAmount > totalStockAmount) {
            throw new StockBucketException("已售库存大于当期所设库存总量！");
        }
        primaryBucket.setTotalStocksAmount(totalStockAmount);

        List<Bucket> presentBuckets = buildBuckets(itemId, totalStockAmount, bucketsAmount, primaryBucket);
        submitBucketsToArrange(itemId, presentBuckets);
    }

    /**
     * 根据增量库存重新分桶
     */
    private void rearrangeStockBucketsBasedIncrementalMode(Long itemId, Integer incrementalStocksAmount, Integer bucketsAmount, List<Bucket> buckets) {
        Optional<Bucket> primaryStockBucketOptional = buckets.stream().filter(Bucket::isPrimaryBucket).findFirst();
        if (!primaryStockBucketOptional.isPresent()) {
            return;
        }
        // 回收分桶库存
        int remainAvailableStocks = buckets.stream().mapToInt(Bucket::getAvailableStocksAmount).sum();
        Integer totalAvailableStocksAmount = remainAvailableStocks + incrementalStocksAmount;
        int presentAvailableStocks = remainAvailableStocks + incrementalStocksAmount;
        if (presentAvailableStocks < 0) {
            throw new StockBucketException("可用库存不足！");
        }

        Bucket primaryBucket = primaryStockBucketOptional.get();
        primaryBucket.increaseTotalStocksAmount(incrementalStocksAmount);

        List<Bucket> presentBuckets = buildBuckets(itemId, totalAvailableStocksAmount, bucketsAmount, primaryBucket);
        submitBucketsToArrange(itemId, presentBuckets);
    }

    private void submitBucketsToArrange(Long itemId, List<Bucket> presentBuckets) {
        boolean result = bucketsDomainService.arrangeBuckets(itemId, presentBuckets);
        if (result) {
            presentBuckets.forEach(bucket -> distributedCacheService.put(getBucketAvailableStocksCacheKey(itemId, bucket.getSerialNo()), bucket.getAvailableStocksAmount()));
            distributedCacheService.put(getItemStockBucketsQuantityCacheKey(itemId), presentBuckets.size());
        } else {
            throw new StockBucketException("库存分桶失败");
        }
    }

    private List<Bucket> buildBuckets(Long itemId, Integer availableStocksAmount, Integer bucketsQuantity, Bucket primaryBucket) {
        if (itemId == null || availableStocksAmount == null || bucketsQuantity == null || bucketsQuantity <= 0) {
            throw new StockBucketException("构建分桶时参数错误");
        }
        List<Bucket> buckets = new ArrayList<>();
        int averageStocksInEachBucket = availableStocksAmount / bucketsQuantity;
        int pieceStocks = availableStocksAmount % bucketsQuantity;
        for (int i = 0; i < bucketsQuantity; i++) {
            if (i == 0) {
                if (primaryBucket == null) {
                    primaryBucket = new Bucket();
                }
                primaryBucket.setSerialNo(i);
                primaryBucket.setAvailableStocksAmount(averageStocksInEachBucket);
                primaryBucket.setStatus(BucketStatus.ENABLED.getCode());
                buckets.add(primaryBucket);
                continue;
            }
            Bucket subBucket = new Bucket()
                    .setStatus(BucketStatus.ENABLED.getCode())
                    .setSerialNo(i)
                    .setItemId(itemId);
            if (i < bucketsQuantity - 1) {
                subBucket.setTotalStocksAmount(averageStocksInEachBucket);
                subBucket.setAvailableStocksAmount(averageStocksInEachBucket);
            }
            if (i == bucketsQuantity - 1) {
                Integer restAvailableStocksAmount = averageStocksInEachBucket + pieceStocks;
                subBucket.setTotalStocksAmount(restAvailableStocksAmount);
                subBucket.setAvailableStocksAmount(restAvailableStocksAmount);
            }
            buckets.add(subBucket);
        }
        return buckets;
    }

    public static String getBucketAvailableStocksCacheKey(Long itemId, Integer serialNumber) {
        return link(ITEM_BUCKET_AVAILABLE_STOCKS_KEY, itemId, serialNumber);
    }

    public static String getItemStockBucketsSuspendKey(Long itemId) {
        return link(ITEM_STOCK_BUCKETS_SUSPEND_KEY, itemId);
    }
}
