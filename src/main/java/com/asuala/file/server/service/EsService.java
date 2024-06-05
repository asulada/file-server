package com.asuala.file.server.service;

import com.asuala.file.server.es.entity.FileInfoEs;
import com.asuala.file.server.vo.FileInfo;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public interface EsService {

    void rebuldData() throws IOException;
     void saveEs(FileInfo fileInfo);
    void saveEsList(List<FileInfo> fileInfos, String name);
    void delEs(FileInfo fileInfo);
    void delEs(FileInfo fileInfo, List<Long> ids);

    public default List<FileInfoEs> convertEsBatch(List<FileInfo> list) {
        return list.stream().map(req -> {
            FileInfoEs fileInfoEs = new FileInfoEs();
            fileInfoEs.setId(req.getId());
            fileInfoEs.setName(req.getName());
            fileInfoEs.setPath(req.getPath());
            fileInfoEs.setSuffix(req.getSuffix());
            fileInfoEs.setSize(req.getSize());
            fileInfoEs.setChangeTime(req.getChangeTime());
            fileInfoEs.setIndex(req.getIndex());
            fileInfoEs.setSId(req.getUId());
            return fileInfoEs;
        }).collect(Collectors.toList());
    }

    public default FileInfoEs convertEs(FileInfo req) {
        FileInfoEs fileInfoEs = new FileInfoEs();
        fileInfoEs.setId(req.getId());
        fileInfoEs.setName(req.getName());
        fileInfoEs.setPath(req.getPath());
        fileInfoEs.setSuffix(req.getSuffix());
        fileInfoEs.setSize(req.getSize());
        fileInfoEs.setChangeTime(req.getChangeTime());
        fileInfoEs.setIndex(req.getIndex());
        fileInfoEs.setSId(req.getUId());
        return fileInfoEs;
    }
}
