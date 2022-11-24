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

import static com.oceanbase.oms.logmessage.LogMessage.UTF8_ENCODING;

import com.oceanbase.oms.common.enums.DbTypeEnum;
import com.oceanbase.oms.logmessage.DataMessage;

public class DB2LogTypeHelper extends LogTypeHelper {
    public static final DB2LogTypeHelper DB2_LOG_TYPE_HELPER = new DB2LogTypeHelper();

    public DB2LogTypeHelper() {
        super(DbTypeEnum.DB2_LUW);
    }

    @Override
    public String correctEncoding(int typeCode, String realEncoding) {
        switch (typeCode) {
            case LogMessageTypeCode.LOG_MSG_TYPE_BINARY:
            case LogMessageTypeCode.LOG_MSG_TYPE_BLOB:
            case LogMessageTypeCode.LOG_MSG_TYPE_TINY_BLOB:
            case LogMessageTypeCode.LOG_MSG_TYPE_VAR_BINARY:
                return EMPTY_ENCODING_STR;
            default:
                return UTF8_ENCODING;
        }
    }

    @Override
    public int correctCode(int typeCode, String encoding) {
        if (typeCode == LogMessageTypeCode.LOG_MSG_TYPE_TINY_BLOB) {
            return LogMessageTypeCode.LOG_MSG_TYPE_VAR_BINARY;
        } else {
            return typeCode;
        }
    }

    @Override
    public void correctField(DataMessage.Record.Field f, String realEncoding) {
        switch (f.type) {
            case LogMessageTypeCode.LOG_MSG_TYPE_BINARY:
            case LogMessageTypeCode.LOG_MSG_TYPE_BLOB:
            case LogMessageTypeCode.LOG_MSG_TYPE_VAR_BINARY:
                f.encoding = EMPTY_ENCODING_STR;
                break;
            case LogMessageTypeCode.LOG_MSG_TYPE_TINY_BLOB:
                f.encoding = EMPTY_ENCODING_STR;
                f.type = LogMessageTypeCode.LOG_MSG_TYPE_VAR_BINARY;
                break;
        }
    }
}
