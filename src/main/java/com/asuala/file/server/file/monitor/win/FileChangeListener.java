package com.asuala.file.server.file.monitor.win;

import com.asuala.file.server.file.monitor.win.enums.FileChangeEventEnum;
import com.asuala.file.server.service.FileInfoService;
import com.asuala.file.server.utils.FileUtils;
import com.asuala.file.server.vo.FileInfo;
import com.sun.jna.platform.FileMonitor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Date;

/**
 * @description:
 * @create: 2024/02/01
 **/
@Slf4j
@AllArgsConstructor
public class FileChangeListener implements FileMonitor.FileListener {

    private FileInfoService fileInfoService;

    private String dir;

    private Long uId;

    private static FileInfo tmpFile;


    @Override
    public void fileChanged(FileMonitor.FileEvent e) {
        if (!e.getFile().getAbsolutePath().startsWith(dir)) {
            return;
        }
        FileChangeEventEnum eventEnum = FileChangeEventEnum.convert(e.getType());
        File file = e.getFile();
        FileInfo fileInfo = null;
        switch (eventEnum) {
            case FILE_CREATED:
                fileInfoService.insert(file, uId);
                break;
            case FILE_MODIFIED:
                if (file.isFile()) {
                    fileInfo = fileInfoService.findFileInfo(file);
                    if (null == fileInfo) {
                        log.warn("{} 修改文件事件-没有文件", file.getName());
                        fileInfoService.insert(file, uId);
                    } else {
                        fileInfo.setSize(file.length());
                        fileInfo.setCreateTime(new Date(file.lastModified()));
                        fileInfo.setUpdateTime(new Date());
                        fileInfoService.updateById(fileInfo);
                        fileInfoService.saveEs(fileInfo);
                    }
                }
//                    else {
//                        fileInfo = FileInfo.builder().id(fileInfo.getId()).changeTime(new Date(file.lastModified())).updateTime(new Date()).build();
//                    }

                break;
            case FILE_NAME_CHANGED_NEW:
                if (null != tmpFile) {
                    String suffix = FileUtils.getSuffix(file.getName());
//                    if (suffix.length() > 20) {
//                        log.warn("文件后缀名过长: {}", file.getAbsolutePath());
//                        break;
//                    }
                    tmpFile.setSuffix(suffix);
                    tmpFile.setName(file.getName());
                    tmpFile.setPath(file.getAbsolutePath());
                    tmpFile.setChangeTime(new Date(file.lastModified()));
                    tmpFile.setUpdateTime(new Date());
                    fileInfoService.updateById(tmpFile);
                    fileInfoService.saveEs(tmpFile);
                } else {
                    log.warn("{} 新名称事件-没有旧文件", file.getName());
                    fileInfoService.insert(file, uId);
                }
                break;
            case FILE_NAME_CHANGED_OLD:
                fileInfo = fileInfoService.findFileInfo(file);
                if (null != fileInfo) {
                    tmpFile = fileInfo;
                } else {
                    log.warn("{} 旧名称事件-没有找到此文件", file.getName());
                }
                break;
            case FILE_DELETED:
                fileInfo = fileInfoService.findFileInfo(file);
                if (null != fileInfo) {
                    fileInfoService.deleteByPrimaryKey(fileInfo.getId());
                    fileInfoService.delEs(fileInfo);
                }
                break;
            default:
                break;

        }
    }


}