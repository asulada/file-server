package com.asuala.file.server.vo.req;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @create: 2024/02/03
 **/
@Data
public class SearchReq {
    private List<String> key;
    private List<String> sortInfo;
    private int pageNum = 1;
    private int pageSize = 10;
}