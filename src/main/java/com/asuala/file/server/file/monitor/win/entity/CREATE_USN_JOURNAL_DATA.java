package com.asuala.file.server.file.monitor.win.entity;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @create: 2024/01/30
 **/
public class CREATE_USN_JOURNAL_DATA extends Structure {

    public long MaximumSize;
    public long AllocationDelta;

    public CREATE_USN_JOURNAL_DATA() {
        super();
    }

    public CREATE_USN_JOURNAL_DATA(Pointer pointer) {
        super(pointer);
    }

    protected List<String> getFieldOrder() {
        return Arrays.asList("MaximumSize", "AllocationDelta");
    }
}