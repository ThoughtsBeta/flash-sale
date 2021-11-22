package com.actionworks.flashsale.app.service.activity.cache.model;

import com.actionworks.flashsale.domain.model.entity.FlashActivity;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FlashActivityCache {
    protected boolean exist;
    private FlashActivity flashActivity;
    private Long version;
    private boolean later;

    public FlashActivityCache with(FlashActivity flashActivity) {
        this.exist = true;
        this.flashActivity = flashActivity;
        return this;
    }


    public FlashActivityCache withVersion(Long version) {
        this.version = version;
        return this;
    }

    public FlashActivityCache tryLater() {
        this.later = true;
        return this;
    }

    public FlashActivityCache notExist() {
        this.exist = false;
        return this;
    }
}
