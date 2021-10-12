/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client.util;

import java.util.Map;

public class Validator {

    private static final int MINIMAL_VALID_PORT = 1;
    private static final int MAXIMAL_VALID_PORT = 65535;

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
    }

    public static void validatePort(int port, String message) {
        if (port < MINIMAL_VALID_PORT || port >= MAXIMAL_VALID_PORT) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(String val, String message) {
        if (val == null || val.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Map<String, String> map, String message) {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
