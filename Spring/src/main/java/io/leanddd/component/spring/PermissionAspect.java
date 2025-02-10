package io.leanddd.component.spring;

import io.leanddd.component.common.Util;
import io.leanddd.component.framework.Context;
import io.leanddd.component.framework.MetadataProvider;
import io.leanddd.component.meta.Metadata.PermissionDef;
import io.leanddd.component.meta.Query;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Aspect
@Order(3)
@Component
@DependsOn("Init")
public class PermissionAspect implements BaseAspect, InitializingBean {

    @Autowired
    ServiceAspect serviceAspect;

    @Autowired
    MetadataProvider metadataProvider;

    private Map<String, PermissionDef> permissionMap;

    static final String PermissinDenied = "Forbidden.PermissinDenied";

    public void init() {
        var funcMetadata = metadataProvider.getMetadata(LocaleContextHolder.getLocale(), null).getServices();
        this.permissionMap = new HashMap<String, PermissionDef>();
        funcMetadata.forEach(func -> {
            func.getPermissions().forEach(perm -> {
                this.permissionMap.put(func.getPermissionDomain() + "." + perm.getName(), perm);
            });
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.init();
    }

    @Override
    @Around("io.leanddd.component.spring.ServiceAspect.TheServiceAspect()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        var context = serviceAspect.getContext(pjp);
        Set<String> permissions = new HashSet<String>();
        var permissionDomain = Util.isNotEmpty(context.aService.permissionDomain()) ? context.aService.permissionDomain() : context.aService.name();
        var aQuery = context.method.getAnnotation(Query.class);
        if (context.aCommand != null) {
            if (context.aCommand.permissions() != null && context.aCommand.permissions().length > 0) {
                permissions.addAll(List.of(context.aCommand.permissions()).stream().map(perm -> permissionDomain + "." + perm).collect(Collectors.toList()));
            } else if (Util.isNotEmpty(context.aCommand.permission())) {
                permissions.add(permissionDomain + "." + context.aCommand.permission());
            }
        } else if (aQuery != null && Util.isNotEmpty(aQuery.permission())) // TODO
            permissions.add(permissionDomain + "." + aQuery.permission());

        // only check permission when enter into service at the first time.
        var permissionChecked = (Boolean) Context.getThreadLocalPropertyOrDefault("_permissionChecked", false);
        Context.setThreadLocalProperty("_permissionChecked", true);

        if (!context.needAspect() || permissions.size() == 0 || permissionChecked)
            return pjp.proceed();
        // use a prefix to require check for function permission only
        var resourcePerms = permissions.stream().filter(perm -> {
            var permissionDef = this.permissionMap.get(perm);
            Util.check(permissionDef != null, "invalid permission: %s", perm);
            return permissionDef.getName().endsWith("@");
        }).collect(Collectors.toSet());

        // check non resource permissions
        var permissionPassed = permissions.stream().filter(perm -> {
            var permissionDef = this.permissionMap.get(perm);
            Util.check(permissionDef != null, "invalid permission: %s", perm);
            return !permissionDef.getName().endsWith("@");
        }).anyMatch(perm -> Context.hasPermission(perm));

        // TODO
        if (resourcePerms.size() == 0) // check global permission only if no resource permission in meta
            Util.checkBiz(permissionPassed, PermissinDenied, "permission denied: %s", permissions);

        Context.setAccessResource(null);

        var result = pjp.proceed();

        // check resource permissions
        if (!permissionPassed) {
            // if exist resource permission and global permission not passed，then check resource permission
            Util.checkBiz(resourcePerms.stream().anyMatch(perm -> {
                var resourceDescriptor = Context.getResourceDescriptor();
                Util.check(resourceDescriptor != null, "no resource for permission: %s", perm);
                var required = perm + resourceDescriptor;
                return Context.hasPermission(required);
            }), PermissinDenied, "permission denied: %s", permissions);
        }
        return result;
    }

}
