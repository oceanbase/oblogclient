/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.oms.logmessage.enums;

/**
 * The class that defines the constants that are used to identify data types.
 */
public class DataType {

    public static final byte DT_UNKNOWN = 0x00;
    public static final byte DT_INT8   = 0x01;
    public static final byte DT_UINT8  = 0x02;
    public static final byte DT_INT16  = 0x03;
    public static final byte DT_UINT16 = 0x04;
    public static final byte DT_INT32  = 0x05;
    public static final byte DT_UINT32 = 0x06;
    public static final byte DT_INT64  = 0x07;
    public static final byte DT_UINT64 = 0x08;
    public static final byte DT_FLOAT  = 0x09;
    public static final byte DT_DOUBLE = 0x0a;
    public static final byte DT_STRING = 0x0b;
    public static final byte TOTAL_DT  = 0x0c;
    public static final byte DT_MASK   = 0x0f;

    public static final byte DC_ARRAY  = 0x10;
    public static final byte DC_NULL   = 0x20;
    public static final byte DC_MASK   = 0x30;

    public static final int getDataTypeLen() {
        return 2;
    }

    public static final int getStringLengthLen() {
        return 4;
    }
}
