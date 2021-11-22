package com.actionworks.flashsale.domain.service.impl;

import com.actionworks.flashsale.domain.exception.DomainException;
import com.actionworks.flashsale.domain.model.StockDeduction;
import com.actionworks.flashsale.domain.repository.BucketsRepository;
import com.actionworks.flashsale.domain.service.StockDeductionDomainService;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.actionworks.flashsale.domain.exception.DomainErrorCode.PARAMS_INVALID;

@ConditionalOnProperty(name = "place_order_type", havingValue = "buckets", matchIfMissing = true)
@Service
public class BucketsStockDeductionService implements StockDeductionDomainService {
    private static final Logger logger = LoggerFactory.getLogger(BucketsStockDeductionService.class);

    @Resource
    private BucketsRepository bucketsRepository;

    @Override
    public boolean decreaseItemStock(StockDeduction stockDeduction) {
        logger.info("decreaseItemStock|扣减库存|{}", JSON.toJSONString(stockDeduction));
        if (stockDeduction == null || stockDeduction.getItemId() == null || stockDeduction.getQuantity() == null || stockDeduction.getSerialNo() == null) {
            throw new DomainException(PARAMS_INVALID);
        }
        return bucketsRepository.decreaseItemStock(stockDeduction.getItemId(), stockDeduction.getQuantity(), stockDeduction.getSerialNo());
    }

    @Override
    public boolean increaseItemStock(StockDeduction stockDeduction) {
        logger.info("increaseItemStock|恢复库存|{}", JSON.toJSONString(stockDeduction));
        if (stockDeduction == null || stockDeduction.getItemId() == null || stockDeduction.getQuantity() == null || stockDeduction.getSerialNo() == null) {
            throw new DomainException(PARAMS_INVALID);
        }
        return bucketsRepository.increaseItemStock(stockDeduction.getItemId(), stockDeduction.getQuantity(), stockDeduction.getSerialNo());
    }
}
