package com.actionworks.flashsale.app.service.item;

import com.actionworks.flashsale.app.auth.AuthorizationService;
import com.actionworks.flashsale.app.auth.model.AuthResult;
import com.actionworks.flashsale.app.exception.BizException;
import com.actionworks.flashsale.app.model.builder.FlashItemAppBuilder;
import com.actionworks.flashsale.app.model.command.FlashItemPublishCommand;
import com.actionworks.flashsale.app.model.dto.FlashItemDTO;
import com.actionworks.flashsale.app.model.query.FlashItemsQuery;
import com.actionworks.flashsale.app.model.result.AppMultiResult;
import com.actionworks.flashsale.app.model.result.AppResult;
import com.actionworks.flashsale.app.model.result.AppSimpleResult;
import com.actionworks.flashsale.app.service.item.cache.FlashItemCacheService;
import com.actionworks.flashsale.app.service.item.cache.FlashItemsCacheService;
import com.actionworks.flashsale.app.service.item.cache.model.FlashItemCache;
import com.actionworks.flashsale.app.service.item.cache.model.FlashItemsCache;
import com.actionworks.flashsale.app.service.stock.ItemStockCacheService;
import com.actionworks.flashsale.app.service.stock.model.ItemStockCache;
import com.actionworks.flashsale.controller.exception.AuthException;
import com.actionworks.flashsale.domain.model.PageResult;
import com.actionworks.flashsale.domain.model.entity.FlashActivity;
import com.actionworks.flashsale.domain.model.entity.FlashItem;
import com.actionworks.flashsale.domain.service.FlashActivityDomainService;
import com.actionworks.flashsale.domain.service.FlashItemDomainService;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.actionworks.flashsale.app.auth.model.ResourceEnum.FLASH_ITEM_CREATE;
import static com.actionworks.flashsale.app.auth.model.ResourceEnum.FLASH_ITEM_OFFLINE;
import static com.actionworks.flashsale.app.exception.AppErrorCode.ACTIVITY_NOT_FOUND;
import static com.actionworks.flashsale.app.exception.AppErrorCode.INVALID_PARAMS;
import static com.actionworks.flashsale.app.exception.AppErrorCode.ITEM_NOT_FOUND;
import static com.actionworks.flashsale.app.model.builder.FlashItemAppBuilder.toDomain;
import static com.actionworks.flashsale.app.model.builder.FlashItemAppBuilder.toFlashItemsQuery;
import static com.actionworks.flashsale.controller.exception.ErrorCode.UNAUTHORIZED_ACCESS;

