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

package com.oceanbase.clogproxy.client.exception;

import com.oceanbase.clogproxy.client.enums.ErrorCode;

/**
 * This is a subclasses of {@link RuntimeException} used to indicate the exception occurs in client
 * with an error code.
 */
public class LogProxyClientException extends RuntimeException {

    /** Error code. */
    private ErrorCode code = ErrorCode.NONE;

    /*The flag of whether the client should stop the stream.*/
    private boolean needStop = false;

    /**
     * Constructor with error code and message.
     *
     * @param code Error code.
     * @param message Error message.
     */
    public LogProxyClientException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Constructor with error code and exception.
     *
     * @param code Error code.
     * @param exception Exception instance.
     */
    public LogProxyClientException(ErrorCode code, Exception exception) {
        super(exception.getMessage(), exception.getCause());
        this.code = code;
    }

    /**
     * Constructor with error code, error message and cause.
     *
     * @param code Error code.
     * @param message Error message.
     * @param throwable Cause.
     */
    public LogProxyClientException(ErrorCode code, String message, Throwable throwable) {
        super(message, throwable);
        this.code = code;
    }

    /**
     * Constructor with error code and message.
     *
     * @param code Error code.
     * @param message Error message.
     * @param needStop The flag of whether the client should stop the stream.
     */
    public LogProxyClientException(ErrorCode code, String message, boolean needStop) {
        super(message);
        this.code = code;
        this.needStop = needStop;
    }

    /**
     * Constructor with error code and exception.
     *
     * @param code Error code.
     * @param exception Exception instance.
     * @param needStop The flag of whether the client should stop the stream.
     */
    public LogProxyClientException(ErrorCode code, Exception exception, boolean needStop) {
        super(exception.getMessage(), exception.getCause());
        this.code = code;
        this.needStop = needStop;
    }

    /**
     * Constructor with error code, error message and cause.
     *
     * @param code Error code.
     * @param message Error message.
     * @param throwable Cause.
     * @param needStop The flag of whether the client should stop the stream.
     */
    public LogProxyClientException(
            ErrorCode code, String message, Throwable throwable, boolean needStop) {
        super(message, throwable);
        this.code = code;
        this.needStop = needStop;
    }

    /**
     * Identify whether the client should stop the stream.
     *
     * @return The flag of whether the client should stop the stream.
     */
    public boolean needStop() {
        return needStop;
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
