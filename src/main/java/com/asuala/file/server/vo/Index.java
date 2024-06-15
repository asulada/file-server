package com.asuala.file.server.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("`index`")
public class Index {
    private Long id;

    /**
     * cpuid
     */
    private String cpuId;

    /**
     * 系统名
     */
    @TableField("`system`")
    private String system;

    /**
     * 创建时间
     */
    private Date createTime;
    private Date updateTime;
    private Integer delFlag;
    private String hostName;

    @TableField(exist = false)
    private Long nextUsn;
    @TableField(exist = false)
    private String volumeNo;
}