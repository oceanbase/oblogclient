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

package com.oceanbase.clogproxy.common.packet;

/** Header type enumeration. Used to identify the type of request or response in protobuf. */
public enum HeaderType {

    /** Error response. */
    ERROR_RESPONSE(-1),

    /** Client handshake request. */
    HANDSHAKE_REQUEST_CLIENT(1),

    /** Client handshake response. */
    HANDSHAKE_RESPONSE_CLIENT(2),

    /** LogReader handshake request. */
    HANDSHAKE_REQUEST_LOGREADER(3),

    /** LogReader handshake response. */
    HANDSHAKE_RESPONSE_LOGREADER(4),

    /** LogReader data stream. */
    DATA_LOGREADER(5),

    /** Client data stream. */
    DATA_CLIENT(6),

    /** Status info of server runtime. */
    STATUS(7),

    /** Status info of LogReader. */
    STATUS_LOGREADER(8);

    /** The ordinal of this enumeration constant. */
    private final int code;

    /**
     * Constructor.
     *
     * @param code The ordinal of this enumeration constant.
     */
    HeaderType(int code) {
        this.code = code;
    }

    /**
     * Returns the enum constant of HeaderType with the specified code.
     *
     * @param code The ordinal of this enumeration constant.
     * @return The enum constant.
     */
    public static HeaderType codeOf(int code) {
        for (HeaderType t : values()) {
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
        return code;
    }
}
