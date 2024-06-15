package com.asuala.file.server.file.monitor.linux;

import cn.hutool.core.collection.CollectionUtil;
import com.asuala.file.server.config.MainConstant;
import com.asuala.file.server.service.FileInfoService;
import com.asuala.file.server.utils.CacheUtils;
import com.asuala.file.server.utils.FileUtils;
import com.asuala.file.server.vo.FileInfo;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * @description:
 * @create: 2024/05/06
 **/
@Slf4j
public class InotifyLibraryUtil {

    public static ExecutorService fixedThreadPool;

    public static Map<Integer, Watch> fdMap = new HashMap<>();

    public static void removeWd(Integer fd, List<String> childPaths) {
        Watch watch = fdMap.get(fd);
        for (String path : childPaths) {
            Integer key = watch.getKey(path);
//            fdMap.get(fd).getPathIdMap().remove(path);
            if (null != key) {
                watch.removeWatchDir(key, path);
            }
        }
    }

    public static void removeWd(Integer fd, String path) {
        Watch watch = fdMap.get(fd);
        Integer key = watch.getKey(path);
//            fdMap.get(fd).getPathIdMap().remove(path);
        if (null != key) {
            watch.removeWatchDir(key, path);
        }
    }

    public static void removePathId(Integer fd, String path) {
        Watch watch = fdMap.get(fd);
        watch.getPathIdMap().remove(path);
    }

    public interface InotifyLibrary extends Library {
        InotifyLibrary INSTANCE = (InotifyLibrary) Native.load("c", InotifyLibrary.class);

        int inotify_init();

        int inotify_add_watch(int fd, String path, int mask);

        int inotify_rm_watch(int fd, int wd);

        int read(int fd, Pointer buf, int size);

        int close(int fd);

    }

    public static void main(String[] args) {
        test001();
    }
        /*
    IN_ACCESS：文件被访问
IN_MODIFY：文件被修改
IN_ATTRIB，文件属性被修改
IN_CLOSE_WRITE，以可写方式打开的文件被关闭
IN_CLOSE_NOWRITE，以不可写方式打开的文件被关闭
IN_OPEN，文件被打开
IN_MOVED_FROM，文件被移出监控的目录
IN_MOVED_TO，文件被移入监控着的目录
IN_CREATE，在监控的目录中新建文件或子目录
IN_DELETE，文件或目录被删除
IN_DELETE_SELF，自删除，即一个可执行文件在执行时删除自己
IN_MOVE_SELF，自移动，即一个可执行文件在执行时移动自己
     */

    private static Map<Integer, String> eventNameMap = new HashMap<Integer, String>() {{
        put(Constant.IN_MOVED_FROM, "文件被移出监控的目录");
        put(Constant.IN_MOVED_TO, "文件被移入监控着的目录");
        put(Constant.IN_CREATE, "在监控的目录中新建文件或子目录");
        put(Constant.IN_DELETE, "文件或目录被删除");
        put(Constant.IN_DELETE_SELF, "自删除，即一个可执行文件在执行时删除自己");
        put(Constant.IN_MOVE_SELF, "自移动，即一个可执行文件在执行时移动自己");
        put(Constant.IN_MODIFY, "文件被修改");
        put(Constant.IN_UNMOUNT, "已卸载备份fs");
        put(Constant.IN_Q_OVERFLOW, "排队的事件溢出");
        put(Constant.IN_IGNORED, "文件被忽略");
    }};

    public static void test001() {
        int fd = InotifyLibrary.INSTANCE.inotify_init();
        String pathname = "/mnt/nfts1/test/test1";
        String pathname1 = "/mnt/nfts1/test/test2";
        int wd = InotifyLibrary.INSTANCE.inotify_add_watch(fd, pathname,
                Constant.IN_MOVED_FROM | Constant.IN_MOVED_TO | Constant.IN_CREATE | Constant.IN_DELETE | Constant.IN_MODIFY | Constant.IN_DELETE_SELF | Constant.IN_MOVE_SELF);
        int wd1 = InotifyLibrary.INSTANCE.inotify_add_watch(fd, pathname1,
                Constant.IN_MOVED_FROM | Constant.IN_MOVED_TO | Constant.IN_CREATE | Constant.IN_DELETE | Constant.IN_MODIFY | Constant.IN_DELETE_SELF | Constant.IN_MOVE_SELF);

        int size = 4096;
        Pointer pointer = new Memory(size);
        try {
//            while (true) {
            for (int k = 0; k < 10; k++) {

                int bytesRead = InotifyLibrary.INSTANCE.read(fd, pointer, size);
                for (int i = 0; i < bytesRead; ) {
                    int wd2 = pointer.getInt(i);
                    i += 4;
                    int mask = pointer.getInt(i);
                    i += 4;
                    int cookie = pointer.getInt(i);
                    i += 4;
                    int nameLen = pointer.getInt(i);
                    i += 4;
                    byte[] nameBytes = new byte[nameLen];
                    pointer.read(i, nameBytes, 0, nameBytes.length);
                    i += nameLen;
                    String name = byteToStr(nameBytes);

                    boolean isDir = false;
                    if ((mask & Constant.IN_ISDIR) != 0) {
                        isDir = true;
                        mask -= Constant.IN_ISDIR;
                    }

                    String action = "";
                    if (eventNameMap.containsKey(mask)) {
                        action = eventNameMap.get(mask);
                    }


                    System.out.println("wd=" + wd2 + " mask=" + mask + " cookie=" + cookie + (isDir ? "文件夹 " : "文件") + " name=" + name.toString() + " " + action);
                }
            }
        } finally {
            InotifyLibrary.INSTANCE.inotify_rm_watch(fd, wd);
            InotifyLibrary.INSTANCE.inotify_rm_watch(fd, wd1);
            InotifyLibrary.INSTANCE.close(fd);

        }
    }

