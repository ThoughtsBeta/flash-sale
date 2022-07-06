package com.actionworks.flashsale.app.service.order;

import com.actionworks.flashsale.app.exception.BizException;
import com.actionworks.flashsale.app.model.builder.FlashOrderAppBuilder;
import com.actionworks.flashsale.app.model.command.FlashPlaceOrderCommand;
import com.actionworks.flashsale.app.model.dto.FlashOrderDTO;
import com.actionworks.flashsale.app.model.query.FlashOrdersQuery;
import com.actionworks.flashsale.app.model.result.AppMultiResult;
import com.actionworks.flashsale.app.model.result.AppResult;
import com.actionworks.flashsale.app.model.result.AppSimpleResult;
import com.actionworks.flashsale.app.model.result.OrderTaskHandleResult;
import com.actionworks.flashsale.app.model.result.PlaceOrderResult;
import com.actionworks.flashsale.app.security.SecurityService;
import com.actionworks.flashsale.app.service.placeorder.PlaceOrderService;
import com.actionworks.flashsale.app.service.placeorder.queued.QueuedPlaceOrderService;
import com.actionworks.flashsale.app.service.stock.ItemStockCacheService;
import com.actionworks.flashsale.domain.model.PageResult;
import com.actionworks.flashsale.domain.model.StockDeduction;
import com.actionworks.flashsale.domain.model.entity.FlashOrder;
import com.actionworks.flashsale.domain.service.FlashOrderDomainService;
import com.actionworks.flashsale.domain.service.StockDeductionDomainService;
import com.actionworks.flashsale.lock.DistributedLock;
import com.actionworks.flashsale.lock.DistributedLockFactoryService;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.actionworks.flashsale.app.exception.AppErrorCode.FREQUENTLY_ERROR;
import static com.actionworks.flashsale.app.exception.AppErrorCode.INVALID_PARAMS;
import static com.actionworks.flashsale.app.exception.AppErrorCode.ORDER_CANCEL_FAILED;
import static com.actionworks.flashsale.app.exception.AppErrorCode.ORDER_NOT_FOUND;
import static com.actionworks.flashsale.app.exception.AppErrorCode.ORDER_TYPE_NOT_SUPPORT;
import static com.actionworks.flashsale.app.exception.AppErrorCode.PLACE_ORDER_FAILED;
import static com.actionworks.flashsale.app.model.builder.FlashOrderAppBuilder.toFlashOrdersQuery;
import static com.actionworks.flashsale.util.StringUtil.link;

