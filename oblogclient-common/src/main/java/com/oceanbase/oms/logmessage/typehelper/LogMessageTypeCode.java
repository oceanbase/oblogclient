/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.oms.logmessage.typehelper;

// compatible with mysql type code
// same code may reference different schema type
public class LogMessageTypeCode {
    public static final int LOG_MSG_TYPE_DECIMAL = 0;
    public static final int LOG_MSG_TYPE_TINY = 1;
    public static final int LOG_MSG_TYPE_SHORT = 2;
    public static final int LOG_MSG_TYPE_LONG = 3;
    public static final int LOG_MSG_TYPE_FLOAT = 4;
    public static final int LOG_MSG_TYPE_DOUBLE = 5;
    public static final int LOG_MSG_TYPE_NULL = 6;
    public static final int LOG_MSG_TYPE_TIMESTAMP = 7;
    public static final int LOG_MSG_TYPE_LONGLONG = 8;
    public static final int LOG_MSG_TYPE_INT24 = 9;
    public static final int LOG_MSG_TYPE_DATE = 10;
    public static final int LOG_MSG_TYPE_TIME = 11;
    public static final int LOG_MSG_TYPE_DATETIME = 12;
    public static final int LOG_MSG_TYPE_YEAR = 13;
    public static final int LOG_MSG_TYPE_NEWDATE = 14;
    public static final int LOG_MSG_TYPE_VARCHAR = 15;
    public static final int LOG_MSG_TYPE_BIT = 16;

    public static final int LOG_MSG_TYPE_TIMESTAMP2 = 17;
    public static final int LOG_MSG_TYPE_DATETIME2 = 18;
    public static final int LOG_MSG_TYPE_TIME2 = 19;

    // appeared in ob define, but should not appeared in drc types
    public static final int LOG_MSG_COMPLEX = 160;
    public static final int LOG_MSG_TYPE_ARRAY = 161;
    public static final int LOG_MSG_TYPE_STRUCT = 162;
    public static final int LOG_MSG_TYPE_CURSOR = 163;
    public static final int LOG_MSG_TYPE_ORA_BLOB = 210;
    public static final int LOG_MSG_TYPE_CLOB = 211;

    public static final int LOG_MSG_TYPE_TEXT = 197;
    public static final int LOG_MSG_TYPE_VAR_BINARY = 198;
    public static final int LOG_MSG_TYPE_BINARY = 199;
    public static final int LOG_MSG_TYPE_TIMESTAMP_WITH_TIME_ZONE = 200;
    public static final int LOG_MSG_TYPE_TIMESTAMP_WITH_LOCAL_TIME_ZONE = 201;
    public static final int LOG_MSG_TYPE_TIMESTAMP_NANO = 202;
    public static final int LOG_MSG_TYPE_RAW = 203;
    public static final int LOG_MSG_TYPE_INTERVAL_YEAR_TO_MONTH = 204;
    public static final int LOG_MSG_TYPE_INTERVAL_DAY_TO_SECOND = 205;
    public static final int LOG_MSG_TYPE_NUMBER_FLOAT = 206;
    public static final int LOG_MSG_TYPE_NVARCHAR2 = 207;
    public static final int LOG_MSG_TYPE_NCHAR = 208;
    public static final int LOG_MSG_TYPE_ROW_ID = 209;

    public static final int LOG_MSG_TYPE_JSON = 245;
    public static final int LOG_MSG_TYPE_NEWDECIMAL = 246;
    public static final int LOG_MSG_TYPE_ENUM = 247;
    public static final int LOG_MSG_TYPE_SET = 248;
    public static final int LOG_MSG_TYPE_TINY_BLOB = 249;
    public static final int LOG_MSG_TYPE_MEDIUM_BLOB = 250;
    public static final int LOG_MSG_TYPE_LONG_BLOB = 251;
    public static final int LOG_MSG_TYPE_BLOB = 252;
    public static final int LOG_MSG_TYPE_VAR_STRING = 253;
    public static final int LOG_MSG_TYPE_STRING = 254;
    public static final int LOG_MSG_TYPE_GEOMETRY = 255;
    public static final int LOG_MSG_TYPE_ORA_BINARY_FLOAT = 256;
    public static final int LOG_MSG_TYPE_ORA_BINARY_DOUBLE = 257;
    public static final int LOG_MSG_TYPE_UNKNOWN = LOG_MSG_TYPE_ORA_BINARY_DOUBLE + 1;

    // type code for xlog
    public static final int XLOG_MSG_TYPE_SHORT = 50;
    public static final int XLOG_MSG_TYPE_INT = 51;
    public static final int XLOG_MSG_TYPE_LONG = 52;
    public static final int XLOG_MSG_TYPE_DECIMAL = 53;
    public static final int XLOG_MSG_TYPE_FLOAT = 54;
    public static final int XLOG_MSG_TYPE_DOUBLE = 55;
    public static final int XLOG_MSG_TYPE_BOOLEAN = 56;
    public static final int XLOG_MSG_TYPE_TINY = 57;
    public static final int XLOG_MSG_TYPE_LONGLONG = 58;
    public static final int XLOG_MSG_TYPE_CHAR = 70;
    public static final int XLOG_MSG_TYPE_VARCHAR = 71;
    public static final int XLOG_MSG_TYPE_BINARY = 72;
    public static final int XLOG_MSG_TYPE_JSON = 73;
    public static final int XLOG_MSG_TYPE_CLOB = 74;
    public static final int XLOG_MSG_TYPE_ENUM = 75;
    public static final int XLOG_MSG_TYPE_SET = 76;
    public static final int XLOG_MSG_TYPE_UUID = 77;
    public static final int XLOG_MSG_TYPE_ROWID = 78;
    public static final int XLOG_MSG_TYPE_VARBINARY = 79;
    public static final int XLOG_MSG_TYPE_TIMESTAMP = 80;
    public static final int XLOG_MSG_TYPE_TIMESTAMP_WITH_TIME_ZONE = 81;
    public static final int XLOG_MSG_TYPE_DATE = 82;
    public static final int XLOG_MSG_TYPE_TIME = 83;
    public static final int XLOG_MSG_TYPE_TIME_WITH_TIME_ZONE = 84;
    public static final int XLOG_MSG_TYPE_INSTANT = 85;
    public static final int XLOG_MSG_TYPE_YEAR = 86;
    public static final int XLOG_MSG_TYPE_INTERVAL_YEAR_TO_MONTH = 87;
    public static final int XLOG_MSG_TYPE_INTERVAL_DAY_TO_SECOND = 88;
    public static final int XLOG_MSG_TYPE_INTERVAL_YEAR_TO_SECOND = 89;
    public static final int XLOG_MSG_TYPE_XML = 90;
    public static final int XLOG_MSG_TYPE_BITMAP = 91;
    public static final int XLOG_MSG_TYPE_GEOMETRY_EWKT = 92;
    public static final int XLOG_MSG_TYPE_GEOMETRY_EWKB = 93;
    public static final int XLOG_MSG_TYPE_GEOGRAPHY_EWKT = 94;
    public static final int XLOG_MSG_TYPE_GEOGRAPHY_EWKB = 95;
    public static final int XLOG_MSG_TYPE_BLOB = 102;
}
