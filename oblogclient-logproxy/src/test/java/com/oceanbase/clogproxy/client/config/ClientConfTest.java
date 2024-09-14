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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ClientConfTest {

    @Test
    public void testBuilderDefaultValues() {
        ClientConf clientConf = ClientConf.builder().build();
        Assert.assertEquals(clientConf.getTransferQueueSize(), 20000);
        Assert.assertEquals(clientConf.getConnectTimeoutMs(), 60000);
        Assert.assertEquals(clientConf.getReadWaitTimeMs(), 2000);
        Assert.assertEquals(clientConf.getRetryIntervalS(), 2);
        Assert.assertEquals(clientConf.getMaxReconnectTimes(), -1);
        Assert.assertEquals(clientConf.getIdleTimeoutS(), 15);
        Assert.assertEquals(clientConf.getNettyDiscardAfterReads(), 16);
        Assert.assertNotNull(clientConf.getClientId());
        Assert.assertFalse(clientConf.isIgnoreUnknownRecordType());
        Assert.assertNull(clientConf.getSslContext());
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        ClientConf clientConf = ClientConf.builder().build();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
        outputStream.writeObject(clientConf);
        outputStream.flush();
        outputStream.close();
        ObjectInputStream inputStream =
                new ObjectInputStream(
                        new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        Object object = inputStream.readObject();
        Assert.assertTrue(object instanceof ClientConf);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(object, clientConf));
    }
}
