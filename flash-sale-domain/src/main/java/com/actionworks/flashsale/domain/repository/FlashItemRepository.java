package com.actionworks.flashsale.domain.repository;

import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashItem;

import java.util.List;
import java.util.Optional;

public interface FlashItemRepository {
    int save(FlashItem flashItem);

    Optional<FlashItem> findById(Long itemId);

    List<FlashItem> findFlashItemsByCondition(PagesQueryCondition pagesQueryCondition);

    Integer countFlashItemsByCondition(PagesQueryCondition pagesQueryCondition);

    boolean decreaseItemStock(Long itemId, Integer quantity);

    boolean increaseItemStock(Long itemId, Integer quantity);
}
