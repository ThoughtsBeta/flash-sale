package com.actionworks.flashsale.app.model.query;

import com.actionworks.flashsale.domain.model.enums.FlashItemStatus;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

@Data
@Accessors(chain = true)
public class FlashItemsQuery {
    private String keyword;
    private Integer pageSize;
    private Integer pageNumber;
    private Integer status;
    private Long version;
    private Long activityId;

    public boolean isOnlineFirstPageQuery() {
        return StringUtils.isEmpty(keyword) && pageNumber != null && pageNumber == 1 && FlashItemStatus.isOnline(status);
    }
}
