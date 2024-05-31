package com.asuala.file.server.vo.req;

import lombok.Data;

/**
 * @description:
 * @create: 2024/02/03
 **/
@Data
public class RebuildReq {
    private String sign;
    private Integer index;
}