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

public abstract class LogTypeHelperFactory {

    public static LogTypeHelper getInstance(DbTypeEnum dbType) {
        switch (dbType) {
            case OB_MYSQL:
            case OB_ORACLE:
            case OB_05:
                return OBLogTypeHelper.OB_LOG_TYPE_HELPER;
            case MYSQL:
                return MySQLLogTypeHelper.MYSQL_LOG_TYPE_HELPER;
            case ORACLE:
                return OracleLogTypeHelper.ORACLE_LOG_TYPE_HELPER;
            case DB2_LUW:
                return DB2LogTypeHelper.DB2_LOG_TYPE_HELPER;
            case POSTGRESQL:
                return XLogTypeHelper.XLOG_TYPE_HELPER;
            default:
                throw new IllegalArgumentException("Unsupported dbType " + dbType);
        }
    }
}
