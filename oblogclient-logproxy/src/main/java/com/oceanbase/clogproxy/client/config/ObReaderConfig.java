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


import com.oceanbase.clogproxy.client.util.Validator;
import com.oceanbase.clogproxy.common.config.SharedConf;
import com.oceanbase.clogproxy.common.packet.LogType;
import com.oceanbase.clogproxy.common.util.CryptoUtil;
import com.oceanbase.clogproxy.common.util.Hex;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This is a configuration class for connection to log proxy. */
public class ObReaderConfig extends AbstractConnectionConfig {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ObReaderConfig.class);

    /** Cluster Id. */
    private final ConfigItem<String> clusterId = new ConfigItem<>("cluster_id", "");

    /** Cluster config url. */
    private final ConfigItem<String> clusterUrl = new ConfigItem<>("cluster_url", "");

    /** Root server list. */
    private final ConfigItem<String> rsList = new ConfigItem<>("rootserver_list", "");

    /** Cluster username. */
    private final ConfigItem<String> clusterUser = new ConfigItem<>("cluster_user", "");

    /** Cluster password. */
    private final ConfigItem<String> clusterPassword = new ConfigItem<>("cluster_password", "");

    private final ConfigItem<String> sysUser = new ConfigItem<>("sys_user", "");

    private final ConfigItem<String> sysPassword = new ConfigItem<>("sys_password", "");

    /** Table whitelist. */
    private final ConfigItem<String> tableWhitelist = new ConfigItem<>("tb_white_list", "*.*.*");

    /** Table blacklist. */
    private final ConfigItem<String> tableBlacklist = new ConfigItem<>("tb_black_list", "|");

    /** Start timestamp. */
    private final ConfigItem<Long> startTimestamp = new ConfigItem<>("first_start_timestamp", 0L);

    /** Start timestamp in microsecond. */
    private final ConfigItem<Long> startTimestampUs =
            new ConfigItem<>("first_start_timestamp_us", 0L);

    /** Timezone offset. */
    private final ConfigItem<String> timezone = new ConfigItem<>("timezone", "+8:00");

    /** Working mode. */
    private final ConfigItem<String> workingMode = new ConfigItem<>("working_mode", "storage");

    /** Constructor with empty arguments. */
    public ObReaderConfig() {}

    /**
     * Constructor with a config map.
     *
     * @param allConfigs Config map.
     */
    public ObReaderConfig(Map<String, String> allConfigs) {
        setConfigs(allConfigs);
    }

    @Override
    public LogType getLogType() {
        return LogType.OCEANBASE;
    }

    @Override
    public boolean valid() {
        try {
            if (StringUtils.isEmpty(clusterUrl.val) && StringUtils.isEmpty(rsList.val)) {
                throw new IllegalArgumentException("empty clusterUrl or rsList");
            }
            Validator.notEmpty(clusterUser.val, "invalid clusterUser");
            Validator.notEmpty(clusterPassword.val, "invalid clusterPassword");
            if (startTimestamp.val < 0L) {
                throw new IllegalArgumentException("invalid startTimestamp");
            }
            return true;
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    @Override
    public String generateConfigurationString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, ConfigItem<Object>> entry : configs.entrySet()) {
            String value = entry.getValue().val.toString();
            // Empty `cluster_url` should be discarded, otherwise the server will
            // use it as a valid value by mistake.
            if (clusterUrl.key.equals(entry.getKey()) && StringUtils.isEmpty(value)) {
                continue;
            }
            if (sysUser.key.equals(entry.getKey()) && StringUtils.isEmpty(value)) {
                continue;
            }
            if (sysPassword.key.equals(entry.getKey()) && StringUtils.isEmpty(value)) {
                continue;
            }
            if (clusterPassword.key.equals(entry.getKey()) && SharedConf.AUTH_PASSWORD_HASH) {
                value = Hex.str(CryptoUtil.sha1(value));
            }
            sb.append(entry.getKey()).append("=").append(value).append(" ");
        }

        for (Map.Entry<String, String> entry : extraConfigs.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(" ");
        }
        return sb.toString();
    }

    @Override
    public Map<String, String> generateConfigurationMap(boolean encryptPassword) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, ConfigItem<Object>> entry : configs.entrySet()) {
            String value = entry.getValue().val.toString();
            // Empty `cluster_url` should be discarded, otherwise the server will
            // use it as a valid value by mistake.
            if (clusterUrl.key.equals(entry.getKey()) && StringUtils.isEmpty(value)) {
                continue;
            }
            if (sysUser.key.equals(entry.getKey()) && StringUtils.isEmpty(value)) {
                continue;
            }
            if (sysPassword.key.equals(entry.getKey()) && StringUtils.isEmpty(value)) {
                continue;
            }
            if (encryptPassword
                    && clusterPassword.key.equals(entry.getKey())
                    && SharedConf.AUTH_PASSWORD_HASH) {
                value = Hex.str(CryptoUtil.sha1(value));
            }
            result.put(entry.getKey(), value);
        }
        result.putAll(extraConfigs);
        return result;
    }

    @Override
    public void updateCheckpoint(String checkpoint) {
        try {
            long timestamp = Long.parseLong(checkpoint);
            if (timestamp < 0) {
                throw new IllegalArgumentException(
                        "update checkpoint with invalid value: " + checkpoint);
            }
            startTimestamp.set(timestamp);
        } catch (NumberFormatException e) {
            // do nothing
        }
    }

    @Override
    public String toString() {
        return (StringUtils.isNotEmpty(clusterUrl.val))
                ? ("cluster_url=" + clusterUrl)
                : ("rootserver_list=" + rsList)
                        + ", cluster_id="
                        + clusterId
                        + ", cluster_user="
                        + clusterUser
                        + ", cluster_password=******, "
                        + ", sys_user="
                        + sysUser
                        + ", sys_password=******, "
                        + "tb_white_list="
                        + tableWhitelist
                        + ", tb_black_list="
                        + tableBlacklist
                        + ", start_timestamp="
                        + startTimestamp
                        + ", start_timestamp_us="
                        + startTimestampUs
                        + ", timezone="
                        + timezone
                        + ", working_mode="
                        + workingMode;
    }

    /**
     * Set cluster id.
     *
     * @param clusterId Cluster Id.
     */
    public void setClusterId(String clusterId) {
        this.clusterId.set(clusterId);
    }

    /**
     * Set cluster config url.
     *
     * @param clusterUrl Cluster config url.
     */
    public void setClusterUrl(String clusterUrl) {
        this.clusterUrl.set(clusterUrl);
    }

    /**
     * Set root server list.
     *
     * @param rsList Root server list.
     */
    public void setRsList(String rsList) {
        this.rsList.set(rsList);
    }

    /**
     * Set cluster username
     *
     * @param clusterUser Cluster username.
     */
    public void setUsername(String clusterUser) {
        this.clusterUser.set(clusterUser);
    }

    /**
     * Set cluster password
     *
     * @param clusterPassword Cluster password.
     */
    public void setPassword(String clusterPassword) {
        this.clusterPassword.set(clusterPassword);
    }

    /**
     * Set cluster sys username
     *
     * @param sysUsername Cluster sys username.
     */
    public void setSysUsername(String sysUsername) {
        this.sysUser.set(sysUsername);
    }

    /**
     * Set cluster sys password
     *
     * @param sysPassword Cluster sys password.
     */
    public void setSysPassword(String sysPassword) {
        this.sysPassword.set(sysPassword);
    }

    /**
     * Set table whitelist. It is composed of three dimensions: tenant, db and table. Pattern
     * matching is provided by `fnmatch`, so asterisk means any, for example: "A.foo.bar",
     * "B.foo.*", "C.*.*", "*.*.*".
     *
     * @param tableWhiteList Table whitelist.
     */
    public void setTableWhiteList(String tableWhiteList) {
        tableWhitelist.set(tableWhiteList);
    }

    /**
     * Set table blacklist, the format is same with table whitelist.
     *
     * @param tableBlackList Table blacklist.
     */
    public void setTableBlackList(String tableBlackList) {
        tableBlacklist.set(tableBlackList);
    }

    /**
     * Set start timestamp, zero means from now on.
     *
     * @param startTimestamp Start timestamp.
     */
    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp.set(startTimestamp);
    }

    public void setStartTimestampUs(Long startTimestampUs) {
        this.startTimestampUs.set(startTimestampUs);
    }

    /**
     * Set the timezone which is used to convert timestamp column.
     *
     * @param timezone Timezone offset from UTC, the value is `+8:00` by default.
     */
    public void setTimezone(String timezone) {
        this.timezone.set(timezone);
    }

    /**
     * Set working mode.
     *
     * @param workingMode Working mode, can be 'memory' or 'storage'.
     */
    public void setWorkingMode(String workingMode) {
        this.workingMode.set(workingMode);
    }
}
