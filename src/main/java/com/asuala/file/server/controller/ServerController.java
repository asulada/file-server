package com.asuala.file.server.controller;

import cn.dev33.satoken.util.SaResult;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import com.alibaba.fastjson2.JSONObject;
import com.asuala.file.server.es.Es8Client;
import com.asuala.file.server.es.entity.FileInfoEs;
import com.asuala.file.server.service.IndexService;
import com.asuala.file.server.service.RecordService;
import com.asuala.file.server.service.ServerService;
import com.asuala.file.server.utils.MD5Utils;
import com.asuala.file.server.vo.Index;
import com.asuala.file.server.vo.req.FileBatchInfoReq;
import com.asuala.file.server.vo.req.FileInfoReq;
import com.asuala.file.server.vo.req.RebuildReq;
import com.asuala.file.server.vo.req.UrlReq;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @create: 2023/08/09
 **/
@RestController
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "file", name = "server.open", havingValue = "true")
@RequestMapping("video")
public class ServerController {

    private final ServerService serverService;

    @Autowired(required = false)
    private Es8Client es8Client;

    @Value("${file.server.http.salt}")
    private String salt;


    @PostConstruct
    public void init() {
        //TODO 2022-09-18: ElasticSearch 创建索引
        try {
            es8Client.createIndexSettingsMappings(FileInfoEs.class);
            log.info("es创建索引结束");
        } catch (Exception e) {
            log.error("连接es创建索引失败", e);
        }
    }


    @PostMapping("saveEs")
    public JSONObject saveEs(@RequestBody FileInfoReq req) {
        JSONObject res = new JSONObject();
        res.put("code", 222);
        if (StringUtils.isBlank(req.getSign())) {
            return res;
        }
        if (!MD5Utils.getSaltverifyMD5(req.getName(), salt, req.getSign())) {
            return res;
        }
        if (StringUtils.isBlank(req.getName())) {
            return res;
        }
        if (StringUtils.isBlank(req.getPath())) {
            return res;
        }
        if (null == req.getIndex()) {
            return res;
        }
        if (null == req.getId()) {
            return res;
        }
        if (null == req.getUId()) {
            return res;
        }
        FileInfoEs fileInfoEs = convertEs(req);
        es8Client.updateDataInsert(fileInfoEs, true);
        res.put("code", 200);
        return res;
    }

    @PostMapping("saveEsBatch")
    public JSONObject saveEsBatch(@RequestBody FileBatchInfoReq req) {
        JSONObject res = new JSONObject();
        res.put("code", 222);
        if (StringUtils.isBlank(req.getSign())) {
            return res;
        }
        if (!MD5Utils.getSaltverifyMD5(req.getName(), salt, req.getSign())) {
            return res;
        }
        if (StringUtils.isBlank(req.getName())) {
            return res;
        }
        List<FileInfoEs> fileInfoEs = convertEsBatch(req);
        es8Client.addData(fileInfoEs, true);

        res.put("code", 200);
        return res;
    }

    private List<FileInfoEs> convertEsBatch(FileBatchInfoReq list) {
        return list.getFiles().stream().map(req -> {
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

    private FileInfoEs convertEs(FileInfoReq req) {
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

    @PostMapping("delEs")
    public JSONObject delEs(@RequestBody FileInfoReq req) throws IOException {
        JSONObject res = new JSONObject();
        res.put("code", 222);
        if (StringUtils.isBlank(req.getSign())) {
            return res;
        }
        if (!MD5Utils.getSaltverifyMD5(req.getName(), salt, req.getSign())) {
            return res;
        }
        if (null == req.getId() || CollectionUtils.isEmpty(req.getIds())) {
            return res;
        }
        if (null != req.getId()) {
            es8Client.delDocId(req.getId().toString(), FileInfoEs.class);
        } else {
            es8Client.delByIds(Query.of(q -> q.terms((t -> t
                    .field("id")
                    .terms(TermsQueryField.of(tf -> tf
                            .value(req.getIds().stream().map(item -> FieldValue.of(item)).collect(Collectors.toList()))  // Replace with actual terms
                    ))))), FileInfoEs.class);
        }
        res.put("code", 200);
        return res;
    }

    @PostMapping("rebuildData")
    public void rebuildData(@RequestBody RebuildReq req) throws IOException {
        if (StringUtils.isBlank(req.getSign())) {
            return;
        }
        if (null == req.getIndex()) {
            return;
        }
        if (!MD5Utils.getSaltverifyMD5(req.getIndex().toString(), salt, req.getSign())) {
            return;
        }
        serverService.rebuildData(req);
    }

    @PostMapping("addWtachEs")
    public void addWtachEs(@RequestBody RebuildReq req) throws IOException {
        if (StringUtils.isBlank(req.getSign())) {
            return;
        }
        if (null == req.getIndex()) {
            return;
        }
        if (!MD5Utils.getSaltverifyMD5(req.getIndex().toString(), salt, req.getSign())) {
            return;
        }
        serverService.addWtachEs(req);
    }

}