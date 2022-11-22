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
