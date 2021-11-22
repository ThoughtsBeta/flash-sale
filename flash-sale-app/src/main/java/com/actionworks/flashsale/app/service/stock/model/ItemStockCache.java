package com.actionworks.flashsale.app.service.stock.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ItemStockCache {
    protected boolean exist;
    private Integer availableStock;
    private boolean success;
    private boolean later;

    public ItemStockCache with(Integer availableStock) {
        this.exist = true;
        this.availableStock = availableStock;
        this.success = true;
        return this;
    }

    public ItemStockCache tryLater() {
        this.later = true;
        this.success = false;
        return this;
    }

    public ItemStockCache notExist() {
        this.exist = false;
        this.success = true;
        return this;
    }
}
