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

public class LogProxyClientException extends RuntimeException {
    private ErrorCode code = ErrorCode.NONE;

    public LogProxyClientException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public LogProxyClientException(ErrorCode code, Exception exception) {
        super(exception.getMessage(), exception.getCause());
        this.code = code;
    }

    public LogProxyClientException(ErrorCode code, String message, Throwable throwable) {
        super(message, throwable);
        this.code = code;
    }

    public boolean needStop() {
        return (code == ErrorCode.E_MAX_RECONNECT) || (code == ErrorCode.E_PROTOCOL) ||
                (code == ErrorCode.E_HEADER_TYPE) || (code == ErrorCode.NO_AUTH) ||
                (code == ErrorCode.E_COMPRESS_TYPE) || (code == ErrorCode.E_LEN) ||
                (code == ErrorCode.E_PARSE);
    }

    public ErrorCode getCode() {
        return code;
    }
}
