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


import com.oceanbase.clogproxy.common.util.NetworkUtil;
import java.lang.management.ManagementFactory;

/** The class used to generate client id. */
public class ClientIdGenerator {
    /**
     * Generate a new client id in format "LocalIP"."PID"."currentTimestamp". Pattern may be
     * changed, never depend on the content of this.
     *
     * @return Client id string.
     */
    public static String generate() {
        return NetworkUtil.getLocalIp()
                + "_"
                + getProcessId()
                + "_"
                + (System.currentTimeMillis() / 1000);
    }

    /**
     * Get the process id.
     *
     * @return Process id.
     */
    private static String getProcessId() {
        // Note: may fail in some JVM implementations
        // therefore fallback has to be provided

        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');

        if (index < 1) {
            return "NOPID";
        }

        try {
            return Long.toString(Long.parseLong(jvmName.substring(0, index)));
        } catch (NumberFormatException e) {
            return "NOPID";
        }
    }
}
