package io.leanddd.component.security;

import io.leanddd.component.common.Util;
import io.leanddd.component.framework.AuthInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.beans.Transient;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
class MyGrantedAuthority implements GrantedAuthority {
    private static final long serialVersionUID = 1L;
    String permission;

    @Override
    public String getAuthority() {
        return permission;
    }
}

@Data
public class AuthResult implements AuthInfo {

    private String userId;
    private String username;
    private Set<String> permissions;
    private String token;
    private String primaryDepartment;
    private boolean authencated = false;

    //from UserDetails
    public AuthResult(AuthInfo user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.permissions = user.getPermissions();
        this.authencated = true;
        this.primaryDepartment = user.getPrimaryDepartment();
    }

    // from token
    public AuthResult() {

    }

    // used to store it to security context
    @Transient
    public List<GrantedAuthority> getAuthorities() {
        if (this.permissions == null || this.permissions.size() == 0)
            return List.of();
        return Util.mapToList(this.permissions.stream(), perm -> new MyGrantedAuthority(perm));
    }
}
