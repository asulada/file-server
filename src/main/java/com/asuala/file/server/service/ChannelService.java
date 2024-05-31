package com.asuala.file.server.service;

import com.asuala.file.server.mapper.ChannelMapper;
import com.asuala.file.server.vo.Channel;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelService extends ServiceImpl<ChannelMapper, Channel> {

    public int deleteByPrimaryKey(Long id) {
        return baseMapper.deleteByPrimaryKey(id);
    }


    public int insertSelective(Channel record) {
        return baseMapper.insertSelective(record);
    }


    public Channel selectByPrimaryKey(Long id) {
        return baseMapper.selectByPrimaryKey(id);
    }


    public int updateByPrimaryKeySelective(Channel record) {
        return baseMapper.updateByPrimaryKeySelective(record);
    }


    public int updateByPrimaryKey(Channel record) {
        return baseMapper.updateByPrimaryKey(record);
    }


    public int updateBatch(List<Channel> list) {
        return baseMapper.updateBatch(list);
    }


    public int updateBatchSelective(List<Channel> list) {
        return baseMapper.updateBatchSelective(list);
    }


    public int batchInsert(List<Channel> list) {
        return baseMapper.batchInsert(list);
    }

}
