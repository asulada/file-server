package com.asuala.file.server.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChannelDetails {
    private Long id;

    private Long pId;

    private String url;

    private String picUrl;

    private String name;

    private String author;

    private Date createTime;

    private Date updateTime;

    /**
    * 0 未处理 1 已处理
    */
    private Integer state;
    private Long rId;
}