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


import com.oceanbase.clogproxy.client.util.ClientIdGenerator;
import com.oceanbase.clogproxy.common.config.SharedConf;
import io.netty.handler.ssl.SslContext;
import java.io.Serializable;

/** The class that defines the constants that are used to generate the connection. */
public class ClientConf extends SharedConf implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Client version. */
    public static final String VERSION = "1.0.7";

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
                    clientId,
                    ignoreUnknownRecordType,
                    sslContext);
        }
    }
}