    /**
     * 去掉byte[]中填充的0 转为String
     *
     * @param buffer
     * @return
     */
    public static String byteToStr(byte[] buffer) {
        try {
            int length = 0;
            for (int i = 0; i < buffer.length; ++i) {
                if (buffer[i] == 0) {
                    length = i;
                    break;
                }
            }
            return new String(buffer, 0, length, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    public static Map<Integer, List<FileInfo>> rebuild(Map<String, FileNode> fileMap, int findThreadNum) throws InterruptedException {
        Map<Integer, List<FileInfo>> map = new ConcurrentHashMap<>();
//        CopyOnWriteArrayList<FileInfo> dirFileArray = new CopyOnWriteArrayList();

        ExecutorService pool;
        if (findThreadNum < 1) {
            pool = Executors.newFixedThreadPool(fileMap.size());
        } else {
            pool = Executors.newFixedThreadPool(findThreadNum);
        }
        List<Callable<String>> tasks = new ArrayList<>();

        for (Map.Entry<String, FileNode> entry : fileMap.entrySet()) {
            tasks.add(() -> {
                try {
                    map.put(entry.getValue().getFd(), findDirFile(entry.getKey(), entry.getValue().getSId()));
                } catch (IOException e) {
                    log.error("重建遍历错误 {}", entry.getKey(), e);
                }
                return null;
            });
        }
        pool.invokeAll(tasks, 10, TimeUnit.MINUTES);
        pool.shutdownNow();
        return map;
    }

    public static void init(Map<String, FileNode> fileMap) {
        fixedThreadPool = Executors.newFixedThreadPool(fileMap.size());
        for (Map.Entry<String, FileNode> entry : fileMap.entrySet()) {
            try {
                List<String> dirPaths = findDir(entry.getKey());
                Watch watch = new Watch(dirPaths, entry.getValue().getSId());
                fixedThreadPool.execute(watch);
                while (watch.getFd() == null) {
                    log.info("等待获取fd {}", entry.getKey());
                    Thread.sleep(1000L);
                }
                fdMap.put(watch.getFd(), watch);
                entry.getValue().setFd(watch.getFd());
                log.info("{} 监控目录数量 {}", entry.getKey(), dirPaths.size());

            } catch (Exception e) {
                log.error("{} 添加监控目录失败", entry.getKey(), e);
            }
        }
    }

    public static List<String> findDir(String path) throws IOException {
        if ("/".equals(path)) {
            return null;
        }
        Path startPath = Paths.get(path);

        List<String> dirs
                = new ArrayList<>();

        // 使用 Files.walk() 方法遍历文件夹
        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {

            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                // 获取文件夹信息
                if (Constant.exclude.contains(dir.getFileName().toString())) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                dirs.add(dir.toString());
                return FileVisitResult.CONTINUE;
            }

        });
        return dirs;
    }

    public static List<FileInfo> findDirFile(String path, Long uId, Integer fd, Long pId) throws IOException {
        // 指定要遍历的文件夹路径
        Path folderPath = Paths.get(path);

        List<FileInfo> files = new ArrayList();

        Map<String, Long> map = new HashMap<>();

        // 使用 Files.walk() 方法遍历文件夹
        Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {

            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String dirName = dir.getFileName().toString();
                // 获取文件夹信息
                if (Constant.exclude.contains(dirName)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                String curPath = dir.toString();

                fdMap.get(fd).addWatchDir(curPath);

                long dId = MainConstant.snowflake.nextId();

                FileInfo fileInfo = FileInfo.builder().index(MainConstant.index).name(dirName).path(curPath).createTime(new Date())
                        .changeTime(new Date(attrs.lastModifiedTime().toMillis())).dir(1).uId(uId).dId(dId).pId(map.get(dir.getParent().toString())).build();
                files.add(fileInfo);
                map.put(curPath, dId);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                // 获取文件修改时间
                // 获取文件大小
                String fileName = file.getFileName().toString();

                String suffix = FileUtils.getSuffix(fileName);
//                if (suffix.length() > 20) {
//                    log.warn("文件后缀名过长: {}", file.toString());
//                    return FileVisitResult.CONTINUE;
//                }
                Long pId = map.get(file.getParent().toString());

                FileInfo fileInfo = FileInfo.builder().index(MainConstant.index).name(fileName).path(file.toString()).createTime(new Date())
                        .changeTime(new Date(attrs.lastModifiedTime().toMillis())).dir(0).size(attrs.size()).suffix(suffix).uId(uId).dId(0L).pId(pId).build();
                files.add(fileInfo);

                return FileVisitResult.CONTINUE;
            }
        });
        if (files.get(0).getPId() == null) {
            files.get(0).setPId(pId);
        } else {
            log.error("{} 根路径pId {}", path, files.get(0).getPId());
        }
        log.info("{} 遍历文件数量 {}", path, files.size());
        return files;
    }


