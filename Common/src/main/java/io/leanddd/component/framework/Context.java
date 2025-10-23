package io.leanddd.component.framework;

import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class Context {

    static final String RESOURCE_DESCRIPTOR = "resource_descriptor";
    static final private AuthInfo EmptyAuth = new AuthInfo() {
        public String getUsername() {
            return null;
        }
    };
    static Context theInstance;
    static String currentTimezone;
    private static ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();
    private final SecurityUtil securityUtil;

    private final BeanMgr beanMgr;

    public static void setTimezone(String currentTimezone) {
        Context.currentTimezone = currentTimezone;
    }

    public static String getTimezone() {
        return Context.currentTimezone;
    }

    public static boolean isAuthenticated() {
        return theInstance.securityUtil.getAuthInfo().isPresent();
    }
    public static String getUserId() {
        return theInstance.securityUtil.getAuthInfo().orElseGet(() -> EmptyAuth).getUserId();
    }

    public static String getUsername() {
        return theInstance.securityUtil.getAuthInfo().orElseGet(() -> EmptyAuth).getUsername();
    }

    public static String getDepartment() {
        return theInstance.securityUtil.getAuthInfo().orElseGet(() -> EmptyAuth).getPrimaryDepartment();
    }

    public static AuthInfo getAuthInfo() {
        return theInstance.securityUtil.getAuthInfo().orElse(null);
    }

    public static boolean hasPermission(String permission) {
        var authInfo = theInstance.securityUtil.getAuthInfo().orElse(null);
        return authInfo != null
                ? authInfo.getPermissions().contains(permission)
                || authInfo.getPermissions().contains(SecurityUtil.AdminPermission)
                : false;
    }

    public static void setAccessResource(String resource) {
        setThreadLocalProperty(RESOURCE_DESCRIPTOR, resource);
    }

    public static String getResourceDescriptor() {
        return (String) getThreadLocalPropertyOrDefault(RESOURCE_DESCRIPTOR, null);
    }

    public static String getProperty(String name) {
        return theInstance.beanMgr.getProperty(name);
    }

    public static Date getCurrentDate() {
        return Calendar.getInstance().getTime();
    }

    public static <T> T getBean(Class<T> cls) {
        return theInstance.beanMgr.getBean(cls);
    }

    public static <T> T getBean(String beanName, Class<T> cls) {
        return theInstance.beanMgr.getBean(beanName, cls);
    }

    public static void publishEvent(Event event) {
        theInstance.beanMgr.publishEvent(event);
    }

    public static void setThreadLocalProperty(String key, Object value) {
        Map<String, Object> params = threadLocal.get();
        if (params == null) {
            params = new HashMap<String, Object>();
            threadLocal.set(params);
        }
        params.put(key, value);
    }

    public static void resetThreadLocal() {
        threadLocal.remove();
    }

    public static Object getThreadLocalPropertyOrDefault(String key, Object defaultValue) {
        var data = threadLocal.get();
        return data != null ? data.getOrDefault(key, defaultValue) : defaultValue;
    }

    @PostConstruct
    public void init() {
        theInstance = this;
    }
}

