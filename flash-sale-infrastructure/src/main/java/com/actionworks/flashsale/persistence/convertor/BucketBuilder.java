package com.actionworks.flashsale.persistence.convertor;

import com.actionworks.flashsale.domain.model.Bucket;
import com.actionworks.flashsale.persistence.model.BucketDO;
import org.springframework.beans.BeanUtils;

public class BucketBuilder {

    public static BucketDO toDataObject(Bucket bucket) {
        BucketDO bucketDO = new BucketDO();
        BeanUtils.copyProperties(bucket, bucketDO);
        return bucketDO;
    }

    public static Bucket toDomainObject(BucketDO bucketDO) {
        Bucket bucket = new Bucket();
        BeanUtils.copyProperties(bucketDO, bucket);
        return bucket;
    }
}
