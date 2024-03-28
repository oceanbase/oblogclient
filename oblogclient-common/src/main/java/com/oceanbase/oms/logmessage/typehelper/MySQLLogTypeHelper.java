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

package com.oceanbase.oms.logmessage.typehelper;

import static com.oceanbase.oms.logmessage.DataMessage.Record.UTF8MB4_ENCODING;

import com.oceanbase.oms.common.enums.DbTypeEnum;
import com.oceanbase.oms.logmessage.DataMessage;
import org.apache.commons.lang3.StringUtils;

public class MySQLLogTypeHelper extends LogTypeHelper {
    public static final MySQLLogTypeHelper MYSQL_LOG_TYPE_HELPER = new MySQLLogTypeHelper();

    public MySQLLogTypeHelper() {
        super(DbTypeEnum.MYSQL);
    }

    @Override
    public String correctEncoding(int type, String realEncoding) {
        switch (type) {
            case LogMessageTypeCode.LOG_MSG_TYPE_VAR_STRING:
            case LogMessageTypeCode.LOG_MSG_TYPE_STRING:
                return realEncoding.isEmpty() ? BINARY_STR : realEncoding;
            case LogMessageTypeCode.LOG_MSG_TYPE_JSON:
                return UTF8MB4_ENCODING;
            default:
                return realEncoding;
        }
    }

    @Override
    public int correctCode(int typeCode, String encoding) {
        switch (typeCode) {
            case LogMessageTypeCode.LOG_MSG_TYPE_VAR_STRING:
                return StringUtils.equals(encoding, BINARY_STR)
                        ? LogMessageTypeCode.LOG_MSG_TYPE_VAR_BINARY
                        : LogMessageTypeCode.LOG_MSG_TYPE_VARCHAR;
            case LogMessageTypeCode.LOG_MSG_TYPE_STRING:
                return StringUtils.equals(encoding, BINARY_STR)
                        ? LogMessageTypeCode.LOG_MSG_TYPE_BINARY
                        : LogMessageTypeCode.LOG_MSG_TYPE_STRING;
            case LogMessageTypeCode.LOG_MSG_TYPE_LONG_BLOB:
            case LogMessageTypeCode.LOG_MSG_TYPE_MEDIUM_BLOB:
            case LogMessageTypeCode.LOG_MSG_TYPE_BLOB:
            case LogMessageTypeCode.LOG_MSG_TYPE_TINY_BLOB:
                return StringUtils.isEmpty(encoding) || StringUtils.equals(encoding, BINARY_STR)
                        ? LogMessageTypeCode.LOG_MSG_TYPE_BLOB
                        : LogMessageTypeCode.LOG_MSG_TYPE_TEXT;
            default:
                return typeCode;
        }
    }

    @Override
    public void correctField(DataMessage.Record.Field field, String enc) {
        if (enc.isEmpty()) {
            if (field.type == LogMessageTypeCode.LOG_MSG_TYPE_STRING) {
                field.encoding = BINARY_STR;
                field.type = LogMessageTypeCode.LOG_MSG_TYPE_BINARY;
            } else if (field.type == LogMessageTypeCode.LOG_MSG_TYPE_VAR_STRING) {
                field.encoding = BINARY_STR;
                field.type = LogMessageTypeCode.LOG_MSG_TYPE_VARCHAR;
            } else if (field.type == LogMessageTypeCode.LOG_MSG_TYPE_JSON) {
                field.encoding = UTF8MB4_ENCODING;
            }
        } else {
            if (field.type >= LogMessageTypeCode.LOG_MSG_TYPE_TINY_BLOB
                    && field.type <= LogMessageTypeCode.LOG_MSG_TYPE_BLOB) {
                field.type = LogMessageTypeCode.LOG_MSG_TYPE_TEXT;
            }
            field.encoding = enc;
        }
    }
}
