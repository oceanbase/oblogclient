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

package com.oceanbase.clogproxy.client.util;

import java.util.Map;

/** Utils class used to validate arguments. */
public class Validator {

    private static final int MINIMAL_VALID_PORT = 1;
    private static final int MAXIMAL_VALID_PORT = 65535;

    /**
     * Validate the object is not null, otherwise throws an {@link NullPointerException}.
     *
     * @param obj Object to be verified.
     * @param message Message in the NullPointerException.
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
    }

    /**
     * Validate the port number is valid, otherwise throws an {@link IllegalArgumentException}.
     *
     * @param port Port number to be verified.
     * @param message Message in the IllegalArgumentException.
     */
    public static void validatePort(int port, String message) {
        if (port < MINIMAL_VALID_PORT || port >= MAXIMAL_VALID_PORT) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validate the string is not null or empty, otherwise throws an {@link
     * IllegalArgumentException}.
     *
     * @param val String to be verified.
     * @param message Message in the IllegalArgumentException.
     */
    public static void notEmpty(String val, String message) {
        if (val == null || val.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validate the map is not null or empty, otherwise throws an {@link IllegalArgumentException}.
     *
     * @param map Map to be verified.
     * @param message Message in the IllegalArgumentException.
     */
    public static void notEmpty(Map<String, String> map, String message) {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
