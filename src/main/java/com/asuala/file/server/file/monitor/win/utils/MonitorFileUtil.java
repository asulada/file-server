package com.asuala.file.server.file.monitor.win.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.asuala.file.server.config.MainConstant;
import com.asuala.file.server.file.monitor.win.entity.*;
import com.asuala.file.server.file.monitor.win.vo.FileTreeNode;
import com.asuala.file.server.service.FileInfoService;
import com.asuala.file.server.utils.FileUtils;
import com.asuala.file.server.utils.ThreadPoolExecutorUtils;
import com.asuala.file.server.vo.FileInfo;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @create: 2024/02/01
 **/
@Slf4j
public class MonitorFileUtil {

    private static final int FSCTL_CREATE_USN_JOURNAL = WinioctlUtil.CTL_CODE(Winioctl.FILE_DEVICE_FILE_SYSTEM, 57, Winioctl.METHOD_NEITHER, Winioctl.FILE_ANY_ACCESS);
    private static final int FSCTL_QUERY_USN_JOURNAL = WinioctlUtil.CTL_CODE(Winioctl.FILE_DEVICE_FILE_SYSTEM, 61, Winioctl.METHOD_BUFFERED, Winioctl.FILE_ANY_ACCESS);
    private static final int FSCTL_ENUM_USN_DATA = WinioctlUtil.CTL_CODE(Winioctl.FILE_DEVICE_FILE_SYSTEM, 44, Winioctl.METHOD_NEITHER, Winioctl.FILE_ANY_ACCESS);
    private static final int FSCTL_DELETE_USN_JOURNAL = WinioctlUtil.CTL_CODE(Winioctl.FILE_DEVICE_FILE_SYSTEM, 62, Winioctl.METHOD_BUFFERED, Winioctl.FILE_ANY_ACCESS);
    private static final int USN_DELETE_FLAG_DELETE = 0x00000001;
    private static final int USN_DELETE_FLAG_NOTIFY = 0x00000002;
    private static final int outputBufferSize = 1024 * 16;
    public static FileInfoService fileInfoServicel;

    public static List<FileInfo> getFileInfo(TreeMap<Long, FileTreeNode> map, Map.Entry<String, Long> entryVolume) {
        long startMillis = System.currentTimeMillis();

        Date date = new Date();
        List<String> paths = new ArrayList<>();
//        volumeNo += Constant.FILESEPARATOR;
        for (Map.Entry<Long, FileTreeNode> entry : map.descendingMap().entrySet()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(MainConstant.FILESEPARATOR).append(entry.getValue().getFileName());
            findPath(map, entry.getValue(), stringBuilder);

            stringBuilder.insert(0, entryVolume.getKey());
            paths.add(stringBuilder.toString());
        }
        List<List<String>> split = CollectionUtil.split(paths, 5000);
        Map<String, Long> dirMap = new HashMap<>();
        for (List<String> strings : split) {
            ThreadPoolExecutorUtils.getThreadPoolExecutorInstance().execute(() -> {
                List<FileInfo> list = new ArrayList<>();

                for (String filePath : strings) {
                    File file = new File(filePath);
                    FileInfo.FileInfoBuilder builder = FileInfo.builder().index(MainConstant.index).name(file.getName()).path(filePath).createTime(date).changeTime(new Date(file.lastModified())).uId(entryVolume.getValue());
                    if (file.isFile()) {
                        String suffix = FileUtils.getSuffix(file.getName());

                        Long pId = dirMap.get(file.getParent());
                        if (null == pId) {
                            pId = MainConstant.snowflake.nextId();
                            dirMap.put(file.getParent(), pId);
                        }
                        builder.size(file.length()).suffix(suffix).dir(0).dId(0L).pId(pId);
                    } else {
                        Long dId = dirMap.get(filePath);
                        if (null == dId) {
                            dId = MainConstant.snowflake.nextId();
                            dirMap.put(filePath, dId);
                        }
                        builder.dir(1).dId(dId).pId(0L);
                    }
                    list.add(builder.build());
                }
                fileInfoServicel.batchInsert(list);
            });
        }
        ThreadPoolExecutor executor = ThreadPoolExecutorUtils.getThreadPoolExecutorInstance();
        executor.shutdown();
        try {
            while (!executor.awaitTermination(1, TimeUnit.SECONDS)) ;
        } catch (InterruptedException e) {
            log.error("{} 线程池失效", entryVolume.getKey(), e);
        }
        long endMillis = System.currentTimeMillis();
        log.info("{} 同步数据库耗时: {}s", entryVolume.getKey(), (endMillis - startMillis) / 1000);
        return null;
    }

    private static void findPath(TreeMap<Long, FileTreeNode> map, FileTreeNode entry, StringBuilder stringBuilder) {
        FileTreeNode parentNode = map.get(entry.getParentFileReferenceNumber());
        if (null != parentNode) {
            stringBuilder.insert(0, parentNode.getFileName()).insert(0, MainConstant.FILESEPARATOR);
            findPath(map, parentNode, stringBuilder);
        }
    }

