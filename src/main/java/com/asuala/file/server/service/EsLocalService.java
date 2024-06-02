package com.asuala.file.server.service;

import cn.hutool.http.HttpUtil;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import com.alibaba.fastjson2.JSON;
import com.asuala.file.server.es.Es8Client;
import com.asuala.file.server.es.entity.FileInfoEs;
import com.asuala.file.server.utils.MD5Utils;
import com.asuala.file.server.vo.FileInfo;
import com.asuala.file.server.vo.req.FileBatchInfoReq;
import com.asuala.file.server.vo.req.FileInfoReq;
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
public class EsLocalService implements EsService{

    @Autowired(required = false)
    private Es8Client es8Client;


    public void saveEs(FileInfo fileInfo) {
        FileInfoEs fileInfoEs = convertEs(fileInfo);
        es8Client.addData(fileInfoEs, true);


    }

    public void saveEsList(List<FileInfo> fileInfos, String name) {
        List<FileInfoEs> fileInfoEs = convertEsBatch(fileInfos);
        es8Client.addData(fileInfoEs, true);

    }

    public void delEs(FileInfo fileInfo) {
        es8Client.delDocId(fileInfo.getId().toString(), FileInfoEs.class);

    }

    public void delEs(FileInfo fileInfo, List<Long> ids) {
        es8Client.delByIds(Query.of(q -> q.terms((t -> t
                .field("sId")
                .terms(TermsQueryField.of(tf -> tf
                        .value(ids.stream().map(item -> FieldValue.of(item)).collect(Collectors.toList()))  // Replace with actual terms
                ))))), FileInfoEs.class);

    }


}