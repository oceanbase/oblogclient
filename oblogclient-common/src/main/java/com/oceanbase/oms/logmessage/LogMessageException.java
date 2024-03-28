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
