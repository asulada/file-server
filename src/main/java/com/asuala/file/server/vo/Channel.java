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
public class Channel {
    private Long id;

    private String name;

    private String url;

    private Date createTime;

    private Date updateTime;

    /**
    * 0 未解析 1 已解析 2 异常
    */
    private Integer state;
}