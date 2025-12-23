package io.leanddd.module.user.api;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/api/users")
public interface UserQueryService {
    @GetMapping
    List<User> queryUsers();

    @PostMapping("search")
    List<User> queryByExample(@RequestBody Map<String, Object> example);

    List<User> queryUsersByOrg(String orgId);

    @GetMapping("department/{departmentId}")
    List<User> getUsersByDepartment(@PathVariable String departmentId);

    @GetMapping("my")
    User getMyProfile();

    User getUserByUsername(String loginName);
}
