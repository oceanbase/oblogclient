/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.oms.logmessage;

/**
 * This is a subclasses of {@link RuntimeException} primarily used in process of parsing {@link
 * LogMessage}.
 */
public class LogMessageException extends RuntimeException {

    /**
     * Constructor with message.
     *
     * @param errMessage Error message.
     */
    public LogMessageException(String errMessage) {
        super(errMessage);
    }

    /** Constructor with no arguments. */
    public LogMessageException() {
        super();
    }

    /**
     * Constructor with cause.
     *
     * @param cause Cause error or exception.
     */
    public LogMessageException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with error message and cause.
     *
     * @param errMessage Error message.
     * @param cause Cause error or exception.
     */
    public LogMessageException(String errMessage, Throwable cause) {
        super(errMessage, cause);
    }
}
