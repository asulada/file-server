package com.asuala.file.server.mapper;

import com.asuala.file.server.vo.ChannelDetails;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChannelDetailsMapper extends BaseMapper<ChannelDetails> {
    int deleteByPrimaryKey(Long id);

    int insertSelective(ChannelDetails record);

    ChannelDetails selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ChannelDetails record);

    int updateByPrimaryKey(ChannelDetails record);

    int updateBatch(List<ChannelDetails> list);

    int updateBatchSelective(List<ChannelDetails> list);

    int batchInsert(@Param("list") List<ChannelDetails> list);
}