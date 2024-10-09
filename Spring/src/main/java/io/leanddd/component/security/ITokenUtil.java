package io.leanddd.component.security;

import io.leanddd.component.framework.AuthInfo;

import javax.servlet.http.HttpServletRequest;

public interface ITokenUtil {
    AuthResult getAuthInfoFromToken(HttpServletRequest request);

    AuthResult generateAuthResult(AuthInfo authInfo, HttpServletRequest request);

    void invalidateToken(HttpServletRequest request);
}
