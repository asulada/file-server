package com.asuala.file.server.task;

import com.asuala.file.server.config.MainConstant;
import com.asuala.file.server.file.monitor.linux.InotifyLibraryUtil;
import com.asuala.file.server.service.IndexService;
import com.asuala.file.server.service.RecordPageService;
import com.asuala.file.server.vo.Index;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @create: 2024/01/26
 **/
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "file", name = "server.open", havingValue = "true")
public class ServerTask {


    @Scheduled(cron = "0 0 1 * * ?")
    public void excute() {
        Map<Integer, InotifyLibraryUtil.Watch> fdMap = InotifyLibraryUtil.fdMap;
        log.info("监控 fd 数量 {}", fdMap.size());
        for (Map.Entry<Integer, InotifyLibraryUtil.Watch> entry : fdMap.entrySet()) {
            log.info(" fd {} 监控 文件夹数量 {} 缓存文件数 {}", entry.getKey(), entry.getValue().getBidiMap().size(), entry.getValue().getPathIdMap().size());
        }
    }

}