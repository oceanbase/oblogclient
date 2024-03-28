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

public class Checkpoint {

    private String recordId;
    private String position;
    private String timestamp;
    private String serverId;

    private static final String DELIMITER = ":";

    public Checkpoint() {
        recordId = position = serverId = timestamp = null;
    }

    public Checkpoint(
            final String recordId,
            final String position,
            final String serverId,
            final String timestamp) {
        this.recordId = recordId;
        this.position = position;
        this.serverId = serverId;
        this.timestamp = timestamp;
    }

    public Checkpoint(final Checkpoint cp) {
        this(cp.recordId, cp.position, cp.serverId, cp.timestamp);
    }

    public boolean equals(final String cp) {
        if (position != null && cp != null) {
            return position.equals(cp);
        }
        if (timestamp != null && cp != null) {
            return timestamp.equals(cp);
        }
        return false;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(final String recordId) {
        this.recordId = recordId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(final String position) {
        String cp = position;
        if (cp.contains("@mysql-bin.")) {
            int m = cp.indexOf('@');
            int p = cp.indexOf('.');
            String cp1 = cp.substring(0, m);
            String cp2 = cp.substring(p + 1);
            long lcp2 = Long.parseLong(cp2);
            cp = cp1 + "@" + lcp2;
        }
        this.position = cp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public String toString() {
        String cp1 = null, cp2 = null;
        if (position != null && !position.isEmpty()) {
            int in = position.indexOf('@');
            cp1 = position.substring(in + 1);
            cp2 = position.substring(0, in);
        }

        StringBuilder builder = new StringBuilder();
        if (serverId == null || serverId.isEmpty()) {
            builder.append(DELIMITER).append(DELIMITER);
        } else {
            int in = serverId.indexOf('-');
            String db = serverId.substring(0, in);
            String dbport = serverId.substring(in + 1);
            builder.append(db).append(DELIMITER).append(dbport).append(DELIMITER);
        }

        if (cp1 != null && cp2 != null) {
            builder.append(cp1).append(DELIMITER).append(cp2).append(DELIMITER);
        } else {
            builder.append(DELIMITER).append(DELIMITER);
        }

        if (timestamp != null) {
            builder.append(timestamp).append(DELIMITER);
        } else {
            builder.append(DELIMITER);
        }

        if (recordId != null) {
            builder.append(recordId);
        }

        return builder.toString();
    }
}
