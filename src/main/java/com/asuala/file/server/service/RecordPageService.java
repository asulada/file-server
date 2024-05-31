package com.asuala.file.server.service;

import com.asuala.file.server.mapper.RecordPageMapper;
import com.asuala.file.server.vo.RecordPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RecordPageService extends ServiceImpl<RecordPageMapper, RecordPage> {

    private Lock lock=new ReentrantLock();

    @Transactional
    public void savePages(Collection<RecordPage> values, Long id) {
        baseMapper.delete(new LambdaQueryWrapper<RecordPage>().eq(RecordPage::getPId,id));
        baseMapper.batchInsert(values);
    }

    public void deleteComplete() {
        baseMapper.deleteComplete();
    }
    public void saveValues(Collection<RecordPage> list){
        lock.lock();
        try {
            baseMapper.batchInsert(list);

        }finally {
            lock.unlock();
        }
    }

}
