package io.leanddd.component.framework;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Pagination {
    private Integer page;
    private Integer limit;
    private Integer totalCount;
    private Integer totalPages;
    private Boolean hasNext;

    public Pagination(Integer page, Integer limit, Integer totalCount) {
        this.page = page;
        this.limit = limit;
        this.totalCount = totalCount;
    }

    public Pagination(Integer page, Integer limit, Integer totalCount, Integer totalPages, Boolean hasNext) {
        this.page = page;
        this.limit = limit;
        this.totalCount = totalCount;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
    }

    public int getOffset() {
        if (page == null || limit == null) {
            return 0;
        }
        return (page - 1) * limit;
    }
}
