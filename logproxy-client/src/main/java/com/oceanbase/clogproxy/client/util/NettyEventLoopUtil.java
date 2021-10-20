/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client.util;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.ThreadFactory;

/**
 * Utils class for netty.
 */
public class NettyEventLoopUtil {

    /**
     * check whether epoll enabled, and it would not be changed during runtime.
     */
    private static final boolean EPOLL_ENABLED = Epoll.isAvailable();

    /**
     * Create the right event loop according to current platform and system property, fallback to NIO when epoll not enabled.
     *
     * @param nThreads      number of threads
     * @param threadFactory ThreadFactory
     * @return an EventLoopGroup suitable for the current platform
     */
    public static EventLoopGroup newEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        return EPOLL_ENABLED ? new EpollEventLoopGroup(nThreads, threadFactory)
            : new NioEventLoopGroup(nThreads, threadFactory);
    }

    /**
     * @return a SocketChannel class suitable for the given EventLoopGroup implementation
     */
    public static Class<? extends SocketChannel> getClientSocketChannelClass() {
        return EPOLL_ENABLED ? EpollSocketChannel.class : NioSocketChannel.class;
    }
}