@Service
public class DefaultFlashItemAppService implements FlashItemAppService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultFlashItemAppService.class);

    @Resource
    private FlashItemDomainService flashItemDomainService;

    @Resource
    private FlashActivityDomainService flashActivityDomainService;

    @Resource
    private AuthorizationService authorizationService;

    @Resource
    private FlashItemCacheService flashItemCacheService;
    @Resource
    private FlashItemsCacheService flashItemsCacheService;
    @Resource
    private ItemStockCacheService itemStockCacheService;

    @Override
    public AppResult publishFlashItem(Long userId, Long activityId, FlashItemPublishCommand itemPublishCommand) {
        logger.info("itemPublish|发布秒杀品|{},{},{}", userId, activityId, JSON.toJSON(itemPublishCommand));
        AuthResult authResult = authorizationService.auth(userId, FLASH_ITEM_CREATE);
        if (!authResult.isSuccess()) {
            throw new AuthException(UNAUTHORIZED_ACCESS);
        }
        if (userId == null || activityId == null || itemPublishCommand == null || !itemPublishCommand.validate()) {
            throw new BizException(INVALID_PARAMS);
        }
        FlashActivity flashActivity = flashActivityDomainService.getFlashActivity(activityId);
        if (flashActivity == null) {
            throw new BizException(ACTIVITY_NOT_FOUND);
        }
        FlashItem flashItem = toDomain(itemPublishCommand);
        flashItem.setActivityId(activityId);
        flashItem.setStockWarmUp(0);
        flashItemDomainService.publishFlashItem(flashItem);
        logger.info("itemPublish|秒杀品已发布");
        return AppResult.buildSuccess();
    }

    @Override
    public AppResult onlineFlashItem(Long userId, Long activityId, Long itemId) {
        logger.info("itemOnline|上线秒杀品|{},{},{}", userId, activityId, itemId);
        AuthResult authResult = authorizationService.auth(userId, FLASH_ITEM_OFFLINE);
        if (!authResult.isSuccess()) {
            throw new AuthException(UNAUTHORIZED_ACCESS);
        }
        if (userId == null || activityId == null || itemId == null) {
            throw new BizException(INVALID_PARAMS);
        }
        flashItemDomainService.onlineFlashItem(itemId);
        logger.info("itemOnline|秒杀品已上线");
        return AppResult.buildSuccess();
    }

    @Override
    public AppResult offlineFlashItem(Long userId, Long activityId, Long itemId) {
        logger.info("itemOffline|下线秒杀品|{},{},{}", userId, activityId, itemId);
        AuthResult authResult = authorizationService.auth(userId, FLASH_ITEM_OFFLINE);
        if (!authResult.isSuccess()) {
            throw new AuthException(UNAUTHORIZED_ACCESS);
        }
        if (userId == null || activityId == null || itemId == null) {
            throw new BizException(INVALID_PARAMS);
        }
        flashItemDomainService.offlineFlashItem(itemId);
        logger.info("itemOffline|秒杀品已下线");
        return AppResult.buildSuccess();
    }

    @Override
    public AppMultiResult<FlashItemDTO> getFlashItems(Long userId, Long activityId, FlashItemsQuery flashItemsQuery) {
        if (flashItemsQuery == null) {
            return AppMultiResult.empty();
        }
        flashItemsQuery.setActivityId(activityId);
        List<FlashItem> items;
        Integer total;
        if (flashItemsQuery.isOnlineFirstPageQuery()) {
            FlashItemsCache flashItemsCache = flashItemsCacheService.getCachedItems(activityId, flashItemsQuery.getVersion());
            if (flashItemsCache.isLater()) {
                return AppMultiResult.tryLater();
            }
            if(flashItemsCache.isEmpty()){
                return AppMultiResult.empty();
            }
            items = flashItemsCache.getFlashItems();
            total = flashItemsCache.getTotal();
        } else {
            PageResult<FlashItem> flashItemPageResult = flashItemDomainService.getFlashItems(toFlashItemsQuery(flashItemsQuery));
            items = flashItemPageResult.getData();
            total = flashItemPageResult.getTotal();
        }

        List<FlashItemDTO> flashItemDTOList = items.stream().map(FlashItemAppBuilder::toFlashItemDTO).collect(Collectors.toList());
        return AppMultiResult.of(flashItemDTOList, total);
    }


    @Override
    public AppSimpleResult<FlashItemDTO> getFlashItem(Long userId, Long activityId, Long itemId, Long version) {
        logger.info("itemGet|读取秒杀品|{},{},{}", userId, activityId, itemId, version);
        FlashItemCache flashItemCache = flashItemCacheService.getCachedItem(itemId, version);
        if (!flashItemCache.isExist()) {
            throw new BizException(ITEM_NOT_FOUND.getErrDesc());
        }
        if (flashItemCache.isLater()) {
            return AppSimpleResult.tryLater();
        }
        updateLatestItemStock(userId, flashItemCache.getFlashItem());
        FlashItemDTO flashItemDTO = FlashItemAppBuilder.toFlashItemDTO(flashItemCache.getFlashItem());
        flashItemDTO.setVersion(flashItemCache.getVersion());
        return AppSimpleResult.ok(flashItemDTO);
    }

    @Override
    public AppSimpleResult<FlashItemDTO> getFlashItem(Long itemId) {
        FlashItemCache flashItemCache = flashItemCacheService.getCachedItem(itemId, null);
        if (!flashItemCache.isExist()) {
            throw new BizException(ITEM_NOT_FOUND.getErrDesc());
        }
        if (flashItemCache.isLater()) {
            return AppSimpleResult.tryLater();
        }
        updateLatestItemStock(null, flashItemCache.getFlashItem());
        FlashItemDTO flashItemDTO = FlashItemAppBuilder.toFlashItemDTO(flashItemCache.getFlashItem());
        flashItemDTO.setVersion(flashItemCache.getVersion());
        return AppSimpleResult.ok(flashItemDTO);
    }

    @Override
    public boolean isAllowPlaceOrderOrNot(Long itemId) {
        FlashItemCache flashItemCache = flashItemCacheService.getCachedItem(itemId, null);
        if (!flashItemCache.isLater()) {
            logger.info("isAllowPlaceOrderOrNot|稍后再试|{}", itemId);
            return false;
        }
        if (!flashItemCache.isExist() || flashItemCache.getFlashItem() == null) {
            logger.info("isAllowPlaceOrderOrNot|秒杀品不存在|{}", itemId);
            return false;
        }
        if (!flashItemCache.getFlashItem().isOnline()) {
            logger.info("isAllowPlaceOrderOrNot|秒杀品尚未上线|{}", itemId);
            return false;
        }
        if (!flashItemCache.getFlashItem().isInProgress()) {
            logger.info("isAllowPlaceOrderOrNot|当前非秒杀时段|{}", itemId);
            return false;
        }
        // 可在此处丰富其他校验规则

        return true;
    }

    private void updateLatestItemStock(Long userId, FlashItem flashItem) {
        if (flashItem == null) {
            return;
        }
        ItemStockCache itemStockCache = itemStockCacheService.getAvailableItemStock(userId, flashItem.getId());
        if (itemStockCache != null && itemStockCache.isSuccess() && itemStockCache.getAvailableStock() != null) {
            flashItem.setAvailableStock(itemStockCache.getAvailableStock());
        }
    }
}
