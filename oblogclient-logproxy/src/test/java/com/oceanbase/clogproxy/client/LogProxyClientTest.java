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
import com.oceanbase.clogproxy.client.exception.LogProxyClientException;
import com.oceanbase.clogproxy.client.listener.RecordListener;
import com.oceanbase.oms.logmessage.DataMessage;
import com.oceanbase.oms.logmessage.LogMessage;
import java.util.stream.Collectors;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class LogProxyClientTest {

    private static final Logger logger = LoggerFactory.getLogger(LogProxyClientTest.class);

    @Test
    public void testLogProxyClient() {
        // Can get it with "show parameters like 'obconfig_url'"
        String clusterUrl =
                "http://127.0.0.1:8080/services"
                        + "?Action=ObRootServiceInfo"
                        + "&User_ID=alibaba"
                        + "&UID=ocpmaster"
                        + "&ObRegion=obcluster";

        // Can get it with "show parameters like 'rootservice_list'"
        String rsList = "127.0.0.1:2882:2881";

        ObReaderConfig config = new ObReaderConfig();

        // Either 'rsList' or 'clusterUrl' should be not empty, will try to use 'clusterUrl' firstly
        config.setRsList(rsList);
        // config.setClusterUrl(clusterUrl);
        config.setUsername("root@test_tenant");
        config.setPassword("pswd");
        config.setStartTimestamp(0L);
        config.setTableWhiteList("test_tenant.test_db.*");
        config.setTableBlackList("test_tenant.test_db.test_table");
        config.setTimezone("+8:00");
        config.setWorkingMode("memory");

        ClientConf clientConf =
                ClientConf.builder()
                        .transferQueueSize(1000)
                        .connectTimeoutMs(3000)
                        .maxReconnectTimes(100)
                        .ignoreUnknownRecordType(true)
                        .clientId("test")
                        .build();

        LogProxyClient client = new LogProxyClient("127.0.0.1", 2983, config, clientConf);

        client.addListener(
                new RecordListener() {

                    @Override
                    public void notify(LogMessage message) {
                        switch (message.getOpt()) {
                            case INSERT:
                            case UPDATE:
                            case DELETE:
                                // note that the db name contains prefix '{tenant}.'
                                logger.info(
                                        "Received log message of type {}: db: {}, table: {}, checkpoint {}",
                                        message.getOpt(),
                                        message.getDbName(),
                                        message.getTableName(),
                                        message.getCheckpoint());
                                // old fields for type 'UPDATE', 'DELETE'
                                logger.info(
                                        "Old field values: {}",
                                        message.getFieldList().stream()
                                                .filter(DataMessage.Record.Field::isPrev)
                                                .collect(Collectors.toList()));
                                // new fields for type 'UPDATE', 'INSERT'
                                logger.info(
                                        "New field values: {}",
                                        message.getFieldList().stream()
                                                .filter(field -> !field.isPrev())
                                                .collect(Collectors.toList()));
                                break;
                            case HEARTBEAT:
                                logger.info(
                                        "Received heartbeat message with checkpoint {}",
                                        message.getCheckpoint());
                                break;
                            case BEGIN:
                            case COMMIT:
                                logger.info("Received transaction message {}", message.getOpt());
                                break;
                            case DDL:
                                logger.info(
                                        "Received log message with DDL: {}",
                                        message.getFieldList().get(0).getValue().toString());
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Unsupported log message type: " + message.getOpt());
                        }
                    }

                    @Override
                    public void onException(LogProxyClientException e) {
                        logger.error(e.getMessage());
                    }
                });
        client.start();
        client.join();
    }
}
