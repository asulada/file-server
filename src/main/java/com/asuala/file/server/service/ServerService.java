package com.asuala.file.server.service;

import com.asuala.file.server.vo.req.RebuildReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @description:
 * @create: 2024/02/03
 **/
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "file", name = "server.open", havingValue = "true")
public class ServerService {

    private final TimerService timerService;

    @Async("threadPoolTaskExecutor")
    public void rebuildData(RebuildReq req) throws IOException {
        //TODO-asuala 2024-02-01: 处理重复文件
        timerService.delRepearFileInfo(req.getIndex());
        //TODO-asuala 2024-02-03: 重置es
        timerService.rebuildEs(req.getIndex());
    }

    @Async("threadPoolTaskExecutor")
    public void addWtachEs(RebuildReq req) throws IOException {
        //TODO-asuala 2024-02-03: 重置es
        timerService.addWtachEs(req.getIndex(),req.getSId());
    }
}