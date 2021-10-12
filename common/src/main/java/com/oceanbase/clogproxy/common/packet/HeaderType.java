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

public enum HeaderType {
    /**
     * error response
     */
    ERROR_RESPONSE(-1),

    /**
     * client request handshake
     */
    HANDSHAKE_REQUEST_CLIENT(1),

    /**
     * response to client handshake
     */
    HANDSHAKE_RESPONSE_CLIENT(2),

    /**
     * logreader request handshake
     */
    HANDSHAKE_REQUEST_LOGREADER(3),

    /**
     * logreader response handshake
     */
    HANDSHAKE_RESPONSE_LOGREADER(4),

    /**
     * logreader data stream
     */
    DATA_LOGREADER(5),

    /**
     * client data stream
     */
    DATA_CLIENT(6),

    /**
     * status info of server runtime
     */
    STATUS(7),

    /**
     * status info of LogReader
     */
    STATUS_LOGREADER(8),
    ;

    private final int code;

    HeaderType(int code) {
        this.code = code;
    }

    public static HeaderType codeOf(int code) {
        for (HeaderType t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        return null;
    }

    public int code() {
        return code;
    }
}
