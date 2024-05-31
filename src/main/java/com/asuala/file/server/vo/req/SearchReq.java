package com.asuala.file.server.vo.req;

import lombok.Data;

/**
 * @description:
 * @create: 2024/02/03
 **/
@Data
public class SearchReq {
    private String key;
    private int pageNum = 1;
    private int pageSize = 10;
}