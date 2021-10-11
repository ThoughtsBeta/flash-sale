package com.actionworks.flashsale.persistence.repository;

import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.domain.model.entity.FlashOrder;
import com.actionworks.flashsale.domain.repository.FlashOrderRepository;
import com.actionworks.flashsale.persistence.convertor.FlashOrderBuilder;
import com.actionworks.flashsale.persistence.mapper.FlashOrderMapper;
import com.actionworks.flashsale.persistence.model.FlashOrderDO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FlashOrderRepositoryImpl implements FlashOrderRepository {
    @Resource
    private FlashOrderMapper flashOrderMapper;

    @Override
    public boolean save(FlashOrder flashOrder) {
        FlashOrderDO flashOrderDO = FlashOrderBuilder.toDataObjectForCreate(flashOrder);
        int effectedRows = flashOrderMapper.insert(flashOrderDO);
        return effectedRows == 1;
    }

    @Override
    public boolean updateStatus(FlashOrder flashOrder) {
        FlashOrderDO flashOrderDO = FlashOrderBuilder.toDataObjectForCreate(flashOrder);
        int effectedRows = flashOrderMapper.updateStatus(flashOrderDO);
        return effectedRows == 1;
    }

    @Override
    public Optional<FlashOrder> findById(Long orderId) {
        FlashOrderDO flashOrderDO = flashOrderMapper.getById(orderId);
        if (flashOrderDO == null) {
            return Optional.empty();
        }
        FlashOrder flashOrder = FlashOrderBuilder.toDomainObject(flashOrderDO);
        return Optional.of(flashOrder);
    }

    @Override
    public List<FlashOrder> findFlashOrdersByCondition(PagesQueryCondition pagesQueryCondition) {
        return flashOrderMapper.findFlashOrdersByCondition(pagesQueryCondition)
                .stream()
                .map(FlashOrderBuilder::toDomainObject)
                .collect(Collectors.toList());
    }

    @Override
    public int countFlashOrdersByCondition(PagesQueryCondition buildParams) {
        return flashOrderMapper.countFlashOrdersByCondition();
    }
}
