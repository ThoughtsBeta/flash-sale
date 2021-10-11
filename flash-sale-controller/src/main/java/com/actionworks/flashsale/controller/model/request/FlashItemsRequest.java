package com.actionworks.flashsale.controller.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FlashItemsRequest {
    private String keyword;
    private Integer pageSize;
    private Integer pageNumber;
}
