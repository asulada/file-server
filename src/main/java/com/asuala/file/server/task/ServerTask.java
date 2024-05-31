package com.asuala.file.server.task;

import com.asuala.file.server.config.MainConstant;
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

/**
 * @description:
 * @create: 2024/01/26
 **/
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "file", name = "server.open", havingValue = "true")
public class ServerTask {



}