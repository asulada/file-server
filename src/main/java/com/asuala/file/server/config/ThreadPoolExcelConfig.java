package com.asuala.file.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @create: 2023/08/22
 **/
@Configuration
public class ThreadPoolExcelConfig {
    @Value("${pool.corePoolSize:2}")
    private int corePoolSize;
    @Value("${pool.maximumPoolSize:3}")
    private int maximum;
    @Value("${pool.keepAliveTime:30}")
    private long keepAliveTime;
    @Value("${pool.workQueue:1500}")
    private int workQueue;
    @Bean("threadPoolTaskExecutor")
    public ThreadPoolExecutor threadPool(){

        return( new ThreadPoolExecutor(
                corePoolSize,
                maximum,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(workQueue),
                new ThreadPoolExecutor.AbortPolicy()
        ));
    }

}