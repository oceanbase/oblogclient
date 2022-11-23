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
import com.oceanbase.clogproxy.client.util.Validator;
import com.oceanbase.clogproxy.common.packet.ProtocolVersion;

/** A client that makes it easy to connect to log proxy and start a {@link ClientStream}. */
public class LogProxyClient {

    /** A {@link ClientStream} instance. */
    private final ClientStream stream;

    /**
     * Constructor with {@link ClientConf}.
     *
     * @param host Log proxy hostname name or ip.
     * @param port Log proxy port.
     * @param config {@link AbstractConnectionConfig} used to create the {@link ClientStream}.
     * @param clientConf {@link ClientConf} used to create netty handler.
     */
    public LogProxyClient(
            String host, int port, AbstractConnectionConfig config, ClientConf clientConf) {
        try {
            Validator.notNull(config.getLogType(), "log type cannot be null");
            Validator.notEmpty(host, "server cannot be null");
            Validator.validatePort(port, "port is not valid");
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Illegal argument for LogProxyClient: " + e.getMessage());
        }
        if (!config.valid()) {
            throw new IllegalArgumentException("Illegal argument for LogProxyClient");
        }
        if (clientConf == null) {
            clientConf = ClientConf.builder().build();
        }

        String clientId = clientConf.getClientId();
        ConnectionParams connectionParams =
                new ConnectionParams(config.getLogType(), clientId, host, port, config);
        connectionParams.setProtocolVersion(
                ProtocolVersion.codeOf(clientConf.getProtocolVersion()));
        this.stream = new ClientStream(clientConf, connectionParams);
    }

    /**
     * Constructor without {@link ClientConf}.
     *
     * @param host Log proxy hostname name or ip.
     * @param port Log proxy port.
     * @param config {@link AbstractConnectionConfig} used to create the {@link ClientStream}.
     */
    public LogProxyClient(String host, int port, AbstractConnectionConfig config) {
        this(host, port, config, null);
    }

    /** Start the client. */
    public void start() {
        stream.start();
    }

    /** Stop the client. */
    public void stop() {
        stream.stop();
    }

    /** Join and wait the client. */
    public void join() {
        stream.join();
    }

    /**
     * Add a {@link RecordListener} to {@link #stream}.
     *
     * @param recordListener A {@link RecordListener}.
     */
    public synchronized void addListener(RecordListener recordListener) {
        stream.addListener(recordListener);
    }

    /**
     * Add a {@link StatusListener} to {@link #stream}.
     *
     * @param statusListener A {@link StatusListener}.
     */
    public synchronized void addStatusListener(StatusListener statusListener) {
        stream.addStatusListener(statusListener);
    }
}
