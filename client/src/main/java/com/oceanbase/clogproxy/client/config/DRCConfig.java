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


import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.oceanbase.clogproxy.client.fliter.DataFilterBase;
import com.oceanbase.clogproxy.client.message.Checkpoint;
import com.oceanbase.clogproxy.client.util.StringUtils;

public class DRCConfig {

    public enum DataAcquireOption {
        ASK_SELF_UNIT, ASK_OTHER_UNIT, ASK_ALL_DATA,
    };

    /* Configures used by DRCClient. */
    private final Map<String, String>  configures;

    /* All configures used to send to the server is stored */
    private final Map<String, String>  userDefinedParams;

    /* All persistent-required attributes. */
    private final Set<String>          persists;

    private Checkpoint checkpoint;
    private DataFilterBase filter;
    private String                     blackList;
    private int                        recordsPerBatch           = 0;
    private int                        maxRetryTimes             = 100;
    private int                        socketTimeout             = 10;
    private int                        connectionTimeout         = 10;
    private boolean                    useBinaryFormat           = false;
    private boolean                    txnMark                   = true;
    private boolean                    requireCompleteTxn        = false;
    private int                        maxRecordsCached          = 10240;
    private int                        maxRecordsBatched         = 1024;
    private int                        maxTxnsBatched            = 10240;
    private long                       maxTimeoutBatched         = 500;                                       // ms
    private boolean                    useDrcNet                 = false;
    private boolean                    drcMarkWorking            = false;
    private boolean                    requiredOtherUnitData     = false;
    private boolean                    usePublicIp               = false;

    private boolean                    useCaseSensitive          = false;

    private boolean                    trimLongType              = false;

    private boolean                    useCheckCRC               = false;

    private boolean                    useIndexIterator          = false;

    private boolean                    needUKRecord              = false;

    /* Properties defined for DRCClient, always defined in drc.properties. */
    private final static String        DRC_VERSION               = "version";
    private final static String        DRC_MANAGERHOST           = "manager.host";
    private final static String        HTTPS_USE                 = "client.https";
    private final static String        DRC_BINLOGLOGNAME         = "DRCClient.Binlog";
    private final static String        DRC_CHECKPOINT_POLLPERIOD = "checkpoint.period";
    private static final String        SERVER_MAX_RETRIES        = "server.maxRetriedTimes";
    private static final String        SERVER_MESSAGE_TYPE       = "server.messageType";
    private static final String        CLIENT_SO_TIMEOUT         = "client.socketTimeout";
    private static final String        CLIENT_CONN_TIMEOUT       = "client.connectionTimeout";
    private static final String        CLIENT_REQ_COMP_TXN       = "client.requireCompleteTxn";
    private static final String        CLIENT_MAX_RECS_CACHE     = "client.maxNumOfRecordsCached";
    private static final String        CLIENT_MAX_RECS_BATCH     = "client.maxNumOfRecordsPerMessage";
    private static final String        CLIENT_MAX_TXNS_BATCH     = "client.maxNumOfTxnsPerMessage";
    private static final String        CLIENT_MAX_TIMEOUT_BATCH  = "client.maxTimeoutPerMessage";
    private static final String        CLIENT_USE_INDEX_ITERATOR = "enableIndexIter";

    /* Parameters could be sent to remote servers, usually provided by users. */
    private final static String        USER_FILTERCONDITIONS     = "condition";
    private final static String        USER_DBNAME               = "dbname";
    private final static String        USER_GROUPNAME            = "groupname";
    private final static String        USER_IDENTIFICATION       = "password";
    private final static String        USER_GROUP                = "group";
    private final static String        USER_SUBGROUP             = "subgroup";
    private final static String        USER_MYSQL                = "instance";
    private final static String        DRC_MARK                  = "drcMark";
    private final static String        NEED_UK_RECORD            = "needUKRecord";
    private final static String        BLACK_REGION_NO           = "black_region_no";

