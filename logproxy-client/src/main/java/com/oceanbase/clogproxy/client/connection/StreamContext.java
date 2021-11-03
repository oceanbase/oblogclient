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

import com.oceanbase.clogproxy.client.config.ClientConf;
import com.oceanbase.clogproxy.client.message.LogMessage;
import com.oceanbase.clogproxy.common.packet.HeaderType;
import io.netty.handler.ssl.SslContext;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.oceanbase.clogproxy.common.packet.protocol.LogProxyProto.RuntimeStatus;

/**
 * This class represents the context of client stream.
 */
public class StreamContext {

    public static class TransferPacket {
        /**
         * Packet header type.
         */
        private final HeaderType type;
        /**
         * Log message record.
         */
        private LogMessage record;
        /**
         * Log proxy runtime status.
         */
        private RuntimeStatus status;

        /**
         * Constructor with a {@link LogMessage}.
         *
         * @param record A {@link LogMessage}.
         */
        public TransferPacket(LogMessage record) {
            this.type = HeaderType.DATA_CLIENT;
            this.record = record;
        }

        /**
         * Constructor with a {@link RuntimeStatus}.
         *
         * @param status A {@link RuntimeStatus}.
         */
        public TransferPacket(RuntimeStatus status) {
            this.type = HeaderType.STATUS;
            this.status = status;
        }

        /**
         * Get header type.
         *
         * @return Packet header type.
         */
        public HeaderType getType() {
            return type;
        }

        /**
         * Get the log message record.
         *
         * @return Log message record.
         */
        public LogMessage getRecord() {
            return record;
        }

        /**
         * Get the log proxy runtime status.
         *
         * @return Log proxy runtime status.
         */
        public RuntimeStatus getStatus() {
            return status;
        }
    }

    /**
     * Blocking queue which stores {@link TransferPacket}.
     */
    private final BlockingQueue<TransferPacket> recordQueue = new LinkedBlockingQueue<>(ClientConf.TRANSFER_QUEUE_SIZE);

    /**
     * Client stream.
     */
    private final ClientStream stream;

    /**
     * Connection params.
     */
    ConnectionParams params;

    /**
     * Netty ssl context.
     *
     * @see SslContext
     */
    private final SslContext sslContext;

    /**
     * Constructor of StreamContext.
     *
     * @param stream     Client stream.
     * @param params     Connection params.
     * @param sslContext Netty ssl context.
     */
    public StreamContext(ClientStream stream, ConnectionParams params, SslContext sslContext) {
        this.stream = stream;
        this.params = params;
        this.sslContext = sslContext;
    }

    /**
     * Get connection params.
     *
     * @return Connection params.
     */
    public ConnectionParams getParams() {
        return params;
    }

    /**
     * Get netty ssl context.
     *
     * @return Netty ssl context.
     */
    public SslContext getSslContext() {
        return sslContext;
    }

    /**
     * Get the client stream.
     *
     * @return Client stream.
     */
    public ClientStream stream() {
        return stream;
    }

    /**
     * Get the record queue.
     *
     * @return Record queue.
     */
    public BlockingQueue<TransferPacket> recordQueue() {
        return recordQueue;
    }
}
