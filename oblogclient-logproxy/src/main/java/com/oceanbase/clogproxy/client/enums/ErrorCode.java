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

/** Error code enumeration. */
public enum ErrorCode {
    ////////// 0~499: process error ////////////
    /** General error. */
    NONE(0),

    /** Inner error */
    E_INNER(1),

    /** Failed to connect. */
    E_CONNECT(2),

    /** Exceed max retry connect count. */
    E_MAX_RECONNECT(3),

    /** User callback throws exception. */
    E_USER(4),

    ////////// 500~: receive data error ////////////
    /** Unknown data protocol. */
    E_PROTOCOL(500),

    /** Unknown header type. */
    E_HEADER_TYPE(501),

    /** Failed to auth. */
    NO_AUTH(502),

    /** Unknown compress type. */
    E_COMPRESS_TYPE(503),

    /** Length not match. */
    E_LEN(504),

    /** Failed to parse data. */
    E_PARSE(505);

    /** The ordinal of this enumeration constant. */
    int code;

    /**
     * Constructor.
     *
     * @param code The ordinal of this enumeration constant.
     */
    ErrorCode(int code) {
        this.code = code;
    }
}
