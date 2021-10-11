package com.actionworks.flashsale.domain.service;

import com.actionworks.flashsale.domain.model.PageResult;
import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashOrder;

import java.util.List;

public interface FlashOrderDomainService {
    /**
     * 下单
     *
     * @param userId     当前用户
     * @param flashOrder 下单信息
     */
    boolean placeOrder(Long userId, FlashOrder flashOrder);

    /**
     * 根据用户获取订单
     *
     * @param userId              当前用户
     * @param pagesQueryCondition 查询条件
     * @return 订单集合
     */
    PageResult<FlashOrder> getOrdersByUser(Long userId, PagesQueryCondition pagesQueryCondition);

    /**
     * 根据条件获取订单
     *
     * @param pagesQueryCondition 查询条件
     * @return 订单集合
     */
    List<FlashOrder> getOrders(PagesQueryCondition pagesQueryCondition);

    /**
     * 根据ID获取指定订单
     *
     * @param userId  当前用户
     * @param orderId 订单ID
     * @return 订单
     */
    FlashOrder getOrder(Long userId, Long orderId);

    /**
     * 根据ID取消订单
     *  @param userId  当前用户
     * @param orderId 订单ID
     */
    boolean cancelOrder(Long userId, Long orderId);
}
