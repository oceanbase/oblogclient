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

package com.oceanbase.clogproxy.client.config;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class ObReaderConfigTest {

    private static ObReaderConfig generateTestConfig() {
        ObReaderConfig config = new ObReaderConfig();
        config.setRsList("127.0.0.1:2882:2881");
        config.setUsername("root@test_tenant");
        config.setPassword("password");
        config.setStartTimestamp(0L);
        config.setStartTimestampUs(0L);
        config.setTableWhiteList("test_tenant.test.*");
        config.setTableBlackList("|");
        config.setTimezone("+8:00");
        config.setWorkingMode("storage");
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
        Assert.assertEquals(configMap.size(), 10);
        Assert.assertEquals(configMap, config.generateConfigurationMap(false));
    }
}
