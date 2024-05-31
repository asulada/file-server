package com.asuala.file.server.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UPath {
    private Long id;

    private Long uId;

    private String path;

    private Long sId;

    @TableField("`index`")
    private Long index;
}