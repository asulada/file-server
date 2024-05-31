package com.asuala.file.server.file.monitor.linux;

import com.asuala.file.server.config.MainConstant;
import com.asuala.file.server.utils.CacheUtils;
import com.asuala.file.server.utils.FileUtils;
import com.asuala.file.server.vo.FileInfo;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @description:
 * @create: 2024/05/06
 **/
@Slf4j
public class InotifyLibraryUtil {


    public static ExecutorService fixedThreadPool;

    public static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, String>> fdMap = new ConcurrentHashMap();

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

    public static CopyOnWriteArrayList<FileInfo>[][] rebuild(Map<String, Long> fileMap) throws IOException {
        CopyOnWriteArrayList<FileInfo>[][] dirFileArray = new CopyOnWriteArrayList[fileMap.size()][2];
        int i = 0;
        for (Map.Entry<String, Long> entry : fileMap.entrySet()) {
            dirFileArray[i++] = findDirFile(entry.getKey(), entry.getValue());

        }
        return dirFileArray;
    }

    public static void init(Map<String, Long> fileMap) {
        fixedThreadPool = Executors.newFixedThreadPool(fileMap.size());
        for (Map.Entry<String, Long> entry : fileMap.entrySet()) {
            try {
                List<String> dirPaths = findDir(entry.getKey());
                fixedThreadPool.execute(new Watch(dirPaths, entry.getValue()));
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
        Files.walk(startPath)
                .filter(Files::isDirectory)
                .forEach(item -> dirs.add(item.toString()));
        return dirs;
    }

    private static boolean report = true;

    public static CopyOnWriteArrayList<FileInfo>[] findDirFile(String path, Long uId) throws IOException {
        // 指定要遍历的文件夹路径
        Path folderPath = Paths.get(path);

        CopyOnWriteArrayList<FileInfo> dirs = new CopyOnWriteArrayList();
        CopyOnWriteArrayList<FileInfo> files = new CopyOnWriteArrayList();

        Thread thread = new Thread(() -> {
            while (report) {
                log.info("{} 已统计文件数量 {}", path, dirs.size() + files.size());
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info("{} 遍历结束 已统计文件数量 {}", path, dirs.size() + files.size());
        });
        thread.start();
        // 使用 Files.walk() 方法遍历文件夹
        Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {

            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // 获取文件夹信息
                if (Constant.exclude.contains(dir.getFileName().toString())){
                    return FileVisitResult.SKIP_SUBTREE;
                }
                FileInfo fileInfo = FileInfo.builder().index(MainConstant.index).name(dir.getFileName().toString()).path(dir.toString()).createTime(new Date())
                        .changeTime(new Date(Files.getLastModifiedTime(dir).toMillis())).dir(1).uId(uId).build();
                dirs.add(fileInfo);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // 获取文件修改时间
                Date modifyTime = new Date(Files.getLastModifiedTime(file).toMillis());

                // 获取文件大小
                long fileSize = Files.size(file);

                String fileName = file.getFileName().toString();

                String suffix = FileUtils.getSuffix(fileName);
//                if (suffix.length() > 20) {
//                    log.warn("文件后缀名过长: {}", file.toString());
//                    return FileVisitResult.CONTINUE;
//                }
                FileInfo fileInfo = FileInfo.builder().index(MainConstant.index).name(fileName).path(file.toString()).createTime(new Date())
                        .changeTime(modifyTime).dir(0).size(fileSize).suffix(suffix).uId(uId).build();
                files.add(fileInfo);

                return FileVisitResult.CONTINUE;
            }
        });
        report = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            log.error("{} 等待统计线程结束失败", path);
        }
        return new CopyOnWriteArrayList[]{dirs, files};
    }

    public static void close() {
        for (Map.Entry<Integer, ConcurrentHashMap<Integer, String>> fdEntry : fdMap.entrySet()) {
            Integer fd = fdEntry.getKey();
            Map<Integer, String> wdMap = fdEntry.getValue();
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

    static class Watch implements Runnable {

        private int fd;
        private ConcurrentHashMap<Integer, String> wdMap = new ConcurrentHashMap<>();
        private List<String> paths;
        private static final int size = 4096;
        private static Long sId;

        public Watch(List<String> paths, Long sId) {
            this.paths = paths;
            this.sId = sId;
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
            fdMap.put(fd, wdMap);

            Pointer pointer = new Memory(size);
            try {
                while (CacheUtils.watchFlag) {
                    int bytesRead = InotifyLibrary.INSTANCE.read(fd, pointer, size);

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
                        String name = byteToStr(nameBytes);


                        String path = wdMap.get(wd2);

                        String filePath = path + MainConstant.FILESEPARATOR + name;
                        boolean isDir = nameLen == 0;
                        if ((mask & Constant.IN_ISDIR) != 0) {
                            mask -= Constant.IN_ISDIR;
                            isDir = true;

                            if (mask == Constant.IN_CREATE) {
                                addWatchDir(filePath);
                            }

                        }
                        event = eventNameMap.get(mask);

                        if (mask == Constant.IN_IGNORED) {
                            filePath = filePath.substring(0, filePath.length() - 1);
                            isDir = true;
                            removeWatchDir(wd2);
                        }

                        if (isDir) {
                            log.debug("目录: {} 事件: {} 关联码: {} 目录名: {} ", path, event, cookie, name);

                        } else {
                            log.debug("目录: {} 事件: {} 关联码: {} 文件名: {} ", path, event, cookie, name);

                        }

                        FileVo fileVo = new FileVo();
                        fileVo.setFullPath(filePath);
                        fileVo.setPath(path);
                        fileVo.setName(name);
                        fileVo.setCode(mask);
                        fileVo.setDir(isDir);
                        fileVo.setSId(sId);
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
            wdMap.put(wd, path);
            log.debug("添加监控路径: {}", path);
        }

        private void removeWatchDir(int wd) {
            InotifyLibrary.INSTANCE.inotify_rm_watch(fd, wd);
            log.debug("移除监控路径: {}", wdMap.get(wd));
            wdMap.remove(wd);
        }
    }


}

