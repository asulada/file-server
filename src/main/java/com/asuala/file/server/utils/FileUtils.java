package com.asuala.file.server.utils;

import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Arrays;

/**
 * @description:
 * @create: 2024/05/30
 **/
public class FileUtils {
    public static void main(String[] args) {
        Arrays.stream(haveSuffix(".tar")).forEach(System.out::println);
    }

    private static final CharSequence[] SPECIAL_SUFFIX = {"tar.bz2", "tar.Z", "tar.gz", "tar.xz"};

    public static String getSuffix(String name) {
        int index = getLastDot(name);
        if (index < 1) {
            return "";
        } else {
            String suffix = getSuffix(name, index);
            if (suffix.length() > 20) {
                return "";
            } else {
                return suffix;
            }
        }
    }

    public static Object[] haveSuffix(String name) {
        int index = getLastDot(name);
        if (index < 1) {
            return new Object[]{false, ""};
        } else {
            return new Object[]{true, getSuffix(name, index)};
        }
    }

    public static int getLastDot(String name) {
        return name.lastIndexOf(".");
    }

    public static String getSuffix(String fileName, int index) {
        final int secondToLastIndex = fileName.substring(0, index).lastIndexOf(StrUtil.DOT);
        final String substr = fileName.substring(secondToLastIndex == -1 ? index : secondToLastIndex + 1);
        if (StrUtil.containsAny(substr, SPECIAL_SUFFIX)) {
            return substr;
        }

        final String ext = fileName.substring(index + 1);
        // 扩展名中不能包含路径相关的符号
        return StrUtil.containsAny(ext, CharUtil.SLASH, CharUtil.BACKSLASH) ? StrUtil.EMPTY : ext;
    }
}