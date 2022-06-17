package com.actionworks.flashsale.app.service.item.cache.model;

import com.actionworks.flashsale.domain.model.entity.FlashItem;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class FlashItemsCache {
    protected boolean exist;
    protected boolean empty;
    private List<FlashItem> flashItems;
    private Long version;
    private boolean later;
    private Integer total;

    public FlashItemsCache with(List<FlashItem> flashItems) {
        this.exist = true;
        this.flashItems = flashItems;
        return this;
    }


    public FlashItemsCache withVersion(Long version) {
        this.version = version;
        return this;
    }

    public FlashItemsCache tryLater() {
        this.later = true;
        return this;
    }

    public FlashItemsCache notExist() {
        this.exist = false;
        return this;
    }
    public FlashItemsCache empty() {
        this.empty = true;
        this.flashItems = new ArrayList<>();
        this.total = 0;
        return this;
    }
}
