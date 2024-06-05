package com.asuala.file.server.service;

import cn.hutool.http.HttpUtil;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import com.alibaba.fastjson2.JSON;
import com.asuala.file.server.config.MainConstant;
import com.asuala.file.server.es.Es8Client;
import com.asuala.file.server.es.entity.FileInfoEs;
import com.asuala.file.server.utils.MD5Utils;
import com.asuala.file.server.vo.FileInfo;
import com.asuala.file.server.vo.req.FileBatchInfoReq;
import com.asuala.file.server.vo.req.FileInfoReq;
import com.asuala.file.server.vo.req.RebuildReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @create: 2024/06/02
 **/
@Slf4j
public class EsHttpServcie implements EsService {
    @Value("${file.server.http.rebuildUrl}")
    private String rebuildUrl;
    @Value("${file.server.http.addUrl}")
    private String addUrl;
    @Value("${file.server.http.delUrl}")
    private String delUrl;
    @Value("${file.server.http.salt}")
    private String salt;

    @Override
    public void rebuldData() {
        RebuildReq req = new RebuildReq();
        req.setIndex(MainConstant.index);
        req.setSign(MD5Utils.getSaltMD5(String.valueOf(MainConstant.index), salt));
        try {
            HttpUtil.post(rebuildUrl, JSON.toJSONString(req));
        } catch (Exception e) {
            log.error("发送重建数据请求失败id: {}", MainConstant.index, e);
        }
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

    public void saveEsList(List<FileInfo> fileInfos, String name) {

        FileBatchInfoReq req = new FileBatchInfoReq();
        req.setSign(MD5Utils.getSaltMD5(name, salt));
        req.setName(name);
        req.setFiles(fileInfos.stream().map(fileInfo -> {
            FileInfoReq vo = new FileInfoReq();
            vo.setId(fileInfo.getId());
            vo.setName(fileInfo.getName());
            vo.setPath(fileInfo.getPath());
            vo.setSuffix(fileInfo.getSuffix());
            vo.setSize(fileInfo.getSize());
            vo.setChangeTime(fileInfo.getChangeTime());
            vo.setIndex(fileInfo.getIndex());
            vo.setUId(fileInfo.getUId());
            return vo;
        }).collect(Collectors.toList()));
        try {
            HttpUtil.post(addUrl, JSON.toJSONString(req));
        } catch (Exception e) {
            log.error("发送es请求失败 文件id: {}", name, e);
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

    public void delEs(FileInfo fileInfo, List<Long> ids) {

        FileInfoReq req = new FileInfoReq();
        req.setIds(ids);
        req.setSign(MD5Utils.getSaltMD5(fileInfo.getName(), salt));
        req.setName(fileInfo.getName());
        try {
            HttpUtil.post(delUrl, JSON.toJSONString(req));
        } catch (Exception e) {
            log.error("发送es请求失败 文件id: {}", fileInfo.getId(), e);
        }

    }


}