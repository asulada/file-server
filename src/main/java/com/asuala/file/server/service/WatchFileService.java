package com.asuala.file.server.service;

import com.asuala.file.server.config.MainConstant;
import com.asuala.file.server.file.monitor.linux.FileNode;
import com.asuala.file.server.file.monitor.linux.InotifyLibraryUtil;
import com.asuala.file.server.file.monitor.win.FileChangeListener;
import com.asuala.file.server.utils.FileIdUtils;
import com.asuala.file.server.vo.Index;
import com.sun.jna.platform.FileMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description:
 * @create: 2024/02/01
 **/
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "watch", name = "open", havingValue = "true")
public class WatchFileService {
    private final FileInfoService fileInfoService;
    private final EsService esService;

    //    private final FileInfoService fileInfoService;
//    @Value("${watch.dir}")
//    private Set<String> dirs;
    public void initFileInfo(Index cpuId, Map<String, FileNode> fileMap) throws Exception {
        if (cpuId.getSystem().contains("LINUX")) {
            InotifyLibraryUtil.init(fileMap);
        } else {
            FileMonitor fileMonitor = FileMonitor.getInstance();
            for (Map.Entry<String, FileNode> entry : fileMap.entrySet()) {
                fileMonitor.addFileListener(new FileChangeListener(fileInfoService, entry.getKey(), entry.getValue().getSId(), esService));
                File file = new File(entry.getKey());
                if (!file.exists()) {
                    file.mkdirs();
                }
                fileMonitor.addWatch(file);

                MainConstant.volumeNos.put(entry.getKey().substring(0, entry.getKey().indexOf(":") + 1), entry.getValue().getSId());
            }

        }
    }


}