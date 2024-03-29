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

import com.oceanbase.clogproxy.common.util.NetworkUtil;

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Objects;
import java.util.Scanner;

/** The class used to generate client id. */
public class ClientUtil {

    public static String getClientVersion() {
        try (InputStream inputStream =
                ClientUtil.class.getResourceAsStream(
                        "/com/oceanbase/clogproxy/client/version.txt")) {
            return new Scanner(Objects.requireNonNull(inputStream))
                    .useDelimiter(System.lineSeparator())
                    .next()
                    .trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read project version", e);
        }
    }

    /**
     * Generate a new client id in format "LocalIP"."PID"."currentTimestamp". Pattern may be
     * changed, never depend on the content of this.
     *
     * @return Client id string.
     */
    public static String generateClientId() {
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
