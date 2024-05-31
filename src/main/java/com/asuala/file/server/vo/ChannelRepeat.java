package com.asuala.file.server.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChannelRepeat {
    private Long id;

    private Long detailId;

    private Long fId;

    private String name;

    private String path;
}