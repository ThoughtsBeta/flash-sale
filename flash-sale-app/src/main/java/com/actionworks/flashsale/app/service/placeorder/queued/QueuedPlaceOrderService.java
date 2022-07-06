package com.actionworks.flashsale.app.service.placeorder.queued;

import com.actionworks.flashsale.app.exception.BizException;
import com.actionworks.flashsale.app.model.PlaceOrderTask;
import com.actionworks.flashsale.app.model.builder.PlaceOrderTaskBuilder;
import com.actionworks.flashsale.app.model.command.FlashPlaceOrderCommand;
import com.actionworks.flashsale.app.model.dto.FlashItemDTO;
import com.actionworks.flashsale.app.model.enums.OrderTaskStatus;
import com.actionworks.flashsale.app.model.result.AppSimpleResult;
import com.actionworks.flashsale.app.model.result.OrderTaskHandleResult;
import com.actionworks.flashsale.app.model.result.OrderTaskSubmitResult;
import com.actionworks.flashsale.app.model.result.PlaceOrderResult;
import com.actionworks.flashsale.app.service.activity.FlashActivityAppService;
import com.actionworks.flashsale.app.service.item.FlashItemAppService;
import com.actionworks.flashsale.app.service.placeorder.PlaceOrderService;
import com.actionworks.flashsale.app.util.OrderNoGenerateContext;
import com.actionworks.flashsale.app.util.OrderNoGenerateService;
import com.actionworks.flashsale.app.util.OrderTaskIdGenerateService;
import com.actionworks.flashsale.cache.redis.RedisCacheService;
import com.actionworks.flashsale.domain.model.StockDeduction;
import com.actionworks.flashsale.domain.model.entity.FlashItem;
import com.actionworks.flashsale.domain.model.entity.FlashOrder;
import com.actionworks.flashsale.domain.service.FlashItemDomainService;
import com.actionworks.flashsale.domain.service.FlashOrderDomainService;
import com.actionworks.flashsale.domain.service.StockDeductionDomainService;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static com.actionworks.flashsale.app.exception.AppErrorCode.GET_ITEM_FAILED;
import static com.actionworks.flashsale.app.exception.AppErrorCode.INVALID_PARAMS;
import static com.actionworks.flashsale.app.exception.AppErrorCode.ITEM_NOT_ON_SALE;
import static com.actionworks.flashsale.app.exception.AppErrorCode.PLACE_ORDER_FAILED;
import static com.actionworks.flashsale.app.exception.AppErrorCode.PLACE_ORDER_TASK_ID_INVALID;
import static com.actionworks.flashsale.app.model.builder.FlashOrderAppBuilder.toDomain;
import static com.actionworks.flashsale.app.model.constants.CacheConstants.HOURS_24;

@Service
@ConditionalOnProperty(name = "place_order_type", havingValue = "queued")
public class QueuedPlaceOrderService implements PlaceOrderService {
    private static final String PLACE_ORDER_TASK_ORDER_ID_KEY = "PLACE_ORDER_TASK_ORDER_ID_KEY_";
    private static final Logger logger = LoggerFactory.getLogger(QueuedPlaceOrderService.class);
    @Resource
    private FlashItemAppService flashItemAppService;
    @Resource
    private OrderTaskIdGenerateService orderTaskIdGenerateService;
    @Resource
    private PlaceOrderTaskService placeOrderTaskService;
    @Resource
    private FlashOrderDomainService flashOrderDomainService;
    @Resource
    private FlashItemDomainService flashItemDomainService;
    @Resource
    private FlashActivityAppService flashActivityAppService;
    @Resource
    private StockDeductionDomainService stockDeductionDomainService;
    @Resource
    private OrderNoGenerateService orderNoGenerateService;
    @Resource
    private RedisCacheService redisCacheService;

    @PostConstruct
    public void init() {
        logger.info("initPlaceOrderService|异步队列下单服务已经初始化");
    }

