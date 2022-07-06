package com.actionworks.flashsale.domain.service;

import com.actionworks.flashsale.domain.model.PageResult;
import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashActivity;

public interface FlashActivityDomainService {
    /**
     * 发布活动
     *
     * @param userId        当前用户
     * @param flashActivity 秒杀活动
     */
    void publishActivity(Long userId, FlashActivity flashActivity);

    void modifyActivity(Long userId, FlashActivity flashActivity);

    /**
     * 上线活动
     *
     * @param userId     当前用户
     * @param activityId 待上线的活动ID
     */
    void onlineActivity(Long userId, Long activityId);

    /**
     * 下线活动
     *
     * @param userId     当前用户
     * @param activityId 待下线的活动ID
     */
    void offlineActivity(Long userId, Long activityId);

    /**
     * 获取秒杀品列表
     *
     * @param pagesQueryCondition 查询条件
     * @return 秒杀品集合
     */
    PageResult<FlashActivity> getFlashActivities(PagesQueryCondition pagesQueryCondition);

    /**
     * 根据ID获取指定秒杀活动
     *
     * @param activityId 秒杀品ID
     * @return 秒杀品
     */
    FlashActivity getFlashActivity(Long activityId);
}
