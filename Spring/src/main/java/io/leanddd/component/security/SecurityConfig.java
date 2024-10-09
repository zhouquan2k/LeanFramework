package io.leanddd.component.security;

import io.leanddd.component.framework.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@ConditionalOnProperty(name = "app.component-features.security-spring", havingValue = "true", matchIfMissing = false)
@EnableWebSecurity
@EnableRedisHttpSession
public class SecurityConfig {

    @Value("${app.security.authenticated:true}")
    boolean authenticateNeeded;

    @Value("${app.web.anonymousAccess}")
    String[] anonymousAccess;
    // List<String> anonymousAccess;

    @Bean
    ITokenUtil createTokenUtil(SecurityProperties properties) {
        // return new JWTTokenUtil(properties);
        return new SessionTokenUtil();
    }

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return new AuthorizationHeaderHttpSessionIdResolver();
    }

    @Bean
    TokenFilter createTokenFilter(ITokenUtil tokenUtil) {
        return new TokenFilter(tokenUtil);
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity, TokenFilter tokenFilter, CorsFilter corsFilter)
            throws Exception {
        // 省略HttpSecurity的配置
        httpSecurity.csrf().disable().addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
                // 授权异常
                .exceptionHandling().authenticationEntryPoint(createAuthenticationEntryPoint())
                .accessDeniedHandler(createAccessDeniedHandler()).and()
                // 防止iframe 造成跨域
                .headers().frameOptions().disable()
                // 不创建会话
                // .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .and().sessionManagement()
                .and().authorizeRequests().antMatchers("/flowable-rest/**").permitAll()
                .antMatchers("/api/public/**").permitAll()
                // 放行OPTIONS请求
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 自定义匿名访问所有url放行 ： 允许匿名和带权限以及登录用户访问
                .antMatchers(this.anonymousAccess).permitAll();

        if (!this.authenticateNeeded) {
            httpSecurity.authorizeRequests().antMatchers("/api/**").permitAll(); //TODO 测试临时关闭安检
        }
        // 所有请求都需要认证
        httpSecurity.authorizeRequests().anyRequest().authenticated();

        return httpSecurity.build();
    }

    private AuthenticationEntryPoint createAuthenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response,
                                 AuthenticationException authException) throws IOException {
                // 当用户尝试访问安全的REST资源而不提供任何凭据时，将调用此方法发送401 响应
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        authException == null ? "Unauthorized" : authException.getMessage());
            }
        };
    }

    private AccessDeniedHandler createAccessDeniedHandler() {
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response,
                               AccessDeniedException accessDeniedException) throws IOException {
                // 当用户在没有授权的情况下访问受保护的REST资源时，将调用此方法发送403 Forbidden响应
                response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
            }
        };
    }

    @Bean
    SecurityUtil createSecurityUtil() {
        return new SecurityUtilImpl();
    }

}
