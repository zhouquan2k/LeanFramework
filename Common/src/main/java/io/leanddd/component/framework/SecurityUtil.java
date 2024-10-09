package io.leanddd.component.framework;

import java.util.Optional;

public interface SecurityUtil {

    public static final String AdminPermission = "security.***";

    Optional<AuthInfo> getAuthInfo();
}
