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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class Connection {

    private static final Logger logger = LoggerFactory.getLogger(Connection.class);

    private Channel channel;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public Connection(Channel channel) {
        this.channel = channel;
    }

    public void close() {
        if (!closed.compareAndSet(false, true)) {
            logger.warn("connection already closed");
        }
        if (channel != null) {
            if (channel.isActive()) {
                try {
                    channel.close().addListener(this::logCloseResult).syncUninterruptibly();
                } catch (Exception e) {
                    logger.warn("close connection to remote address {} exception",
                            NetworkUtil.parseRemoteAddress(channel), e);
                }
            }
            channel = null;
        }
    }

    private void logCloseResult(Future future) {
        if (future.isSuccess()) {
            if (logger.isInfoEnabled()) {
                logger.info("close connection to remote address {} success", NetworkUtil.parseRemoteAddress(channel));
            }
        } else {
            logger.warn("close connection to remote address {} fail", NetworkUtil.parseRemoteAddress(channel), future.cause());
        }
    }
}
