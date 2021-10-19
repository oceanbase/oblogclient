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

import com.google.common.collect.Maps;
import com.oceanbase.clogproxy.client.util.Validator;
import com.oceanbase.clogproxy.common.config.ShareConf;
import com.oceanbase.clogproxy.common.packet.LogType;
import com.oceanbase.clogproxy.common.util.CryptoUtil;
import com.oceanbase.clogproxy.common.util.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ObReaderConfig extends AbstractConnectionConfig {
    private static final Logger logger = LoggerFactory.getLogger(ObReaderConfig.class);

    private static final ConfigItem<String> RS_LIST = new ConfigItem<>("rootserver_list", "");
    private static final ConfigItem<String> CLUSTER_USER = new ConfigItem<>("cluster_user", "");
    private static final ConfigItem<String> CLUSTER_PASSWORD = new ConfigItem<>("cluster_password", "");
    private static final ConfigItem<String> TABLE_WHITE_LIST = new ConfigItem<>("tb_white_list", "");
    private static final ConfigItem<Long> START_TIMESTAMP = new ConfigItem<>("first_start_timestamp", 0L);

    public ObReaderConfig() {
        super(Maps.newHashMap());
    }

    public ObReaderConfig(Map<String, String> allConfigs) {
        super(allConfigs);
    }

    @Override
    public LogType getLogType() {
        return LogType.OCEANBASE;
    }

    @Override
    public boolean valid() {
        try {
            Validator.notEmpty(RS_LIST.val, "invalid rsList");
            Validator.notEmpty(CLUSTER_USER.val, "invalid clusterUser");
            Validator.notEmpty(CLUSTER_PASSWORD.val, "invalid clusterPassword");
            if (START_TIMESTAMP.val < 0L) {
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
            if (CLUSTER_PASSWORD.key.equals(entry.getKey()) && ShareConf.AUTH_PASSWORD_HASH) {
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
    public void updateCheckpoint(String checkpoint) {
        try {
            START_TIMESTAMP.set(Long.parseLong(checkpoint));
        } catch (NumberFormatException e) {
            // do nothing
        }
    }

    @Override
    public String toString() {
        return "rootserver_list=" + RS_LIST + ", cluster_user=" + CLUSTER_USER + ", cluster_password=******, " +
                "tb_white_list=" + TABLE_WHITE_LIST + ", start_timestamp=" + START_TIMESTAMP;
    }

    /**
     * 设置管控服务列表
     *
     * @param rsList 管控服务列表
     */
    public void setRsList(String rsList) {
        RS_LIST.set(rsList);
    }

    /**
     * 设置连接OB用户名
     *
     * @param clusterUser 用户名
     */
    public void setUsername(String clusterUser) {
        CLUSTER_USER.set(clusterUser);
    }

    /**
     * 设置连接OB密码
     *
     * @param clusterPassword 密码
     */
    public void setPassword(String clusterPassword) {
        CLUSTER_PASSWORD.set(clusterPassword);
    }

    /**
     * 配置过滤规则，由租户.库.表3个维度组成，每一段 * 表示任意，如：A.foo.bar，B.foo.*，C.*.*，*.*.*
     *
     * @param tableWhiteList 监听表的过滤规则
     */
    public void setTableWhiteList(String tableWhiteList) {
        TABLE_WHITE_LIST.set(tableWhiteList);
    }

    /**
     * 设置起始订阅的 UNIX时间戳，0表示从当前，通常不要早于1小时
     *
     * @param startTimestamp 起始时间戳
     */
    public void setStartTimestamp(Long startTimestamp) {
        START_TIMESTAMP.set(startTimestamp);
    }
}
