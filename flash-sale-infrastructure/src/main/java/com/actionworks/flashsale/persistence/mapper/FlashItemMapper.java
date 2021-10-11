package com.actionworks.flashsale.persistence.mapper;

import com.actionworks.flashsale.domain.model.PagesQueryCondition;
import com.actionworks.flashsale.persistence.model.FlashItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FlashItemMapper {
    int insert(FlashItemDO flashItemDO);

    int update(FlashItemDO flashItemDO);

    FlashItemDO getById(@Param("itemId") Long itemId);

    List<FlashItemDO> findFlashItemsByCondition(PagesQueryCondition pagesQueryCondition);

    Integer countFlashItemsByCondition(PagesQueryCondition pagesQueryCondition);

    int decreaseItemStock(@Param("itemId") Long itemId, @Param("quantity") Integer quantity);

    int increaseItemStock(@Param("itemId") Long itemId, @Param("quantity") Integer quantity);
}
