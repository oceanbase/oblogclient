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

package com.oceanbase.clogproxy.client.util;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.concurrent.ThreadFactory;

/** Utils class for netty. */
public class NettyEventLoopUtil {

    /** Check whether epoll enabled, and it would not be changed during runtime. */
    private static final boolean EPOLL_ENABLED = Epoll.isAvailable();

    /**
     * Create a new {@link EventLoopGroup} according to current platform and system property,
     * fallback to NIO when epoll not enabled.
     *
     * @param nThreads Number of threads.
     * @param threadFactory A {@link ThreadFactory} instance.
     * @return An {@link EventLoopGroup} instance.
     */
    public static EventLoopGroup newEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        return EPOLL_ENABLED
                ? new EpollEventLoopGroup(nThreads, threadFactory)
                : new NioEventLoopGroup(nThreads, threadFactory);
    }

    /**
     * Get the suitable {@link SocketChannel} class according to current platform and system
     * property.
     *
     * @return A {@link SocketChannel} implementation class.
     */
    public static Class<? extends SocketChannel> getClientSocketChannelClass() {
        return EPOLL_ENABLED ? EpollSocketChannel.class : NioSocketChannel.class;
    }
}
