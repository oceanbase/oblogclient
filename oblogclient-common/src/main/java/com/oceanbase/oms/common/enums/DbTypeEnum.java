/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

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
