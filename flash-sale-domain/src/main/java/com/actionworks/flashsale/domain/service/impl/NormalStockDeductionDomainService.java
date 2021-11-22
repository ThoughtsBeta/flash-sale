package com.actionworks.flashsale.domain.service.impl;

import com.actionworks.flashsale.domain.exception.DomainException;
import com.actionworks.flashsale.domain.model.StockDeduction;
import com.actionworks.flashsale.domain.repository.FlashItemRepository;
import com.actionworks.flashsale.domain.service.StockDeductionDomainService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.actionworks.flashsale.domain.exception.DomainErrorCode.PARAMS_INVALID;

@Service
@ConditionalOnProperty(name = "place_order_type", havingValue = "normal", matchIfMissing = true)
public class NormalStockDeductionDomainService implements StockDeductionDomainService {
    @Resource
    private FlashItemRepository flashItemRepository;

    @Override
    public boolean decreaseItemStock(StockDeduction stockDeduction) {
        if (stockDeduction == null || stockDeduction.getItemId() == null || stockDeduction.getQuantity() == null) {
            throw new DomainException(PARAMS_INVALID);
        }
        return flashItemRepository.decreaseItemStock(stockDeduction.getItemId(), stockDeduction.getQuantity());
    }

    @Override
    public boolean increaseItemStock(StockDeduction stockDeduction) {
        if (stockDeduction == null || stockDeduction.getItemId() == null || stockDeduction.getQuantity() == null) {
            throw new DomainException(PARAMS_INVALID);
        }
        return flashItemRepository.increaseItemStock(stockDeduction.getItemId(), stockDeduction.getQuantity());
    }
}
