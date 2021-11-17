package com.actionworks.flashsale.app.service;

import com.actionworks.flashsale.app.cache.ItemStockCacheService;
import com.actionworks.flashsale.app.model.command.FlashPlaceOrderCommand;
import com.actionworks.flashsale.app.model.result.PlaceOrderResult;
import com.actionworks.flashsale.app.facade.PlaceOrderService;
import com.actionworks.flashsale.app.util.OrderNoGenerateContext;
import com.actionworks.flashsale.app.util.OrderNoGenerateService;
import com.actionworks.flashsale.domain.model.entity.FlashItem;
import com.actionworks.flashsale.domain.model.entity.FlashOrder;
import com.actionworks.flashsale.domain.service.FlashActivityDomainService;
import com.actionworks.flashsale.domain.service.FlashItemDomainService;
import com.actionworks.flashsale.domain.service.FlashOrderDomainService;
import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static com.actionworks.flashsale.app.exception.AppErrorCode.PLACE_ORDER_FAILED;
import static com.actionworks.flashsale.app.model.builder.FlashOrderAppBuilder.toDomain;

@Service
@ConditionalOnProperty(name = "place_order_type", havingValue = "default", matchIfMissing = true)
public class DefaultPlaceOrderService implements PlaceOrderService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultPlaceOrderService.class);
    @Resource
    private FlashOrderDomainService flashOrderDomainService;
    @Resource
    private FlashItemDomainService flashItemDomainService;
    @Resource
    private FlashActivityDomainService flashActivityDomainService;
    @Resource
    private ItemStockCacheService itemStockCacheService;
    @Resource
    private OrderNoGenerateService orderNoGenerateService;

    @PostConstruct
    public void init() {
        logger.info("initPlaceOrderService|默认下单服务已经初始化");
    }

    @Override
    public PlaceOrderResult doPlaceOrder(Long userId, FlashPlaceOrderCommand placeOrderCommand) {
        logger.info("placeOrder|开始下单|{},{}", userId, JSON.toJSONString(placeOrderCommand));
        boolean isActivityAllowPlaceOrder = flashActivityDomainService.isAllowPlaceOrderOrNot(placeOrderCommand.getActivityId());
        if (!isActivityAllowPlaceOrder) {
            logger.info("placeOrder|秒杀活动下单规则校验未通过|{},{}", userId, placeOrderCommand.getActivityId());
            return PlaceOrderResult.failed(PLACE_ORDER_FAILED);
        }
        boolean isItemAllowPlaceOrder = flashItemDomainService.isAllowPlaceOrderOrNot(placeOrderCommand.getItemId());
        if (!isItemAllowPlaceOrder) {
            logger.info("placeOrder|秒杀品下单规则校验未通过|{},{}", userId, placeOrderCommand.getActivityId());
            return PlaceOrderResult.failed(PLACE_ORDER_FAILED);
        }
        FlashItem flashItem = flashItemDomainService.getFlashItem(placeOrderCommand.getItemId());

        Long orderId = orderNoGenerateService.generateOrderNo(new OrderNoGenerateContext());
        FlashOrder flashOrderToPlace = toDomain(placeOrderCommand);
        flashOrderToPlace.setItemTitle(flashItem.getItemTitle());
        flashOrderToPlace.setFlashPrice(flashItem.getFlashPrice());
        flashOrderToPlace.setUserId(userId);
        flashOrderToPlace.setId(orderId);

        boolean preDecreaseStockSuccess = false;
        try {
            preDecreaseStockSuccess = itemStockCacheService.decreaseItemStock(userId, placeOrderCommand.getItemId(), placeOrderCommand.getQuantity());
            if (!preDecreaseStockSuccess) {
                logger.info("placeOrder|库存预扣减失败|{},{}", userId, JSON.toJSONString(placeOrderCommand));
                return PlaceOrderResult.failed(PLACE_ORDER_FAILED.getErrCode(), PLACE_ORDER_FAILED.getErrDesc());
            }
            boolean decreaseStockSuccess = flashItemDomainService.decreaseItemStock(placeOrderCommand.getItemId(), placeOrderCommand.getQuantity());
            if (!decreaseStockSuccess) {
                logger.info("placeOrder|库存扣减失败|{},{}", userId, JSON.toJSONString(placeOrderCommand));
                return PlaceOrderResult.failed(PLACE_ORDER_FAILED.getErrCode(), PLACE_ORDER_FAILED.getErrDesc());
            }
            boolean placeOrderSuccess = flashOrderDomainService.placeOrder(userId, flashOrderToPlace);
            if (!placeOrderSuccess) {
                throw new BizException(PLACE_ORDER_FAILED.getErrDesc());
            }
        } catch (Exception e) {
            if (preDecreaseStockSuccess) {
                boolean recoverStockSuccess = itemStockCacheService.increaseItemStock(userId, placeOrderCommand.getItemId(), placeOrderCommand.getQuantity());
                if (!recoverStockSuccess) {
                    logger.error("placeOrder|预扣库存恢复失败|{},{}", userId, JSON.toJSONString(placeOrderCommand), e);
                }
            }
            logger.error("placeOrder|下单失败|{},{}", userId, JSON.toJSONString(placeOrderCommand), e);
            throw new BizException(PLACE_ORDER_FAILED.getErrDesc());
        }
        logger.error("placeOrder|下单成功|{},{}", userId, orderId);
        return PlaceOrderResult.ok(orderId);
    }
}
