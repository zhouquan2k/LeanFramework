package io.leanddd.component.security;

import io.leanddd.component.framework.AuthInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.session.web.http.HttpSessionIdResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.Duration;
import java.util.Collections;
import java.util.List;


// TODO for session: tokenFilter seems meaningness
public class SessionTokenUtil implements ITokenUtil {

    // TODO using app.security.tokenValidityInSeconds?
    @Value("${spring.session.timeout:30m}")
    String sessionTimeout;

    @Override
    public AuthResult getAuthInfoFromToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (AuthResult) session.getAttribute("_authInfo");
    }

    @Override
    public AuthResult generateAuthResult(AuthInfo authInfo, HttpServletRequest request) {
        AuthResult result = new AuthResult(authInfo);
        var session = request.getSession();
        result.setToken(session.getId());
        session.setAttribute("_authInfo", result);
        var seconds = Duration.parse("PT" + sessionTimeout).getSeconds();
        session.setMaxInactiveInterval((int) seconds);
        return result;
    }

    @Override
    public void invalidateToken(HttpServletRequest request) {
        request.getSession().removeAttribute("_authInfo");
        request.getSession().invalidate();
    }
}


class AuthorizationHeaderHttpSessionIdResolver implements HttpSessionIdResolver {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    @Override
    public List<String> resolveSessionIds(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            var auth = request.getParameter("auth");
            if (auth != null && auth.length() > 0 && auth.startsWith(PREFIX))
                return Collections.singletonList(auth.substring(PREFIX.length()));
            return Collections.emptyList();
        }
        return Collections.singletonList(header.substring(PREFIX.length()));
    }

    @Override
    public void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId) {
        response.setHeader(AUTHORIZATION_HEADER, PREFIX + sessionId);
    }

    @Override
    public void expireSession(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader(AUTHORIZATION_HEADER, "");
    }
}
