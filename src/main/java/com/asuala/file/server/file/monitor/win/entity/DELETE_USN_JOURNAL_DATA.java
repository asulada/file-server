package com.asuala.file.server.file.monitor.win.entity;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.TypeMapper;
import com.sun.jna.platform.win32.WinDef;

import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @create: 2024/02/01
 **/
public class DELETE_USN_JOURNAL_DATA extends Structure {
    public DELETE_USN_JOURNAL_DATA() {
    }

    public DELETE_USN_JOURNAL_DATA(TypeMapper mapper) {
        super(mapper);
    }

    public DELETE_USN_JOURNAL_DATA(int alignType) {
        super(alignType);
    }

    public DELETE_USN_JOURNAL_DATA(int alignType, TypeMapper mapper) {
        super(alignType, mapper);
    }

    public DELETE_USN_JOURNAL_DATA(Pointer p) {
        super(p);
    }

    public DELETE_USN_JOURNAL_DATA(Pointer p, int alignType) {
        super(p, alignType);
    }

    public DELETE_USN_JOURNAL_DATA(Pointer p, int alignType, TypeMapper mapper) {
        super(p, alignType, mapper);
    }

    /**
     typedef struct {
     DWORDLONG UsnJournalID;
     DWORD     DeleteFlags;
     } DELETE_USN_JOURNAL_DATA, *PDELETE_USN_JOURNAL_DATA;
    **/
    public WinDef.DWORDLONG UsnJournalID;
    public int DeleteFlags;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("UsnJournalID", "DeleteFlags");
    }
}