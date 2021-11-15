package com.actionworks.flashsale.persistence.repository;

import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashItem;
import com.actionworks.flashsale.domain.repository.FlashItemRepository;
import com.actionworks.flashsale.persistence.convertor.FlashItemBuilder;
import com.actionworks.flashsale.persistence.mapper.FlashItemMapper;
import com.actionworks.flashsale.persistence.model.FlashItemDO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FlashItemRepositoryImpl implements FlashItemRepository {
    @Resource
    private FlashItemMapper flashItemMapper;

    @Override
    public int save(FlashItem flashItem) {
        FlashItemDO flashItemDO = FlashItemBuilder.toDataObjectForCreate(flashItem);
        if (flashItem.getId() == null) {
            return flashItemMapper.insert(flashItemDO);
        }
        return flashItemMapper.update(flashItemDO);
    }

    @Override
    public Optional<FlashItem> findById(Long itemId) {
        FlashItemDO flashItemDO = flashItemMapper.getById(itemId);
        if (flashItemDO == null) {
            return Optional.empty();
        }
        FlashItem flashItem = FlashItemBuilder.toDomainObject(flashItemDO);
        return Optional.of(flashItem);
    }

    @Override
    public List<FlashItem> findFlashItemsByCondition(PagesQueryCondition pagesQueryCondition) {
        return flashItemMapper.findFlashItemsByCondition(pagesQueryCondition)
                .stream()
                .map(FlashItemBuilder::toDomainObject)
                .collect(Collectors.toList());
    }

    @Override
    public Integer countFlashItemsByCondition(PagesQueryCondition pagesQueryCondition) {
        return flashItemMapper.countFlashItemsByCondition(pagesQueryCondition);
    }

    @Override
    public boolean decreaseItemStock(Long itemId, Integer quantity) {
        return flashItemMapper.decreaseItemStock(itemId, quantity) == 1;
    }

    @Override
    public boolean increaseItemStock(Long itemId, Integer quantity) {
        return flashItemMapper.increaseItemStock(itemId, quantity) == 1;
    }
}
