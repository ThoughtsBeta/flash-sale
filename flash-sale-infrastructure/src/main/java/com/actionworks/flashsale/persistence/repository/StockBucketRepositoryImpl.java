package com.actionworks.flashsale.persistence.repository;

import com.actionworks.flashsale.domain.model.Bucket;
import com.actionworks.flashsale.domain.repository.BucketsRepository;
import com.actionworks.flashsale.persistence.convertor.BucketBuilder;
import com.actionworks.flashsale.persistence.mapper.BucketMapper;
import com.actionworks.flashsale.persistence.model.BucketDO;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class StockBucketRepositoryImpl implements BucketsRepository {
    @Resource
    private BucketMapper bucketMapper;

    @Override
    public boolean submitBuckets(Long itemId, List<Bucket> buckets) {
        if (itemId == null || CollectionUtils.isEmpty(buckets)) {
            return false;
        }
        List<BucketDO> bucketDOS = buckets.stream().map(BucketBuilder::toDataObject).collect(toList());
        bucketMapper.deleteByItem(itemId);
        bucketMapper.insertBatch(bucketDOS);
        return true;
    }

    @Override
    public boolean suspendBuckets(Long itemId) {
        if (itemId == null) {
            return false;
        }
        bucketMapper.updateStatusByItem(itemId, 0);
        return true;
    }

    @Override
    public boolean resumeStockBuckets(Long itemId) {
        if (itemId == null) {
            return false;
        }
        bucketMapper.updateStatusByItem(itemId, 1);
        return true;
    }

    @Override
    public List<Bucket> getBucketsByItem(Long itemId) {
        if (itemId == null) {
            return new ArrayList<>();
        }
        List<BucketDO> bucketDOS = bucketMapper.getBucketsByItem(itemId);
        return bucketDOS.stream().map(BucketBuilder::toDomainObject).collect(toList());
    }

    @Override
    public boolean decreaseItemStock(Long itemId, Integer quantity, Integer serialNo) {
        if (itemId == null || quantity == null || serialNo == null) {
            return false;
        }
        return bucketMapper.decreaseItemStock(itemId, quantity, serialNo);
    }

    @Override
    public boolean increaseItemStock(Long itemId, Integer quantity, Integer serialNo) {
        if (itemId == null || quantity == null || serialNo == null) {
            return false;
        }
        return bucketMapper.increaseItemStock(itemId, quantity, serialNo);
    }
}
