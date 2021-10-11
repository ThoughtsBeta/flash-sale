package com.actionworks.flashsale.persistence.mapper;

import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.persistence.model.FlashOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FlashOrderMapper {
    int insert(FlashOrderDO flashOrderDO);

    int updateStatus(FlashOrderDO flashOrderDO);

    FlashOrderDO getById(@Param("orderId") Long orderId);

    List<FlashOrderDO> findFlashOrdersByCondition(PagesQueryCondition pagesQueryCondition);

    Integer countFlashOrdersByCondition();
}
