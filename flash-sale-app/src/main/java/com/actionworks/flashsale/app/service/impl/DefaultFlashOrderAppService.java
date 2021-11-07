package com.actionworks.flashsale.app.service.impl;

import com.actionworks.flashsale.app.auth.AuthorizationService;
import com.actionworks.flashsale.app.auth.model.AuthResult;
import com.actionworks.flashsale.app.cache.ItemStockCacheService;
import com.actionworks.flashsale.app.model.builder.FlashOrderAppBuilder;
import com.actionworks.flashsale.app.model.command.FlashPlaceOrderCommand;
import com.actionworks.flashsale.app.model.dto.FlashOrderDTO;
import com.actionworks.flashsale.app.model.query.FlashOrdersQuery;
import com.actionworks.flashsale.app.model.result.AppMultiResult;
import com.actionworks.flashsale.app.model.result.AppResult;
import com.actionworks.flashsale.app.model.result.AppSingleResult;
import com.actionworks.flashsale.app.model.result.OrderTaskHandleResult;
import com.actionworks.flashsale.app.model.result.PlaceOrderResult;
import com.actionworks.flashsale.app.security.SecurityService;
import com.actionworks.flashsale.app.service.FlashOrderAppService;
import com.actionworks.flashsale.app.service.PlaceOrderService;
import com.actionworks.flashsale.controller.exception.AuthException;
import com.actionworks.flashsale.domain.model.PageResult;
import com.actionworks.flashsale.domain.model.entity.FlashOrder;
import com.actionworks.flashsale.domain.service.FlashItemDomainService;
import com.actionworks.flashsale.domain.service.FlashOrderDomainService;
import com.actionworks.flashsale.lock.DistributedLock;
import com.actionworks.flashsale.lock.DistributedLockFactoryService;
import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.actionworks.flashsale.app.exception.AppErrorCode.FREQUENTLY_ERROR;
import static com.actionworks.flashsale.app.exception.AppErrorCode.ORDER_CANCEL_FAILED;
import static com.actionworks.flashsale.app.exception.AppErrorCode.ORDER_NOT_FOUND;
import static com.actionworks.flashsale.app.exception.AppErrorCode.ORDER_TYPE_NOT_SUPPORT;
import static com.actionworks.flashsale.app.exception.AppErrorCode.PLACE_ORDER_FAILED;
import static com.actionworks.flashsale.app.model.builder.FlashOrderAppBuilder.toFlashOrdersQuery;
import static com.actionworks.flashsale.controller.exception.ErrorCode.INVALID_TOKEN;

