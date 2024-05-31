package com.asuala.file.server.file.monitor.win.entity;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @create: 2024/01/30
 **/
public class MTF_ENUM_DATA extends Structure {

    /**
     typedef struct {
     DWORDLONG StartFileReferenceNumber;
     USN       LowUsn;
     USN       HighUsn;
     } MFT_ENUM_DATA, *PMFT_ENUM_DATA;
    **/
    public long StartFileReferenceNumber;
    public long LowUsn;
    public long HighUsn;

    protected List<String> getFieldOrder() {
        return Arrays.asList("StartFileReferenceNumber", "LowUsn", "HighUsn");
    }
}