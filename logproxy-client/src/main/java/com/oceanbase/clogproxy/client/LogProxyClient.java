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

/**
 * A client that makes it easy to connect to log proxy and start a {@link ClientStream}.
 */
public class LogProxyClient {

    /**
     * a {@link ClientStream}
     */
    private final ClientStream stream;

    /**
     * Create a {@link LogProxyClient}
     *
     * @param host       log proxy hostname name or ip
     * @param port       log proxy port
     * @param config     {@link AbstractConnectionConfig} used to create the {@link ClientStream}
     * @param sslContext {@link SslContext} to create netty handler
     */
    public LogProxyClient(String host, int port, AbstractConnectionConfig config, SslContext sslContext) {
        try {
            Validator.notNull(config.getLogType(), "log type cannot be null");
            Validator.notEmpty(host, "server cannot be null");
            Validator.validatePort(port, "port is not valid");
        } catch (Exception e) {
            throw new IllegalArgumentException("Illegal argument for LogProxyClient");
        }
        if (!config.valid()) {
            throw new IllegalArgumentException("Illegal argument for LogProxyClient");
        }
        String clientId = ClientConf.USER_DEFINED_CLIENTID.isEmpty() ? ClientIdGenerator.generate() : ClientConf.USER_DEFINED_CLIENTID;
        ConnectionParams connectionParams = new ConnectionParams(config.getLogType(), clientId, host, port, config);
        connectionParams.setProtocolVersion(ProtocolVersion.V2);
        this.stream = new ClientStream(connectionParams, sslContext);
    }

    /**
     * Create a {@link LogProxyClient} without {@link SslContext}
     *
     * @param host   log proxy hostname name or ip
     * @param port   log proxy port
     * @param config {@link AbstractConnectionConfig} used to create the {@link ClientStream}
     */
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

    /**
     * Add a {@link RecordListener} to {@link ClientStream}
     *
     * @param recordListener a {@link RecordListener}
     */
    public synchronized void addListener(RecordListener recordListener) {
        stream.addListener(recordListener);
    }

    /**
     * Add a {@link StatusListener} to {@link ClientStream}
     *
     * @param statusListener a {@link StatusListener}
     */
    public synchronized void addStatusListener(StatusListener statusListener) {
        stream.addStatusListener(statusListener);
    }
}
