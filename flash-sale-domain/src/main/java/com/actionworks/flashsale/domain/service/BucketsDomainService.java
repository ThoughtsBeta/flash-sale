package com.actionworks.flashsale.domain.service;

import com.actionworks.flashsale.domain.model.Bucket;

import java.util.List;

public interface BucketsDomainService {
    boolean suspendBuckets(Long itemId);

    List<Bucket> getBucketsByItem(Long itemId);

    boolean arrangeBuckets(Long itemId, List<Bucket> buckets);

    boolean resumeBuckets(Long itemId);
}
