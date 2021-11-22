package com.actionworks.flashsale.app.model.builder;

import com.actionworks.flashsale.app.model.dto.StockBucketDTO;
import com.actionworks.flashsale.domain.model.Bucket;
import org.springframework.beans.BeanUtils;

public class StockBucketBuilder {
    public static StockBucketDTO toStockBucketDTO(Bucket bucket) {
        if (bucket == null) {
            return null;
        }
        StockBucketDTO stockBucketDTO = new StockBucketDTO();
        BeanUtils.copyProperties(bucket, stockBucketDTO);
        return stockBucketDTO;
    }
}
