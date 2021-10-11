package com.actionworks.flashsale.util;

import com.actionworks.flashsale.domain.util.OrderNoGenerateContext;
import com.actionworks.flashsale.domain.util.OrderNoGenrateService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
public class SnowflakeOrderNoGenerateService implements OrderNoGenrateService {

    private SnowflakeIdWorker snowflakeIdWorker;

    /**
     * 分布式部署时需要动态获取机器的ID，此处为方便演示使用随机数作为机器ID
     */
    @PostConstruct
    public void initWorker() {
        Random random = new Random(1);
        snowflakeIdWorker = new SnowflakeIdWorker(random.nextInt(32), 1, 1);
    }

    @Override
    public Long generateOrderNo(OrderNoGenerateContext orderNoGenerateContext) {
        return snowflakeIdWorker.nextId();
    }
}
