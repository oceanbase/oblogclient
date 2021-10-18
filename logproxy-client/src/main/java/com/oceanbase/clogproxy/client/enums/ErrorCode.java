/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client.enums;

public enum ErrorCode {
    ////////// 0~499: process error ////////////
    /**
     * general error
     */
    NONE(0),

    /**
     * inner error
     */
    E_INNER(1),

    /**
     * failed to connect
     */
    E_CONNECT(2),

    /**
     * exceed max retry connect count
     */
    E_MAX_RECONNECT(3),

    /**
     * user callback throws exception
     */
    E_USER(4),

    ////////// 500~: receive data error ////////////
    /**
     * unknown data protocol
     */
    E_PROTOCOL(500),

    /**
     * unknown header type
     */
    E_HEADER_TYPE(501),

    /**
     * failed to auth
     */
    NO_AUTH(502),

    /**
     * unknown compress type
     */
    E_COMPRESS_TYPE(503),

    /**
     * length not match
     */
    E_LEN(504),

    /**
     * failed to parse data
     */
    E_PARSE(505);

    int code;

    ErrorCode(int code) {
        this.code = code;
    }
}
