/*
 * Copyright 2024 OceanBase.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oceanbase.oms.logmessage.enums;

/** The class that defines the constants that are used to identify data types. */
public class DataType {

    public static final byte DT_UNKNOWN = 0x00;
    public static final byte DT_INT8 = 0x01;
    public static final byte DT_UINT8 = 0x02;
    public static final byte DT_INT16 = 0x03;
    public static final byte DT_UINT16 = 0x04;
    public static final byte DT_INT32 = 0x05;
    public static final byte DT_UINT32 = 0x06;
    public static final byte DT_INT64 = 0x07;
    public static final byte DT_UINT64 = 0x08;
    public static final byte DT_FLOAT = 0x09;
    public static final byte DT_DOUBLE = 0x0a;
    public static final byte DT_STRING = 0x0b;
    public static final byte TOTAL_DT = 0x0c;
    public static final byte DT_MASK = 0x0f;

    public static final byte DC_ARRAY = 0x10;
    public static final byte DC_NULL = 0x20;
    public static final byte DC_MASK = 0x30;

    public static final int getDataTypeLen() {
        return 2;
    }

    public static final int getStringLengthLen() {
        return 4;
    }
}
