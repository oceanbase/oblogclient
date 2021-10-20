/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client.config;

import com.oceanbase.clogproxy.common.packet.LogType;
import com.oceanbase.clogproxy.common.util.TypeTrait;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This is an abstract implementation class of the interface {@link ConnectionConfig}.
 */
public abstract class AbstractConnectionConfig implements ConnectionConfig {

    /**
     * defined structure configurations
     */
    protected static Map<String, ConfigItem<Object>> configs = new HashMap<>();

    /**
     * extra configurations
     */
    protected final Map<String, String> extraConfigs = new HashMap<>();

    @SuppressWarnings("unchecked")
    protected static class ConfigItem<T> {
        protected String key;
        protected T val;

        public ConfigItem(String key, T val) {
            this.key = key;
            this.val = val;
            configs.put(key, (ConfigItem<Object>) this);
        }

        public void set(T val) {
            this.val = val;
        }

        public void fromString(String val) {
            this.val = TypeTrait.fromString(val, this.val.getClass());
        }

        @Override
        public String toString() {
            return val.toString();
        }
    }

    public AbstractConnectionConfig(Map<String, String> allConfigs) {
        if (allConfigs != null) {
            for (Entry<String, String> entry : allConfigs.entrySet()) {
                if (!configs.containsKey(entry.getKey())) {
                    extraConfigs.put(entry.getKey(), entry.getValue());
                } else {
                    set(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public abstract LogType getLogType();

    public void setExtraConfigs(Map<String, String> extraConfigs) {
        this.extraConfigs.putAll(extraConfigs);
    }

    void set(String key, String val) {
        ConfigItem<Object> cs = configs.get(key);
        if (cs != null) {
            cs.fromString(val);
        }
    }

    /**
     * validate if defined configurations
     *
     * @return True or False
     */
    public abstract boolean valid();
}
