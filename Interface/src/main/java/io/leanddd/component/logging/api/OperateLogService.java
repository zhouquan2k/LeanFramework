package io.leanddd.component.logging.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@RequestMapping("/api/operatelog")
public interface OperateLogService {

    void persist(OperateLog log);

    // TODO 分页支持
    @GetMapping
    List<OperateLog> queryOperateLogs();

    @PostMapping("search")
    List<OperateLog> queryOperateLogs(@RequestBody Map<String, Object> example);

}
