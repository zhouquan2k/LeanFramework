package io.leanddd.component.framework;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PaginationList<T> extends ArrayList<T> implements List<T> {
    private final Pagination pagination;

    public PaginationList(List<T> items, Pagination pagination) {
        super(items);
        this.pagination = pagination;
    }
}
