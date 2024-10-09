package io.leanddd.component.security;

import io.leanddd.component.framework.AuthInfo;
import io.leanddd.component.framework.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtilImpl implements SecurityUtil {
    @Override
    public Optional<AuthInfo> getAuthInfo() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var authInfo = authentication != null ? authentication.getPrincipal() : null;
        return Optional.ofNullable((authInfo instanceof AuthInfo) ? (AuthInfo) authInfo : null);
    }
}

