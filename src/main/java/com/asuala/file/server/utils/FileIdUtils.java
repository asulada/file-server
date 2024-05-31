package com.asuala.file.server.utils;

import java.security.MessageDigest;

/**
 * @description: 根据字符串生成唯一id
 * @create: 2024/05/26
 **/
public class FileIdUtils {

    public static void main(String[] args) {
        System.out.println(generateUniqueNumber("/mnt/nfts2"));
    }

    public static Long generateUniqueNumber(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));

            // 使用前8个字节生成一个long值
            long hashCode = 0;
            for (int i = 0; i < 8; i++) {
                hashCode <<= 8;
                hashCode |= (hash[i] & 0xff);
            }
            return hashCode & 0x7fffffffffffffffL;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Long buildFileId(Integer index, String path) {
        return generateUniqueNumber(index + path);
    }
}