/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client;

import com.oceanbase.clogproxy.client.config.AbstractConnectionConfig;
import com.oceanbase.clogproxy.client.config.ClientConf;
import com.oceanbase.clogproxy.client.connection.ClientStream;
import com.oceanbase.clogproxy.client.connection.ConnectionParams;
import com.oceanbase.clogproxy.client.listener.RecordListener;
import com.oceanbase.clogproxy.client.listener.StatusListener;
import com.oceanbase.clogproxy.client.util.ClientIdGenerator;
import com.oceanbase.clogproxy.client.util.Validator;
import com.oceanbase.clogproxy.common.packet.ProtocolVersion;
import io.netty.handler.ssl.SslContext;

public class LogProxyClient {

    private final ClientStream stream;

    /**
     * @param host       server hostname name or ip
     * @param port       server port
     * @param config     real config object according to what-you-expected
     * @param sslContext ssl context to create netty handler
     */
    public LogProxyClient(String host, int port, AbstractConnectionConfig config, SslContext sslContext) {
        Validator.notNull(config.getLogType(), "log type cannot be null");
        Validator.notNull(host, "server cannot be null");
        Validator.validatePort(port, "port is not valid");
        String clientId = ClientConf.USER_DEFINED_CLIENTID.isEmpty() ? ClientIdGenerator.generate() : ClientConf.USER_DEFINED_CLIENTID;
        ConnectionParams connectionParams = new ConnectionParams(config.getLogType(), clientId, host, port, config);
        connectionParams.setProtocolVersion(ProtocolVersion.V2);
        this.stream = new ClientStream(connectionParams, sslContext);
    }

    public LogProxyClient(String host, int port, AbstractConnectionConfig config) {
        this(host, port, config, null);
    }

    public void start() {
        stream.start();
    }

    public void stop() {
        stream.stop();
    }

    public void join() {
        stream.join();
    }

    public synchronized void addListener(RecordListener recordListener) {
        stream.addListener(recordListener);
    }

    public synchronized void addStatusListener(StatusListener statusListener) {
        stream.addStatusListener(statusListener);
    }
}
