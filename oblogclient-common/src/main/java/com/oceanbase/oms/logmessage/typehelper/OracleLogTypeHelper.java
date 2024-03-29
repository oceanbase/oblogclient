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

public class OracleLogTypeHelper extends LogTypeHelper {
    public static final OracleLogTypeHelper ORACLE_LOG_TYPE_HELPER = new OracleLogTypeHelper();

    public OracleLogTypeHelper() {
        super(DbTypeEnum.ORACLE);
    }

    @Override
    public String correctEncoding(int typeCode, String realEncoding) {
        switch (typeCode) {
            case LogMessageTypeCode.LOG_MSG_TYPE_BLOB:
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
            case LogMessageTypeCode.LOG_MSG_TYPE_BLOB:
                f.encoding = EMPTY_ENCODING_STR;
        }
    }
}
