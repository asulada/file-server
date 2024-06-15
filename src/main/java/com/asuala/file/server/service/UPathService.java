package com.asuala.file.server.service;

import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.collection.CollectionUtil;
import com.asuala.file.server.config.MainConstant;
import com.asuala.file.server.file.monitor.linux.FileNode;
import com.asuala.file.server.file.monitor.linux.InotifyLibraryUtil;
import com.asuala.file.server.mapper.UPathMapper;
import com.asuala.file.server.search.UPathReq;
import com.asuala.file.server.utils.FileIdUtils;
import com.asuala.file.server.vo.FileInfo;
import com.asuala.file.server.vo.UPath;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UPathService extends ServiceImpl<UPathMapper, UPath> {

    @Autowired(required = false)
    private WatchFileService watchFileService;
    private final FileInfoService fileInfoService;

    @Value("${watch.insertSize:5000}")
    private int insertSize;

    public int deleteByPrimaryKey(Long id) {
        return baseMapper.deleteByPrimaryKey(id);
    }


    public int insertSelective(UPath record) {
        return baseMapper.insertSelective(record);
    }


    public UPath selectByPrimaryKey(Long id) {
        return baseMapper.selectByPrimaryKey(id);
    }


    public int updateByPrimaryKeySelective(UPath record) {
        return baseMapper.updateByPrimaryKeySelective(record);
    }


    public int updateByPrimaryKey(UPath record) {
        return baseMapper.updateByPrimaryKey(record);
    }


    public int batchInsert(List<UPath> list) {
        return baseMapper.batchInsert(list);
    }

    public SaResult addWatch(UPathReq req) throws Exception {
        //TODO-asuala 2024-06-15: 判断
        if (baseMapper.selectCount(new LambdaQueryWrapper<UPath>().eq(UPath::getIndex, req.getIndex()).likeRight(UPath::getPath, req.getPath())) > 0) {
            return SaResult.error("路径已存在");
        }
        Long sId = FileIdUtils.buildFileId(req.getIndex().intValue(), req.getPath());
        List<UPath> pathInserts = new ArrayList<>();
        req.getUId().forEach(item -> {
            pathInserts.add(UPath.builder().index(req.getIndex()).path(req.getPath()).uId(item).sId(sId).build());
            Set<Long> longs = MainConstant.userResource.get(item);

            if (null != longs) {
                longs.add(sId);
            } else {
                longs = new HashSet<>();
                longs.add(sId);
            }
        });
        baseMapper.batchInsert(pathInserts);
        Map<String, FileNode> fileMap = new HashMap<>();
        fileMap.put(req.getPath(), FileNode.builder().sId(sId).build());
        watchFileService.initFileInfo(MainConstant.systemInfo, fileMap);

        Map<Integer, List<FileInfo>> map = InotifyLibraryUtil.rebuild(fileMap, 0);
        //保存文件信息
        for (Map.Entry<Integer, List<FileInfo>> entry : map.entrySet()) {
            List<List<FileInfo>> split = CollectionUtil.split(entry.getValue(), insertSize);
            for (List<FileInfo> infos : split) {
                fileInfoService.initBatchSave(infos, entry.getKey());
            }
        }
        return SaResult.ok();
    }
}
