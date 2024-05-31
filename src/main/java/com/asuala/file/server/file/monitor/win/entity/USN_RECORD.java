package com.asuala.file.server.file.monitor.win.entity;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;

import java.util.Arrays;
import java.util.List;

/**
 * @description: 文件记录
 * @create: 2024/01/30
 **/
public class USN_RECORD extends Structure {

    public USN_RECORD() {
    }

    public USN_RECORD(Pointer p) {
        super(p);
    }

    /**
     * typedef struct {
     * DWORD         RecordLength;4
     * WORD          MajorVersion;2
     * WORD          MinorVersion;2
     * DWORDLONG     FileReferenceNumber;8
     * DWORDLONG     ParentFileReferenceNumber;8
     * USN           Usn;8
     * LARGE_INTEGER TimeStamp;8
     * DWORD         Reason;4
     * DWORD         SourceInfo;4
     * DWORD         SecurityId;4
     * DWORD         FileAttributes;4
     * WORD          FileNameLength;2
     * WORD          FileNameOffset;2
     * WCHAR         FileName[1];2
     * } USN_RECORD, *PUSN_RECORD;
     **/
    public WinDef.DWORD RecordLength;
    public WinDef.WORD MajorVersion;
    public WinDef.WORD MinorVersion;
    public WinDef.DWORDLONG FileReferenceNumber;
    public WinDef.DWORDLONG ParentFileReferenceNumber;
    public WinDef.LONGLONG Usn;
    public WinNT.LARGE_INTEGER TimeStamp;
    public WinDef.DWORD Reason;
    public WinDef.DWORD SourceInfo;
    public WinDef.DWORD SecurityId;
    public WinDef.DWORD FileAttributes;
    public WinDef.WORD FileNameLength;
    public WinDef.WORD FileNameOffset;
    public char[] FileName = new char[1];

    protected List<String> getFieldOrder() {
        return Arrays.asList("RecordLength", "MajorVersion", "MinorVersion", "FileReferenceNumber", "ParentFileReferenceNumber",
                "Usn", "TimeStamp", "Reason", "SourceInfo", "SecurityId", "FileAttributes", "FileNameLength","FileNameOffset","FileName");
    }
}