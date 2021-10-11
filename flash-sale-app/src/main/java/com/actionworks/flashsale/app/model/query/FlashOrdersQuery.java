package com.actionworks.flashsale.app.model.query;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FlashOrdersQuery {
    private String keyword;
    private Integer pageSize;
    private Integer pageNumber;
    private Integer status;
}
