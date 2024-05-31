package com.asuala.file.server.vo.req;

import lombok.Data;

/**
 * @description:
 * @create: 2024/01/12
 **/
@Data
public class UrlReq {

    private String fileName;
    private String url;
    private String quality;
    private String pageUrl;
    private String author;
    private Integer index;
    private boolean dbDlick = false;
    private String picUrl;
    private long duration;
}