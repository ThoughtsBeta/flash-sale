package com.actionworks.flashsale.persistence.mapper;

import com.actionworks.flashsale.persistence.model.BucketDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BucketMapper {
    boolean increaseItemStock(@Param("itemId") Long itemId, @Param("quantity") Integer quantity, @Param("serialNo") Integer serialNo);

    boolean decreaseItemStock(@Param("itemId") Long itemId, @Param("quantity") Integer quantity, @Param("serialNo") Integer serialNo);

    List<BucketDO> getBucketsByItem(@Param("itemId") Long itemId);

    int updateStatusByItem(@Param("itemId") Long itemId, @Param("status") int status);

    void deleteByItem(@Param("itemId") Long itemId);

    void insertBatch(List<BucketDO> bucketDOS);
}
