package com.asuala.file.server.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Record {
    private Long id;

    private String name;

    private String url;

    private Date createTime;

    /**
     * 0 未下载  1 已下载 2 txt 3 txt已下载 4 解析下载地址失败
     */
    private Integer state;

    private Date updateTime;

    /**
     * 质量
     */
    private String quality;

    private Integer delFlag;

    private String pageUrl;
    private String author;
    private String picUrl;

    @TableField("`index`")
    private Integer index;

    private Integer failNum;

    private String timeHum;
}