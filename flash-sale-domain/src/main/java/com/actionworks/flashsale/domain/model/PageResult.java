package com.actionworks.flashsale.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PageResult<T> {
    private List<T> data;
    private int total;

    private PageResult(int total, List<T> data) {
        this.setData(data);
        this.total = total;
    }

    public static <T> PageResult<T> with(List<T> data, int total) {
        return new PageResult<>(total, data);
    }
}
