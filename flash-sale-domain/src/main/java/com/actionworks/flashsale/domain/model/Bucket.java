package com.actionworks.flashsale.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

import static com.actionworks.flashsale.domain.model.enums.BucketType.PRIMARY;

@Data
@Accessors(chain = true)
public class Bucket {
    private Integer serialNo;
    private Integer totalStocksAmount;
    private Integer availableStocksAmount;
    private Integer status;
    private Long itemId;

    public boolean isSubBucket() {
        return !PRIMARY.getCode().equals(serialNo);
    }

    public void addAvailableStocks(int availableStocksAmount) {
        if (this.availableStocksAmount == null) {
            return;
        }
        this.availableStocksAmount += availableStocksAmount;
    }

    public Integer getSerialNo() {
        return serialNo;
    }

    public boolean isPrimaryBucket() {
        return PRIMARY.getCode().equals(serialNo);
    }

    public Bucket initPrimary() {
        this.serialNo = PRIMARY.getCode();
        return this;
    }

    public void increaseTotalStocksAmount(Integer incrementalStocksAmount) {
        if (incrementalStocksAmount == null) {
            return;
        }
        this.totalStocksAmount += incrementalStocksAmount;
    }
}
