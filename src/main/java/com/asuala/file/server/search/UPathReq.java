package com.asuala.file.server.search;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @create: 2024/06/15
 **/
@Data
public class UPathReq {

    @JsonProperty("uId")
    private List<Long> uId;

    private String path;

    private Long sId;

    private Long index;
}