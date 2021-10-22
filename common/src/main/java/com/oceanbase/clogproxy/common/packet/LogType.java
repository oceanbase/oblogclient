/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.common.packet;

/**
 * Log type enumeration.
 */
public enum LogType {

    /**
     * LogProxy OceanBase LogReader.
     */
    OCEANBASE(0);

    /**
     * The ordinal of this enumeration constant.
     */
    private final int code;

    /**
     * Constructor.
     *
     * @param code The ordinal of this enumeration constant.
     */
    LogType(int code) {
        this.code = code;
    }

    /**
     * Returns the enum constant of LogType with the specified code.
     *
     * @param code The ordinal of this enumeration constant.
     * @return The enum constant.
     */
    public static LogType codeOf(int code) {
        for (LogType t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        return null;
    }

    /**
     * Get the ordinal of this enumeration constant.
     *
     * @return The ordinal of this enumeration constant.
     */
    public int code() {
        return this.code;
    }
}