    public static List<FileInfo> findDirFile(String path, Long uId) throws IOException {
        // 指定要遍历的文件夹路径
        Path folderPath = Paths.get(path);

//        List<FileInfo> dirs = new ArrayList<>();
        List<FileInfo> files = new ArrayList();

        Map<String, Long> map = new HashMap<>();

        final AtomicBoolean report = new AtomicBoolean();
        Thread thread = new Thread(() -> {
            while (report.get()) {
                log.info("{} 已统计文件数量 {}", path, files.size());
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info("{} 遍历结束 已统计文件数量 {}", path, files.size());
        });
        thread.start();
        // 使用 Files.walk() 方法遍历文件夹
        Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {

            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                // 获取文件夹信息
                if (Constant.exclude.contains(dir.getFileName().toString())) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                long dId = MainConstant.snowflake.nextId();
                String path = dir.toString();
                FileInfo fileInfo = FileInfo.builder().index(MainConstant.index).name(dir.getFileName().toString()).path(path).createTime(new Date())
                        .changeTime(new Date(attrs.lastModifiedTime().toMillis())).dir(1).uId(uId).dId(dId).pId(map.get(dir.getParent().toString())).build();
                files.add(fileInfo);
                map.put(path, dId);
//                log.info("目录 {} id {}", path, dId);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                // 获取文件修改时间
                // 获取文件大小
                String fileName = file.getFileName().toString();

                String suffix = FileUtils.getSuffix(fileName);
//
                Long pId = map.get(file.getParent().toString());
//                log.info("父级目录 {} id {}", file.getParent(), pId);

                FileInfo fileInfo = FileInfo.builder().index(MainConstant.index).name(fileName).path(file.toString()).createTime(new Date())
                        .changeTime(new Date(attrs.lastModifiedTime().toMillis())).dir(0).size(attrs.size()).suffix(suffix).uId(uId).dId(0L).pId(pId).build();
                files.add(fileInfo);

                return FileVisitResult.CONTINUE;
            }
        });
        report.set(false);
        try {
            thread.join();
        } catch (InterruptedException e) {
            log.error("{} 等待统计线程结束失败", path);
        }
        if (files.get(0).getPId() == null) {
            files.get(0).setPId(0L);
        } else {
            log.error("{} 根路径pId {}", path, files.get(0).getPId());
        }
        log.info("{} 遍历文件数量 {}", path, files.size());
//        dirFileArray.addAll(dirs);
        return files;
    }

    public static void close() {
        for (Map.Entry<Integer, Watch> fdEntry : fdMap.entrySet()) {

            Integer fd = fdEntry.getKey();
            BidiMap<Integer, String> wdMap = fdEntry.getValue().getBidiMap();
            for (Map.Entry<Integer, String> entry : wdMap.entrySet()) {
                try {
                    InotifyLibrary.INSTANCE.inotify_rm_watch(fd, entry.getKey());
                    log.debug("释放inotify watch成功! 路径 {}", entry.getValue());
                } catch (Exception e) {
                    log.error("释放inotify watch失败!!! 路径 {}", entry.getValue(), e);
                }
            }
            InotifyLibrary.INSTANCE.close(fd);
            log.info("close 释放inotify fd {} 结束", fd);
        }
    }

    public static class Watch implements Runnable {

        private Integer fd;
        private List<String> paths;
        private static final int size = 4096;
        private static Long sId;
        private BidiMap<Integer, String> bidiMap = new DualHashBidiMap<>();
        private Map<String, FileMemory> pathIdMap = new ConcurrentHashMap<>(512);
        private ReadWriteLock lock = new ReentrantReadWriteLock();

        public Map<String, FileMemory> getPathIdMap() {
            return pathIdMap;
        }

        public BidiMap<Integer, String> getBidiMap() {
            return bidiMap;
        }

        public Integer getKey(String value) {
            lock.readLock().lock();
            try {
                return bidiMap.getKey(value);
                // other read operations
            } finally {
                lock.readLock().unlock();
            }
        }

        public String getValue(Integer key) {
            lock.readLock().lock();
            try {
                return bidiMap.get(key);
                // other read operations
            } finally {
                lock.readLock().unlock();
            }
        }

        public void setBidi(Integer key, String value) {

            lock.writeLock().lock();
            try {
                bidiMap.put(key, value);
                // other write operations
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void removeBidi(Integer key) {
            lock.writeLock().lock();
            try {
                bidiMap.remove(key);
                // other write operations
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void removeBidi(String value) {
            lock.writeLock().lock();
            try {
                bidiMap.removeValue(value);
                // other write operations
            } finally {
                lock.writeLock().unlock();
            }
        }

        public Watch(List<String> paths, Long sId) {
            this.paths = paths;
            this.sId = sId;
        }

        public Integer getFd() {
            return fd;
        }

        @Override
        public void run() {
            if (null == paths) {
                log.warn("监控目录为空");
                return;
            }
            fd = InotifyLibrary.INSTANCE.inotify_init();

            for (String path : paths) {
                addWatchDir(path);
            }
            paths.clear();
            Pointer pointer = new Memory(size);
            try {
                StringBuilder strb = new StringBuilder();
                while (CacheUtils.watchFlag) {
                    int bytesRead = InotifyLibrary.INSTANCE.read(fd, pointer, size);

                    if (!CacheUtils.watchFlag) {
                        break;
                    }
                    for (int i = 0; i < bytesRead; ) {
                        String event = "";
                        int wd2 = pointer.getInt(i);
                        i += 4;
                        int mask = pointer.getInt(i);

                        i += 4;
                        int cookie = pointer.getInt(i);
                        i += 4;
                        int nameLen = pointer.getInt(i);
                        i += 4;
                        byte[] nameBytes = new byte[nameLen];
                        pointer.read(i, nameBytes, 0, nameBytes.length);
                        i += nameLen;
                        if (mask == Constant.IN_IGNORED) {
                            continue;
                        }
                        String name = byteToStr(nameBytes);

                        String path = getValue(wd2);
                        strb.append(path);
                        if (StringUtils.isNotBlank(name)) {
                            strb.append(MainConstant.FILESEPARATOR).append(name);
                        }
                        String filePath = strb.toString();
                        strb.setLength(0);
                        boolean isDir = false;
                        if ((mask & Constant.IN_ISDIR) != 0) {
                            mask -= Constant.IN_ISDIR;
                            isDir = true;

                            if (mask == Constant.IN_CREATE) {
                                if (Constant.exclude.contains(name)) {
                                    log.debug("忽略文件夹 {}", filePath);
                                    continue;
                                } else {
                                    addWatchDir(filePath);
                                }
                            } else if (mask == Constant.IN_DELETE) {
                                continue;
                            }

                        }
                        event = eventNameMap.get(mask);

//                        if (mask == Constant.IN_IGNORED) {
//                            filePath = filePath.substring(0, filePath.length() - 1);
//                            isDir = true;
//                            removeWatchDir(wd2, path);
//                        }

                        if (isDir) {
                            log.debug("目录: {} 事件: {} 关联码: {} 目录名: {} ", path, event, cookie, name);

                        } else {
                            log.debug("目录: {} 事件: {} 关联码: {} 文件名: {} ", path, event, cookie, name);

                        }

                        FileVo fileVo = new FileVo();
                        fileVo.setFullPath(filePath);
                        fileVo.setParentPath(path);
                        fileVo.setName(name);
                        fileVo.setCode(mask);
                        fileVo.setDir(isDir);
                        fileVo.setSId(sId);

                        fileVo.setFd(fd);
                        CacheUtils.queue.offer(fileVo);

                    }
                }
            } catch (Exception e) {
                log.error("monitor error ", e);
            }
        }

        private void addWatchDir(String path) {
            int wd = InotifyLibrary.INSTANCE.inotify_add_watch(fd, path,
                    Constant.IN_MOVED_FROM | Constant.IN_MOVED_TO | Constant.IN_CREATE | Constant.IN_DELETE | Constant.IN_DELETE_SELF);
            setBidi(wd, path);
            log.info("添加监控路径: {}", path);
        }

        private void removeWatchDir(int wd, String path) {
            InotifyLibrary.INSTANCE.inotify_rm_watch(fd, wd);
            log.info("移除监控路径: {}", path);
            removeBidi(wd);
        }
    }


}

