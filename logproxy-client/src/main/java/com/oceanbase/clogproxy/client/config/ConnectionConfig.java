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

/**
 * This is the interface of connection config.
 */
public interface ConnectionConfig {

    /**
     * Generate a configuration string from connection parameters.
     *
     * @return the configuration string
     */
    String generateConfigurationString();

    /**
     * Update the checkpoint.
     *
     * @param checkpoint a checkpoint string
     */
    void updateCheckpoint(String checkpoint);

    /**
     * Overrides {@link Object#toString()} to structure a string.
     *
     * @return the structured string
     */
    @Override
    String toString();
}
