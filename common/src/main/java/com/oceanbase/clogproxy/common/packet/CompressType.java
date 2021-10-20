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
 * Compress type enumeration. Primarily used for {@link com.oceanbase.clogproxy.common.packet.protocol.LogProxyProto.RecordData}.
 */
public enum CompressType {
    /**
     * no compress
     */
    NONE(0),

    /**
     * lz4 compress
     */
    LZ4(1);

    private final int code;

    CompressType(int code) {
        this.code = code;
    }

    public static CompressType codeOf(int code) {
        for (CompressType v : values()) {
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
