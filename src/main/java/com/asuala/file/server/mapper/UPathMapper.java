package com.asuala.file.server.mapper;

import com.asuala.file.server.vo.UPath;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UPathMapper extends BaseMapper<UPath> {
    int deleteByPrimaryKey(Long id);

    int insertSelective(UPath record);

    UPath selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UPath record);

    int updateByPrimaryKey(UPath record);

    int batchInsert(@Param("list") List<UPath> list);

    int batchUpdate(@Param("list") List<UPath> list);
}