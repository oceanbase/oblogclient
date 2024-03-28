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

/** Log type enumeration. */
public enum LogType {

    /** LogProxy OceanBase LogReader. */
    OCEANBASE(0);

    /** The ordinal of this enumeration constant. */
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
