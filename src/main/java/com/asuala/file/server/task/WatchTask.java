package com.asuala.file.server.task;

import com.asuala.file.server.config.MainConstant;
import com.asuala.file.server.file.monitor.linux.FileMemory;
import com.asuala.file.server.file.monitor.linux.InotifyLibraryUtil;
import com.asuala.file.server.mapper.FileInfoMapper;
import com.asuala.file.server.service.EsService;
import com.asuala.file.server.service.FileInfoService;
import com.asuala.file.server.vo.FileInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @create: 2024/01/26
 **/
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "watch", name = "open", havingValue = "true")
public class WatchTask {

    private final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final FileInfoService fileInfoService;
    private final EsService esService;

    @Scheduled(cron = "0 15 2 * * ?")
    public void excute() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);
        String end = today.format(dateFormatter);
        String start = yesterday.format(dateFormatter);
        int i = 1;
        List<FileInfo> list = new ArrayList<>();
        int size = 0;
        while (true) {
            Page<FileInfo> page = new Page<>(i, 10000);
            Page<FileInfo> result = fileInfoService.page(page, new LambdaQueryWrapper<FileInfo>().select(FileInfo::getId, FileInfo::getPath, FileInfo::getSize).between(FileInfo::getChangeTime, start, end).eq(FileInfo::getIndex, MainConstant.systemInfo.getId()).
                    eq(FileInfo::getDir, 0));
            for (FileInfo record : result.getRecords()) {
                File file = new File(record.getPath());
                if (file.length() != record.getSize()) {
                    record.setSize(file.length());
                    list.add(record);
                }
            }
            if (list.size() > 0) {
                size += list.size();
                fileInfoService.updateBatchById(list);
                esService.updateEsList(list, list.get(0).getName());
                list.clear();
            }
            if (i++ >= result.getPages()) {
                break;
            }
        }
        log.info("{} - {} 更新文件占用空间数据 {}", start, end, size);
    }

}