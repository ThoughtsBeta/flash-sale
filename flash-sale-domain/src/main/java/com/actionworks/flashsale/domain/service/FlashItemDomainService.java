package com.actionworks.flashsale.domain.service;

import com.actionworks.flashsale.domain.model.PageResult;
import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashItem;

public interface FlashItemDomainService {
    /**
     * 发布新的秒杀品
     *
     * @param flashItem 秒杀品
     */
    void publishFlashItem(FlashItem flashItem);

    /**
     * 上线秒杀品
     *
     * @param itemId 待上线的秒杀品ID
     */
    void onlineFlashItem(Long itemId);

    /**
     * 下线秒杀品
     *
     * @param itemId 待下线的秒杀品ID
     */
    void offlineFlashItem(Long itemId);

    /**
     * 获取秒杀品列表
     *
     * @param pagesQueryCondition 查询条件
     * @return 秒杀品集合
     */
    PageResult<FlashItem> getFlashItems(PagesQueryCondition pagesQueryCondition);

    /**
     * 根据ID获取指定秒杀品
     *
     * @param itemId 秒杀品ID
     * @return 秒杀品
     */
    FlashItem getFlashItem(Long itemId);
}