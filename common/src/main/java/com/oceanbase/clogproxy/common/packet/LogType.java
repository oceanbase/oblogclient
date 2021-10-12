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

import java.util.HashMap;
import java.util.Map;

public enum LogType {

    /**
     * LogProxy OceanBase LogReader
     */
    OCEANBASE(0);

    private final int code;

    private static final Map<Integer, LogType> CODE_TYPES = new HashMap<>(values().length);

    static {
        for (LogType logCaptureType : values()) {
            CODE_TYPES.put(logCaptureType.code, logCaptureType);
        }
    }

    LogType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static LogType fromString(String string) {
        if (string == null) {
            throw new NullPointerException("logTypeString is null");
        }
        return valueOf(string.toUpperCase());
    }

    public static LogType fromCode(int code) {
        if (CODE_TYPES.containsKey(code)) {
            return CODE_TYPES.get(code);
        }
        return null;
    }
}
