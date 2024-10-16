package io.leanddd.component.spring;

import org.aspectj.lang.ProceedingJoinPoint;

public interface BaseAspect {
	Object doAround(ProceedingJoinPoint pjp) throws Throwable;
}
