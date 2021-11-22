package com.actionworks.flashsale.app.service.activity.cache.model;

import com.actionworks.flashsale.domain.model.entity.FlashActivity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class FlashActivitiesCache {
    protected boolean exist;
    private List<FlashActivity> flashActivities;
    private Long version;
    private boolean later;
    private Integer total;

    public FlashActivitiesCache with(List<FlashActivity> flashActivity) {
        this.exist = true;
        this.flashActivities = flashActivity;
        return this;
    }


    public FlashActivitiesCache withVersion(Long version) {
        this.version = version;
        return this;
    }

    public FlashActivitiesCache tryLater() {
        this.later = true;
        return this;
    }

    public FlashActivitiesCache notExist() {
        this.exist = false;
        return this;
    }
}
