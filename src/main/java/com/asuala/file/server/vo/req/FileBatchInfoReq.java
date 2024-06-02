package com.asuala.file.server.vo.req;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @create: 2024/06/02
 **/
@Data
public class FileBatchInfoReq {
    private List<FileInfoReq> files;
    private String sign;
    private String name;

}