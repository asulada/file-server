package com.asuala.file.server.service;

import com.asuala.file.server.mapper.ChannelRepeatMapper;
import com.asuala.file.server.vo.ChannelRepeat;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ChannelRepeatService{

    @Resource
    private ChannelRepeatMapper channelRepeatMapper;

    
    public int deleteByPrimaryKey(Long id) {
        return channelRepeatMapper.deleteByPrimaryKey(id);
    }

    
    public int insertSelective(ChannelRepeat record) {
        return channelRepeatMapper.insertSelective(record);
    }

    
    public ChannelRepeat selectByPrimaryKey(Long id) {
        return channelRepeatMapper.selectByPrimaryKey(id);
    }

    
    public int updateByPrimaryKeySelective(ChannelRepeat record) {
        return channelRepeatMapper.updateByPrimaryKeySelective(record);
    }

    
    public int updateByPrimaryKey(ChannelRepeat record) {
        return channelRepeatMapper.updateByPrimaryKey(record);
    }

    
    public int updateBatch(List<ChannelRepeat> list) {
        return channelRepeatMapper.updateBatch(list);
    }

    
    public int updateBatchSelective(List<ChannelRepeat> list) {
        return channelRepeatMapper.updateBatchSelective(list);
    }

    
    public int batchInsert(List<ChannelRepeat> list) {
        return channelRepeatMapper.batchInsert(list);
    }

}
