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


import com.oceanbase.clogproxy.client.enums.ErrorCode;
import com.oceanbase.clogproxy.client.exception.LogProxyClientException;
import com.oceanbase.clogproxy.client.util.NamedThreadFactory;
import com.oceanbase.clogproxy.client.util.NettyEventLoopUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;

/** This is a factory class of {@link Connection}. */
public class ConnectionFactory {

    /** A static class that holds the singleton instance of {@link ConnectionFactory}. */
    private static class Singleton {

        /** The singleton instance of {@link ConnectionFactory}. */
        private static final ConnectionFactory INSTANCE = new ConnectionFactory();
    }

    /**
     * Get the singleton instance of {@link ConnectionFactory}.
     *
     * @return The singleton instance of {@link ConnectionFactory}.
     */
    public static ConnectionFactory instance() {
        return Singleton.INSTANCE;
    }

    /** Sole constructor. It can only be used in {@link Singleton} class. */
    private ConnectionFactory() {}

    /** Context key. */
    public static final AttributeKey<StreamContext> CONTEXT_KEY = AttributeKey.valueOf("context");

    /** Worker group in type of {@link EventLoopGroup}. */
    private static final EventLoopGroup WORKER_GROUP =
            NettyEventLoopUtil.newEventLoopGroup(
                    1, new NamedThreadFactory("log-proxy-client-worker", true));

    /**
     * Create a {@link Bootstrap} instance.
     *
     * @param context The {@link StreamContext} used in channels.
     * @return A {@link Bootstrap} instance.
     */
    private Bootstrap initBootstrap(StreamContext context) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .attr(CONTEXT_KEY, context)
                .group(WORKER_GROUP)
                .channel(NettyEventLoopUtil.getClientSocketChannelClass())
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);

        SslContext sslContext = context.config().getSslContext();
        bootstrap.handler(
                new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) {
                        if (sslContext != null) {
                            ch.pipeline().addFirst(sslContext.newHandler(ch.alloc()));
                        }
                        ch.pipeline()
                                .addLast(
                                        new IdleStateHandler(
                                                context.config().getIdleTimeoutS(), 0, 0));
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });
        return bootstrap;
    }

    /**
     * Create a {@link Connection} with specific {@link StreamContext}.
     *
     * @param context Stream context.
     * @return A {@link Connection}.
     * @throws LogProxyClientException If exception occurs.
     */
    public Connection createConnection(StreamContext context) throws LogProxyClientException {
        Bootstrap bootstrap = initBootstrap(context);
        bootstrap.option(
                ChannelOption.CONNECT_TIMEOUT_MILLIS, context.config().getConnectTimeoutMs());
        ChannelFuture channelFuture =
                bootstrap.connect(
                        new InetSocketAddress(
                                context.params().getHost(), context.params().getPort()));
        channelFuture.awaitUninterruptibly();
        if (!channelFuture.isDone()) {
            throw new LogProxyClientException(ErrorCode.E_CONNECT, "timeout of create connection!");
        }
        if (channelFuture.isCancelled()) {
            throw new LogProxyClientException(
                    ErrorCode.E_CONNECT, "cancelled by user of create connection!");
        }
        if (!channelFuture.isSuccess()) {
            throw new LogProxyClientException(
                    ErrorCode.E_CONNECT, "failed to create connection!", channelFuture.cause());
        }
        return new Connection(channelFuture.channel());
    }
}
