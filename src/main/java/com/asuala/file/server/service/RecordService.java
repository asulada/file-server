package com.asuala.file.server.service;

import com.asuala.file.server.enums.state.RecordEnum;
import com.asuala.file.server.mapper.RecordMapper;
import com.asuala.file.server.utils.CacheUtils;
import com.asuala.file.server.vo.Record;
import com.asuala.file.server.vo.req.UrlReq;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @create: 2024/01/14
 **/
@Service
@Slf4j
public class RecordService extends ServiceImpl<RecordMapper, Record> {

    public void success(Long id) {
        Record record = new Record();
        record.setId(id);
        record.setState(RecordEnum.HANDLED.getCode());
        record.setUpdateTime(new Date());
        baseMapper.updateById(record);

        CacheUtils.removeCacheRecord(record.getId());
    }

    public void pauseRecord(Long id, String fileName, Integer failNum) {
        failNum++;
        log.error("暂停下载: id {}, {}, 失败次数: {}", id, fileName, failNum);
        Record record = new Record();
        record.setId(id);
        record.setFailNum(failNum);
        record.setUpdateTime(new Date());
        if (failNum > 1) {
            record.setState(RecordEnum.FORBID_DOWN.getCode());
        } else {
            record.setState(RecordEnum.PAUSE_DOWN.getCode());
        }
        baseMapper.updateByPrimaryKeySelective(record);
        CacheUtils.removeCacheRecord(record.getId());
//        baseMapper.updateById(record);
    }


    public List<String> findQualityByAuthorAndName(String author, String name) {
        return baseMapper.findQualityByAuthorAndName(author, name);
    }

    public Record getLastSameFile(UrlReq req) {
        return baseMapper.findLastSameFile(req);
    }

    public void updateByPrimaryKeySelective(Record record) {
        baseMapper.updateByPrimaryKeySelective(record);
    }

    public List<Record> pagePageUrl(Page<Record> page, int index, Long lastId) {
        return baseMapper.pagePageUrl(page, index, lastId);
    }

}