    @Override
    public PlaceOrderResult doPlaceOrder(Long userId, FlashPlaceOrderCommand placeOrderCommand) {
        logger.info("placeOrder|开始下单|{},{}", userId, JSON.toJSONString(placeOrderCommand));
        if (placeOrderCommand == null || !placeOrderCommand.validateParams()) {
            return PlaceOrderResult.failed(INVALID_PARAMS);
        }

        AppSimpleResult<FlashItemDTO> flashItemResult = flashItemAppService.getFlashItem(placeOrderCommand.getItemId());
        if (!flashItemResult.isSuccess() || flashItemResult.getData() == null) {
            logger.info("placeOrder|获取秒杀品失败|{},{}", userId, placeOrderCommand.getActivityId());
            return PlaceOrderResult.failed(GET_ITEM_FAILED);
        }
        FlashItemDTO flashItemDTO = flashItemResult.getData();
        if (!flashItemDTO.isOnSale()) {
            logger.info("placeOrder|当前非在售时间|{},{}", userId, placeOrderCommand.getActivityId());
            return PlaceOrderResult.failed(ITEM_NOT_ON_SALE);
        }

        String placeOrderTaskId = orderTaskIdGenerateService.generatePlaceOrderTaskId(userId, placeOrderCommand.getItemId());

        PlaceOrderTask placeOrderTask = PlaceOrderTaskBuilder.with(userId, placeOrderCommand);
        placeOrderTask.setPlaceOrderTaskId(placeOrderTaskId);
        OrderTaskSubmitResult submitResult = placeOrderTaskService.submit(placeOrderTask);
        logger.info("placeOrder|任务提交结果|{},{},{}", userId, placeOrderTaskId, JSON.toJSONString(placeOrderTask));

        if (!submitResult.isSuccess()) {
            logger.info("placeOrder|下单任务提交失败|{},{}", userId, placeOrderCommand.getActivityId());
            return PlaceOrderResult.failed(submitResult.getCode(), submitResult.getMessage());
        }
        logger.info("placeOrder|下单任务提交完成|{},{}", userId, placeOrderTaskId);
        return PlaceOrderResult.ok(placeOrderTaskId);
    }

    @Transactional
    public void handlePlaceOrderTask(PlaceOrderTask placeOrderTask) {
        try {
            Long userId = placeOrderTask.getUserId();
            boolean isActivityAllowPlaceOrder = flashActivityAppService.isAllowPlaceOrderOrNot(placeOrderTask.getActivityId());
            if (!isActivityAllowPlaceOrder) {
                logger.info("handleOrderTask|秒杀活动下单规则校验未通过|{},{}", placeOrderTask.getPlaceOrderTaskId(), placeOrderTask.getActivityId());
                placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
                return;
            }
            boolean isItemAllowPlaceOrder = flashItemAppService.isAllowPlaceOrderOrNot(placeOrderTask.getItemId());
            if (!isItemAllowPlaceOrder) {
                logger.info("handleOrderTask|秒杀品下单规则校验未通过|{},{}", placeOrderTask.getPlaceOrderTaskId(), placeOrderTask.getActivityId());
                placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
                return;
            }
            FlashItem flashItem = flashItemDomainService.getFlashItem(placeOrderTask.getItemId());
            Long orderId = orderNoGenerateService.generateOrderNo(new OrderNoGenerateContext());
            FlashOrder flashOrderToPlace = toDomain(placeOrderTask);
            flashOrderToPlace.setItemTitle(flashItem.getItemTitle());
            flashOrderToPlace.setFlashPrice(flashItem.getFlashPrice());
            flashOrderToPlace.setUserId(userId);
            flashOrderToPlace.setId(orderId);

            StockDeduction stockDeduction = new StockDeduction()
                    .setItemId(placeOrderTask.getItemId())
                    .setQuantity(placeOrderTask.getQuantity());
            boolean decreaseStockSuccess = stockDeductionDomainService.decreaseItemStock(stockDeduction);
            if (!decreaseStockSuccess) {
                logger.info("handleOrderTask|库存扣减失败|{},{}", placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(placeOrderTask));
                return;
            }
            boolean placeOrderSuccess = flashOrderDomainService.placeOrder(userId, flashOrderToPlace);
            if (!placeOrderSuccess) {
                throw new BizException(PLACE_ORDER_FAILED.getErrDesc());
            }
            placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), true);
            redisCacheService.put(PLACE_ORDER_TASK_ORDER_ID_KEY + placeOrderTask.getPlaceOrderTaskId(), orderId, HOURS_24);
            logger.info("handleOrderTask|下单任务处理完成|{},{}", placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(placeOrderTask));
        } catch (Exception e) {
            placeOrderTaskService.updateTaskHandleResult(placeOrderTask.getPlaceOrderTaskId(), false);
            logger.error("handleOrderTask|下单任务处理错误|{},{}", placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(placeOrderTask), e);
            throw new BizException(e.getMessage());
        }
    }

    public OrderTaskHandleResult getPlaceOrderResult(Long userId, Long itemId, String placeOrderTaskId) {
        String generatedPlaceOrderTaskId = orderTaskIdGenerateService.generatePlaceOrderTaskId(userId, itemId);
        if (!generatedPlaceOrderTaskId.equals(placeOrderTaskId)) {
            return OrderTaskHandleResult.failed(PLACE_ORDER_TASK_ID_INVALID);
        }
        OrderTaskStatus orderTaskStatus = placeOrderTaskService.getTaskStatus(placeOrderTaskId);
        if (orderTaskStatus == null) {
            return OrderTaskHandleResult.failed(PLACE_ORDER_TASK_ID_INVALID);
        }
        if (!OrderTaskStatus.SUCCESS.equals(orderTaskStatus)) {
            return OrderTaskHandleResult.failed(orderTaskStatus);
        }
        Long orderId = redisCacheService.getObject(PLACE_ORDER_TASK_ORDER_ID_KEY + placeOrderTaskId, Long.class);
        return OrderTaskHandleResult.ok(orderId);
    }
}
