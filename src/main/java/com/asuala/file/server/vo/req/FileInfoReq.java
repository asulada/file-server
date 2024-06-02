package com.asuala.file.server.vo.req;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @create: 2024/02/02
 **/
@Data
public class FileInfoReq {

    private Long id;
    private String name;
    private String path;
    private String suffix;
    private Long size;
    private Date changeTime;
    private Integer index;
    private String sign;
    private Long uId;
    private List<Long> ids;
}