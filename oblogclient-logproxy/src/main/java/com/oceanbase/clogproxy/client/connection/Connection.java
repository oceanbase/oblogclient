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

import com.oceanbase.clogproxy.common.util.NetworkUtil;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/** This class represents a connection which contains a netty channel. */
public class Connection {

    private static final Logger logger = LoggerFactory.getLogger(Connection.class);

    /** A netty channel. */
    private Channel channel;

    /** A flag of whether the channel is closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Sole constructor.
     *
     * @param channel A netty channel.
     */
    public Connection(Channel channel) {
        this.channel = channel;
    }

    /** Close this connection. */
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            logger.warn("Connection already closed");
        }
        if (channel != null) {
            if (channel.isActive()) {
                try {
                    channel.close().addListener(this::logCloseResult).syncUninterruptibly();
                } catch (Exception e) {
                    logger.warn(
                            "Close connection to remote address {} exception",
                            NetworkUtil.parseRemoteAddress(channel),
                            e);
                }
            }
            channel = null;
        }
    }

    /**
     * A callback that will logging the result of {@link Channel#close()}.
     *
     * @param future The source {@link Future} which called this callback.
     */
    @SuppressWarnings("rawtypes")
    private void logCloseResult(Future future) {
        if (future.isSuccess()) {
            if (logger.isInfoEnabled()) {
                logger.info(
                        "Close connection to remote address {} success",
                        NetworkUtil.parseRemoteAddress(channel));
            }
        } else {
            logger.warn(
                    "Close connection to remote address {} fail",
                    NetworkUtil.parseRemoteAddress(channel),
                    future.cause());
        }
    }
}
