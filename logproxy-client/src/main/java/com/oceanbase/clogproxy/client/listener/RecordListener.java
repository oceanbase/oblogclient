/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client.listener;


import com.oceanbase.clogproxy.client.exception.LogProxyClientException;
import com.oceanbase.oms.logmessage.LogMessage;

/** This interface defined a kind of listener for record response. */
public interface RecordListener {

    /**
     * Handle the {@link LogMessage}.
     *
     * @param logMessage A {@link LogMessage} instance.
     */
    void notify(LogMessage logMessage);

    /**
     * Handle the exception.
     *
     * @param e An exception.
     */
    void onException(LogProxyClientException e);
}
