package com.asuala.file.server.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordPage {
    @TableId
    private Long id;

    private Long pId;

    /**
    * 编号
    */
    private Integer num;

    /**
    * 名称
    */
    private String name;
}