package com.actionworks.flashsale.persistence.repository;

import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashActivity;
import com.actionworks.flashsale.domain.repository.FlashActivityRepository;
import com.actionworks.flashsale.persistence.convertor.FlashActivityBuilder;
import com.actionworks.flashsale.persistence.mapper.FlashActivityMapper;
import com.actionworks.flashsale.persistence.model.FlashActivityDO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FlashActivityRepositoryImpl implements FlashActivityRepository {
    @Resource
    private FlashActivityMapper flashActivityMapper;

    @Override
    public int save(FlashActivity flashActivity) {
        FlashActivityDO flashActivityDO = FlashActivityBuilder.toDataObjectForCreate(flashActivity);
        if (flashActivityDO.getId() == null) {
            int effectedRows = flashActivityMapper.insert(flashActivityDO);
            flashActivity.setId(flashActivityDO.getId());
            return effectedRows;
        }
        return flashActivityMapper.update(flashActivityDO);
    }

    @Override
    public Optional<FlashActivity> findById(Long activityId) {
        FlashActivityDO flashActivityDO = flashActivityMapper.getById(activityId);
        if (flashActivityDO == null) {
            return Optional.empty();
        }
        FlashActivity flashActivity = FlashActivityBuilder.toDomainObject(flashActivityDO);
        return Optional.of(flashActivity);
    }

    @Override
    public List<FlashActivity> findFlashActivitiesByCondition(PagesQueryCondition pagesQueryCondition) {
        return flashActivityMapper.findFlashActivitiesByCondition(pagesQueryCondition)
                .stream()
                .map(FlashActivityBuilder::toDomainObject)
                .collect(Collectors.toList());
    }

    @Override
    public Integer countFlashActivitiesByCondition(PagesQueryCondition pagesQueryCondition) {
        return flashActivityMapper.countFlashActivitiesByCondition(pagesQueryCondition);
    }
}
