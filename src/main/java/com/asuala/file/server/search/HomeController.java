package com.asuala.file.server.search;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import com.alibaba.fastjson2.JSONObject;
import com.asuala.file.server.config.MainConstant;
import com.asuala.file.server.enums.state.RecordEnum;
import com.asuala.file.server.es.Es8Client;
import com.asuala.file.server.es.entity.FileInfoEs;
import com.asuala.file.server.mapper.UserMapper;
import com.asuala.file.server.service.FileInfoService;
import com.asuala.file.server.service.IndexService;
import com.asuala.file.server.service.RecordService;
import com.asuala.file.server.utils.TimeUtils;
import com.asuala.file.server.vo.FileInfo;
import com.asuala.file.server.vo.Index;
import com.asuala.file.server.vo.Record;
import com.asuala.file.server.vo.User;
import com.asuala.file.server.vo.req.SearchReq;
import com.asuala.file.server.vo.req.UrlReq;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @create: 2024/05/25
 **/
@Controller
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "file", name = "server.open", havingValue = "true")
public class HomeController {
    private final FileInfoService fileInfoService;
    private final UserMapper userMapper;
    private final IndexService indexService;

    @Autowired(required = false)
    private Es8Client es8Client;

    @Value("${search.urlOptions}")
    private String urlOptions;

    private static final List<String> fields = new ArrayList<String>() {{
        add("name");
    }};

    @RequestMapping("/")
    public String index() {
        return "index.html";
    }

    @RequestMapping("login")
    @ResponseBody
    public SaResult doLogin(@RequestBody UserReq user) {
        User userVo = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getName, user.getName()));
        // 此处仅作模拟示例，真实项目需要从数据库中查询数据进行比对
        if (!BCrypt.checkpw(user.getPassword(), userVo.getPasswd())) {
            return SaResult.error("账号或密码错误");
        }
        log.info("{} 登录", user.getName());
        StpUtil.login(userVo.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return SaResult.data(tokenInfo);
    }

    @PostMapping("pass")
    @ResponseBody
    public SaResult pass(@RequestBody Map<String, String> map) {
        String pass = map.get("pass");
        String pw_hash = BCrypt.hashpw(pass, BCrypt.gensalt(10));
        log.info("pass {}", pw_hash);
        return SaResult.ok();
    }


    @PostMapping("search")
    @ResponseBody
    public JSONObject search(@RequestBody SearchReq req) throws IOException {
        JSONObject res = new JSONObject();
        res.put("code", 222);

        if (StringUtils.isBlank(req.getKey())) {
            res.put("msg", "关键字为空");
            return res;
        }
        Set<Long> fileIds = MainConstant.userResource.get(StpUtil.getLoginIdAsLong());
        if (CollectionUtils.isEmpty(fileIds)) {
            res.put("msg", "没有权限");
            return res;
        }
        Page<FileInfo> page = new Page<>(req.getPageNum(), req.getPageSize());
        List<FileInfo> list = fileInfoService.list(page, new LambdaQueryWrapper<FileInfo>().in(FileInfo::getUId, fileIds).likeRight(FileInfo::getName, req.getKey()).orderByDesc(FileInfo::getChangeTime));
        if (list.size() > 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", page.getTotal());
            res.put("data", result);
            res.put("code", 200);
            return res;
        }
//        Query query = Query.of(q -> q.wildcard(w -> w.field("name").value(key)));
//        Query query = Query.of(q -> q.matchPhrase(m -> m.query(req.getKey()).field("name").slop(6)));
        Query query = Query.of(q -> q.bool(b -> b.must(mustQuery -> mustQuery.terms(t -> t
                .field("sId")
                .terms(TermsQueryField.of(tf -> tf
                        .value(fileIds.stream().map(item -> FieldValue.of(item)).collect(Collectors.toList()))  // Replace with actual terms
                )))).
//                must(mustQuery -> mustQuery.matchPhrase(m -> m.query(req.getKey()).field("name").slop(6)))));
        must(mustQuery -> mustQuery.term(m -> m.field("name").value(req.getKey())))));
//        Query query = Query.of(q -> q.match(m -> m.query(key).field("name")));
        Map<String, Object> map = es8Client.complexQueryHighlight(query, FileInfoEs.class, fields, req.getPageNum(), req.getPageSize());
        res.put("data", map);
        res.put("code", 200);
        return res;
    }

    @GetMapping("openUrl")
    @ResponseBody
    public JSONObject openUrl() {
        return JSONObject.parseObject(urlOptions);
    }


    @GetMapping("hostNames")
    @ResponseBody
    public SaResult hostNames() {
        return SaResult.data(indexService.list(new LambdaQueryWrapper<Index>().select(Index::getId, Index::getCpuId, Index::getHostName, Index::getSystem)));
    }


    @GetMapping("users")
    @ResponseBody
    public SaResult users() {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().select(User::getId, User::getName));
        return SaResult.data(users);
    }
}