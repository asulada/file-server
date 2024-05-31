package com.asuala.file.server.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @create: 2024/01/13
 **/
public class ThreadPoolExecutorUtils {
    /**
     * default value
     */
    private static int corePoolSite = 4;
    private static int maxPoolSite = 10;
    private static int queueCapacity = 500;
    private static Long keepAliveTime = 1L;

    private static volatile ThreadPoolExecutor threadPoolExecutorInstance = null;

    private ThreadPoolExecutorUtils() {}
    // 自定义线程池参数
    public static void initialize(int corePoolSite, int maxPoolSite, int queueCapacity, long keepAliveTime) {
        ThreadPoolExecutorUtils.corePoolSite = corePoolSite;
        ThreadPoolExecutorUtils.maxPoolSite = maxPoolSite;
        ThreadPoolExecutorUtils.queueCapacity = queueCapacity;
        ThreadPoolExecutorUtils.keepAliveTime = keepAliveTime;
    }
    // 创建线程池
    public static ThreadPoolExecutor getThreadPoolExecutorInstance() {
        if (threadPoolExecutorInstance == null || threadPoolExecutorInstance.isShutdown()) {
            synchronized (ThreadPoolExecutorUtils.class) {
                // double check
                if (threadPoolExecutorInstance == null || threadPoolExecutorInstance.isShutdown()) {
                    threadPoolExecutorInstance = new ThreadPoolExecutor(
                            corePoolSite,
                            maxPoolSite,
                            keepAliveTime,
                            TimeUnit.SECONDS,
                            new ArrayBlockingQueue<>(queueCapacity),
                            r -> new Thread(r),
                            new ThreadPoolExecutor.DiscardPolicy());
                }
            }
        }
        return threadPoolExecutorInstance;
    }
}