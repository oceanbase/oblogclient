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


import com.oceanbase.clogproxy.common.util.NetworkUtil;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
