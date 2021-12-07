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
import org.apache.commons.lang3.StringUtils;

import static com.oceanbase.oms.logmessage.DataMessage.Record.UTF8MB4_ENCODING;

public class MySQLLogTypeHelper extends LogTypeHelper {
    public static final MySQLLogTypeHelper MYSQL_LOG_TYPE_HELPER = new MySQLLogTypeHelper();

    public MySQLLogTypeHelper() {
        super(DBType.MYSQL);
    }

    @Override
    public String correctEncoding(int type, String realEncoding) {
        switch (type) {
            case LogMessageTypeCode.DRC_MSG_TYPE_VAR_STRING:
            case LogMessageTypeCode.DRC_MSG_TYPE_STRING:
                return realEncoding.isEmpty() ? BINARY_STR : realEncoding;
            case LogMessageTypeCode.DRC_MSG_TYPE_JSON:
                return UTF8MB4_ENCODING;
            default:
                return realEncoding;
        }
    }

    @Override
    public int correctCode(int typeCode, String encoding) {
        switch (typeCode) {
            case LogMessageTypeCode.DRC_MSG_TYPE_VAR_STRING:
                return StringUtils.equals(encoding, BINARY_STR) ? LogMessageTypeCode.DRC_MSG_TYPE_VAR_BINARY : LogMessageTypeCode.DRC_MSG_TYPE_VARCHAR;
            case LogMessageTypeCode.DRC_MSG_TYPE_STRING:
                return StringUtils.equals(encoding, BINARY_STR) ? LogMessageTypeCode.DRC_MSG_TYPE_BINARY : LogMessageTypeCode.DRC_MSG_TYPE_STRING;
            case LogMessageTypeCode.DRC_MSG_TYPE_LONG_BLOB:
            case LogMessageTypeCode.DRC_MSG_TYPE_MEDIUM_BLOB:
            case LogMessageTypeCode.DRC_MSG_TYPE_BLOB:
            case LogMessageTypeCode.DRC_MSG_TYPE_TINY_BLOB:
                return StringUtils.isEmpty(encoding) || StringUtils.equals(encoding, BINARY_STR) ? LogMessageTypeCode.DRC_MSG_TYPE_BLOB : LogMessageTypeCode.DRC_MSG_TYPE_TEXT;
            default:
                return typeCode;
        }
    }

    @Override
    public void correctField(DataMessage.Record.Field field, String enc) {
        if (enc.isEmpty()) {
            if (field.type == LogMessageTypeCode.DRC_MSG_TYPE_STRING) {
                field.encoding = BINARY_STR;
                field.type = LogMessageTypeCode.DRC_MSG_TYPE_BINARY;
            } else if (field.type == LogMessageTypeCode.DRC_MSG_TYPE_VAR_STRING) {
                field.encoding = BINARY_STR;
                field.type = LogMessageTypeCode.DRC_MSG_TYPE_VARCHAR;
            } else if (field.type == LogMessageTypeCode.DRC_MSG_TYPE_JSON) {
                field.encoding = UTF8MB4_ENCODING;
            }
        } else {
            if (field.type >= LogMessageTypeCode.DRC_MSG_TYPE_TINY_BLOB && field.type <= LogMessageTypeCode.DRC_MSG_TYPE_BLOB) {
                field.type = LogMessageTypeCode.DRC_MSG_TYPE_TEXT;
            }
            field.encoding = enc;
        }
    }

}
