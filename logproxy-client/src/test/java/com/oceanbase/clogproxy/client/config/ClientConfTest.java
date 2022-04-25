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


import org.junit.Assert;
import org.junit.Test;

public class ClientConfTest {

    @Test
    public void builderTest() {
        ClientConf clientConf = ClientConf.builder().build();
        Assert.assertEquals(clientConf.getTransferQueueSize(), 20000);
        Assert.assertEquals(clientConf.getConnectTimeoutMs(), 5000);
        Assert.assertEquals(clientConf.getReadWaitTimeMs(), 2000);
        Assert.assertEquals(clientConf.getRetryIntervalS(), 2);
        Assert.assertEquals(clientConf.getMaxReconnectTimes(), -1);
        Assert.assertEquals(clientConf.getIdleTimeoutS(), 15);
        Assert.assertEquals(clientConf.getNettyDiscardAfterReads(), 16);
        Assert.assertNotNull(clientConf.getClientId());
        Assert.assertFalse(clientConf.isIgnoreUnknownRecordType());
        Assert.assertNull(clientConf.getSslContext());
    }
}
