package com.actionworks.flashsale.domain.repository;

import com.actionworks.flashsale.domain.model.Bucket;

import java.util.List;

public interface BucketsRepository {
    boolean submitBuckets(Long itemId, List<Bucket> buckets);

    boolean decreaseItemStock(Long itemId, Integer quantity, Integer serialNo);

    boolean increaseItemStock(Long itemId, Integer quantity, Integer serialNo);

    List<Bucket> getBucketsByItem(Long itemId);

    boolean suspendBuckets(Long itemId);

    boolean resumeStockBuckets(Long itemId);
}
