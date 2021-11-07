package com.actionworks.flashsale.app.util;

public interface OrderTaskIdGenerateService {
    /**
     * 生成下单请求标识
     *
     * @param userId 下单用户ID
     * @param itemId 秒杀品ID
     * @return 加密后的请求标识
     */
    String generatePlaceOrderTaskId(Long userId, Long itemId);
}
