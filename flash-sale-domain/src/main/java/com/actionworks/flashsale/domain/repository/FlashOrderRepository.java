package com.actionworks.flashsale.domain.repository;

import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashOrder;

import java.util.List;
import java.util.Optional;

public interface FlashOrderRepository {
    boolean save(FlashOrder flashOrder);

    boolean updateStatus(FlashOrder flashOrder);

    Optional<FlashOrder> findById(Long orderId);

    List<FlashOrder> findFlashOrdersByCondition(PagesQueryCondition pagesQueryCondition);

    int countFlashOrdersByCondition(PagesQueryCondition buildParams);
}
