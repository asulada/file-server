package com.asuala.file.server.config;

import com.asuala.file.server.file.monitor.linux.FileListener;
import com.asuala.file.server.file.monitor.linux.InotifyLibraryUtil;
import com.asuala.file.server.utils.CacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @create: 2024/05/21
 **/
@Slf4j
@Component
public class ShutdownHandler implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        // 执行退出前的清理工作
        log.info("Spring Boot应用程序正在关闭...");
        if (null != MainConstant.systemInfo) {
            if (MainConstant.systemInfo.getSystem().contains("LINUX")) {
                CacheUtils.watchFlag = false;
                //TODO-asuala 2024-05-21: 关闭监听
                InotifyLibraryUtil.close();
                InotifyLibraryUtil.fixedThreadPool.shutdownNow();
                try {
                    int i = 0;
                    while (!InotifyLibraryUtil.fixedThreadPool.awaitTermination(1, TimeUnit.SECONDS)) {
                        if (i++ > 10) {
                            log.error("关闭linux文件事件监听失败");
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.error("关闭linux文件事件监听错误", e);
                }
                //TODO-asuala 2024-05-21: 关闭消费
                FileListener.fixedThreadPool.shutdown();
                try {
                    int i = 0;
                    while (!FileListener.fixedThreadPool.awaitTermination(1, TimeUnit.SECONDS)) {
                        if (i++ > 10) {
                            log.error("关闭linux文件事件消费失败");
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.error("关闭linux文件事件消费错误", e);
                }
            }
        }

        // 例如，关闭数据库连接、停止后台线程等
        // ...
        log.info("清理完成，应用程序已关闭。");
    }
}