    private final static String        DEFAULT_DRC_MARK          = "drc.t*x_begin4unit_mark_[0-9]*|*.drc_txn";
    private static final String        USE_DRC_NET               = "useDrcNet";
    private static final String        IPMAPS                    = "ipmaps";
    //from 40，if store receive this parameter，timestamp return is unix timestamp，otherwise yyyy-mm-dd hh:mm:ss
    public static final String         CLIENT_VERSION            = "client.version";
    public static final String         CLIENT_VERSION_ID         = "58_SP";
    public static final String         REQUIRE_OTHER_UNIT_DATA   = "askOtherUnit";
    public static final String         DATA_REQUIRED_OPTION      = "client.data_acquire_option";
    public static final String         SELF_UNIT                 = "self_unit";
    public static final String         OTHER_UNIT                = "other_unit";
    @Deprecated
    private final static String        USER_FILTERSTRICT         = "strict";
    @Deprecated
    private final static String        USER_FILTERWHERE          = "where";

    /*
     * Mark the persistent location information in the local file.
     */
    public final static String         POSITION_INFO             = "Global_position_info:";

    private static Map<String, String> ipportMaps;

    private String                     regionId;
    private String                     blackRegionNo;

    /**
     * Private constructor providing primary initialization.
     */
    private DRCConfig() {
        configures = new HashMap<String, String>();
        userDefinedParams = new HashMap<String, String>();
        userDefinedParams.put(DRC_MARK, DEFAULT_DRC_MARK);
        persists = new HashSet<String>();
        configures.put(DRC_VERSION, "2.0.0");
        configures.put(DRC_CHECKPOINT_POLLPERIOD, "500");
        configures.put(CLIENT_SO_TIMEOUT, "120");
        configures.put(CLIENT_CONN_TIMEOUT, "120");
        checkpoint = new Checkpoint();
        ipportMaps = new HashMap<String, String>();
        useDrcNet = false;
    }

    /**
     * Read configures from a reader.
     * @param reader java.io.Reader
     * @throws IOException when properties load failed
     */
    public DRCConfig(final Reader reader) throws IOException {
        this();
        Properties properties = new Properties();
        properties.load(reader);
        loadProperties(properties);
    }

    /**
     * Read configures from a properties file.
     * @param propertiesFile is the file under the classpath.
     * @throws IOException when properties load failed
     */
    public DRCConfig(final String propertiesFile) throws IOException {
        this();
        InputStream drcProperties = this.getClass().getClassLoader()
            .getResourceAsStream(propertiesFile);
        Properties properties = new Properties();
        properties.load(drcProperties);
        loadProperties(properties);
    }

    public DRCConfig(final Properties properties) {
        this();
        loadProperties(properties);
    }

