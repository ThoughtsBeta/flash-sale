package com.actionworks.flashsale.persistence.mapper;

import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.persistence.model.FlashActivityDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FlashActivityMapper {
    int insert(FlashActivityDO flashActivityDO);

    int update(FlashActivityDO flashActivityDO);

    FlashActivityDO getById(@Param("activityId") Long activityId);

    List<FlashActivityDO> findFlashActivitiesByCondition(PagesQueryCondition pagesQueryCondition);

    Integer countFlashActivitiesByCondition(PagesQueryCondition pagesQueryCondition);
}
