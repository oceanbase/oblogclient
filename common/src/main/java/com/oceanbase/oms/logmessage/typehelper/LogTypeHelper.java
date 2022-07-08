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
