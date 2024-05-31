package com.asuala.file.server.mapper;

import com.asuala.file.server.vo.RecordPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface RecordPageMapper extends BaseMapper<RecordPage> {
    int deleteByPrimaryKey(Long id);

    int insertSelective(RecordPage record);

    RecordPage selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RecordPage record);

    int updateByPrimaryKey(RecordPage record);

    int updateBatch(List<RecordPage> list);

    int updateBatchSelective(List<RecordPage> list);

    int batchInsert(@Param("list") Collection<RecordPage> list);

    void deleteComplete();

}