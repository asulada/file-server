package com.asuala.file.server.config;

import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.lang.reflect.Method;
import java.util.concurrent.Executors;

/**
 * @description: 定时任务进行异步调度
 * @create: 2024/01/18
 **/
@Configuration
public class ScheduleConfig implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        Method[] methods = BatchProperties.Job.class.getMethods();
        int defaultPoolSize = 20;
        int corePoolSize = 0;
        if (methods != null && methods.length > 0) {
            System.out.println(methods.length);
            for (Method method : methods) {
                Scheduled annotation = method.getAnnotation(Scheduled.class);
                if (annotation != null) {
                    corePoolSize++;
                }
            }
            if (defaultPoolSize > corePoolSize) {
                corePoolSize = defaultPoolSize;
            }
        }
        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(corePoolSize));
    }
}