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
     * Set configs.
     *
     * @param allConfigs The map of configurations.
     */
    public void setConfigs(Map<String, String> allConfigs) {
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