@Service
public class DefaultFlashOrderAppService implements FlashOrderAppService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultFlashOrderAppService.class);
    private static final String PLACE_ORDER_LOCK_KEY = "PLACE_ORDER_LOCK_KEY";

    @Resource
    private FlashOrderDomainService flashOrderDomainService;
    @Resource
    private StockDeductionDomainService stockDeductionDomainService;
    @Resource
    private ItemStockCacheService itemStockCacheService;
    @Resource
    private DistributedLockFactoryService lockFactoryService;
    @Resource
    private SecurityService securityService;
    @Resource
    private PlaceOrderService placeOrderService;

    @Override
    @Transactional
    public AppSimpleResult<PlaceOrderResult> placeOrder(Long userId, FlashPlaceOrderCommand placeOrderCommand) {
        logger.info("placeOrder|下单|{},{}", userId, JSON.toJSONString(placeOrderCommand));
        if (userId == null || placeOrderCommand == null || !placeOrderCommand.validateParams()) {
            throw new BizException(INVALID_PARAMS);
        }
        String placeOrderLockKey = getPlaceOrderLockKey(userId);
        DistributedLock placeOrderLock = lockFactoryService.getDistributedLock(placeOrderLockKey);
        try {
            boolean isLockSuccess = placeOrderLock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!isLockSuccess) {
                return AppSimpleResult.failed(FREQUENTLY_ERROR.getErrCode(), FREQUENTLY_ERROR.getErrDesc());
            }
            boolean isPassRiskInspect = securityService.inspectRisksByPolicy(userId);
            if (!isPassRiskInspect) {
                logger.info("placeOrder|综合风控检验未通过|{}", userId);
                return AppSimpleResult.failed(PLACE_ORDER_FAILED);
            }
            PlaceOrderResult placeOrderResult = placeOrderService.doPlaceOrder(userId, placeOrderCommand);
            if (!placeOrderResult.isSuccess()) {
                return AppSimpleResult.failed(placeOrderResult.getCode(), placeOrderResult.getMessage());
            }
            logger.info("placeOrder|下单完成|{}", userId);
            return AppSimpleResult.ok(placeOrderResult);
        } catch (Exception e) {
            logger.error("placeOrder|下单失败|{},{}", userId, JSON.toJSONString(placeOrderCommand), e);
            return AppSimpleResult.failed(PLACE_ORDER_FAILED);
        } finally {
            placeOrderLock.unlock();
        }
    }

    @Override
    public AppSimpleResult<OrderTaskHandleResult> getPlaceOrderTaskResult(Long userId, Long itemId, String placeOrderTaskId) {
        if (userId == null || itemId == null || StringUtils.isEmpty(placeOrderTaskId)) {
            throw new BizException(INVALID_PARAMS);
        }
        if (placeOrderService instanceof QueuedPlaceOrderService) {
            QueuedPlaceOrderService queuedPlaceOrderService = (QueuedPlaceOrderService) placeOrderService;
            OrderTaskHandleResult orderTaskHandleResult = queuedPlaceOrderService.getPlaceOrderResult(userId, itemId, placeOrderTaskId);
            if (!orderTaskHandleResult.isSuccess()) {
                return AppSimpleResult.failed(orderTaskHandleResult.getCode(), orderTaskHandleResult.getMessage(), orderTaskHandleResult);
            }
            return AppSimpleResult.ok(orderTaskHandleResult);
        } else {
            return AppSimpleResult.failed(ORDER_TYPE_NOT_SUPPORT);
        }
    }

    @Override
    public AppMultiResult<FlashOrderDTO> getOrdersByUser(Long userId, FlashOrdersQuery flashOrdersQuery) {
        PageResult<FlashOrder> flashOrderPageResult = flashOrderDomainService.getOrdersByUser(userId, toFlashOrdersQuery(flashOrdersQuery));

        List<FlashOrderDTO> flashOrderDTOList = flashOrderPageResult.getData().stream().map(FlashOrderAppBuilder::toFlashOrderDTO).collect(Collectors.toList());
        return AppMultiResult.of(flashOrderDTOList, flashOrderPageResult.getTotal());
    }

    @Override
    @Transactional
    public AppResult cancelOrder(Long userId, Long orderId) {
        logger.info("cancelOrder|取消订单|{},{}", userId, orderId);
        if (userId == null || orderId == null) {
            throw new BizException(INVALID_PARAMS);
        }
        FlashOrder flashOrder = flashOrderDomainService.getOrder(userId, orderId);
        if (flashOrder == null) {
            throw new BizException(ORDER_NOT_FOUND);
        }
        boolean cancelSuccess = flashOrderDomainService.cancelOrder(userId, orderId);
        if (!cancelSuccess) {
            logger.info("cancelOrder|订单取消失败|{}", orderId);
            return AppResult.buildFailure(ORDER_CANCEL_FAILED);
        }
        StockDeduction stockDeduction = new StockDeduction()
                .setItemId(flashOrder.getItemId())
                .setQuantity(flashOrder.getQuantity())
                .setUserId(userId);

        boolean stockRecoverSuccess = stockDeductionDomainService.increaseItemStock(stockDeduction);
        if (!stockRecoverSuccess) {
            logger.info("cancelOrder|库存恢复失败|{}", orderId);
            throw new BizException(ORDER_CANCEL_FAILED);
        }
        boolean stockInRedisRecoverSuccess = itemStockCacheService.increaseItemStock(stockDeduction);
        if (!stockInRedisRecoverSuccess) {
            logger.info("cancelOrder|Redis库存恢复失败|{}", orderId);
            throw new BizException(ORDER_CANCEL_FAILED);
        }
        logger.info("cancelOrder|订单取消成功|{}", orderId);
        return AppResult.buildSuccess();
    }

    private String getPlaceOrderLockKey(Long userId) {
        return link(PLACE_ORDER_LOCK_KEY, userId);
    }
}
