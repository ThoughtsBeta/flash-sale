package com.actionworks.flashsale.domain.service;

import com.actionworks.flashsale.domain.model.StockDeduction;

public interface StockDeductionDomainService {
    /**
     * 库存扣减
     *
     * @param stockDeduction 库存扣减信息
     */
    boolean decreaseItemStock(StockDeduction stockDeduction);

    /**
     * 库存恢复
     *
     * @param stockDeduction 库存恢复信息
     */
    boolean increaseItemStock(StockDeduction stockDeduction);
}
