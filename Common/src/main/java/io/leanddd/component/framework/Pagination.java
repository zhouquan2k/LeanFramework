package io.leanddd.component.framework;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pagination {
    private Integer page;
    private Integer limit;
    private Integer totalCount;

    public int getOffset() {
        if (page == null || limit == null) {
            return 0;
        }
        return (page - 1) * limit;
    }
}
