/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client;

import com.oceanbase.clogproxy.client.config.ObReaderConfig;
import com.oceanbase.clogproxy.client.exception.LogProxyClientException;
import com.oceanbase.clogproxy.client.listener.RecordListener;
import com.oceanbase.clogproxy.client.message.LogMessage;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.junit.Ignore;
import org.junit.Test;

import javax.net.ssl.SSLException;

@Ignore
public class LogProxyClientTest {

    @Test
    public void testLogProxyClient() {
        ObReaderConfig config = new ObReaderConfig();
        config.setRsList("127.0.0.1:2882:2881");
        config.setUsername("root@sys");
        config.setPassword("root@sys");
        config.setStartTimestamp(0L);
        config.setTableWhiteList("sys.test.*");

        LogProxyClient client = new LogProxyClient("127.0.0.1", 2983, config);

        client.addListener(new RecordListener() {

            @Override
            public void notify(LogMessage record) {
                System.out.println(record);
            }

            @Override
            public void onException(LogProxyClientException e) {
                if (e.needStop()) {
                    System.out.println(e.getMessage());
                    client.stop();
                }
            }
        });
        client.start();
        client.join();
    }

    @Test
    public void testLogProxyClientWithSsl() throws SSLException {
        ObReaderConfig config = new ObReaderConfig();
        config.setRsList("127.0.0.1:2882:2881");
        config.setUsername("root@sys");
        config.setPassword("root@sys");
        config.setStartTimestamp(0L);
        config.setTableWhiteList("sys.test.*");

        LogProxyClient client = new LogProxyClient("127.0.0.1", 2983, config, sslContext());

        client.addListener(new RecordListener() {

            @Override
            public void notify(LogMessage record) {
                System.out.println(record);
            }

            @Override
            public void onException(LogProxyClientException e) {
                if (e.needStop()) {
                    System.out.println(e.getMessage());
                    client.stop();
                }
            }
        });
        client.start();
        client.join();
    }

    private SslContext sslContext() throws SSLException {
        return SslContextBuilder.forClient()
                .sslProvider(SslContext.defaultClientProvider())
                .trustManager(this.getClass().getClassLoader().getResourceAsStream("certs/ca.crt"))
                .keyManager(this.getClass().getClassLoader().getResourceAsStream("certs/client.crt"),
                        this.getClass().getClassLoader().getResourceAsStream("certs/client.key"))
                .build();
    }
}
