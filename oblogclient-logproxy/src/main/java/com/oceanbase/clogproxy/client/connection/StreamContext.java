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

package com.oceanbase.clogproxy.client.connection;

import static com.oceanbase.clogproxy.common.packet.protocol.LogProxyProto.RuntimeStatus;

import com.oceanbase.clogproxy.client.config.ClientConf;
import com.oceanbase.clogproxy.common.packet.HeaderType;
import com.oceanbase.oms.logmessage.LogMessage;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/** This class represents the context of client stream. */
public class StreamContext {

    public static class TransferPacket {
        /** Packet header type. */
        protected final HeaderType type;
        /** Log message record. */
        protected LogMessage record;
        /** Log proxy runtime status. */
        protected RuntimeStatus status;

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

    /** Blocking queue which stores {@link TransferPacket}. */
    private final BlockingQueue<TransferPacket> recordQueue;

    /** Client stream. */
    private final ClientStream stream;

    /** Client config. */
    private final ClientConf config;

    /** Connection params. */
    private final ConnectionParams params;

    /**
     * Constructor of StreamContext.
     *
     * @param stream Client stream.
     * @param config Client config.
     * @param params Connection params.
     */
    public StreamContext(ClientStream stream, ClientConf config, ConnectionParams params) {
        this.stream = Objects.requireNonNull(stream);
        this.config = Objects.requireNonNull(config);
        this.params = Objects.requireNonNull(params);
        this.recordQueue = new LinkedBlockingQueue<>(config.getTransferQueueSize());
    }

    /**
     * Get connection params.
     *
     * @return Connection params.
     */
    public ConnectionParams params() {
        return params;
    }

    /**
     * Get client config.
     *
     * @return Client config.
     */
    public ClientConf config() {
        return config;
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
