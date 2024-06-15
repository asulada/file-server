package com.asuala.file.server.search;

import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.collection.CollectionUtil;
import com.asuala.file.server.config.MainConstant;
import com.asuala.file.server.mapper.UserMapper;
import com.asuala.file.server.service.UPathService;
import com.asuala.file.server.vo.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

/**
 * @description:
 * @create: 2024/06/15
 **/
@RestController
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "watch", name = "open", havingValue = "true")
@RequestMapping("watch")
public class WatchController {

    private final UPathService uPathService;
    private final UserMapper userMapper;

    @PostMapping("addWatch")
    public SaResult addWatch(@RequestBody UPathReq req) throws Exception {
        if (StringUtils.isBlank(req.getPath()) || CollectionUtil.isEmpty(req.getUId()) || null == req.getIndex()) {
            return SaResult.error("参数缺失");
        }
        if (MainConstant.index.intValue() != req.getIndex().intValue()) {
            return SaResult.error("主机错误");
        }
        File file = new File(req.getPath());
        if (!file.exists() || file.isFile()) {
            return SaResult.error("文件夹不存在");
        }
        return uPathService.addWatch(req);
    }

}