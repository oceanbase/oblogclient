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

import org.apache.commons.lang3.StringUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Message {

    protected int type;
    protected long id;
    protected Map<String, String> attributes;
    // this attribute means  the source store's ip and port which this client is pulling data from.
    public static final String SOURCEIPANDPORT = "sourceIPAndPort";

    public Message() {
        attributes = new HashMap<String, String>();
    }

    public long getMid() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getAttribute(final String key) {
        return attributes.get(key);
    }

    public String getStoreIpAndPort() {
        return attributes.get(SOURCEIPANDPORT);
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void addAttribute(final String key, final String value) {
        attributes.put(key, value);
    }

    public void addAttributes(final Map<String, String> attrs) {
        attributes.putAll(attrs);
    }

    @SuppressWarnings("deprecation")
    void mergeFrom(final DataInputStream reader) throws IOException {
        String line;
        while (!(line = reader.readLine()).isEmpty()) {
            String[] kv = StringUtils.split(line, ':');
            if (kv.length != 2) {
                throw new IOException("Parse message attribute " + line + " error");
            }
            addAttribute(kv[0], kv[1]);
        }
    }

    public void clear() {
        type = 0;
        id = -1;
        attributes.clear();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Entry<String, String> entry : attributes.entrySet()) {
            builder.append(entry.getKey() + ":" + entry.getValue());
            builder.append(System.getProperty("line.separator"));
        }
        builder.append(System.getProperty("line.separator"));
        return builder.toString();
    }
}
