package com.asuala.file.server.mapper;

import com.asuala.file.server.vo.Record;
import com.asuala.file.server.vo.req.UrlReq;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RecordMapper extends BaseMapper<Record> {
    int deleteByPrimaryKey(Long id);

    int insertSelective(Record record);

    Record selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Record record);

    int updateByPrimaryKey(Record record);

    int updateBatch(List<Record> list);

    int batchInsert(@Param("list") List<Record> list);

    void updateDelAndTime(Record record);

    Record findLastSameFile(UrlReq req);

    List<Record> pagePageUrl(Page<Record> page, @Param("index") int index, @Param("lastId") Long lastId);

    List<String> findQualityByAuthorAndName(@Param("author")String author,@Param("name")String name);

    Long findIdByNameAndAuthor(@Param("name")String name,@Param("author")String author);

    List<String> findIdGroupByNameAndAuthor();

    List<Record> findByIdAndState(@Param("id")Long id,@Param("state")Integer state);

	Long countByNameAndAuthorAndIdNot(@Param("name")String name,@Param("author")String author,@Param("notId")Long notId);


}