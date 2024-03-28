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

package com.oceanbase.clogproxy.client.config;


import com.oceanbase.clogproxy.client.util.ClientIdGenerator;
import com.oceanbase.clogproxy.common.config.SharedConf;
import com.oceanbase.clogproxy.common.packet.ProtocolVersion;
import io.netty.handler.ssl.SslContext;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;

/** The class that defines the constants that are used to generate the connection. */
public class ClientConf extends SharedConf implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Queue size for storing records received from log proxy. */
    private final int transferQueueSize;

    /** Connection timeout in milliseconds. */
    private final int connectTimeoutMs;

    /** Reading queue timeout in milliseconds. */
    private final int readWaitTimeMs;

    /** Time to sleep in seconds when retrying. */
    private final int retryIntervalS;

    /** Idle timeout in seconds for netty handler. */
    private final int idleTimeoutS;

    /**
     * Maximum number of retries after disconnect, if not data income lasting {@link #idleTimeoutS},
     * a reconnection will be triggered.
     */
    private final int maxReconnectTimes;

    /** Maximum number of reads, after which data will be discarded. */
    private final int nettyDiscardAfterReads;

    private final int protocolVersion;

    /** User defined client id. */
    private final String clientId;

    /**
     * Ignore unknown or unsupported record type with a warning log instead of throwing an
     * exception.
     */
    private final boolean ignoreUnknownRecordType;

    /** Netty ssl context */
    private final SslContext sslContext;

    private ClientConf(
            int transferQueueSize,
            int connectTimeoutMs,
            int readWaitTimeMs,
            int retryIntervalS,
            int maxReconnectTimes,
            int idleTimeoutS,
            int nettyDiscardAfterReads,
            int protocolVersion,
            String clientId,
            boolean ignoreUnknownRecordType,
            SslContext sslContext) {
        this.transferQueueSize = transferQueueSize;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readWaitTimeMs = readWaitTimeMs;
        this.retryIntervalS = retryIntervalS;
        this.maxReconnectTimes = maxReconnectTimes;
        this.idleTimeoutS = idleTimeoutS;
        this.nettyDiscardAfterReads = nettyDiscardAfterReads;
        this.protocolVersion = protocolVersion;
        this.clientId = clientId;
        this.ignoreUnknownRecordType = ignoreUnknownRecordType;
        this.sslContext = sslContext;
    }

    public int getTransferQueueSize() {
        return transferQueueSize;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int getReadWaitTimeMs() {
        return readWaitTimeMs;
    }

    public int getRetryIntervalS() {
        return retryIntervalS;
    }

    public int getMaxReconnectTimes() {
        return maxReconnectTimes;
    }

    public int getIdleTimeoutS() {
        return idleTimeoutS;
    }

    public int getNettyDiscardAfterReads() {
        return nettyDiscardAfterReads;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public String getClientId() {
        return clientId;
    }

    public boolean isIgnoreUnknownRecordType() {
        return ignoreUnknownRecordType;
    }

    public SslContext getSslContext() {
        return sslContext;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** ClientConf builder with default values. */
    public static class Builder {
        private int transferQueueSize = 20000;
        private int connectTimeoutMs = 5000;
        private int readWaitTimeMs = 2000;
        private int retryIntervalS = 2;
        private int maxReconnectTimes = -1;
        private int idleTimeoutS = 15;
        private int nettyDiscardAfterReads = 16;
        private int protocolVersion = ProtocolVersion.V2.code();
        private String clientId = ClientIdGenerator.generate();
        private boolean ignoreUnknownRecordType = false;
        private SslContext sslContext = null;

        public Builder transferQueueSize(int transferQueueSize) {
            this.transferQueueSize = transferQueueSize;
            return this;
        }

        public Builder connectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
            return this;
        }

        public Builder readWaitTimeMs(int readWaitTimeMs) {
            this.readWaitTimeMs = readWaitTimeMs;
            return this;
        }

        public Builder retryIntervalS(int retryIntervalS) {
            this.retryIntervalS = retryIntervalS;
            return this;
        }

        public Builder maxReconnectTimes(int maxReconnectTimes) {
            this.maxReconnectTimes = maxReconnectTimes;
            return this;
        }

        public Builder idleTimeoutS(int idleTimeoutS) {
            this.idleTimeoutS = idleTimeoutS;
            return this;
        }

        public Builder nettyDiscardAfterReads(int nettyDiscardAfterReads) {
            this.nettyDiscardAfterReads = nettyDiscardAfterReads;
            return this;
        }

        public Builder protocolVersion(int protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder ignoreUnknownRecordType(boolean ignoreUnknownRecordType) {
            this.ignoreUnknownRecordType = ignoreUnknownRecordType;
            return this;
        }

        public Builder sslContext(SslContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public ClientConf build() {
            return new ClientConf(
                    transferQueueSize,
                    connectTimeoutMs,
                    readWaitTimeMs,
                    retryIntervalS,
                    maxReconnectTimes,
                    idleTimeoutS,
                    nettyDiscardAfterReads,
                    protocolVersion,
                    clientId,
                    ignoreUnknownRecordType,
                    sslContext);
        }
    }

    public String getVersion() {
        try {
            return new String(
                            Files.readAllBytes(
                                    Paths.get(
                                            getClass()
                                                    .getResource(
                                                            "com/oceanbase/clogproxy/client/version.txt")
                                                    .toURI())))
                    .trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read project version", e);
        }
    }
}
