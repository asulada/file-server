package com.asuala.file.server;

import cn.hutool.http.HttpGlobalConfig;
import com.asuala.file.server.config.MainConstant;
import com.asuala.file.server.service.EsHttpServcie;
import com.asuala.file.server.service.EsLocalService;
import com.asuala.file.server.service.EsService;
import com.asuala.file.server.utils.CPUUtils;
import com.asuala.file.server.vo.Index;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.asuala.file.server.mapper") //扫描mapper的包，或者读者可以在对应的mapper上加上@Mapper的注解
@EnableAsync
@EnableAspectJAutoProxy
public class MockApplication {
    @Value("${file.server.open}")
    private boolean server;

    public static void main(String[] args) throws IOException {
        HttpGlobalConfig.setTimeout(10000);
        ConfigurableApplicationContext context = SpringApplication.run(MockApplication.class, args);
//        SpringContextUtil.setApplicationContext(context);

        // 自定义关闭钩子逻辑
//        Runtime.getRuntime().addShutdownHook(new Thread(()->{
//            System.out.println("Ctrl+C 被触发，执行关闭逻辑...");
            // 在这里添加你的关闭逻辑
//        }));
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        taskScheduler.initialize();
        return taskScheduler;
    }
    @Bean
    public EsService esService() {
        if (server) {
            return new EsLocalService();
        }
        return new EsHttpServcie();
    }

}
