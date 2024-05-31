package com.asuala.file.server.utils;

/**
 * @description:
 * @create: 2024/02/23
 **/
public class ConstantMethodUtils {
    private static final String regexSpecial = "[\\\\/:*?\"<>|]";
    private static final String regexSpace = "\\s+";
    private static final String replacement = " ";

    public static String removeSpecialCharacters(String fileName) {
        fileName = fileName.replaceAll(regexSpecial, replacement);
        fileName = fileName.replaceAll(regexSpace, replacement);
        return fileName.trim();
    }
}