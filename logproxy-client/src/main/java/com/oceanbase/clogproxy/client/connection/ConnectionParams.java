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


public class ConnectionParams {
    private final LogType logType;
    private final String clientId;
    private final String host;
    private final int port;

    private final ConnectionConfig connectionConfig;
    private String configurationString;

    private ProtocolVersion protocolVersion;
    private boolean enableMonitor;

    public ConnectionParams(LogType logType, String clientId, String host, int port, ConnectionConfig connectionConfig) {
        this.logType = logType;
        this.clientId = clientId;
        this.host = host;
        this.port = port;
        this.connectionConfig = connectionConfig;
        this.configurationString = connectionConfig.generateConfigurationString();
    }

    public void updateCheckpoint(String checkpoint) {
        connectionConfig.updateCheckpoint(checkpoint);
        configurationString = connectionConfig.generateConfigurationString();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String info() {
        return clientId + ": " + connectionConfig.toString();
    }

    public LogType getLogType() {
        return logType;
    }

    public String getClientId() {
        return clientId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getConfigurationString() {
        return configurationString;
    }

    public ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public boolean isEnableMonitor() {
        return enableMonitor;
    }

    public void setEnableMonitor(boolean enableMonitor) {
        this.enableMonitor = enableMonitor;
    }
}
