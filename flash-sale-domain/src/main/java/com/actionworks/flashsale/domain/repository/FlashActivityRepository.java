package com.actionworks.flashsale.domain.repository;

import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashActivity;

import java.util.List;
import java.util.Optional;

public interface FlashActivityRepository {
    int save(FlashActivity flashActivity);

    Optional<FlashActivity> findById(Long activityId);

    List<FlashActivity> findFlashActivitiesByCondition(PagesQueryCondition pagesQueryCondition);

    Integer countFlashActivitiesByCondition(PagesQueryCondition pagesQueryCondition);
}
