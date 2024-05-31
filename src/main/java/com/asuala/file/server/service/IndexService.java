package com.asuala.file.server.service;

import com.asuala.file.server.mapper.IndexMapper;
import com.asuala.file.server.vo.Index;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class IndexService extends ServiceImpl<IndexMapper, Index> {


    public int deleteByPrimaryKey(Long id) {
        return baseMapper.deleteByPrimaryKey(id);
    }


    public int insertSelective(Index record) {
        return baseMapper.insertSelective(record);
    }


    public Index selectByPrimaryKey(Long id) {
        return baseMapper.selectByPrimaryKey(id);
    }


    public int updateByPrimaryKeySelective(Index record) {
        return baseMapper.updateByPrimaryKeySelective(record);
    }


    public int updateByPrimaryKey(Index record) {
        return baseMapper.updateByPrimaryKey(record);
    }


    public int updateBatch(List<Index> list) {
        return baseMapper.updateBatch(list);
    }


    public int updateBatchSelective(List<Index> list) {
        return baseMapper.updateBatchSelective(list);
    }


    public int batchInsert(List<Index> list) {
        return baseMapper.batchInsert(list);
    }

    public Index findByCpuId(String cpuId) {
        return baseMapper.findByCpuId(cpuId);
    }

    public void updateUpdateTimeById(long index) {
        baseMapper.updateUpdateTimeById(new Date(), index);
    }
}
