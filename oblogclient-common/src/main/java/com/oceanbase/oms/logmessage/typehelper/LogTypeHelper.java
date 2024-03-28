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

public abstract class LogTypeHelper {

    public static final String BINARY_STR = "binary";

    public static final String EMPTY_ENCODING_STR = "";

    protected final DbTypeEnum dbType;

    public LogTypeHelper(DbTypeEnum dbType) {
        this.dbType = dbType;
    }

    public DbTypeEnum getDbType() {
        return dbType;
    }

    public abstract String correctEncoding(int typeCode, String realEncoding);

    public abstract int correctCode(int typeCode, String encoding);

    public abstract void correctField(DataMessage.Record.Field f, String realEncoding);
}
