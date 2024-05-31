package com.asuala.file.server.utils;

import com.asuala.file.server.vo.Record;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @description:
 * @create: 2024/01/18
 **/
@Slf4j
public class CacheUtils {

    public static boolean watchFlag = true;


    public static ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();
    //缓存过期时间 单位 s
    public static final int expireTime = 120;

    private static Map<String, LocalDateTime> cache = new HashMap<>();
    private static Map<Long, Record> cacheRecord = new LinkedHashMap<>();

    private static Lock cacheLock = new ReentrantLock();


    private static Lock cacheRecordLock = new ReentrantLock();

    // 创建一个初始值为 null 的 AtomicReference 对象
    public static AtomicBoolean transcodeAtomic = new AtomicBoolean(false);

    public static void setCacheRecord(List<Record> list) {
        cacheRecordLock.lock();
        try {
            cacheRecord.clear();
            for (Record record : list) {
                cacheRecord.put(record.getId(), record);
            }
        } finally {
            cacheRecordLock.unlock();
        }
    }

    public static Map<Long, Record> getCacheRecord() {
        cacheRecordLock.lock();
        try {
            return cacheRecord;
        } finally {
            cacheRecordLock.unlock();
        }
    }

    public static void clearCacheRecord() {
        cacheRecordLock.lock();
        try {
            cacheRecord.clear();
        } finally {
            cacheRecordLock.unlock();
        }
    }

    public static void removeCacheRecord(Long key) {
        cacheRecordLock.lock();
        try {
            cacheRecord.remove(key);
        } finally {
            cacheRecordLock.unlock();
        }
    }

    public static void addCacheRecord(List<Record> list) {
        cacheRecordLock.lock();
        try {
            for (Record record : list) {
                cacheRecord.put(record.getId(), record);
            }
        } finally {
            cacheRecordLock.unlock();
        }
    }

    public static Long getLastCacheRecordKey() {
        cacheRecordLock.lock();
        Long key = 0L;
        try {
            try {
                if (cacheRecord.size() > 0) {
                    key = getTailByReflection((LinkedHashMap<Long, Record>) cacheRecord).getKey();
                }
            } catch (Exception e) {
                log.error("获取最后一个元素失败!", e);
            }
            return key;
        } finally {
            cacheRecordLock.unlock();
        }
    }

    public static <K, V> Map.Entry<K, V> getTailByReflection(LinkedHashMap<K, V> map)
            throws NoSuchFieldException, IllegalAccessException {
        Field tail = map.getClass().getDeclaredField("tail");
        tail.setAccessible(true);
        return (Map.Entry<K, V>) tail.get(map);
    }

    public static Map<String, LocalDateTime> cache(String key, LocalDateTime time) {
        cacheLock.lock();
        try {
            if (null != time) {
                if (StringUtils.isNotBlank(key)) {
                    cache.put(key, time);
                } else {
                    LocalDateTime oldTime = time.minusSeconds(expireTime);
                    Iterator<Map.Entry<String, LocalDateTime>> iterator = cache.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, LocalDateTime> entry = iterator.next();
                        if (oldTime.isAfter(entry.getValue())) {
                            iterator.remove();
                        }
                    }
                }
            } else if (StringUtils.isNotBlank(key)) {
                cache.remove(key);
            }
            return cache;
        } finally {
            cacheLock.unlock();
        }
    }

//    public static void setCache(Map<String, LocalDateTime> cache) {
//        CacheUtils.cache = cache;
//    }


    public static int x = 0;

    public static int pauseSecond = 60;
    private static LocalDateTime pauseTime;
    private static Lock pauseLock = new ReentrantLock();

    public static boolean downFlag(LocalDateTime time) {
        pauseLock.lock();
        try {
            if (null == time) {
                if (null == pauseTime) {
                    return true;
                }
                boolean flag = LocalDateTime.now().minusSeconds(pauseSecond * x).isAfter(pauseTime);
                return flag;
            }
            if (null != time) {
                x++;
                log.debug("暂停因数 x = {}", CacheUtils.x);
                pauseTime = time;
            }
            return false;
        } finally {
            pauseLock.unlock();
        }
    }


    private static long lastId = -1L;
    private static Lock lock = new ReentrantLock();

    public static long setLastId(Long id) {
        lock.lock();
        try {
            if (null == id) {
                return lastId;
            } else if (lastId == id) {
                lastId = -1;
            } else if (lastId < id) {
                lastId = id;
            }
            // 原子操作
            return lastId;
        } finally {
            lock.unlock();
        }
    }
}