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
import com.oceanbase.clogproxy.client.enums.ErrorCode;
import com.oceanbase.clogproxy.client.exception.LogProxyClientException;
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
     * @param sslContext The {@link SslContext} used for encrypted communication.
     * @return A {@link Bootstrap} instance.
     */
    private Bootstrap initBootstrap(SslContext sslContext) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(WORKER_GROUP)
                .channel(NettyEventLoopUtil.getClientSocketChannelClass())
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);

        bootstrap.handler(
                new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) {
                        if (sslContext != null) {
                            ch.pipeline().addFirst(sslContext.newHandler(ch.alloc()));
                        }
                        ch.pipeline()
                                .addLast(new IdleStateHandler(ClientConf.IDLE_TIMEOUT_S, 0, 0));
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
        Bootstrap bootstrap = initBootstrap(context.getSslContext());
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ClientConf.CONNECT_TIMEOUT_MS);
        ChannelFuture channelFuture =
                bootstrap.connect(
                        new InetSocketAddress(
                                context.getParams().getHost(), context.getParams().getPort()));
        channelFuture.channel().attr(CONTEXT_KEY).set(context);
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
