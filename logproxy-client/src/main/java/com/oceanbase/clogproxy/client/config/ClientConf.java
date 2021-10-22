/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client.config;

import com.oceanbase.clogproxy.common.config.SharedConf;

/**
 * The class that defines the constants that are used to generate the connection.
 */
public class ClientConf extends SharedConf {
    /**
     * Client version.
     */
    public static final String VERSION = "1.0.1";

    /**
     * Queue size for storing records received from log proxy.
     */
    public static int TRANSFER_QUEUE_SIZE = 20000;

    /**
     * Connection timeout in milliseconds.
     */
    public static int CONNECT_TIMEOUT_MS = 5000;

    /**
     * Reading queue timeout in milliseconds.
     */
    public static int READ_WAIT_TIME_MS = 2000;

    /**
     * Time to sleep in seconds when retrying.
     */
    public static int RETRY_INTERVAL_S = 2;

    /**
     * Maximum number of retries after disconnect, if not data income lasting {@link #IDLE_TIMEOUT_S}, a reconnection will be triggered.
     */
    public static int MAX_RECONNECT_TIMES = -1;

    /**
     * idle timeout in seconds for netty handler
     */
    public static int IDLE_TIMEOUT_S = 15;

    /**
     * maximum number of reads, after which data will be discarded
     */
    public static int NETTY_DISCARD_AFTER_READS = 16;
    /**
     * user defined client id
     */
    public static String USER_DEFINED_CLIENTID = "";

    /**
     * ignore unknown or unsupported record type with a warning log instead of throwing an exception
     */
    public static boolean IGNORE_UNKNOWN_RECORD_TYPE = false;
}
