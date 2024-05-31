package com.asuala.file.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @create: 2024/02/01
 **/
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "watch", name = "open", havingValue = "true")
public class WatchFileService {

//    private final FileInfoService fileInfoService;
//    @Value("${watch.dir}")
//    private Set<String> dirs;



}