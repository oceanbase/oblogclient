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

package com.oceanbase.clogproxy.client.listener;


import com.oceanbase.clogproxy.common.packet.protocol.LogProxyProto;

/** This interface defined a kind of listener for {@link LogProxyProto.RuntimeStatus} response. */
public interface StatusListener {
    /**
     * Handle the response of {@link LogProxyProto.RuntimeStatus}.
     *
     * @param status A {@link LogProxyProto.RuntimeStatus} response.
     */
    void notify(LogProxyProto.RuntimeStatus status);
}
