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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/** This is an abstract implementation class of the interface {@link ConnectionConfig}. */
public abstract class AbstractConnectionConfig implements ConnectionConfig {

    /** Defined configurations map. */
    protected final Map<String, ConfigItem<Object>> configs = new HashMap<>();

    /** Extra configurations map. */
    protected final Map<String, String> extraConfigs = new HashMap<>();

    /**
     * This class is used to define configuration with a default value.
     *
     * @param <T> The type of stored value.
     */
    @SuppressWarnings("unchecked")
    protected class ConfigItem<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        protected String key;
        protected T val;

        /**
         * Sole constructor.
         *
         * @param key Config key.
         * @param val Config value.
         */
        public ConfigItem(String key, T val) {
            this.key = key;
            this.val = val;
            configs.put(key, (ConfigItem<Object>) this);
        }

        /**
         * Set value to config item.
         *
         * @param val Value of specific type.
         */
        public void set(T val) {
            this.val = val;
        }

        /**
         * Set value of specific type from string.
         *
         * @param val Value of string type.
         */
        public void fromString(String val) {
            this.val = TypeTrait.fromString(val, this.val.getClass());
        }

        @Override
        public String toString() {
            return val.toString();
        }
    }

    /**
     * Sole constructor.
     *
     * @param allConfigs The map of configurations.
     */
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

    /**
     * Get log type set in configurations.
     *
     * @return The enum constant of {@link LogType}.
     */
    public abstract LogType getLogType();

    /**
     * Add configurations to {@link #extraConfigs}
     *
     * @param extraConfigs A map of configurations.
     */
    public void setExtraConfigs(Map<String, String> extraConfigs) {
        this.extraConfigs.putAll(extraConfigs);
    }

    /**
     * Update value into define configurations map.
     *
     * @param key Config key.
     * @param val New config value.
     */
    void set(String key, String val) {
        ConfigItem<Object> cs = configs.get(key);
        if (cs != null) {
            cs.fromString(val);
        }
    }

    /**
     * Validate defined configurations.
     *
     * @return Flag of whether all the defined configurations are valid.
     */
    public abstract boolean valid();
}
