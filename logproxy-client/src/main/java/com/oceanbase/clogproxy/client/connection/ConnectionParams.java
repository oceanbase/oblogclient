/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client.connection;


import com.oceanbase.clogproxy.client.config.ConnectionConfig;
import com.oceanbase.clogproxy.common.packet.LogType;
import com.oceanbase.clogproxy.common.packet.ProtocolVersion;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/** This is a configuration class of connection parameters. */
public class ConnectionParams {

    /** Log type. */
    private final LogType logType;

    /** Client id. */
    private final String clientId;

    /** Log proxy host. */
    private final String host;

    /** Log proxy port. */
    private final int port;

    /** Connection config. */
    private final ConnectionConfig connectionConfig;

    /** Generated configuration string. */
    private String configurationString;

    /** Protocol version. */
    private ProtocolVersion protocolVersion;

    /** Flag of whether enable monitor. */
    private boolean enableMonitor;

    /**
     * Constructor.
     *
     * @param logType Log type.
     * @param clientId Client id.
     * @param host Log proxy host.
     * @param port Log proxy port.
     * @param connectionConfig Connection config.
     */
    public ConnectionParams(
            LogType logType,
            String clientId,
            String host,
            int port,
            ConnectionConfig connectionConfig) {
        this.logType = logType;
        this.clientId = clientId;
        this.host = host;
        this.port = port;
        this.connectionConfig = connectionConfig;
        this.configurationString = connectionConfig.generateConfigurationString();
    }

    /**
     * Update checkpoint in connection config.
     *
     * @param checkpoint Checkpoint of the last record put in queue.
     */
    public void updateCheckpoint(String checkpoint) {
        connectionConfig.updateCheckpoint(checkpoint);
        configurationString = connectionConfig.generateConfigurationString();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    /**
     * Get the basic info of connection, which contains client id and connection config.
     *
     * @return A string of client id and connection config.
     */
    public String info() {
        return clientId + ": " + connectionConfig.toString();
    }

    /**
     * Get the log type.
     *
     * @return The log type.
     */
    public LogType getLogType() {
        return logType;
    }

    /**
     * Get the client id.
     *
     * @return The client id.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Get the host of log proxy.
     *
     * @return The host of log proxy.
     */
    public String getHost() {
        return host;
    }

    /**
     * Get the port of log proxy.
     *
     * @return The port of log proxy.
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the generated configuration string.
     *
     * @return The configuration string.
     */
    public String getConfigurationString() {
        return configurationString;
    }

    /**
     * Get the protocol version.
     *
     * @return Protocol version.
     */
    public ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Set the protocol version.
     *
     * @param protocolVersion Protocol version.
     */
    public void setProtocolVersion(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * Get the flag of whether enable monitor.
     *
     * @return The flag of whether enable monitor.
     */
    public boolean isEnableMonitor() {
        return enableMonitor;
    }

    /**
     * Set the flag of whether enable monitor.
     *
     * @param enableMonitor The flag of whether enable monitor.
     */
    public void setEnableMonitor(boolean enableMonitor) {
        this.enableMonitor = enableMonitor;
    }
}
