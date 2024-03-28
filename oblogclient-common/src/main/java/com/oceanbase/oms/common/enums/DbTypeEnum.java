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

package com.oceanbase.oms.common.enums;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum DbTypeEnum {

    /** OceanBase 0.5. */
    OB_05(DbCategoryEnum.RDB, new HashSet<>(Arrays.asList("oceanbase", "ob05"))),

    /** OceanBase in MySQL mode. */
    OB_MYSQL(
            DbCategoryEnum.RDB,
            new HashSet<>(Arrays.asList("oceanbase1", "ob10", "oceanbase_mysql_mode"))),

    /** OceanBase in Oracle mode. */
    OB_ORACLE(
            DbCategoryEnum.RDB,
            new HashSet<>(Arrays.asList("oceanbase_oracle_mode", "ob_in_oracle_mode"))),

    MYSQL(DbCategoryEnum.RDB),
    ORACLE(DbCategoryEnum.RDB),
    DB2_LUW(DbCategoryEnum.RDB, Collections.singleton("db2")),
    POSTGRESQL(DbCategoryEnum.RDB),
    HBASE(DbCategoryEnum.NOSQL),
    UNKNOWN(null);

    DbTypeEnum(DbCategoryEnum category) {
        this.category = category;
        this.aliases = Collections.emptySet();
    }

    DbTypeEnum(DbCategoryEnum category, Set<String> aliases) {
        this.category = category;
        this.aliases = aliases;
    }

    public static DbTypeEnum fromAlias(String alias) {
        return ALIAS_ENUM_MAP.get(alias.toLowerCase());
    }

    public static DbTypeEnum valueOfIgnoreCase(String value) {
        try {
            if (DbTypeEnum.fromAlias(value) != null) {
                return DbTypeEnum.fromAlias(value);
            } else {
                return DbTypeEnum.valueOf(value.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static final Map<String, DbTypeEnum> ALIAS_ENUM_MAP = new HashMap<>();

    static {
        for (DbTypeEnum one : values()) {
            for (String alias : one.getAliases()) {
                assert !ALIAS_ENUM_MAP.containsKey(alias.toLowerCase());
                ALIAS_ENUM_MAP.put(alias.toLowerCase(), one);
            }
        }
    }

    private final DbCategoryEnum category;
    private final Set<String> aliases;

    public DbCategoryEnum getCategory() {
        return category;
    }

    public Set<String> getAliases() {
        return aliases;
    }
}
