package com.actionworks.flashsale.app.service.item;

import com.actionworks.flashsale.app.auth.AuthorizationService;
import com.actionworks.flashsale.app.auth.model.AuthResult;
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
import com.alibaba.cola.exception.BizException;
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
import static com.actionworks.flashsale.app.exception.AppErrorCode.ITEM_NOT_FOUND;
import static com.actionworks.flashsale.app.model.builder.FlashItemAppBuilder.toDomain;
import static com.actionworks.flashsale.app.model.builder.FlashItemAppBuilder.toFlashItemsQuery;
import static com.actionworks.flashsale.controller.exception.ErrorCode.INVALID_TOKEN;

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
    public AppResult publishFlashItem(String token, Long activityId, FlashItemPublishCommand itemPublishCommand) {
        logger.info("itemPublish|发布秒杀品|{},{},{}", token, activityId, JSON.toJSON(itemPublishCommand));
        AuthResult authResult = authorizationService.auth(token, FLASH_ITEM_CREATE);
        if (!authResult.isSuccess()) {
            throw new AuthException(INVALID_TOKEN);
        }
        FlashActivity flashActivity = flashActivityDomainService.getFlashActivity(activityId);
        if (flashActivity == null) {
            throw new BizException(ACTIVITY_NOT_FOUND.getErrDesc());
        }
        FlashItem flashItem = toDomain(itemPublishCommand);
        flashItem.setActivityId(activityId);
        flashItem.setStockWarmUp(0);
        flashItemDomainService.publishFlashItem(flashItem);
        logger.info("itemPublish|秒杀品已发布");
        return AppResult.buildSuccess();
    }

    @Override
    public AppResult onlineFlashItem(String token, Long activityId, Long itemId) {
        logger.info("itemOnline|上线秒杀品|{},{},{}", token, activityId, itemId);
        AuthResult authResult = authorizationService.auth(token, FLASH_ITEM_OFFLINE);
        if (!authResult.isSuccess()) {
            throw new AuthException(INVALID_TOKEN);
        }
        flashItemDomainService.onlineFlashItem(itemId);
        logger.info("itemOnline|秒杀品已上线");
        return AppResult.buildSuccess();
    }

    @Override
    public AppResult offlineFlashItem(String token, Long activityId, Long itemId) {
        logger.info("itemOffline|下线秒杀品|{},{},{}", token, activityId, itemId);
        AuthResult authResult = authorizationService.auth(token, FLASH_ITEM_OFFLINE);
        if (!authResult.isSuccess()) {
            throw new AuthException(INVALID_TOKEN);
        }
        flashItemDomainService.offlineFlashItem(itemId);
        logger.info("itemOffline|秒杀品已下线");
        return AppResult.buildSuccess();
    }

    @Override
    public AppMultiResult<FlashItemDTO> getFlashItems(String token, Long activityId, FlashItemsQuery flashItemsQuery) {
        if (flashItemsQuery == null) {
            return AppMultiResult.empty();
        }
        flashItemsQuery.setActivityId(activityId);
        List<FlashItem> activities;
        Integer total;
        if (flashItemsQuery.isOnlineFirstPageQuery()) {
            FlashItemsCache flashItemsCache = flashItemsCacheService.getCachedItems(activityId, flashItemsQuery.getVersion());
            if (flashItemsCache.isLater()) {
                return AppMultiResult.tryLater();
            }
            activities = flashItemsCache.getFlashItems();
            total = flashItemsCache.getTotal();
        } else {
            PageResult<FlashItem> flashItemPageResult = flashItemDomainService.getFlashItems(toFlashItemsQuery(flashItemsQuery));
            activities = flashItemPageResult.getData();
            total = flashItemPageResult.getTotal();
        }

        List<FlashItemDTO> flashItemDTOList = activities.stream().map(FlashItemAppBuilder::toFlashItemDTO).collect(Collectors.toList());
        return AppMultiResult.of(flashItemDTOList, total);
    }


    @Override
    public AppSimpleResult<FlashItemDTO> getFlashItem(String token, Long activityId, Long itemId, Long version) {
        logger.info("itemGet|读取秒杀品|{},{},{}", token, activityId, itemId, version);
        AuthResult authResult = authorizationService.auth(token);
        if (!authResult.isSuccess()) {
            throw new AuthException(INVALID_TOKEN);
        }

        FlashItemCache flashItemCache = flashItemCacheService.getCachedItem(itemId, version);
        if (!flashItemCache.isExist()) {
            throw new BizException(ITEM_NOT_FOUND.getErrDesc());
        }
        if (flashItemCache.isLater()) {
            return AppSimpleResult.tryLater();
        }
        updateLatestItemStock(authResult.getUserId(), flashItemCache.getFlashItem());
        FlashItemDTO flashItemDTO = FlashItemAppBuilder.toFlashItemDTO(flashItemCache.getFlashItem());
        flashItemDTO.setVersion(flashItemCache.getVersion());
        return AppSimpleResult.ok(flashItemDTO);
    }

    @Override
    public AppSimpleResult<FlashItemDTO> getFlashItem(Long itemId) {
        FlashItemCache flashItemCache = flashItemCacheService.getCachedItem(itemId, null);
        if (!flashItemCache.isExist()) {
            throw new BizException(ACTIVITY_NOT_FOUND.getErrDesc());
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
        try {
            FlashItemCache flashItemCache = null;
            for (int i = 0; i < 3; i++) {
                flashItemCache = flashItemCacheService.getCachedItem(itemId, null);
                if (!flashItemCache.isExist()) {
                    return false;
                }
                if (flashItemCache.isLater()) {
                    Thread.sleep(20);
                }
                if (flashItemCache.getFlashItem() != null) {
                    break;
                }
            }
            if (flashItemCache.getFlashItem() != null) {
                if (!flashItemCache.getFlashItem().isOnline()) {
                    logger.info("isAllowPlaceOrderOrNot|秒杀品尚未上线|{}", itemId);
                    return false;
                }
                if (!flashItemCache.getFlashItem().isInProgress()) {
                    logger.info("isAllowPlaceOrderOrNot|当前非秒杀时段|{}", itemId);
                    return false;
                }
                return true;
            }
        } catch (InterruptedException e) {
            return false;
        }
        return false;
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
