package com.actionworks.flashsale.app.service.stock;

import com.actionworks.flashsale.app.service.stock.model.ItemStockCache;
import com.actionworks.flashsale.domain.model.StockDeduction;

public interface ItemStockCacheService {
    boolean alignItemStocks(Long itemId);

    boolean decreaseItemStock(StockDeduction stockDeduction);

    boolean increaseItemStock(StockDeduction stockDeduction);

    ItemStockCache getAvailableItemStock(Long userId, Long itemId);
}