@Service
public class DefaultFlashOrderAppService implements FlashOrderAppService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultFlashOrderAppService.class);

    @Resource
    private FlashOrderDomainService flashOrderDomainService;
    @Resource
    private FlashItemDomainService flashItemDomainService;

    @Resource
    private AuthorizationService authorizationService;
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
    public AppSingleResult<PlaceOrderResult> placeOrder(String token, FlashPlaceOrderCommand placeOrderCommand) {
        AuthResult authResult = authorizationService.auth(token);
        if (!authResult.isSuccess()) {
            throw new AuthException(INVALID_TOKEN);
        }
        String placeOrderLockKey = "PLACE_ORDER_" + authResult.getUserId();
        DistributedLock placeOrderLock = lockFactoryService.getDistributedLock(placeOrderLockKey);
        try {
            boolean isLockSuccess = placeOrderLock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!isLockSuccess) {
                return AppSingleResult.failed(FREQUENTLY_ERROR.getErrCode(), FREQUENTLY_ERROR.getErrDesc());
            }
            boolean isPassRiskInspect = securityService.inspectRisksByPolicy(authResult.getUserId());
            if (!isPassRiskInspect) {
                logger.info("placeOrder|综合风控检验未通过:{}", authResult.getUserId());
                return AppSingleResult.failed(PLACE_ORDER_FAILED);
            }
            PlaceOrderResult placeOrderResult = placeOrderService.placeOrder(authResult.getUserId(), placeOrderCommand);
            if (!placeOrderResult.isSuccess()) {
                return AppSingleResult.failed(placeOrderResult.getCode(), placeOrderResult.getMessage());
            }
            return AppSingleResult.ok(placeOrderResult);
        } catch (Exception e) {
            logger.error("placeOrder|下单失败:{},{}", authResult.getUserId(), JSON.toJSONString(placeOrderCommand), e);
            return AppSingleResult.failed(PLACE_ORDER_FAILED);
        } finally {
            placeOrderLock.forceUnlock();
        }
    }

    @Override
    public AppSingleResult<OrderTaskHandleResult> getPlaceOrderTaskResult(String token, Long itemId, String placeOrderTaskId) {
        AuthResult authResult = authorizationService.auth(token);
        if (!authResult.isSuccess()) {
            throw new AuthException(INVALID_TOKEN);
        }
        if (placeOrderService instanceof QueuedPlaceOrderService) {
            QueuedPlaceOrderService queuedPlaceOrderService = (QueuedPlaceOrderService) placeOrderService;
            OrderTaskHandleResult orderTaskHandleResult = queuedPlaceOrderService.getPlaceOrderResult(authResult.getUserId(), itemId, placeOrderTaskId);
            if (!orderTaskHandleResult.isSuccess()) {
                return AppSingleResult.failed(orderTaskHandleResult.getCode(), orderTaskHandleResult.getMessage(), orderTaskHandleResult);
            }
            return AppSingleResult.ok(orderTaskHandleResult);
        } else {
            return AppSingleResult.failed(ORDER_TYPE_NOT_SUPPORT);
        }
    }

    @Override
    public AppMultiResult<FlashOrderDTO> getOrdersByUser(String token, FlashOrdersQuery flashOrdersQuery) {
        AuthResult authResult = authorizationService.auth(token);
        if (!authResult.isSuccess()) {
            throw new AuthException(INVALID_TOKEN);
        }
        PageResult<FlashOrder> flashOrderPageResult = flashOrderDomainService.getOrdersByUser(authResult.getUserId(), toFlashOrdersQuery(flashOrdersQuery));

        List<FlashOrderDTO> flashOrderDTOList = flashOrderPageResult.getData().stream().map(FlashOrderAppBuilder::toFlashOrderDTO).collect(Collectors.toList());
        return AppMultiResult.of(flashOrderDTOList, flashOrderPageResult.getTotal());
    }

    @Override
    @Transactional
    public AppResult cancelOrder(String token, Long orderId) {
        AuthResult authResult = authorizationService.auth(token);
        if (!authResult.isSuccess()) {
            throw new AuthException(INVALID_TOKEN);
        }
        FlashOrder flashOrder = flashOrderDomainService.getOrder(authResult.getUserId(), orderId);
        if (flashOrder == null) {
            throw new BizException(ORDER_NOT_FOUND.getErrDesc());
        }
        boolean cancelSuccess = flashOrderDomainService.cancelOrder(authResult.getUserId(), orderId);
        if (!cancelSuccess) {
            logger.info("cancelOrder|订单取消失败:{},{}", authResult.getUserId(), orderId);
            return AppResult.buildFailure(ORDER_CANCEL_FAILED);
        }
        boolean stockRecoverSuccess = flashItemDomainService.increaseItemStock(flashOrder.getItemId(), flashOrder.getQuantity());
        if (!stockRecoverSuccess) {
            logger.info("cancelOrder|库存恢复失败:{},{}", authResult.getUserId(), orderId);
            throw new BizException(ORDER_CANCEL_FAILED.getErrDesc());
        }
        boolean stockInRedisRecoverSuccess = itemStockCacheService.increaseItemStock(authResult.getUserId(), flashOrder.getItemId(), flashOrder.getQuantity());
        if (!stockInRedisRecoverSuccess) {
            logger.info("cancelOrder|Redis库存恢复失败:{},{}", authResult.getUserId(), orderId);
            throw new BizException(ORDER_CANCEL_FAILED.getErrDesc());
        }
        return AppResult.buildSuccess();
    }
}
