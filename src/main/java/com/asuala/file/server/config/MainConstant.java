package com.asuala.file.server.config;

import com.asuala.file.server.vo.Index;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @description:
 * @create: 2024/05/21
 **/
public class MainConstant {
    public static Index systemInfo;
    public static Map<Long, Set<Long>> userResource = new HashMap<>();
    public static Integer index;
    public static final String FILESEPARATOR = System.getProperty("file.separator");
    public static Map<String, Long> volumeNos = new HashMap<>();

}