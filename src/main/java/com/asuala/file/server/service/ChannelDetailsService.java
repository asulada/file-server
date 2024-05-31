package com.asuala.file.server.service;

import com.asuala.file.server.mapper.ChannelDetailsMapper;
import com.asuala.file.server.vo.ChannelDetails;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ChannelDetailsService{

    @Resource
    private ChannelDetailsMapper channelDetailsMapper;

    
    public int deleteByPrimaryKey(Long id) {
        return channelDetailsMapper.deleteByPrimaryKey(id);
    }

    
    public int insertSelective(ChannelDetails record) {
        return channelDetailsMapper.insertSelective(record);
    }

    
    public ChannelDetails selectByPrimaryKey(Long id) {
        return channelDetailsMapper.selectByPrimaryKey(id);
    }

    
    public int updateByPrimaryKeySelective(ChannelDetails record) {
        return channelDetailsMapper.updateByPrimaryKeySelective(record);
    }

    
    public int updateByPrimaryKey(ChannelDetails record) {
        return channelDetailsMapper.updateByPrimaryKey(record);
    }

    
    public int updateBatch(List<ChannelDetails> list) {
        return channelDetailsMapper.updateBatch(list);
    }

    
    public int updateBatchSelective(List<ChannelDetails> list) {
        return channelDetailsMapper.updateBatchSelective(list);
    }

    
    public int batchInsert(List<ChannelDetails> list) {
        return channelDetailsMapper.batchInsert(list);
    }

}
