package com.asuala.file.server.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.asuala.file.server.config.MainConstant;
import com.asuala.file.server.mapper.FileInfoMapper;
import com.asuala.file.server.utils.FileUtils;
import com.asuala.file.server.utils.MD5Utils;
import com.asuala.file.server.vo.FileInfo;
import com.asuala.file.server.vo.req.FileInfoReq;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class FileInfoService extends ServiceImpl<FileInfoMapper, FileInfo> {

    @Value("${file.server.http.addUrl}")
    private String addUrl;
    @Value("${file.server.http.delUrl}")
    private String delUrl;
    @Value("${file.server.http.salt}")
    private String salt;

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

    public void insert(File file, Long sId) {
        FileInfo.FileInfoBuilder builder = FileInfo.builder().name(file.getName()).path(file.getAbsolutePath()).createTime(new Date()).index(MainConstant.index).changeTime(new Date(file.lastModified())).uId(sId);
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
        saveEs(fileInfo);
    }


    public void saveEs(FileInfo fileInfo) {
        FileInfoReq req = new FileInfoReq();
        req.setId(fileInfo.getId());
        req.setName(fileInfo.getName());
        req.setPath(fileInfo.getPath());
        req.setSuffix(fileInfo.getSuffix());
        req.setSize(fileInfo.getSize());
        req.setChangeTime(fileInfo.getChangeTime());
        req.setIndex(fileInfo.getIndex());
        req.setSign(MD5Utils.getSaltMD5(fileInfo.getName(), salt));
        req.setUId(fileInfo.getUId());
        try {
            HttpUtil.post(addUrl, JSON.toJSONString(req));
        } catch (Exception e) {
            log.error("发送es请求失败 文件id: {}", fileInfo.getId(), e);
        }
    }

    public void delEs(FileInfo fileInfo) {
        FileInfoReq req = new FileInfoReq();
        req.setId(fileInfo.getId());
        req.setSign(MD5Utils.getSaltMD5(fileInfo.getName(), salt));
        req.setName(fileInfo.getName());
        try {
            HttpUtil.post(delUrl, JSON.toJSONString(req));
        } catch (Exception e) {
            log.error("发送es请求失败 文件id: {}", fileInfo.getId(), e);
        }
    }
}
