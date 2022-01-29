package com.actionworks.flashsale.domain.service.impl;

import com.actionworks.flashsale.domain.event.DomainEventPublisher;
import com.actionworks.flashsale.domain.event.FlashActivityEvent;
import com.actionworks.flashsale.domain.event.FlashActivityEventType;
import com.actionworks.flashsale.domain.exception.DomainException;
import com.actionworks.flashsale.domain.model.PageResult;
import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashActivity;
import com.actionworks.flashsale.domain.model.enums.FlashActivityStatus;
import com.actionworks.flashsale.domain.repository.FlashActivityRepository;
import com.actionworks.flashsale.domain.service.FlashActivityDomainService;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static com.actionworks.flashsale.domain.exception.DomainErrorCode.FLASH_ACTIVITY_DOES_NOT_EXIST;
import static com.actionworks.flashsale.domain.exception.DomainErrorCode.FLASH_ACTIVITY_NOT_ONLINE;
import static com.actionworks.flashsale.domain.exception.DomainErrorCode.ONLINE_FLASH_ACTIVITY_PARAMS_INVALID;
import static com.actionworks.flashsale.domain.exception.DomainErrorCode.PARAMS_INVALID;

@Service
public class FlashActivityDomainServiceImpl implements FlashActivityDomainService {
    private static final Logger logger = LoggerFactory.getLogger(FlashActivityDomainServiceImpl.class);
    @Resource
    private FlashActivityRepository flashActivityRepository;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public void publishActivity(Long userId, FlashActivity flashActivity) {
        logger.info("activityPublish|发布秒杀活动|{},{}", userId, JSON.toJSONString(flashActivity));
        if (flashActivity == null || !flashActivity.validateParamsForCreateOrUpdate()) {
            throw new DomainException(ONLINE_FLASH_ACTIVITY_PARAMS_INVALID);
        }
        flashActivity.setStatus(FlashActivityStatus.PUBLISHED.getCode());
        flashActivityRepository.save(flashActivity);
        logger.info("activityPublish|活动已发布|{},{}", userId, flashActivity.getId());

        FlashActivityEvent flashActivityEvent = new FlashActivityEvent();
        flashActivityEvent.setEventType(FlashActivityEventType.PUBLISHED);
        flashActivityEvent.setFlashActivity(flashActivity);
        domainEventPublisher.publish(flashActivityEvent);
        logger.info("activityPublish|活动发布事件已发布|{}", JSON.toJSON(flashActivityEvent));
    }

    @Override
    public void modifyActivity(Long userId, FlashActivity flashActivity) {
        logger.info("activityModification|秒杀活动修改|{},{}", userId, JSON.toJSONString(flashActivity));
        if (flashActivity == null || !flashActivity.validateParamsForCreateOrUpdate()) {
            throw new DomainException(ONLINE_FLASH_ACTIVITY_PARAMS_INVALID);
        }
        flashActivityRepository.save(flashActivity);
        logger.info("activityModification|活动已修改|{},{}", userId, flashActivity.getId());

        FlashActivityEvent flashActivityEvent = new FlashActivityEvent();
        flashActivityEvent.setEventType(FlashActivityEventType.MODIFIED);
        flashActivityEvent.setFlashActivity(flashActivity);
        domainEventPublisher.publish(flashActivityEvent);
        logger.info("activityModification|活动修改事件已发布|{}", JSON.toJSON(flashActivityEvent));
    }

    @Override
    public void onlineActivity(Long userId, Long activityId) {
        logger.info("activityOnline|上线秒杀活动|{},{}", userId, activityId);
        if (StringUtils.isEmpty(userId) || activityId == null) {
            throw new DomainException(PARAMS_INVALID);
        }
        Optional<FlashActivity> flashActivityOptional = flashActivityRepository.findById(activityId);
        if (!flashActivityOptional.isPresent()) {
            throw new DomainException(FLASH_ACTIVITY_DOES_NOT_EXIST);
        }
        FlashActivity flashActivity = flashActivityOptional.get();
        if (FlashActivityStatus.isOnline(flashActivity.getStatus())) {
            return;
        }
        flashActivity.setStatus(FlashActivityStatus.ONLINE.getCode());
        flashActivityRepository.save(flashActivity);
        logger.info("activityOnline|活动已上线|{},{}", userId, flashActivity.getId());

        FlashActivityEvent flashActivityEvent = new FlashActivityEvent();
        flashActivityEvent.setEventType(FlashActivityEventType.ONLINE);
        flashActivityEvent.setFlashActivity(flashActivity);
        domainEventPublisher.publish(flashActivityEvent);
        logger.info("activityOnline|活动上线事件已发布|{}", JSON.toJSON(flashActivityEvent));
    }

    @Override
    public void offlineActivity(Long userId, Long activityId) {
        logger.info("activityOffline|下线秒杀活动|{},{}", userId, activityId);
        if (StringUtils.isEmpty(userId) || activityId == null) {
            throw new DomainException(PARAMS_INVALID);
        }
        Optional<FlashActivity> flashActivityOptional = flashActivityRepository.findById(activityId);
        if (!flashActivityOptional.isPresent()) {
            throw new DomainException(FLASH_ACTIVITY_DOES_NOT_EXIST);
        }
        FlashActivity flashActivity = flashActivityOptional.get();
        if (FlashActivityStatus.isOffline(flashActivity.getStatus())) {
            return;
        }
        if (!FlashActivityStatus.isOnline(flashActivity.getStatus())) {
            throw new DomainException(FLASH_ACTIVITY_NOT_ONLINE);
        }
        flashActivity.setStatus(FlashActivityStatus.OFFLINE.getCode());
        flashActivityRepository.save(flashActivity);
        logger.info("activityOffline|活动已下线|{},{}", userId, flashActivity.getId());

        FlashActivityEvent flashActivityEvent = new FlashActivityEvent();
        flashActivityEvent.setEventType(FlashActivityEventType.OFFLINE);
        flashActivityEvent.setFlashActivity(flashActivity);
        domainEventPublisher.publish(flashActivityEvent);
        logger.info("activityOffline|活动下线事件已发布|{}", JSON.toJSON(flashActivityEvent));
    }

    @Override
    public PageResult<FlashActivity> getFlashActivities(PagesQueryCondition pagesQueryCondition) {
        if (pagesQueryCondition == null) {
            pagesQueryCondition = new PagesQueryCondition();
        }
        List<FlashActivity> flashActivities = flashActivityRepository.findFlashActivitiesByCondition(pagesQueryCondition.buildParams());
        Integer total = flashActivityRepository.countFlashActivitiesByCondition(pagesQueryCondition);
        return PageResult.with(flashActivities, total);
    }

    @Override
    public FlashActivity getFlashActivity(Long activityId) {
        if (activityId == null) {
            throw new DomainException(PARAMS_INVALID);
        }
        Optional<FlashActivity> flashActivityOptional = flashActivityRepository.findById(activityId);
        return flashActivityOptional.orElse(null);
    }
}
