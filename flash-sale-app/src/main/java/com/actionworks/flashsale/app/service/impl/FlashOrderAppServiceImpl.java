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
import com.actionworks.flashsale.app.security.SecurityService;
import com.actionworks.flashsale.app.service.FlashOrderAppService;
import com.actionworks.flashsale.controller.exception.AuthException;
import com.actionworks.flashsale.domain.model.PageResult;
import com.actionworks.flashsale.domain.model.entity.FlashItem;
import com.actionworks.flashsale.domain.model.entity.FlashOrder;
import com.actionworks.flashsale.domain.service.FlashActivityDomainService;
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

import static com.actionworks.flashsale.app.exception.AppErrorCode.OPERATE_TOO_QUICKILY_ERROR;
import static com.actionworks.flashsale.app.exception.AppErrorCode.ORDER_CANCEL_FAILED;
import static com.actionworks.flashsale.app.exception.AppErrorCode.ORDER_NOT_FOUND;
import static com.actionworks.flashsale.app.exception.AppErrorCode.PLACE_ORDER_FAILED;
import static com.actionworks.flashsale.app.model.builder.FlashOrderAppBuilder.toDomain;
import static com.actionworks.flashsale.app.model.builder.FlashOrderAppBuilder.toFlashOrdersQuery;
import static com.actionworks.flashsale.controller.exception.ErrorCode.INVALID_TOKEN;

@Service
public class FlashOrderAppServiceImpl implements FlashOrderAppService {
    private static final Logger logger = LoggerFactory.getLogger(FlashOrderAppServiceImpl.class);

    @Resource
    private FlashOrderDomainService flashOrderDomainService;
    @Resource
    private FlashItemDomainService flashItemDomainService;
    @Resource
    private FlashActivityDomainService flashActivityDomainService;
    @Resource
    private AuthorizationService authorizationService;
    @Resource
    private ItemStockCacheService itemStockCacheService;
    @Resource
    private DistributedLockFactoryService lockFactoryService;
    @Resource
    private SecurityService securityService;

    @Override
    @Transactional
    public AppResult placeOrder(String token, FlashPlaceOrderCommand placeOrderCommand) {
        AuthResult authResult = authorizationService.auth(token);
        if (!authResult.isSuccess()) {
            throw new AuthException(INVALID_TOKEN);
        }
        boolean isPassRiskInspect = securityService.inspectRisksByPolicy(authResult.getUserId());
        if (!isPassRiskInspect) {
            logger.info("placeOrder|综合风控检验未通过:{}", authResult.getUserId());
            return AppResult.buildFailure(PLACE_ORDER_FAILED);
        }
        boolean isActivityAllowPlaceOrder = flashActivityDomainService.isAllowPlaceOrderOrNot(placeOrderCommand.getActivityId());
        if (!isActivityAllowPlaceOrder) {
            logger.info("placeOrder|秒杀活动下单规则校验未通过:{}", authResult.getUserId(), placeOrderCommand.getActivityId());
            return AppResult.buildFailure(PLACE_ORDER_FAILED);
        }
        boolean isItemAllowPlaceOrder = flashItemDomainService.isAllowPlaceOrderOrNot(placeOrderCommand.getItemId());
        if (!isItemAllowPlaceOrder) {
            logger.info("placeOrder|秒杀品下单规则校验未通过:{}", authResult.getUserId(), placeOrderCommand.getActivityId());
            return AppResult.buildFailure(PLACE_ORDER_FAILED);
        }
        FlashItem flashItem = flashItemDomainService.getFlashItem(placeOrderCommand.getItemId());
        FlashOrder flashOrderToPlace = toDomain(placeOrderCommand);
        flashOrderToPlace.setItemTitle(flashItem.getItemTitle());
        flashOrderToPlace.setFlashPrice(flashItem.getFlashPrice());
        flashOrderToPlace.setUserId(authResult.getUserId());

        String placeOrderLockKey = "PLACE_ORDER_" + authResult.getUserId();
        DistributedLock placeOrderLock = lockFactoryService.getDistributedLock(placeOrderLockKey);
        boolean preDecreaseStockSuccess = false;
        try {
            boolean isLockSuccess = placeOrderLock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!isLockSuccess) {
                return AppResult.buildFailure(OPERATE_TOO_QUICKILY_ERROR.getErrCode(), OPERATE_TOO_QUICKILY_ERROR.getErrDesc());
            }
            preDecreaseStockSuccess = itemStockCacheService.decreaseItemStock(authResult.getUserId(), placeOrderCommand.getItemId(), placeOrderCommand.getQuantity());
            if (!preDecreaseStockSuccess) {
                logger.info("placeOrder|库存预扣减失败:{},{}", authResult.getUserId(), JSON.toJSONString(placeOrderCommand));
                return AppResult.buildFailure(PLACE_ORDER_FAILED.getErrCode(), PLACE_ORDER_FAILED.getErrDesc());
            }
            boolean decreaseStockSuccess = flashItemDomainService.decreaseItemStock(placeOrderCommand.getItemId(), placeOrderCommand.getQuantity());
            if (!decreaseStockSuccess) {
                logger.info("placeOrder|库存预扣减失败:{},{}", authResult.getUserId(), JSON.toJSONString(placeOrderCommand));
                return AppResult.buildFailure(PLACE_ORDER_FAILED.getErrCode(), PLACE_ORDER_FAILED.getErrDesc());
            }
            boolean placeOrderSuccess = flashOrderDomainService.placeOrder(authResult.getUserId(), flashOrderToPlace);
            if (!placeOrderSuccess) {
                throw new BizException(PLACE_ORDER_FAILED.getErrDesc());
            }
        } catch (Exception e) {
            if (preDecreaseStockSuccess) {
                boolean recoverStockSuccess = itemStockCacheService.increaseItemStock(authResult.getUserId(), placeOrderCommand.getItemId(), placeOrderCommand.getQuantity());
                if (!recoverStockSuccess) {
                    logger.error("placeOrder|预扣库存恢复失败:{},{}", authResult.getUserId(), JSON.toJSONString(placeOrderCommand), e);
                }
            }
            logger.error("placeOrder|下单失败:{},{}", authResult.getUserId(), JSON.toJSONString(placeOrderCommand), e);
            throw new BizException(PLACE_ORDER_FAILED.getErrDesc());
        } finally {
            placeOrderLock.forceUnlock();
        }
        return AppResult.buildSuccess();
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
