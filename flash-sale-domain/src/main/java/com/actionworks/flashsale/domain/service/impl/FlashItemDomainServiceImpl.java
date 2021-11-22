package com.actionworks.flashsale.domain.service.impl;

import com.actionworks.flashsale.domain.event.DomainEventPublisher;
import com.actionworks.flashsale.domain.event.FlashItemEvent;
import com.actionworks.flashsale.domain.event.FlashItemEventType;
import com.actionworks.flashsale.domain.exception.DomainException;
import com.actionworks.flashsale.domain.model.PageResult;
import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashItem;
import com.actionworks.flashsale.domain.model.enums.FlashItemStatus;
import com.actionworks.flashsale.domain.repository.FlashItemRepository;
import com.actionworks.flashsale.domain.service.FlashItemDomainService;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static com.actionworks.flashsale.domain.exception.DomainErrorCode.FLASH_ITEM_DOES_NOT_EXIST;
import static com.actionworks.flashsale.domain.exception.DomainErrorCode.ONLINE_FLASH_ITEM_PARAMS_INVALID;
import static com.actionworks.flashsale.domain.exception.DomainErrorCode.PARAMS_INVALID;

@Service
public class FlashItemDomainServiceImpl implements FlashItemDomainService {
    private static final Logger logger = LoggerFactory.getLogger(FlashItemDomainServiceImpl.class);

    @Resource
    private FlashItemRepository flashItemRepository;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public void publishFlashItem(FlashItem flashItem) {
        logger.info("itemPublish|发布秒杀品|{}", JSON.toJSON(flashItem));
        if (flashItem == null || !flashItem.validateParamsForCreate()) {
            throw new DomainException(ONLINE_FLASH_ITEM_PARAMS_INVALID);
        }
        flashItem.setStatus(FlashItemStatus.PUBLISHED.getCode());
        flashItemRepository.save(flashItem);
        logger.info("itemPublish|秒杀品已发布|{}", flashItem.getId());

        FlashItemEvent flashItemEvent = new FlashItemEvent();
        flashItemEvent.setEventType(FlashItemEventType.PUBLISHED);
        flashItemEvent.setFlashItem(flashItem);
        domainEventPublisher.publish(flashItemEvent);
        logger.info("itemPublish|秒杀品发布事件已发布|{}", flashItem.getId());
    }

    @Override
    public void onlineFlashItem(Long itemId) {
        logger.info("itemOnline|上线秒杀品|{}", itemId);
        if (itemId == null) {
            throw new DomainException(PARAMS_INVALID);
        }
        Optional<FlashItem> flashItemOptional = flashItemRepository.findById(itemId);
        if (!flashItemOptional.isPresent()) {
            throw new DomainException(FLASH_ITEM_DOES_NOT_EXIST);
        }
        FlashItem flashItem = flashItemOptional.get();
        if (FlashItemStatus.isOnline(flashItem.getStatus())) {
            return;
        }
        flashItem.setStatus(FlashItemStatus.ONLINE.getCode());
        flashItemRepository.save(flashItem);
        logger.info("itemOnline|秒杀品已上线|{}", itemId);

        FlashItemEvent flashItemPublishEvent = new FlashItemEvent();
        flashItemPublishEvent.setEventType(FlashItemEventType.ONLINE);
        flashItemPublishEvent.setFlashItem(flashItem);
        domainEventPublisher.publish(flashItemPublishEvent);
        logger.info("itemOnline|秒杀品上线事件已发布|{}", itemId);
    }

    @Override
    public void offlineFlashItem(Long itemId) {
        logger.info("itemOffline|下线秒杀品|{}", itemId);
        if (itemId == null) {
            throw new DomainException(PARAMS_INVALID);
        }
        Optional<FlashItem> flashItemOptional = flashItemRepository.findById(itemId);
        if (!flashItemOptional.isPresent()) {
            throw new DomainException(FLASH_ITEM_DOES_NOT_EXIST);
        }
        FlashItem flashItem = flashItemOptional.get();
        if (FlashItemStatus.isOffline(flashItem.getStatus())) {
            return;
        }
        flashItem.setStatus(FlashItemStatus.OFFLINE.getCode());
        flashItemRepository.save(flashItem);
        logger.info("itemOffline|秒杀品已下线|{}", itemId);

        FlashItemEvent flashItemEvent = new FlashItemEvent();
        flashItemEvent.setEventType(FlashItemEventType.OFFLINE);
        flashItemEvent.setFlashItem(flashItem);
        domainEventPublisher.publish(flashItemEvent);
        logger.info("itemOffline|秒杀品下线事件已发布|{}", itemId);
    }

    @Override
    public PageResult<FlashItem> getFlashItems(PagesQueryCondition pagesQueryCondition) {
        if (pagesQueryCondition == null) {
            pagesQueryCondition = new PagesQueryCondition();
        }
        List<FlashItem> flashItems = flashItemRepository.findFlashItemsByCondition(pagesQueryCondition.buildParams());
        Integer total = flashItemRepository.countFlashItemsByCondition(pagesQueryCondition);
        logger.info("Get flash items:{}", flashItems.size());
        return PageResult.with(flashItems, total);
    }

    @Override
    public FlashItem getFlashItem(Long itemId) {
        if (itemId == null) {
            throw new DomainException(PARAMS_INVALID);
        }
        Optional<FlashItem> flashItemOptional = flashItemRepository.findById(itemId);
        if (!flashItemOptional.isPresent()) {
            throw new DomainException(FLASH_ITEM_DOES_NOT_EXIST);
        }
        return flashItemOptional.get();
    }
}
