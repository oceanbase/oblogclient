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


import com.oceanbase.oms.logmessage.typehelper.LogMessageTypeCode;

/** This interface defined a kind of listener for field parsing. */
public interface FieldParseListener {

    /**
     * Handle the filed parsing result.
     *
     * @param fieldName Field name.
     * @param type {@link LogMessageTypeCode}.
     * @param encoding Encoding of value.
     * @param value Field value.
     * @param notNull Flag of whether the field is not null (not optional).
     * @param isPrev Flag of whether the value is the old one.
     */
    void parseNotify(
            String fieldName,
            int type,
            String encoding,
            ByteString value,
            boolean notNull,
            boolean isPrev);

    /**
     * Handle the filed parsing result. Only support value, as we already know schema info.
     *
     * @param type {@link LogMessageTypeCode}.
     * @param value Field value.
     * @param encoding Encoding of value.
     * @param isPrev Flag of whether the value is the old one.
     */
    void parseNotify(int type, ByteString value, String encoding, boolean isPrev);

    /**
     * Flag of whether schema info (fieldName, type, encoding, notNull) is needed.
     *
     * @return True for needed, otherwise false.
     */
    boolean needSchemaInfo();
}
