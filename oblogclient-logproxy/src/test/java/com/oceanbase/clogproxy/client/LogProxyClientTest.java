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

package com.oceanbase.clogproxy.client;

import com.oceanbase.clogproxy.client.config.ClientConf;
import com.oceanbase.clogproxy.client.config.ObReaderConfig;
import com.oceanbase.clogproxy.client.enums.ErrorCode;
import com.oceanbase.clogproxy.client.exception.LogProxyClientException;
import com.oceanbase.clogproxy.client.listener.RecordListener;
import com.oceanbase.oms.logmessage.DataMessage;
import com.oceanbase.oms.logmessage.LogMessage;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class LogProxyClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(LogProxyClientTest.class);

    private static final String LOG_PROXY_HOST = "localhost";
    private static final int LOG_PROXY_PORT = 2983;
    private static final String RS_LIST = "127.0.0.1:2882:2881";
    private static final String TEST_TENANT = "test";
    private static final String TEST_USERNAME = "root@" + TEST_TENANT;
    private static final String SYS_PASSWORD = "sys_password";
    private static final String TEST_PASSWORD = "test_password";
    private static final String TEST_DATABASE = "test";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);

    @ClassRule
    public static final GenericContainer<?> OB_SERVER =
            new GenericContainer<>("oceanbase/oceanbase-ce:4.2.0.0")
                    .withNetworkMode("host")
                    .withEnv("MODE", "slim")
                    .withEnv("OB_ROOT_PASSWORD", SYS_PASSWORD)
                    .withEnv("OB_DATAFILE_SIZE", "1G")
                    .withEnv("OB_LOG_DISK_SIZE", "4G")
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("sql/docker_init.sql"),
                            "/root/boot/init.d/init.sql")
                    .waitingFor(Wait.forLogMessage(".*boot success!.*", 1))
                    .withStartupTimeout(Duration.ofMinutes(4))
                    .withLogConsumer(new Slf4jLogConsumer(LOG));

    @ClassRule
    public static final GenericContainer<?> LOG_PROXY =
            new GenericContainer<>("whhe/oblogproxy:1.1.3_4x")
                    .withNetworkMode("host")
                    .withEnv("OB_SYS_PASSWORD", SYS_PASSWORD)
                    .waitingFor(Wait.forLogMessage(".*boot success!.*", 1))
                    .withStartupTimeout(Duration.ofMinutes(1))
                    .withLogConsumer(new Slf4jLogConsumer(LOG));

    @Test
    public void testLogProxyClient() throws Exception {
        String table = "t_product";

        BlockingQueue<LogMessage> messageQueue = new LinkedBlockingQueue<>(4);
        AtomicBoolean started = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        LogProxyClient client = client();
        client.addListener(
                new RecordListener() {

                    @Override
                    public void notify(LogMessage message) {
                        switch (message.getOpt()) {
                            case HEARTBEAT:
                                LOG.info(
                                        "Received heartbeat with checkpoint {}",
                                        message.getCheckpoint());
                                if (started.compareAndSet(false, true)) {
                                    latch.countDown();
                                }
                                break;
                            case BEGIN:
                                LOG.info("Received transaction begin: {}", message);
                                break;
                            case COMMIT:
                                LOG.info("Received transaction commit: {}", message);
                                break;
                            case INSERT:
                            case UPDATE:
                            case DELETE:
                            case DDL:
                                try {
                                    messageQueue.put(message);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException("Failed to add message to queue", e);
                                }
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Unsupported log message type: " + message.getOpt());
                        }
                    }

                    @Override
                    public void onException(LogProxyClientException e) {
                        Assert.fail("Got exception: " + e.getMessage());
                    }
                });
        client.start();

        if (!latch.await(CONNECT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
            Assert.fail("Timeout to receive heartbeat message");
        }

        String ddl = "CREATE TABLE t_product (id INT(10) PRIMARY KEY, name VARCHAR(20))";

        try (Connection connection =
                        DriverManager.getConnection(
                                "jdbc:oceanbase://" + OB_SERVER.getHost() + ":2881/test",
                                "root@test",
                                TEST_PASSWORD);
                Statement statement = connection.createStatement()) {
            statement.execute(ddl);
            statement.execute("INSERT INTO t_product VALUES (1, 'meat')");
            statement.execute("UPDATE t_product SET name = 'water' WHERE id = 1");
            statement.execute("DELETE FROM t_product WHERE id = 1");
        }

        while (messageQueue.size() < 4) {
            Thread.sleep(1000);
        }

        LogMessage message = messageQueue.take();
        Assert.assertEquals(message.getOpt(), DataMessage.Record.Type.DDL);
        Assert.assertEquals(message.getFieldList().get(0).getValue().toString(), ddl);

        verify(
                messageQueue.take(),
                DataMessage.Record.Type.INSERT,
                table,
                Collections.emptyMap(),
                new HashMap<String, String>() {
                    {
                        put("id", "1");
                        put("name", "meat");
                    }
                });

        verify(
                messageQueue.take(),
                DataMessage.Record.Type.UPDATE,
                table,
                new HashMap<String, String>() {
                    {
                        put("id", "1");
                        put("name", "meat");
                    }
                },
                new HashMap<String, String>() {
                    {
                        put("id", "1");
                        put("name", "water");
                    }
                });

        verify(
                messageQueue.take(),
                DataMessage.Record.Type.DELETE,
                table,
                new HashMap<String, String>() {
                    {
                        put("id", "1");
                        put("name", "water");
                    }
                },
                Collections.emptyMap());

        client.stop();
    }

    @Test
    public void testLogProxyClientOnException() throws Exception {
        String exceptionMessage = "Something is going wrong";

        final AtomicReference<LogProxyClientException> exception = new AtomicReference<>();

        LogProxyClient client = client();
        client.addListener(
                new RecordListener() {

                    @Override
                    public void notify(LogMessage logMessage) {
                        throw new RuntimeException(exceptionMessage);
                    }

                    @Override
                    public void onException(LogProxyClientException e) {
                        try {
                            // assume the exception handler takes a long time
                            Thread.sleep(5000L);
                        } catch (InterruptedException interruptedException) {
                            Assert.fail(interruptedException.getMessage());
                        }
                        exception.set(e);
                    }
                });

        client.start();
        Assert.assertNull(exception.get());

        client.join();

        LogProxyClientException clientException = exception.get();
        Assert.assertNotNull(clientException);

        LOG.info("Caught exception: {}", clientException.toString());
        Assert.assertEquals(clientException.getMessage(), exceptionMessage);
    }

    @Test
    public void testLogProxyClientReconnect() {
        int maxReconnectTimes = 3;

        ClientConf clientConf =
                ClientConf.builder()
                        .transferQueueSize(10)
                        .connectTimeoutMs(1000)
                        .maxReconnectTimes(maxReconnectTimes)
                        .ignoreUnknownRecordType(true)
                        .build();

        LogProxyClient client =
                new LogProxyClient("invalid_host", LOG_PROXY_PORT, config(), clientConf);

        final AtomicReference<LogProxyClientException> exception = new AtomicReference<>();

        client.addListener(
                new RecordListener() {
                    @Override
                    public void notify(LogMessage logMessage) {}

                    @Override
                    public void onException(LogProxyClientException e) {
                        exception.set(e);
                    }
                });

        client.start();
        client.join();

        LogProxyClientException clientException = exception.get();
        Assert.assertNotNull(clientException);

        LOG.info("Caught exception: {}", clientException.toString());
        Assert.assertEquals(clientException.getCode(), ErrorCode.E_MAX_RECONNECT);
    }

    private LogProxyClient client() {
        return new LogProxyClient(LOG_PROXY_HOST, LOG_PROXY_PORT, config(), clientConf());
    }

    private ObReaderConfig config() {
        ObReaderConfig config = new ObReaderConfig();
        config.setRsList(RS_LIST);
        config.setUsername(TEST_USERNAME);
        config.setPassword(TEST_PASSWORD);
        config.setStartTimestamp(0L);
        config.setTableWhiteList(TEST_TENANT + "." + TEST_DATABASE + ".*");
        config.setTimezone("+08:00");
        config.setWorkingMode("memory");
        return config;
    }

    private ClientConf clientConf() {
        return ClientConf.builder()
                .transferQueueSize(1000)
                .connectTimeoutMs((int) CONNECT_TIMEOUT.toMillis())
                .maxReconnectTimes(0)
                .ignoreUnknownRecordType(true)
                .build();
    }

    private void verify(
            LogMessage message,
            DataMessage.Record.Type type,
            String table,
            Map<String, String> before,
            Map<String, String> after) {
        Assert.assertEquals(message.getOpt(), type);
        Assert.assertEquals(message.getDbName(), TEST_TENANT + "." + TEST_DATABASE);
        Assert.assertEquals(message.getTableName(), table);

        for (DataMessage.Record.Field field : message.getFieldList()) {
            String expected =
                    field.isPrev()
                            ? before.get(field.getFieldname())
                            : after.get(field.getFieldname());
            String actual = field.getValue() == null ? null : field.getValue().toString();
            Assert.assertEquals(expected, actual);
        }
    }
}
