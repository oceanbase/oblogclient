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

public enum ProtocolVersion {
    /**
     * v0 version
     */
    V0(0),
    V1(1),
    V2(2);

    private final int code;

    ProtocolVersion(int code) {
        this.code = code;
    }

    public static ProtocolVersion codeOf(int code) {
        for (ProtocolVersion v : values()) {
            if (v.code == code) {
                return v;
            }
        }
        return null;
    }

    public int code() {
        return code;
    }
}
