package io.leanddd.component.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {

    private final ITokenUtil tokenUtil;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        // TODO 从配置文件中读取
        if (!requestURI.startsWith("/api/public/")) {
            AuthResult authInfo = tokenUtil.getAuthInfoFromToken(request);
            if (authInfo == null) {
                filterChain.doFilter(request, response);
                return;
            }

            if (authInfo != null) //&& SecurityContextHolder.getContext().getAuthentication() == null
            {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(authInfo,
                        null, authInfo.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("set Authentication to security context for '{}', uri: {}", authentication.getName(),
                        requestURI);
            } else {
                // TODO tokenUtil.removeToken(authToken);
                log.debug("no valid JWT token found, uri: {}", requestURI);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized, no valid token");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
