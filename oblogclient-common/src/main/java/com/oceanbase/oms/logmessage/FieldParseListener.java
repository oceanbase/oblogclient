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
