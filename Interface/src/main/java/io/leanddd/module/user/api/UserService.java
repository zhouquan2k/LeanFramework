package io.leanddd.module.user.api;

import io.leanddd.component.common.BizException;
import io.leanddd.component.misc.api.CrudService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Set;

@RequestMapping("/api/users")
public interface UserService extends CrudService<User> {

    @PutMapping("/{userId}/password-reset")
    void resetPassword(@PathVariable String userId);

    @PostMapping("/{userId}/assign")
    void assignRoles(@PathVariable String userId, @RequestParam(defaultValue = "") String orgId, @RequestBody Set<User.UserRole> roles);

    @DeleteMapping("/{userId}/remove/org/{orgId}")
    void removeFromOrg(@PathVariable String userId, @PathVariable String orgId);

    @PutMapping("my")
    void updateMyProfile(@RequestBody User user);

    @PutMapping("my/password")
    void updateMyPassword(@RequestBody UpdatePasswordParams params) throws BizException;

    @Data
    public class UpdatePasswordParams implements Serializable {
        private static final long serialVersionUID = 1L;
        public String oldPass;
        public String newPass;
    }
}

