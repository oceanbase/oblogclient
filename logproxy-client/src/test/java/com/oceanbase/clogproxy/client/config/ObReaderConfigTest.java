/*
 *  Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
 * oblogclient is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.oceanbase.clogproxy.client.config;


import java.io.*;
import java.util.Collections;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class ObReaderConfigTest {

    private static ObReaderConfig generateTestConfig() {
        ObReaderConfig config =
                new ObReaderConfig(Collections.singletonMap("working_mode", "storage"));
        config.setRsList("127.0.0.1:2882:2881");
        config.setUsername("root@test_tenant");
        config.setPassword("password");
        config.setStartTimestamp(0L);
        config.setTableWhiteList("test_tenant.test.*");
        config.setTableBlackList("|");
        config.setTimezone("+8:00");
        return config;
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        ObReaderConfig config = generateTestConfig();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
        outputStream.writeObject(config);
        outputStream.flush();
        outputStream.close();
        ObjectInputStream inputStream =
                new ObjectInputStream(
                        new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        Object object = inputStream.readObject();

        Assert.assertTrue(object instanceof ObReaderConfig);
        Map<String, String> configMap = ((ObReaderConfig) object).generateConfigurationMap(false);
        Assert.assertEquals(configMap.size(), 8);
        Assert.assertEquals(configMap, config.generateConfigurationMap(false));
    }
}
