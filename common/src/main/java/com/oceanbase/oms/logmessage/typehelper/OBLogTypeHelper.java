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

public class OBLogTypeHelper extends LogTypeHelper {
    public static final OBLogTypeHelper OB_LOG_TYPE_HELPER = new OBLogTypeHelper();

    private static final String DEFAULT_ENCODING = "";

    public OBLogTypeHelper() {
        super(DBType.OCEANBASE1);
    }

    @Override
    public String correctEncoding(int typeCode, String realEncoding) {
        switch (typeCode) {
            case LogMessageTypeCode.DRC_MSG_TYPE_VAR_STRING:
            case LogMessageTypeCode.DRC_MSG_TYPE_STRING:
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
            case LogMessageTypeCode.DRC_MSG_TYPE_TINY_BLOB:
            case LogMessageTypeCode.DRC_MSG_TYPE_MEDIUM_BLOB:
            case LogMessageTypeCode.DRC_MSG_TYPE_LONG_BLOB:
            case LogMessageTypeCode.DRC_MSG_TYPE_BLOB:
                if (!StringUtils.isEmpty(encoding) && !StringUtils.equals(encoding, "binary")) {
                    return LogMessageTypeCode.DRC_MSG_TYPE_CLOB;
                }
                break;
            case LogMessageTypeCode.DRC_MSG_TYPE_VAR_STRING:
                if (StringUtils.isEmpty(encoding) || StringUtils.equals(encoding, "binary")) {
                    return LogMessageTypeCode.DRC_MSG_TYPE_VAR_BINARY;
                } else {
                    return LogMessageTypeCode.DRC_MSG_TYPE_VARCHAR;
                }
            case LogMessageTypeCode.DRC_MSG_TYPE_STRING:
                if (StringUtils.isEmpty(encoding) || StringUtils.equals(encoding, "binary")) {
                    return LogMessageTypeCode.DRC_MSG_TYPE_BINARY;
                }
                break;
        }
        return typeCode;
    }

    @Override
    public void correctField(DataMessage.Record.Field f, String realEncoding) {
        switch (f.type) {
            case LogMessageTypeCode.DRC_MSG_TYPE_TINY_BLOB:
            case LogMessageTypeCode.DRC_MSG_TYPE_MEDIUM_BLOB:
            case LogMessageTypeCode.DRC_MSG_TYPE_LONG_BLOB:
            case LogMessageTypeCode.DRC_MSG_TYPE_BLOB:
                if (!StringUtils.isEmpty(f.encoding) && !StringUtils.equals(f.encoding, "binary")) {
                    f.type = LogMessageTypeCode.DRC_MSG_TYPE_CLOB;
                }
                break;
            case LogMessageTypeCode.DRC_MSG_TYPE_VAR_STRING:
                if (StringUtils.isEmpty(f.encoding) || StringUtils.equals(f.encoding, "binary")) {
                    f.type = LogMessageTypeCode.DRC_MSG_TYPE_VAR_BINARY;
                } else {
                    f.type = LogMessageTypeCode.DRC_MSG_TYPE_VARCHAR;
                }
                break;
            case LogMessageTypeCode.DRC_MSG_TYPE_STRING:
                if (StringUtils.isEmpty(f.encoding) || StringUtils.equals(f.encoding, "binary")) {
                    f.type = LogMessageTypeCode.DRC_MSG_TYPE_BINARY;
                }
                break;
            default:
                f.encoding = DEFAULT_ENCODING;
        }
    }
}
