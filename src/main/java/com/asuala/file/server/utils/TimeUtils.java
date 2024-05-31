package com.asuala.file.server.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @create: 2024/02/19
 **/
@Slf4j
public class TimeUtils {
    public static String convertSecondsToHMS(long seconds) {
        if (0L == seconds) {
            return "-";
        }
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }
}