package io.leanddd.module.user.api;

import io.leanddd.component.data.BaseEntity;
import io.leanddd.component.meta.EnumDescription;
import io.leanddd.component.meta.EnumTag;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity<User> {
    private static final long serialVersionUID = 1L;

    String userId;
    String loginName;
    String username;
    String userCode;
    String phone;
    UserStatus status;
    String remark;
    private Set<String> permissions;
    private Set<UserRole> roles;
    private Map<String, Object> options;

    @RequiredArgsConstructor
    @Getter
    public enum UserStatus implements EnumDescription {
        Active(EnumTag.Success), Disabled(EnumTag.Warning);

        private final EnumTag tag;
        private String desc;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRole {
        String userId;
        String roleId;
        String roleName;
        String orgId;

        Role role;
    }
}

