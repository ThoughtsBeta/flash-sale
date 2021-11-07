package com.actionworks.flashsale.domain.service.impl;

import com.actionworks.flashsale.domain.event.DomainEventPublisher;
import com.actionworks.flashsale.domain.event.FlashOrderEvent;
import com.actionworks.flashsale.domain.event.FlashOrderEventType;
import com.actionworks.flashsale.domain.exception.DomainException;
import com.actionworks.flashsale.domain.model.PageResult;
import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashOrder;
import com.actionworks.flashsale.domain.model.enums.FlashOrderStatus;
import com.actionworks.flashsale.domain.repository.FlashOrderRepository;
import com.actionworks.flashsale.domain.service.FlashOrderDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static com.actionworks.flashsale.domain.exception.DomainErrorCode.FLASH_ITEM_DOES_NOT_EXIST;
import static com.actionworks.flashsale.domain.exception.DomainErrorCode.ONLINE_FLASH_ITEM_PARAMS_INVALID;
import static com.actionworks.flashsale.domain.exception.DomainErrorCode.PARAMS_INVALID;

@Service
public class FlashOrderDomainServiceImpl implements FlashOrderDomainService {
    private static final Logger logger = LoggerFactory.getLogger(FlashOrderDomainServiceImpl.class);

    @Resource
    private FlashOrderRepository flashOrderRepository;
    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public boolean placeOrder(Long userId, FlashOrder flashOrder) {
        logger.info("Preparing to create flash order:{},{}", userId, flashOrder);
        if (flashOrder == null || !flashOrder.validateParamsForCreate()) {
            throw new DomainException(ONLINE_FLASH_ITEM_PARAMS_INVALID);
        }
        flashOrder.setStatus(FlashOrderStatus.CREATED.getCode());
        boolean saveSuccess = flashOrderRepository.save(flashOrder);
        if (saveSuccess) {
            FlashOrderEvent flashOrderEvent = new FlashOrderEvent();
            flashOrderEvent.setEventType(FlashOrderEventType.CREATED);
            domainEventPublisher.publish(flashOrderEvent);
        }
        logger.info("Flash order was created:{},{},{}", userId, flashOrder.getId(), saveSuccess);
        return saveSuccess;
    }

    @Override
    public PageResult<FlashOrder> getOrdersByUser(Long userId, PagesQueryCondition pagesQueryCondition) {
        if (pagesQueryCondition == null) {
            pagesQueryCondition = new PagesQueryCondition();
        }
        List<FlashOrder> flashOrders = flashOrderRepository.findFlashOrdersByCondition(pagesQueryCondition.buildParams());
        Integer total = flashOrderRepository.countFlashOrdersByCondition(pagesQueryCondition.buildParams());
        logger.info("Get flash orders:{},{}", userId, flashOrders.size());
        return PageResult.with(flashOrders, total);
    }

    @Override
    public List<FlashOrder> getOrders(PagesQueryCondition pagesQueryCondition) {
        if (pagesQueryCondition == null) {
            pagesQueryCondition = new PagesQueryCondition();
        }
        List<FlashOrder> flashOrders = flashOrderRepository.findFlashOrdersByCondition(pagesQueryCondition.buildParams());
        logger.info("Get flash orders:{},{}", flashOrders.size());
        return flashOrders;
    }

    @Override
    public FlashOrder getOrder(Long userId, Long orderId) {
        if (StringUtils.isEmpty(userId) || orderId == null) {
            throw new DomainException(PARAMS_INVALID);
        }
        Optional<FlashOrder> flashOrderOptional = flashOrderRepository.findById(orderId);
        if (!flashOrderOptional.isPresent()) {
            throw new DomainException(FLASH_ITEM_DOES_NOT_EXIST);
        }
        return flashOrderOptional.get();
    }

    @Override
    public boolean cancelOrder(Long userId, Long orderId) {
        logger.info("Preparing to cancel flash order:{},{}", userId, orderId);
        if (StringUtils.isEmpty(userId) || orderId == null) {
            throw new DomainException(PARAMS_INVALID);
        }
        Optional<FlashOrder> flashOrderOptional = flashOrderRepository.findById(orderId);
        if (!flashOrderOptional.isPresent()) {
            throw new DomainException(FLASH_ITEM_DOES_NOT_EXIST);
        }
        FlashOrder flashOrder = flashOrderOptional.get();
        if (!flashOrder.getUserId().equals(userId)) {
            throw new DomainException(FLASH_ITEM_DOES_NOT_EXIST);
        }
        if (FlashOrderStatus.isCancled(flashOrder.getStatus())) {
            return false;
        }
        flashOrder.setStatus(FlashOrderStatus.CANCELED.getCode());
        boolean saveSuccess = flashOrderRepository.updateStatus(flashOrder);
        if (saveSuccess) {
            FlashOrderEvent flashOrderEvent = new FlashOrderEvent();
            flashOrderEvent.setEventType(FlashOrderEventType.CANCEL);
            domainEventPublisher.publish(flashOrderEvent);
        }
        logger.info("Flash order was canceled:{},{}", userId, orderId, saveSuccess);
        return saveSuccess;
    }
}
