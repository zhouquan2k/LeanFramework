package io.leanddd.module.user.api;

import io.leanddd.component.meta.EnumDescription;
import io.leanddd.component.meta.EnumTag;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
public class Role {
    private String roleId;

    private String roleName;
    private RoleType roleType;
    private String orgId;
    private Boolean enabled;
    private Boolean workflowGroup;
    private List<String> permissions;

    @RequiredArgsConstructor
    @Getter
    public enum RoleType implements EnumDescription {
        Global(EnumTag.Info), GroupPublic(EnumTag.Success), GroupPrivate(EnumTag.Warning);

        private final EnumTag tag;
        private String desc;
    }

}

