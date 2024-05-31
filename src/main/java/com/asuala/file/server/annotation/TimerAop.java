package com.asuala.file.server.annotation;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @description:
 * @create: 2024/02/03
 **/
@Slf4j
@Aspect
@Component
public class TimerAop {

    private final String POINT_CUT = "@annotation(com.asuala.file.server.annotation.Timer)";

    @Pointcut(POINT_CUT)
    public void pointCut() {
    }


    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startMillis = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endMillis = System.currentTimeMillis();

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String name = method.getAnnotation(Timer.class).value();
        log.info("{} 耗时: {}s",name, (endMillis - startMillis) / 1000);
        return result;
    }

}