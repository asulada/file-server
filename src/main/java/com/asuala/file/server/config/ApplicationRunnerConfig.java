package com.asuala.file.server.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.asuala.file.server.file.monitor.linux.Constant;
import com.asuala.file.server.file.monitor.linux.FileMemory;
import com.asuala.file.server.file.monitor.linux.FileNode;
import com.asuala.file.server.file.monitor.linux.InotifyLibraryUtil;
import com.asuala.file.server.file.monitor.win.FileChangeListener;
import com.asuala.file.server.file.monitor.win.utils.MonitorFileUtil;
import com.asuala.file.server.file.monitor.win.vo.FileTreeNode;
import com.asuala.file.server.mapper.FileInfoMapper;
import com.asuala.file.server.mapper.UPathMapper;
import com.asuala.file.server.mapper.UserMapper;
import com.asuala.file.server.service.*;
import com.asuala.file.server.utils.CPUUtils;
import com.asuala.file.server.utils.FileIdUtils;
import com.asuala.file.server.utils.MD5Utils;
import com.asuala.file.server.vo.FileInfo;
import com.asuala.file.server.vo.Index;
import com.asuala.file.server.vo.UPath;
import com.asuala.file.server.vo.User;
import com.asuala.file.server.vo.req.RebuildReq;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sun.jna.platform.FileMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @description:
 * @create: 2020/07/16
 **/
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationRunnerConfig implements ApplicationRunner {

    private final IndexService indexService;

    private final FileInfoMapper fileInfoMapper;
    private final FileInfoService fileInfoService;
    private final UPathMapper uPathMapper;
    private final UserMapper userMapper;
    private final EsService esService;

    @Autowired(required = false)
    private WatchFileService watchFileService;

    @Value("${watch.rebuldFlag:false}")
    private boolean rebuldFlag;
    @Value("${file.server.open}")
    private boolean server;
    @Value("${file.server.http.salt}")
    private String salt;

    @Value("${watch.deleteLimit:1000000}")
    private int deleteLimit;
    @Value("${watch.open}")
    private boolean watchOpen;
    @Value("${watch.exclude}")
    private String exclude;
    @Value("${watch.insertSize:5000}")
    private int insertSize;
    @Value("${watch.findThreadNum:2}")
    private int findThreadNum;

    @PostConstruct
    public void init() throws Exception {
        String[] split = exclude.split(",");
        for (String s : split) {
            if (StringUtils.isNotBlank(s)) {
                Constant.exclude.add(s);
            }
        }
        Index index = CPUUtils.getCpuId();
        MainConstant.systemInfo = index;
        log.info("忽略文件夹 {} 数量 {}", exclude, Constant.exclude.size());
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Override
    public void run(ApplicationArguments args) throws Exception {
        Index index = MainConstant.systemInfo;
        addIndex(index);
        MainConstant.snowflake = IdUtil.getSnowflake(index.getId());
        if (watchOpen) {
            openWatch(index);
        }
        if (server) {
//            LocalDateTime now = LocalDateTime.now();
//            List<Index> list = indexService.list(new LambdaQueryWrapper<Index>().ge(Index::getUpdateTime, now.minusMinutes(40).format(formatter)));
//            if (list.size() > 0) {
//                MainConstant.index = Math.toIntExact(list.get(0).getId());
//                log.debug("客户端信息 {}", MainConstant.index);
//            } else {
//                log.error("没有取到存活的客户端信息 !!!");
//            }

            //计算文件搜索权限
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
            for (User user : users) {
                List<UPath> uPaths = uPathMapper.selectList(Wrappers.emptyWrapper());
                if (uPaths.size() > 0) {
                    Map<Long, List<UPath>> listMap = uPaths.stream().collect(Collectors.groupingBy(UPath::getUId));
                    List<UPath> paths = listMap.get(user.getId());
                    if (CollectionUtil.isNotEmpty(paths)) {
                        MainConstant.userResource.put(user.getId(), paths.stream().map(item -> FileIdUtils.buildFileId(item.getIndex().intValue(), item.getPath())).collect(Collectors.toSet()));

                    }
                }
            }
        }
        log.info("初始化结束 客户端号: {}", MainConstant.index);

    }

    private void openWatch(Index index) throws Exception {
        List<UPath> uPaths = uPathMapper.selectList(new LambdaQueryWrapper<UPath>().select(UPath::getPath, UPath::getIndex, UPath::getSId, UPath::getId).eq(UPath::getIndex, index.getId()));
        if (uPaths.size() == 0) {
            return;
        }
        Map<String, FileNode> fileMap = new HashMap<>();
        List<UPath> update = new ArrayList<>();
        for (UPath item : uPaths) {
            if (item.getSId() == 0L) {
                Long sId = FileIdUtils.buildFileId(item.getIndex().intValue(), item.getPath());
                fileMap.put(item.getPath(), FileNode.builder().sId(sId).build());

                item.setSId(sId);
                update.add(item);

            } else {
                fileMap.put(item.getPath(), FileNode.builder().sId(item.getSId()).build());

            }
        }
        if (update.size() > 0) {
            uPathMapper.batchUpdate(update);
        }
        if (rebuldFlag) {
            //TODO-asuala 2024-02-02: 删除表数据
            Long count = fileInfoMapper.selectCount(new LambdaQueryWrapper<FileInfo>().eq(FileInfo::getIndex, MainConstant.index));
            if (count > deleteLimit) {
                fileInfoMapper.dropIndex(MainConstant.index);
            } else {
                while (count > 0) {
                    count = fileInfoMapper.deleteLimit(MainConstant.index);
                }
            }
            watchFileService.initFileInfo(index, fileMap);

            if (index.getSystem().contains("LINUX")) {
                Map<Integer, List<FileInfo>> map = InotifyLibraryUtil.rebuild(fileMap, findThreadNum);
                //保存文件信息
                for (Map.Entry<Integer, List<FileInfo>> entry : map.entrySet()) {
                    List<List<FileInfo>> split = CollectionUtil.split(entry.getValue(), insertSize);
                    for (List<FileInfo> infos : split) {
                        fileInfoService.initBatchSave(infos, entry.getKey());
                    }
                }

            } else {
                MonitorFileUtil.fileInfoServicel = fileInfoService;
                for (Map.Entry<String, Long> entry : MainConstant.volumeNos.entrySet()) {
                    TreeMap<Long, FileTreeNode> map = MonitorFileUtil.buildFileInfo(entry.getKey());
                    MonitorFileUtil.getFileInfo(map, entry);
                }

            }
            esService.rebuldData();
        } else {
            watchFileService.initFileInfo(index, fileMap);

            if (index.getSystem().contains("LINUX")) {
                for (Map.Entry<String, FileNode> entry : fileMap.entrySet()) {
                    ConcurrentHashMap<String, FileMemory> pathIdMap = (ConcurrentHashMap) InotifyLibraryUtil.fdMap.get(entry.getValue().getFd()).getPathIdMap();
                    while (true) {
                        int i = 1;
                        Page<FileInfo> page = new Page<>(1, insertSize);
                        Page<FileInfo> result = fileInfoMapper.selectPage(page, new LambdaQueryWrapper<FileInfo>().select(FileInfo::getId, FileInfo::getPath, FileInfo::getDId).eq(FileInfo::getUId, entry.getValue().getSId()));
                        result.getRecords().stream().forEach(item -> pathIdMap.putIfAbsent(item.getPath(), FileMemory.builder().id(item.getId()).dId(item.getDId()).build()));
                        if (i++ == result.getPages()) {
                            break;
                        }
                    }
                }
            }
        }
    }


    private void addIndex(Index index) {
        try {
            Index one = indexService.findByCpuId(index.getCpuId());
            Date now = new Date();
            if (null == one) {
                index.setCreateTime(now);
                index.setUpdateTime(now);
                indexService.save(index);
                one = index;
            } else {
                one.setDelFlag(0);
                one.setUpdateTime(now);
                one.setHostName(index.getHostName());
                indexService.updateById(one);
            }
            index.setId(one.getId());
            MainConstant.index = Integer.parseInt(one.getId().toString());
        } catch (Exception e) {
            log.error("获取cpuid失败", e);
        }
    }


}