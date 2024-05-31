package com.asuala.file.server.mapper;

import com.asuala.file.server.vo.Channel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChannelMapper extends BaseMapper<Channel> {
    int deleteByPrimaryKey(Long id);

    int insertSelective(Channel record);

    Channel selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Channel record);

    int updateByPrimaryKey(Channel record);

    int updateBatch(List<Channel> list);

    int updateBatchSelective(List<Channel> list);

    int batchInsert(@Param("list") List<Channel> list);
}