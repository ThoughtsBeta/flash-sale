package com.actionworks.flashsale.app.cache;

import com.actionworks.flashsale.app.cache.model.ItemStockCache;

public interface ItemStockCacheService {
    boolean initItemStock(Long itemId);

    boolean decreaseItemStock(Long userId, Long itemId, Integer quantity);

    boolean increaseItemStock(Long userId, Long itemId, Integer quantity);

    ItemStockCache getAvailableItemStock(Long itemId);
}
