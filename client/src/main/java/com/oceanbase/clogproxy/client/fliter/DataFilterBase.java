/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client.fliter;

import com.oceanbase.clogproxy.client.enums.DBType;
import com.oceanbase.clogproxy.client.exception.DRCClientException;

import java.util.List;
import java.util.Map;

public interface DataFilterBase {
    String FILTER_SEPARATOR_INNER = ".";
    String FILTER_SEPARATOR       = "|";

    /**
     * Get the formatted filter string which will be delivered to store.
     * Notice: before validate filter function called, getConnectStoreFilterConditions may return null.
     * @return filter string
     */
    String getConnectStoreFilterConditions();

    /**
     * Validate if the filter user passed is legal
     * @param dbType database type which may be ob, mysql, oracle.
     * For now, only ob1.0 need special handle which 4 tuple contains tenant, db, tb, cols is strictly required.
     * @return true if filter is valid
     * @throws DRCClientException if exception occurs
     */
    boolean validateFilter(DBType dbType) throws DRCClientException;

    /**
     * This function is compatible for old usage.
     * @param branchDb the original branched db name.
     */
    void setBranchDb(String branchDb);

    /**
     * Fast match if cols are all needed.
     * @return true if all cols are needed
     */
    boolean getIsAllMatch();

    Map<String, Map<String, List<String>>> getReflectionMap();

    Map<String, Map<String, List<String>>> getRequireMap();

}
