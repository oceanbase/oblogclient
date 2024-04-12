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

package com.oceanbase.samples;

import com.oceanbase.clogproxy.client.LogProxyClient;
import com.oceanbase.clogproxy.client.config.ClientConf;
import com.oceanbase.clogproxy.client.config.ObReaderConfig;
import com.oceanbase.clogproxy.client.exception.LogProxyClientException;
import com.oceanbase.clogproxy.client.listener.RecordListener;
import com.oceanbase.clogproxy.client.util.ClientUtil;
import com.oceanbase.oms.logmessage.LogMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;

public class LogProxyClientSample {

    private static final Logger LOG = LoggerFactory.getLogger(LogProxyClientSample.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        try (InputStream inputStream =
                args.length > 0
                        ? Files.newInputStream(Paths.get(args[0]))
                        : LogProxyClientSample.class
                                .getClassLoader()
                                .getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            LOG.error("Failed to load properties files", e);
            return;
        }
        LOG.info("Loaded properties: {}", properties);

        ObReaderConfig obReaderConfig = new ObReaderConfig();
        obReaderConfig.setUsername(properties.getProperty("username"));
        obReaderConfig.setPassword(properties.getProperty("password"));

        obReaderConfig.setRsList(properties.getProperty("libobcdc.rootservice_list"));
        obReaderConfig.setClusterUrl(properties.getProperty("libobcdc.obconfig_url"));
        obReaderConfig.setTableWhiteList(properties.getProperty("libobcdc.tb_white_list"));
        obReaderConfig.setStartTimestamp(
                Long.parseLong(properties.getProperty("libobcdc.start_timestamp")));
        obReaderConfig.setTimezone(properties.getProperty("libobcdc.timezone"));
        obReaderConfig.setWorkingMode(properties.getProperty("libobcdc.working_mode"));
        LOG.info("ObReaderConfig info: {}", obReaderConfig);

        String clientId = properties.getProperty("logproxy.client_id");
        clientId =
                clientId == null || clientId.isEmpty() ? ClientUtil.generateClientId() : clientId;
        int connectTimeoutMs =
                (int) Duration.parse(properties.getProperty("logproxy.connect_timeout")).toMillis();
        int maxReconnectTimes =
                Integer.parseInt(properties.getProperty("logproxy.max_reconnect_times"));

        ClientConf clientConf =
                ClientConf.builder()
                        .transferQueueSize(1000)
                        .clientId(clientId)
                        .connectTimeoutMs(connectTimeoutMs)
                        .maxReconnectTimes(maxReconnectTimes)
                        .ignoreUnknownRecordType(true)
                        .build();

        LogProxyClient client =
                new LogProxyClient(
                        properties.getProperty("logproxy.host"),
                        Integer.parseInt(properties.getProperty("logproxy.port")),
                        obReaderConfig,
                        clientConf);

        client.addListener(
                new RecordListener() {
                    @Override
                    public void notify(LogMessage logMessage) {
                        LOG.info("Received message: {}", logMessage);
                    }

                    @Override
                    public void onException(LogProxyClientException e) {
                        LOG.error(e.toString());
                    }
                });

        try {
            client.start();
            client.join();
        } finally {
            client.stop();
        }
    }
}
