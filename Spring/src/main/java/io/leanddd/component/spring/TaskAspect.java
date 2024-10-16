package io.leanddd.component.spring;

import io.leanddd.component.common.Util;
import io.leanddd.component.event.ServiceInvokedEvent;
import io.leanddd.component.framework.Context;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(2)
@Component
@Slf4j
public class TaskAspect implements BaseAspect {

    @Autowired
    ServiceAspect serviceAspect;

    @Override
    @Around("io.leanddd.component.spring.ServiceAspect.TheServiceAspect()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        var context = serviceAspect.getContext(pjp);
        if (!context.needAspect() || context.aCommand == null)
            return pjp.proceed();

        var result = pjp.proceed();

        if (context.aCommand != null && context.request != null) {
            var taskId = context.request.getHeader("taskid");
            if (Util.isNotEmpty(taskId)) {
                log.info("taskid: " + taskId);
                Context.publishEvent(new ServiceInvokedEvent(context.aService.name(), context.methodName,
                        context.parameterValues,
                        result, taskId));
            }
        }
        return result;
    }

}
