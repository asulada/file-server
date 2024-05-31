package com.asuala.file.server.mapper;

import com.asuala.file.server.vo.Index;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface IndexMapper extends BaseMapper<Index> {
    int deleteByPrimaryKey(Long id);

    int insertSelective(Index record);

    Index selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Index record);

    int updateByPrimaryKey(Index record);

    int updateBatch(List<Index> list);

    int updateBatchSelective(List<Index> list);

    int batchInsert(@Param("list") List<Index> list);

    Index findByCpuId(@Param("cpuId") String cpuId);


    int updateUpdateTimeById(@Param("updatedUpdateTime")Date updatedUpdateTime,@Param("id")Long id);


}