    /**
     * Use privately to load all properties to inner map. 
     * @param properties
     */
    private void loadProperties(Properties properties) {

        for (Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getKey().equals(SERVER_MAX_RETRIES)) {
                maxRetryTimes = Integer.parseInt((String) entry.getValue());
            } else if (entry.getKey().equals(CLIENT_SO_TIMEOUT)) {
                socketTimeout = Integer.parseInt((String) entry.getValue());
            } else if (entry.getKey().equals(CLIENT_CONN_TIMEOUT)) {
                connectionTimeout = Integer.parseInt((String) entry.getValue());
            } else if (entry.getKey().equals(SERVER_MESSAGE_TYPE)) {
                useBinaryFormat = "binary".equals(entry.getValue());
            } else if (entry.getKey().equals(CLIENT_REQ_COMP_TXN)) {
                requireCompleteTxn = "true".equals(entry.getValue());
            } else if (entry.getKey().equals(CLIENT_MAX_RECS_BATCH)) {
                // requireCompleteTxn = true;
                maxRecordsBatched = Integer.parseInt((String) entry.getValue());
            } else if (entry.getKey().equals(CLIENT_MAX_TIMEOUT_BATCH)) {
                // requireCompleteTxn = true;
                maxTimeoutBatched = Long.parseLong((String) entry.getValue());
            } else if (entry.getKey().equals(CLIENT_MAX_TXNS_BATCH)) {
                maxTxnsBatched = Integer.parseInt((String) entry.getValue());
            } else if (entry.getKey().equals(CLIENT_MAX_RECS_CACHE)) {
                maxRecordsCached = Integer.parseInt((String) entry.getValue());
            } else if (entry.getKey().equals(IPMAPS)) {
                String[] ipmaps = StringUtils.split((String) entry.getValue(), '|');
                for (String ippair : ipmaps) {
                    String[] ips = StringUtils.split(ippair, '-');
                    if (ips.length == 2) {
                        ipportMaps.put(ips[0], ips[1]);
                    }
                }
            } else if (entry.getKey().equals(USE_DRC_NET)) {
                useDrcNet = "true".equals(entry.getValue());
            } else if (entry.getKey().equals(CLIENT_USE_INDEX_ITERATOR)) {
                useIndexIterator = "true".equals(entry.getValue());
            } else if (entry.getKey().equals(DATA_REQUIRED_OPTION)) {
                String value = ((String) entry.getValue());
                DataAcquireOption dataAcquireOption = DataAcquireOption.ASK_ALL_DATA;
                if (org.apache.commons.lang3.StringUtils.equals(value, SELF_UNIT)) {
                    dataAcquireOption = DataAcquireOption.ASK_SELF_UNIT;
                } else if (org.apache.commons.lang3.StringUtils.equals(value, OTHER_UNIT)) {
                    dataAcquireOption = DataAcquireOption.ASK_OTHER_UNIT;
                }
                setRequireOtherUnitData(dataAcquireOption);
            } else if (entry.getKey().equals(NEED_UK_RECORD)) {
                needUKRecord = "true".equals(entry.getValue());
            } else if (entry.getKey().equals(BLACK_REGION_NO)) {
                blackRegionNo = (String) entry.getValue();
            }
            configures.put((String) entry.getKey(), (String) entry.getValue());
        }
    }

    public boolean needUKRecord() {
        return needUKRecord;
    }

    public String getBlackRegionNo() {
        return blackRegionNo;
    }

    public boolean getUseDrcNet() {
        return useDrcNet;
    }

    public void setUseDrcNet(boolean useDrcNet) {
        this.useDrcNet = useDrcNet;
    }

    public void setForceUseIndexIter(boolean forceUseIndexIter) {
        this.useIndexIterator = forceUseIndexIter;
    }

    public boolean getForceUseIndexIter() {
        return this.useIndexIterator;
    }

    public void setDataFilter(final DataFilterBase filter) {
        this.filter = filter;
    }

    public DataFilterBase getDataFilter() {
        return filter;
    }

    public Checkpoint getCheckpoint() {
        return checkpoint;
    }

    public int getMaxRetriedTimes() {
        return maxRetryTimes;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Get the group name.
     * @return the group name.
     */
    final String getGroupName() {
        return userDefinedParams.get(USER_GROUPNAME);
    }

    /**
     * Set the group name.
     * @param groupName is the group name.
     */
    final void setGroupName(final String groupName) {
        userDefinedParams.put(USER_GROUPNAME, groupName);
    }

    /**
     * Get the group title, if empty, use group name (user) instead. 
     * @return
     */
    final String getGroup() {
        final String group = userDefinedParams.get(USER_GROUP);
        if (group == null || group.isEmpty()) {
            return getGroupName();
        }
        return group;
    }

    /**
     * Set the group title, note that the group could be different
     * from groupname(user name).
     * @param group
     */
    final void setGroup(final String group) {
        userDefinedParams.put(USER_GROUP, group);
    }

    /**
     * Set the target physical database name.
     * @param dbname database name such as icdb0, uic_main_000 and so on.
     */
    final void setDbname(final String dbname) {
        userDefinedParams.put(USER_DBNAME, dbname);
    }

    /**
     * Set the subgroup name.
     * @param subgroup
     */
    final void setSubGroup(final String subgroup) {
        userDefinedParams.put(USER_SUBGROUP, subgroup);
    }

    /**
     * Get the subgroup name, if empty, use db name instead
     * @return subgroup 
     */
    final String getSubGroup() {
        final String subgroup = userDefinedParams.get(USER_SUBGROUP);
        if (subgroup == null || subgroup.isEmpty()) {
            return getDbname();
        }
        return subgroup;
    }

    /**
     * Get the target physical database name.
     * @return the database name.
     */
    final String getDbname() {
        return userDefinedParams.get(USER_DBNAME);
    }

    /**
     * Set the user's identification, e.t., password.
     * @param id is the identification.
     */
    final void setIdentification(final String id) {
        userDefinedParams.put(USER_IDENTIFICATION, id);
    }

    /**
     * Set a filename to store checkpoint.
     * @param filename
     */
    final void setBinlogFilename(final String filename) {
        configures.put(DRC_BINLOGLOGNAME, filename);
    }

    final String getIdentification() {
        return userDefinedParams.get(USER_IDENTIFICATION);
    }

    /**
     * Get the binlog filename.
     * @return the filename.
     */
    final String getBinlogFilename() {
        return configures.get(DRC_BINLOGLOGNAME);
    }

    /**
     * Get the period to record one checkpoint.
     * @return the period.
     */
    final int getCheckpointPeriod() {
        return Integer.parseInt(configures.get(DRC_CHECKPOINT_POLLPERIOD));
    }

    /**
     * Set user-defined checkpoint.
     * @param checkpoint
     */
    final void setCheckpoint(final Checkpoint checkpoint) {
        this.checkpoint = checkpoint;
    }

    final void setCheckpoint(final String checkpoint) {
        this.checkpoint.setPosition(checkpoint);
    }

    /**
     * Set user-defined starting time stamp.
     * @param timestamp
     */
    final void setGmtModified(final String timestamp) {
        checkpoint.setTimestamp(timestamp);
    }

    /**
     * Set checkpoint or gmtModified as the starting point.
     * @param startingPoint
     */
    final void setStartingPoint(final String startingPoint) {

        if (startingPoint.contains("@")) {
            setCheckpoint(startingPoint);
        } else {
            if (startingPoint.length() == 13) {
                throw new IllegalArgumentException(
                    "Error the unit of the starting time is second, but " + startingPoint
                            + " is in ms");
            }
            setGmtModified(startingPoint);
        }
    }

    /**
     * Get meta version.
     * @return meta version.
     */
    final String getVersion() {
        return configures.get(DRC_VERSION);
    }

    /**
     * Get address of the cluster manager.
     * @return a URL.
     */
    final String getClusterManagerAddresses() {
        return configures.get(DRC_MANAGERHOST);
    }

    /**
     * Set the connected mysql address.
     * @param mysql is the mysql address.
     */
    final void setInstance(final String mysql) {
        userDefinedParams.put(USER_MYSQL, mysql);
        checkpoint.setServerId(mysql);
    }

    /**
     * Get the last connected mysql address.
     * @return the mysql address.
     */
    final String getInstance() {
        return userDefinedParams.get(USER_MYSQL);
    }

    /**
     * Usually use internally to add user-defined parameters. 
     * @param key is the name of the parameter.
     * @param value is the value of the parameter.
     */
    final void addParam(final String key, final String value) {
        userDefinedParams.put(key, value);
    }

    /**
     * Get user-defined parameter by name.
     * @param key is the name of the parameter.
     * @return
     */
    final String getParam(final String key) {
        return userDefinedParams.get(key);
    }

    /**
     * Get all user-defined parameters.
     * @return parameters used by the server.
     */
    final Map<String, String> getParams() {
        return userDefinedParams;
    }

    /**
     * Usually use internally to add drc-related configures.
     * @param key is the name of the configure.
     * @param value is the value of the configure.
     */
    final void addConfigure(final String key, final String value) {
        configures.put(key, value);
    }

    /**
     * Get the value of one specific parameter.
     * @param key the name of the parameter.
     * @return
     */
    final String getConfigure(final String key) {
        return configures.get(key);
    }

    /**
     * Get all configures.
     * @return all the configures.
     */
    final Map<String, String> getConfigures() {
        return configures;
    }

    /**
     * Get the persistent-required attributes' names.
     * @return the list of names.
     */
    final Set<String> getPersists() {
        return persists;
    }

    /**
     * Add more persistent-required attributes.
     * @param p is the new attributes.
     */
    final void addPersists(List<String> p) {
        persists.addAll(p);
    }

    /**
     * Set user-wanted tables and columns.
     * @param conditions well-formatted tables and columns.
     */
    final void setRequiredTablesAndColumns(final String conditions) {
        userDefinedParams.put(USER_FILTERCONDITIONS, conditions);
    }

    /**
     * Set user-wanted where conditions.
     * @param where well-formated where conditions.
     */
    final void setWhereFilters(final String where) {
        userDefinedParams.put(USER_FILTERWHERE, where);
    }

    /**
     * Define that the user only want a record which at least has one
     * field required changed. 
     */
    final void setFilterUnchangedRecords() {
        userDefinedParams.put(USER_FILTERSTRICT, "true");
    }

    public void setDRCMark(String mark) {
        userDefinedParams.put(DRC_MARK, mark);
    }

    public String getDRCMark() {
        return userDefinedParams.get(DRC_MARK);
    }

    final void requireTxnMark(boolean need) {
        txnMark = need;
    }

    final boolean isTxnMarkRequired() {
        return txnMark;
    }

    final boolean isBinaryFormat() {
        return useBinaryFormat;
    }

    public void setNumOfRecordsPerBatch(int threshold) {
        recordsPerBatch = threshold;
    }

    public int getNumOfRecordsPerBatch() {
        return recordsPerBatch;
    }

    public boolean isTxnRequiredCompleted() {
        return requireCompleteTxn;
    }

    public int getMaxRecordsPerTxn() {
        return maxRecordsCached;
    }

    public int getMaxRecordsBatched() {
        return maxRecordsBatched;
    }

    public long getMaxTimeoutBatched() {
        return maxTimeoutBatched;
    }

    public int getMaxTxnsBatched() {
        return maxTxnsBatched;
    }

    public final String getMappedIpPort(final String ip) {
        return ipportMaps.get(ip);
    }

    public void useDrcMark() {
        drcMarkWorking = true;
    }

    public boolean isDrcMarkWorking() {
        return drcMarkWorking;
    }

    public String getBlackList() {
        return blackList;
    }

    public void setBlackList(String blackList) {
        this.blackList = blackList;
    }

    public boolean getUseHTTPS() {
        String use = configures.get(HTTPS_USE);
        return use != null && !"false".equals(use);
    }

    public void usePublicIp() {
        usePublicIp = true;
    }

    public boolean isUsePublicIp() {
        return usePublicIp;
    }

    public void useCaseSensitive() {
        useCaseSensitive = true;
    }

    public boolean isUseCaseSensitive() {
        return useCaseSensitive;
    }

    public void trimLongType() {
        trimLongType = true;
    }

    public boolean isTrimLongType() {
        return trimLongType;
    }

    public boolean isUseCheckCRC() {
        return useCheckCRC;
    }

    public void setUseCheckCRC(boolean useCheckCRC) {
        this.useCheckCRC = useCheckCRC;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public void setRequireOtherUnitData(DataAcquireOption dataAcquireOption) {
        switch (dataAcquireOption) {
            case ASK_SELF_UNIT: {
                this.requiredOtherUnitData = false;
                this.drcMarkWorking = true;
                break;
            }
            case ASK_OTHER_UNIT: {
                this.requiredOtherUnitData = true;
                this.drcMarkWorking = false;
                break;
            }
            case ASK_ALL_DATA: {
                this.requiredOtherUnitData = false;
                this.drcMarkWorking = false;
                break;
            }
        }
    }

    public boolean getRequireOtherUnitData() {
        return this.requiredOtherUnitData;
    }
}
