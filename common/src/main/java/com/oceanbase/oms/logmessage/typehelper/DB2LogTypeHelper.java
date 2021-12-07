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

import com.oceanbase.oms.logmessage.DataMessage;
import com.oceanbase.oms.logmessage.enums.DBType;

import static com.oceanbase.oms.logmessage.LogMessage.UTF8_ENCODING;

public class DB2LogTypeHelper extends LogTypeHelper {
    public static final DB2LogTypeHelper DB2_LOG_TYPE_HELPER = new DB2LogTypeHelper();

    public DB2LogTypeHelper() {
        super(DBType.DB2);
    }

    @Override
    public String correctEncoding(int typeCode, String realEncoding) {
        switch (typeCode) {
            case LogMessageTypeCode.DRC_MSG_TYPE_BINARY:
            case LogMessageTypeCode.DRC_MSG_TYPE_BLOB:
            case LogMessageTypeCode.DRC_MSG_TYPE_TINY_BLOB:
            case LogMessageTypeCode.DRC_MSG_TYPE_VAR_BINARY:
                return EMPTY_ENCODING_STR;
            default:
                return UTF8_ENCODING;
        }
    }

    @Override
    public int correctCode(int typeCode, String encoding) {
        if (typeCode == LogMessageTypeCode.DRC_MSG_TYPE_TINY_BLOB) {
            return LogMessageTypeCode.DRC_MSG_TYPE_VAR_BINARY;
        } else {
            return typeCode;
        }
    }

    @Override
    public void correctField(DataMessage.Record.Field f, String realEncoding) {
        switch (f.type) {
            case LogMessageTypeCode.DRC_MSG_TYPE_BINARY:
            case LogMessageTypeCode.DRC_MSG_TYPE_BLOB:
            case LogMessageTypeCode.DRC_MSG_TYPE_VAR_BINARY:
                f.encoding = EMPTY_ENCODING_STR;
                break;
            case LogMessageTypeCode.DRC_MSG_TYPE_TINY_BLOB:
                f.encoding = EMPTY_ENCODING_STR;
                f.type = LogMessageTypeCode.DRC_MSG_TYPE_VAR_BINARY;
                break;
        }
    }
}
