package io.leanddd.component.api;

import io.leanddd.component.common.BizException;
import org.springframework.web.bind.annotation.*;

public interface CrudService<VO> {
    @GetMapping("/{id}")
    VO getById(@PathVariable String id);

    @PostMapping
    VO create(@RequestBody VO one);

    @PutMapping("/{id}")
    VO update(@PathVariable String id, @RequestBody VO one);

    @DeleteMapping("/{id}")
    void delete(@PathVariable String id) throws BizException;
}
