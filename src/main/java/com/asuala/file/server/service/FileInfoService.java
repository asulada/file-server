package com.asuala.file.server.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpUtil;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import com.alibaba.fastjson2.JSON;
import com.asuala.file.server.config.MainConstant;
import com.asuala.file.server.es.Es8Client;
import com.asuala.file.server.es.entity.FileInfoEs;
import com.asuala.file.server.file.monitor.linux.FileMemory;
import com.asuala.file.server.file.monitor.linux.FileVo;
import com.asuala.file.server.file.monitor.linux.InotifyLibraryUtil;
import com.asuala.file.server.mapper.FileInfoMapper;
import com.asuala.file.server.utils.FileUtils;
import com.asuala.file.server.utils.MD5Utils;
import com.asuala.file.server.vo.FileInfo;
import com.asuala.file.server.vo.req.FileBatchInfoReq;
import com.asuala.file.server.vo.req.FileInfoReq;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileInfoService extends ServiceImpl<FileInfoMapper, FileInfo> {
    @Value("${watch.insertSize:5000}")
    private int insertSize;

    private final EsService esService;

    private static final Lock lock = new ReentrantLock();

    public int deleteByPrimaryKey(Long id) {
        return baseMapper.deleteByPrimaryKey(id);
    }


    public int insertSelective(FileInfo record) {
        return baseMapper.insertSelective(record);
    }


    public FileInfo selectByPrimaryKey(Long id) {
        return baseMapper.selectByPrimaryKey(id);
    }


    public int updateByPrimaryKeySelective(FileInfo record) {
        return baseMapper.updateByPrimaryKeySelective(record);
    }


    public int updateByPrimaryKey(FileInfo record) {
        return baseMapper.updateByPrimaryKey(record);
    }


    public int batchInsert(List<FileInfo> list) {
        lock.lock();
        int i = 0;
        try {
            i = baseMapper.batchInsert(list);
        } finally {
            lock.unlock();
        }
        return i;
    }


    public FileInfo findFileInfo(File file) {
        List<FileInfo> list = baseMapper.selectList(new LambdaQueryWrapper<FileInfo>().eq(FileInfo::getName, file.getName()).eq(FileInfo::getIndex, MainConstant.index));
        FileInfo fileInfo = null;
        for (FileInfo info : list) {
            if (info.getPath().equals(file.getAbsolutePath())) {
                fileInfo = info;
                break;
            }
        }
        return fileInfo;
    }

    public FileInfo findFileInfo(FileVo file) {
        FileMemory fileMemory = InotifyLibraryUtil.fdMap.get(file.getFd()).getPathIdMap().get(file.getFullPath());
        if (null != fileMemory) {
            return FileInfo.builder().name(file.getName()).id(fileMemory.getId()).dId(fileMemory.getDId()).build();
        }
        if ("".equals(file.getName())) {
            return null;
        }
        List<FileInfo> list = baseMapper.selectList(new LambdaQueryWrapper<FileInfo>().eq(FileInfo::getName, file.getName()).eq(FileInfo::getIndex, MainConstant.index));
        FileInfo fileInfo = null;
        for (FileInfo info : list) {
            if (info.getPath().equals(file.getFullPath())) {
                fileInfo = info;
                break;
            }
        }
        return fileInfo;
    }

    public FileInfo findFileInfo(String name, String path) {
        List<FileInfo> list = baseMapper.selectList(new LambdaQueryWrapper<FileInfo>().eq(FileInfo::getName, name).eq(FileInfo::getIndex, MainConstant.index));
        FileInfo fileInfo = null;
        for (FileInfo info : list) {
            if (info.getPath().equals(path)) {
                fileInfo = info;
                break;
            }
        }
        return fileInfo;
    }

    @Deprecated
    public Long selectDidByPath(String path) {
        return baseMapper.selectDidByPath(path);
    }

    public Long insert(File file, Long sId) {
        long dId = MainConstant.snowflake.nextId();
        FileInfo.FileInfoBuilder builder = FileInfo.builder().name(file.getName()).path(file.getAbsolutePath()).createTime(new Date()).index(MainConstant.index).changeTime(new Date(file.lastModified())).uId(sId).pId(0L)
                .dId(dId);
        if (file.isFile()) {
            String suffix = FileUtils.getSuffix(file.getName());
//            if (suffix.length() > 20) {
//                log.warn("文件后缀名过长: {}", file.getAbsolutePath());
//                return;
//            }
            builder.size(file.length()).suffix(suffix).dir(0);
        } else {
            builder.dir(1);
        }
        FileInfo fileInfo = builder.build();
        baseMapper.insert(fileInfo);
        esService.saveEs(fileInfo);
        return dId;
    }

    public FileInfo insert(File file, Long sId, Long pId) {
        FileInfo.FileInfoBuilder builder = FileInfo.builder().name(file.getName()).path(file.getAbsolutePath()).createTime(new Date()).index(MainConstant.index).changeTime(new Date(file.lastModified()))
                .uId(sId).pId(pId);
        if (file.isFile()) {
            String suffix = FileUtils.getSuffix(file.getName());
//            if (suffix.length() > 20) {
//                log.warn("文件后缀名过长: {}", file.getAbsolutePath());
//                return;
//            }
            builder.size(file.length()).suffix(suffix).dir(0).dId(0L);
        } else {
            long dId = MainConstant.snowflake.nextId();
            builder.dir(1).dId(dId);
        }
        FileInfo fileInfo = builder.build();
        baseMapper.insert(fileInfo);
        esService.saveEs(fileInfo);
        return fileInfo;
    }


    public void batchSave(List<FileInfo> files, String name, int fd) {
        if (files.size() > 0) {
            List<List<FileInfo>> split = CollectionUtil.split(files, insertSize);
            for (List<FileInfo> fileInfos : split) {
                batchSaveReturnId(fileInfos, fd);
                esService.saveEsList(fileInfos, name);
            }

        }
    }

    public void batchSaveReturnId(List<FileInfo> files, int fd) {
        baseMapper.batchInsert(files);
        //TODO-asuala 2024-06-03: 添加路径id对应
        ConcurrentHashMap<String, FileMemory> pathIdMap = (ConcurrentHashMap) InotifyLibraryUtil.fdMap.get(fd).getPathIdMap();
        files.stream().forEach(item -> pathIdMap.put(item.getPath(), FileMemory.builder().id(item.getId()).dId(item.getDId()).build()));
    }

    public void initBatchSave(List<FileInfo> files, int fd) {
        baseMapper.batchInsert(files);
        //TODO-asuala 2024-06-03: 添加路径id对应
        ConcurrentHashMap<String, FileMemory> pathIdMap = (ConcurrentHashMap) InotifyLibraryUtil.fdMap.get(fd).getPathIdMap();
        files.stream().forEach(item -> pathIdMap.putIfAbsent(item.getPath(), FileMemory.builder().id(item.getId()).dId(item.getDId()).build()));
    }

    public void clearDir(FileInfo fileInfo, FileVo poll) {
        List<Long> ids = new ArrayList<>();
        findChildFile(fileInfo.getDId(), poll.getFd(), ids);
        if (ids.size() > 0) {
            List<List<Long>> split = CollectionUtil.split(ids, insertSize);
            for (List<Long> longs : split) {
                removeBatchByIds(longs);
                esService.delEs(fileInfo, ids);
            }
        }
    }

    public void findChildFile(Long pId, int fd, List<Long> ids) {
        //TODO-asuala 2024-06-01: 找到剩下的文件
        List<FileInfo> list = baseMapper.selectList(new LambdaQueryWrapper<FileInfo>().select(FileInfo::getId, FileInfo::getDId, FileInfo::getPath, FileInfo::getDir).eq(FileInfo::getPId, pId).
                eq(FileInfo::getIndex, MainConstant.index));
        List<FileInfo> dirs = new ArrayList<>();
        List<String> childPaths = new ArrayList<>();

        for (FileInfo fileInfo : list) {
            InotifyLibraryUtil.fdMap.get(fd).getPathIdMap().remove(fileInfo.getPath());
            ids.add(fileInfo.getId());
            if (fileInfo.getDir() == 1) {
                dirs.add(fileInfo);
                childPaths.add(fileInfo.getPath());
            }
        }
        if (dirs.size() > 0) {
            InotifyLibraryUtil.removeWd(fd, childPaths);
            for (FileInfo dir : dirs) {
                findChildFile(dir.getDId(), fd, ids);
            }
        }
    }
}
