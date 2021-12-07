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

public class OracleLogTypeHelper extends LogTypeHelper {
    public static final OracleLogTypeHelper ORACLE_LOG_TYPE_HELPER = new OracleLogTypeHelper();

    public OracleLogTypeHelper() {
        super(DBType.ORACLE);
    }

    @Override
    public String correctEncoding(int typeCode, String realEncoding) {
        switch (typeCode) {
            case LogMessageTypeCode.DRC_MSG_TYPE_BLOB:
                return EMPTY_ENCODING_STR;
            default:
                return realEncoding;
        }
    }

    @Override
    public int correctCode(int typeCode, String encoding) {
        return typeCode;
    }

    @Override
    public void correctField(DataMessage.Record.Field f, String realEncoding) {
        switch (f.type) {
            case LogMessageTypeCode.DRC_MSG_TYPE_BLOB:
                f.encoding = EMPTY_ENCODING_STR;
        }
    }

}
