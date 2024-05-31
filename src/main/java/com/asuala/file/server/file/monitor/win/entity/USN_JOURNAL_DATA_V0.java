package com.asuala.file.server.file.monitor.win.entity;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;

import java.util.Arrays;
import java.util.List;

/**
* @Description 磁盘信息
* @Date 2024-02-01
**/
public class USN_JOURNAL_DATA_V0 extends Structure {

    /**
     typedef struct {

     DWORDLONG UsnJournalID;
     USN FirstUsn;
     USN NextUsn;
     USN LowestValidUsn;
     USN MaxUsn;
     DWORDLONG MaximumSize;
     DWORDLONG AllocationDelta;
     WORD   MinSupportedMajorVersion;
     WORD   MaxSupportedMajorVersion;

     } USN_JOURNAL_DATA_V1, *PUSN_JOURNAL_DATA_V1;
    **/
    public WinDef.DWORDLONG UsnJournalID;
    public long FirstUsn;
    public long NextUsn;
    public long LowestValidUsn;
    public long MaxUsn;
    public long MaximumSize;
    public long AllocationDelta;
    public short MinSupportedMajorVersion;
    public short MaxSupportedMajorVersion;

    public USN_JOURNAL_DATA_V0() {
        super();
    }

    public USN_JOURNAL_DATA_V0(Pointer pointer) {
        super(pointer);
    }

    protected List<String> getFieldOrder() {
        return Arrays.asList("UsnJournalID", "FirstUsn", "NextUsn", "LowestValidUsn", "MaxUsn",
                "MaximumSize", "AllocationDelta", "MinSupportedMajorVersion", "MaxSupportedMajorVersion");
    }
}