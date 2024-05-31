package com.asuala.file.server.controller;

import com.alibaba.fastjson2.JSONObject;
import com.asuala.file.server.es.Es8Client;
import com.asuala.file.server.es.entity.FileInfoEs;
import com.asuala.file.server.service.RecordService;
import com.asuala.file.server.service.ServerService;
import com.asuala.file.server.utils.MD5Utils;
import com.asuala.file.server.vo.req.FileInfoReq;
import com.asuala.file.server.vo.req.RebuildReq;
import com.asuala.file.server.vo.req.UrlReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

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

    private final RecordService recordService;
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

    @PostMapping("analysis")
    public JSONObject analysis(@RequestBody UrlReq req) {
        JSONObject res = new JSONObject();
        res.put("code", 222);
        if (StringUtils.isBlank(req.getUrl())) {
            res.put("msg", "解析地址为空");
        } else {
            try {
                String cmd = "you-get -x 127.0.0.1:7890 -i " + req.getUrl();
                // 执行命令
                Process process = Runtime.getRuntime().exec(cmd);

                // 读取命令执行结果
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                log.info("打印命令执行结果 {}", output.toString());
                int exitVal = process.waitFor();
                log.info("获取命令执行返回值 {}", exitVal);
            } catch (IOException | InterruptedException e) {
                log.error("解析地址失败", e);
                res.put("msg", "解析地址失败");
            }
        }
        return res;
    }

    @PostMapping("repeat")
    public JSONObject repeat(@RequestBody UrlReq req) {
        JSONObject res = new JSONObject();
        res.put("code", 222);
        if (StringUtils.isBlank(req.getFileName())) {
            res.put("msg", "名称为空");
            return res;
        }
        if (StringUtils.isBlank(req.getAuthor())) {
            res.put("msg", "作者为空");
            return res;
        }
        req.setFileName(req.getFileName().trim());

        List<String> list = recordService.findQualityByAuthorAndName(req.getAuthor(), req.getFileName());
        if (list.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("已有");
            for (String record : list) {
                stringBuilder.append(" ").append(record);
            }
            stringBuilder.append(" 任务");
            res.put("msg", stringBuilder.toString());
            return res;
        }
        res.put("code", 200);
        return res;
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

        es8Client.addData(fileInfoEs, false);
        res.put("code", 200);
        return res;
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
        if (null == req.getId()) {
            return res;
        }

        es8Client.delDocId(req.getId().toString(), FileInfoEs.class);
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

}