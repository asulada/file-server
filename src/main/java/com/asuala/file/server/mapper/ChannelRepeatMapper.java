package com.asuala.file.server.mapper;

import com.asuala.file.server.vo.ChannelRepeat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChannelRepeatMapper extends BaseMapper<ChannelRepeat> {
    int deleteByPrimaryKey(Long id);

    int insertSelective(ChannelRepeat record);

    ChannelRepeat selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ChannelRepeat record);

    int updateByPrimaryKey(ChannelRepeat record);

    int updateBatch(List<ChannelRepeat> list);

    int updateBatchSelective(List<ChannelRepeat> list);

    int batchInsert(@Param("list") List<ChannelRepeat> list);
}