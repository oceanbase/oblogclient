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

import com.oceanbase.clogproxy.common.config.ShareConf;

public class ClientConf extends ShareConf {
    public static final String VERSION = "1.1.0";

    public static int TRANSFER_QUEUE_SIZE = 20000;
    public static int CONNECT_TIMEOUT_MS = 5000;
    public static int READ_WAIT_TIME_MS = 2000;
    public static int RETRY_INTERVAL_S = 2;
    /**
     * max retry time after disconnect, if not data income lasting IDLE_TIMEOUT_S, a reconnect we be trigger
     */
    public static int MAX_RECONNECT_TIMES = -1;
    public static int IDLE_TIMEOUT_S = 15;
    public static int NETTY_DISCARD_AFTER_READS = 16;
    /**
     * set user defined userid,
     * for inner use only
     */
    public static String USER_DEFINED_CLIENTID = "";

    /**
     * ignore unknown or unsupported record type with a warning log instead throwing an exception
     */
    public static boolean IGNORE_UNKNOWN_RECORD_TYPE = false;
}
