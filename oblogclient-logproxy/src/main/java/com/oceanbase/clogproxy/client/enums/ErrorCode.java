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
