package io.leanddd.module.user.api;

import io.leanddd.component.misc.api.CrudService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/roles")
public interface RoleService extends CrudService<Role> {

    @PostMapping("/{roleId}/assign")
    void assignRolePermissions(@PathVariable String roleId, @RequestBody List<String> permissionCodes);

    // Queries

    @GetMapping
    List<Role> getRoles(@RequestParam(value = "roleType", defaultValue = "") Role.RoleType roleType);

    @GetMapping("/by-org/{orgId}")
    List<Role> queryByOrgId(@PathVariable String orgId);
}

