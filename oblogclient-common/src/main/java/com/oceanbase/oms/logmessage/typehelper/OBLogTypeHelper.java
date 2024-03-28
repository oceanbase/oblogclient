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


import com.oceanbase.oms.common.enums.DbTypeEnum;
import com.oceanbase.oms.logmessage.DataMessage;
import org.apache.commons.lang3.StringUtils;

public class OBLogTypeHelper extends LogTypeHelper {
    public static final OBLogTypeHelper OB_LOG_TYPE_HELPER = new OBLogTypeHelper();

    private static final String DEFAULT_ENCODING = "";

    public OBLogTypeHelper() {
        super(DbTypeEnum.OB_MYSQL);
    }

    @Override
    public String correctEncoding(int typeCode, String realEncoding) {
        switch (typeCode) {
            case LogMessageTypeCode.LOG_MSG_TYPE_VAR_STRING:
            case LogMessageTypeCode.LOG_MSG_TYPE_STRING:
                return realEncoding;
            default:
                if (StringUtils.equals(realEncoding, "binary")) {
                    return DEFAULT_ENCODING;
                } else {
                    return realEncoding;
                }
        }
    }

    @Override
    public int correctCode(int typeCode, String encoding) {
        switch (typeCode) {
            case LogMessageTypeCode.LOG_MSG_TYPE_TINY_BLOB:
            case LogMessageTypeCode.LOG_MSG_TYPE_MEDIUM_BLOB:
            case LogMessageTypeCode.LOG_MSG_TYPE_LONG_BLOB:
            case LogMessageTypeCode.LOG_MSG_TYPE_BLOB:
                if (!StringUtils.isEmpty(encoding) && !StringUtils.equals(encoding, "binary")) {
                    return LogMessageTypeCode.LOG_MSG_TYPE_CLOB;
                }
                break;
            case LogMessageTypeCode.LOG_MSG_TYPE_VAR_STRING:
                if (StringUtils.isEmpty(encoding) || StringUtils.equals(encoding, "binary")) {
                    return LogMessageTypeCode.LOG_MSG_TYPE_VAR_BINARY;
                } else {
                    return LogMessageTypeCode.LOG_MSG_TYPE_VARCHAR;
                }
            case LogMessageTypeCode.LOG_MSG_TYPE_STRING:
                if (StringUtils.isEmpty(encoding) || StringUtils.equals(encoding, "binary")) {
                    return LogMessageTypeCode.LOG_MSG_TYPE_BINARY;
                }
                break;
        }
        return typeCode;
    }

    @Override
    public void correctField(DataMessage.Record.Field f, String realEncoding) {
        switch (f.type) {
            case LogMessageTypeCode.LOG_MSG_TYPE_TINY_BLOB:
            case LogMessageTypeCode.LOG_MSG_TYPE_MEDIUM_BLOB:
            case LogMessageTypeCode.LOG_MSG_TYPE_LONG_BLOB:
            case LogMessageTypeCode.LOG_MSG_TYPE_BLOB:
                if (!StringUtils.isEmpty(f.encoding) && !StringUtils.equals(f.encoding, "binary")) {
                    f.type = LogMessageTypeCode.LOG_MSG_TYPE_CLOB;
                }
                break;
            case LogMessageTypeCode.LOG_MSG_TYPE_VAR_STRING:
                if (StringUtils.isEmpty(f.encoding) || StringUtils.equals(f.encoding, "binary")) {
                    f.type = LogMessageTypeCode.LOG_MSG_TYPE_VAR_BINARY;
                } else {
                    f.type = LogMessageTypeCode.LOG_MSG_TYPE_VARCHAR;
                }
                break;
            case LogMessageTypeCode.LOG_MSG_TYPE_STRING:
                if (StringUtils.isEmpty(f.encoding) || StringUtils.equals(f.encoding, "binary")) {
                    f.type = LogMessageTypeCode.LOG_MSG_TYPE_BINARY;
                }
                break;
            default:
                f.encoding = DEFAULT_ENCODING;
        }
    }
}
