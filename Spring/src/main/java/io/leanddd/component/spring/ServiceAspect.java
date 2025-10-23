package io.leanddd.component.spring;

import io.leanddd.component.common.Util;
import io.leanddd.component.meta.Command;
import io.leanddd.component.meta.Query;
import io.leanddd.component.meta.Service;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * define service aspect
 */
@Aspect
@Slf4j
@Component
public class ServiceAspect {

    @Value("${app.basePackage:}")
    String basePackage;

    public ServiceAspect() throws ClassNotFoundException {
        log.info("ServiceAspect");
    }

    @Pointcut("@within(io.leanddd.component.meta.Service)")
    public void TheServiceAspect() {
    }

    static class Context {
        private final ProceedingJoinPoint pjp;
        private final String basePackage;
        Class<?> theClass;
        Command aCommand;
        Query aQuery;
        Service aService;
        MethodSignature methodSig;
        Method method;
        String methodName;
        HttpServletRequest request = null;
        Object[] parameterValues;

        Context(ProceedingJoinPoint pjp, String basePackage) {
            this.pjp = pjp;
            this.basePackage = basePackage;
            this.theClass = pjp.getTarget().getClass();
            this.methodSig = (MethodSignature) pjp.getSignature();
            this.method = methodSig.getMethod();
            this.methodName = methodSig.getName();
            this.aService = theClass.getAnnotation(Service.class);
            this.aCommand = method.getAnnotation(Command.class);
            this.aQuery = method.getAnnotation(Query.class);
            this.parameterValues = pjp.getArgs();
            ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (sra != null) {
                request = sra.getRequest();
            }
        }

        public boolean needAspect() {
            Class<?> theClass = pjp.getTarget().getClass();
            String className = theClass.getName();
            return Util.isEmpty(basePackage) || className.startsWith(basePackage);
        }
    }

    // TODO cache for each method
    public Context getContext(ProceedingJoinPoint pjp) {
        return new Context(pjp, basePackage);
    }
}
