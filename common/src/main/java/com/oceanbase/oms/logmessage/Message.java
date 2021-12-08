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

import org.apache.commons.lang3.StringUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Message {

    protected int                 type;
    protected long                id;
    protected Map<String, String> attributes;
    //this attribute means  the source store's ip and port which this client is pulling data from.
    public static final String    SOURCEIPANDPORT = "sourceIPAndPort";

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
