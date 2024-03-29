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

package com.oceanbase.clogproxy.common.util;

import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

import java.net.*;
import java.util.Enumeration;

/** Utils class for network. */
public class NetworkUtil {

    /** Local ip. */
    private static String IP;

    static {
        try {
            for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
                    e.hasMoreElements(); ) {
                NetworkInterface item = e.nextElement();
                for (InterfaceAddress address : item.getInterfaceAddresses()) {
                    if (item.isLoopback() || !item.isUp()) {
                        continue;
                    }
                    if (address.getAddress() instanceof Inet4Address) {
                        Inet4Address inet4Address = (Inet4Address) address.getAddress();
                        IP = inet4Address.getHostAddress();
                        break;
                    }
                }
            }
            if (IP.isEmpty()) {
                IP = InetAddress.getLocalHost().getHostAddress();
            }
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get local ip.
     *
     * @return Local ip.
     */
    public static String getLocalIp() {
        return IP;
    }

    /**
     * Parse the remote address of the channel.
     *
     * @param channel A channel
     * @return The address string.
     */
    public static String parseRemoteAddress(final Channel channel) {
        if (null == channel) {
            return StringUtils.EMPTY;
        }
        final SocketAddress remote = channel.remoteAddress();
        return doParse(remote != null ? remote.toString().trim() : StringUtils.EMPTY);
    }

    /**
     * Parse the address with rules:
     *
     * <ol>
     *   <li>If an address starts with a '/', skip it.
     *   <li>If an address contains a '/', substring it.
     * </ol>
     */
    private static String doParse(String addr) {
        if (StringUtils.isBlank(addr)) {
            return StringUtils.EMPTY;
        }
        if (addr.charAt(0) == '/') {
            return addr.substring(1);
        } else {
            int len = addr.length();
            for (int i = 1; i < len; ++i) {
                if (addr.charAt(i) == '/') {
                    return addr.substring(i + 1);
                }
            }
            return addr;
        }
    }
}
