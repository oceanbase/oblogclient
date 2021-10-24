/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client.exception;

import com.oceanbase.clogproxy.client.enums.ErrorCode;

/**
 * This is a subclasses of {@link RuntimeException} used to indicate the exception occurs in client with an error code.
 */
public class LogProxyClientException extends RuntimeException {

    /**
     * Error code.
     */
    private ErrorCode code = ErrorCode.NONE;

    /**
     * Constructor with error code and message.
     *
     * @param code    Error code.
     * @param message Error message.
     */
    public LogProxyClientException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Constructor with error code and exception.
     *
     * @param code      Error code.
     * @param exception Exception instance.
     */
    public LogProxyClientException(ErrorCode code, Exception exception) {
        super(exception.getMessage(), exception.getCause());
        this.code = code;
    }

    /**
     * Constructor with error code, error message and cause.
     *
     * @param code      Error code.
     * @param message   Error message.
     * @param throwable Cause.
     */
    public LogProxyClientException(ErrorCode code, String message, Throwable throwable) {
        super(message, throwable);
        this.code = code;
    }

    /**
     * Identify whether the client should stop the stream.
     *
     * @return The flag of whether the client should stop the stream.
     */
    public boolean needStop() {
        return (code == ErrorCode.E_MAX_RECONNECT) || (code == ErrorCode.E_PROTOCOL) ||
            (code == ErrorCode.E_HEADER_TYPE) || (code == ErrorCode.NO_AUTH) ||
            (code == ErrorCode.E_COMPRESS_TYPE) || (code == ErrorCode.E_LEN) ||
            (code == ErrorCode.E_PARSE);
    }

    /**
     * Get the error code.
     *
     * @return Error code.
     */
    public ErrorCode getCode() {
        return code;
    }
}