    /**
     * @param volumeNo
     * @Description TODO-asuala 索引卷上所有文件并返回
     * @Return {@link TreeMap< Long, FileTreeNode>}
     * @Date 2024-02-01
     **/
    public static TreeMap<Long, FileTreeNode> buildFileInfo(String volumeNo) throws Exception {
        long startMillis = System.currentTimeMillis();
        Kernel32 kernel32 = Kernel32.INSTANCE;
        //"\\\\.\\E:"
        WinNT.HANDLE hVolume = kernel32.CreateFile("\\\\.\\" + volumeNo, WinNT.GENERIC_READ | WinNT.GENERIC_WRITE, WinNT.FILE_SHARE_READ | WinNT.FILE_SHARE_WRITE, null, WinNT.OPEN_EXISTING, WinNT.FILE_ATTRIBUTE_NORMAL, null);
        if (WinNT.INVALID_HANDLE_VALUE.equals(hVolume)) {
            log.error("{} 打开卷失败! code {}", volumeNo, kernel32.GetLastError());
            throw new Exception();
        }
        CREATE_USN_JOURNAL_DATA createUsnJournalData = new CREATE_USN_JOURNAL_DATA();
        IntByReference resultInt = new IntByReference();
        if (!kernel32.DeviceIoControl(hVolume, FSCTL_CREATE_USN_JOURNAL, createUsnJournalData.getPointer(), createUsnJournalData.size(), null, 0, resultInt, null)) {
            log.error("{} 创建usn日志失败! 错误码 {}", volumeNo, kernel32.GetLastError());
            throw new Exception();
        }

        USN_JOURNAL_DATA_V0 usnJournalData = new USN_JOURNAL_DATA_V0();
        long nextUsn = 0;
        int count = 0;
        while (true) {
            if (!kernel32.DeviceIoControl(hVolume, FSCTL_QUERY_USN_JOURNAL, null, 0, usnJournalData.getPointer(), usnJournalData.size(), resultInt, null)) {
                log.error("{} 读取卷失败! 错误码 {}", volumeNo, kernel32.GetLastError());
                throw new Exception();
            }
            usnJournalData.read();
            if (nextUsn == 0) {
                count++;
                if (count > 20) {
                    //TODO-asuala 2024-02-03: 删除usn后要对卷有操作才能触发nextUsn
                    break;
                }
                nextUsn = usnJournalData.NextUsn;
            } else if (nextUsn == usnJournalData.NextUsn) {
                break;
            } else {
                nextUsn = usnJournalData.NextUsn;
                log.debug("nextusn 是 {}", nextUsn);
            }
            Thread.sleep(300);
        }

        log.debug("UsnJournalID: {}", usnJournalData.UsnJournalID);
        log.debug("FirstUsn: {}", usnJournalData.FirstUsn);
        log.debug("NextUsn: {}", usnJournalData.NextUsn);
        log.debug("LowestValidUsn: {}", usnJournalData.LowestValidUsn);
        log.debug("MaxUsn: {}", usnJournalData.MaxUsn);
        log.debug("MaximumSize: {}", usnJournalData.MaximumSize);
        log.debug("AllocationDelta: {}", usnJournalData.AllocationDelta);


        // 构建输入缓冲区
        MTF_ENUM_DATA mtf_enum_data = new MTF_ENUM_DATA();
        mtf_enum_data.LowUsn = usnJournalData.FirstUsn;
        mtf_enum_data.HighUsn = usnJournalData.NextUsn;
        mtf_enum_data.write();
        Pointer buffer = new Memory(outputBufferSize);
        // 调用DeviceIoControl函数
        TreeMap<Long, FileTreeNode> map = new TreeMap<>();

        while (kernel32.DeviceIoControl(hVolume, FSCTL_ENUM_USN_DATA, mtf_enum_data.getPointer(),
                mtf_enum_data.size(), buffer, outputBufferSize, resultInt, null)) {

            int dwRetBytes = resultInt.getValue() - WinDef.LONGLONG.SIZE;
            long[] start = new long[1];
            buffer.read(0, start, 0, 1);
            // 找到第一个 USN 记录
            USN_RECORD usnRecord = new USN_RECORD(buffer.share(WinDef.LONGLONG.SIZE));
            while (dwRetBytes > 0) {
                try {
                    usnRecord.read();
                    char[] name = new char[usnRecord.FileNameLength.intValue() / 2];
                    usnRecord.getPointer().read(usnRecord.FileNameOffset.intValue(), name, 0, usnRecord.FileNameLength.intValue() / 2);
                    map.put(usnRecord.FileReferenceNumber.longValue(), FileTreeNode.builder().FileName(String.valueOf(name)).FileReferenceNumber(usnRecord.FileReferenceNumber.longValue()).ParentFileReferenceNumber(usnRecord.ParentFileReferenceNumber.longValue()).build());
                    dwRetBytes -= usnRecord.RecordLength.intValue();
                    usnRecord = new USN_RECORD(usnRecord.getPointer().share(usnRecord.RecordLength.intValue()));
                } catch (Exception e) {
                    log.error("", e);
                }
            }
            mtf_enum_data.StartFileReferenceNumber = start[0];
            mtf_enum_data.write();
        }
        log.debug("{} 读取USN记录错误码: {}", volumeNo, kernel32.GetLastError());

        DELETE_USN_JOURNAL_DATA deleteUsnJournalData = new DELETE_USN_JOURNAL_DATA();
        deleteUsnJournalData.UsnJournalID = usnJournalData.UsnJournalID;
        deleteUsnJournalData.DeleteFlags = USN_DELETE_FLAG_DELETE | USN_DELETE_FLAG_NOTIFY;
        deleteUsnJournalData.write();
        if (!kernel32.DeviceIoControl(hVolume, FSCTL_DELETE_USN_JOURNAL, deleteUsnJournalData.getPointer(), deleteUsnJournalData.size(), null, 0, resultInt, null)) {
            log.error("{} 删除卷日志失败! {}", volumeNo, kernel32.GetLastError());
            throw new Exception();
        }
        kernel32.CloseHandle(hVolume);
        long endMillis = System.currentTimeMillis();
        log.info("读取 {} usn日志时间: {}s", volumeNo, (endMillis - startMillis) / 1000);
        return map;
    }


}