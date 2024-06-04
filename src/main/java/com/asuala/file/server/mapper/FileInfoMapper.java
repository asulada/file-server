package com.asuala.file.server.mapper;

import com.asuala.file.server.vo.FileInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface FileInfoMapper extends BaseMapper<FileInfo> {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FileInfo record);

    FileInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FileInfo record);

    int updateByPrimaryKey(FileInfo record);

    int batchInsert(@Param("list") List<FileInfo> list);

    List<Map<String, String>> findNameByIndex(@Param("index") Integer index);

    long deleteLimit(@Param("index") int index);

    void dropIndex(@Param("index") int index);

    Long selectDidByPath(@Param("path")String path);


}