package com.asuala.file.server.service;

import cn.hutool.core.collection.CollectionUtil;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.asuala.file.server.annotation.Timer;
import com.asuala.file.server.es.Es8Client;
import com.asuala.file.server.es.entity.FileInfoEs;
import com.asuala.file.server.mapper.FileInfoMapper;
import com.asuala.file.server.utils.ThreadPoolExecutorUtils;
import com.asuala.file.server.vo.FileInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description:
 * @create: 2024/05/30
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class TimerService {

    private final FileInfoMapper fileInfoMapper;

    @Autowired(required = false)
    private Es8Client es8Client;

    @Timer(value = "重置es")
    public void rebuildEs(int index) throws IOException {
        es8Client.delQuery(Query.of(q -> q.match(m -> m.query(index).field("index"))), FileInfoEs.class);
        //TODO-asuala 2024-02-01: es添加数据
        int pageNum = 1;
        while (true) {
            Page<FileInfo> page = new Page<>(pageNum++, 10000);
            List<FileInfo> fileInfos = fileInfoMapper.selectList(page, new LambdaQueryWrapper<FileInfo>().eq(FileInfo::getIndex, index).orderByAsc(FileInfo::getId));
            if (fileInfos.size() == 0) {
                break;
            }
            List<FileInfoEs> listEs = fileInfos.stream().map(item -> FileInfoEs.builder().id(item.getId()).changeTime(item.getChangeTime()).index(item.getIndex()).
                    path(item.getPath()).size(item.getSize()).name(item.getName()).suffix(item.getSuffix()).sId(item.getUId()).build()).collect(Collectors.toList());
            es8Client.addDatas(listEs, false);
        }
    }

    @Timer("处理数据库重复文件")
    public void delRepearFileInfo(int index) {
        List<Map<String, String>> maps = fileInfoMapper.findNameByIndex(index);
        List<List<Map<String, String>>> split = CollectionUtil.split(maps, 20000);
        ThreadPoolExecutor executor = ThreadPoolExecutorUtils.getThreadPoolExecutorInstance();
        CopyOnWriteArrayList<Long> delIds = new CopyOnWriteArrayList();
        for (List<Map<String, String>> mapList : split) {
            executor.execute(() -> {
                for (Map<String, String> map : mapList) {
                    String[] paths = map.get("path").split(";");
                    String[] ids = map.get("id").split(";");
                    Set<String> pathSet = new HashSet<>();

                    for (int i = 0; i < paths.length; i++) {
                        String value = paths[i];
                        if (pathSet.contains(value)) {
                            delIds.add(Long.parseLong(ids[i]));
                        } else {
                            pathSet.add(value);
                        }
                    }
                }
            });
        }
        executor.shutdown();
        try {
            while (!executor.awaitTermination(1, TimeUnit.SECONDS)) ;
        } catch (InterruptedException e) {
            log.error("线程池失效", e);
        }
        if (delIds.size() > 0) {
            log.info("删除重复文件数: {}", delIds.size());
            fileInfoMapper.deleteBatchIds(delIds);
        }
    }

}