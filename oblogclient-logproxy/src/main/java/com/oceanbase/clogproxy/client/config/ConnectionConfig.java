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

import java.io.Serializable;
import java.util.Map;

/** This is the interface of connection config. */
public interface ConnectionConfig extends Serializable {

    /**
     * Generate a configuration string from connection parameters.
     *
     * @return The configuration string.
     */
    String generateConfigurationString();

    /**
     * Generate a configuration map from connection parameters.
     *
     * @param encryptPassword The flag of whether encrypt the password.
     * @return The configuration map.
     */
    Map<String, String> generateConfigurationMap(boolean encryptPassword);

    /**
     * Update the checkpoint.
     *
     * @param checkpoint A checkpoint string.
     */
    void updateCheckpoint(String checkpoint);

    /**
     * Overrides {@link Object#toString()} to structure a string.
     *
     * @return The structured string.
     */
    @Override
    String toString();
}
