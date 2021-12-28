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


import com.oceanbase.oms.logmessage.enums.DBType;
import com.oceanbase.oms.logmessage.typehelper.LogTypeHelper;
import com.oceanbase.oms.logmessage.typehelper.OBLogTypeHelper;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/** Message contains database updating data. */
public class DataMessage extends Message {

    /** Record contains data of one record. */
    public static class Record {
        public static LogTypeHelper logTypeHelper = OBLogTypeHelper.OB_LOG_TYPE_HELPER;

        public static final String UTF8MB4_ENCODING = "utf8mb4";
        public static final String TRACEID_STRING = "traceid";

        protected Type type;

        /* Record attributes. */
        protected Map<String, String> attributes;

        /* Fields */
        protected List<Field> fields;

        protected String timestamp;

        protected String safeTimestamp;

        protected static ThreadLocal<String> globalSafeTimestamp = new ThreadLocal<String>();

        protected static ThreadLocal<Boolean> txEnd =
                new ThreadLocal<Boolean>() {

                    @Override
                    protected Boolean initialValue() {
                        return true;
                    }
                };
        private boolean isConnectionFirstRecord = false;

        public void setColFilter(List<String> colFilter) {
            throw new UnsupportedOperationException();
        }

        // A Fake function
        public List<String> getPrimaryKeyValue() {
            throw new UnsupportedOperationException();
        }

        public Set<String> getKeysValue() throws Exception {
            throw new UnsupportedOperationException();
        }

        public List<String> getPrimaryValues() throws Exception {
            throw new UnsupportedOperationException();
        }

        public String getRegionId() {
            return regionId;
        }

        public void setRegionId(String regionId) {
            this.regionId = regionId;
        }

        public List<ByteString> getFirstPKValue() {
            return null;
        }

        public void setIsConnectionFirstRecord(boolean value) {
            isConnectionFirstRecord = value;
        }

        public boolean getIsConnectionFirstRecord() {
            return isConnectionFirstRecord;
        }

        public Long getLogSeqNum() {
            return 0L;
        }

        /** Field contains data of one field */
        public static class Field {

            public long length;

            public boolean primaryKey;

            public String name;

            public int type;

            public int flag;

            public String encoding;

            public ByteString value;

            public boolean changeValue = true;

            public boolean prev = false;

            public enum Type {
                INT8,
                INT16,
                INT24,
                INT32,
                INT64,
                DECIMAL,
                FLOAT,
                DOUBLE,
                NULL,
                TIMESTAMP,
                DATE,
                TIME,
                DATETIME,
                YEAR,
                BIT,
                ENUM,
                SET,
                BLOB,
                GEOMETRY,
                STRING,
                JSON,
                BINARY,
                TIMESTAMP_WITH_TIME_ZONE,
                TIMESTAMP_WITH_LOCAL_TIME_ZONE,
                TIMESTAMP_NANO,
                RAW,
                INTERVAL_YEAR_TO_MONTH,
                INTERVAL_DAY_TO_SECOND,
                UNKOWN
            }

            public Field() {
                name = null;
                type = 17; // not existed in mysql
                flag = 0;
                length = 0;
                value = null;
                primaryKey = false;
            }

            public Field(String name, int type, String encoding, ByteString value, boolean pk) {
                this.name = name;
                this.type = type;
                this.encoding = encoding;
                if (getType() == Type.STRING) {
                    if (this.encoding.isEmpty()) {
                        this.encoding = "binary";
                    }
                }

                this.value = value;
                if (value == null) {
                    length = -1;
                } else {
                    length = value.getLen();
                }
                primaryKey = pk;
            }

            public final boolean isGenerated() {
                return (this.flag & 1L) == 1L;
            }

            public void setFlag(int flag) {
                this.flag = flag;
            }

            public final boolean isPrimary() {
                return primaryKey;
            }

            public final int getRawType() {
                return type;
            }

            public void setPrimary(boolean primary) {
                primaryKey = primary;
            }

            /**
             * Get the name of the field.
             *
             * @return the name of the field.
             */
            public final String getFieldname() {
                return name;
            }

            /**
             * Get the encoding of the field.
             *
             * @return the encoding of the field.
             */
            public final String getEncoding() {
                if ("utf8mb4".equalsIgnoreCase(encoding)) {
                    return "utf8";
                }
                return encoding;
            }

            public static Type[] MYSQL_TYPES = new Type[256];

            static {
                MYSQL_TYPES[0] = Type.DECIMAL;
                MYSQL_TYPES[1] = Type.INT8;
                MYSQL_TYPES[2] = Type.INT16;
                MYSQL_TYPES[3] = Type.INT32;
                MYSQL_TYPES[4] = Type.FLOAT;
                MYSQL_TYPES[5] = Type.DOUBLE;
                MYSQL_TYPES[6] = Type.NULL;
                MYSQL_TYPES[7] = Type.TIMESTAMP;
                MYSQL_TYPES[8] = Type.INT64;
                MYSQL_TYPES[9] = Type.INT24;
                MYSQL_TYPES[10] = Type.DATE;
                MYSQL_TYPES[11] = Type.TIME;
                MYSQL_TYPES[12] = Type.DATETIME;
                MYSQL_TYPES[13] = Type.YEAR;
                MYSQL_TYPES[14] = Type.DATETIME;
                MYSQL_TYPES[15] = Type.STRING;
                MYSQL_TYPES[16] = Type.BIT;
                // special
                MYSQL_TYPES[199] = Type.BINARY;
                MYSQL_TYPES[200] = Type.TIMESTAMP_WITH_TIME_ZONE;
                MYSQL_TYPES[201] = Type.TIMESTAMP_WITH_LOCAL_TIME_ZONE;
                MYSQL_TYPES[202] = Type.TIMESTAMP_NANO;
                MYSQL_TYPES[203] = Type.RAW;
                MYSQL_TYPES[204] = Type.INTERVAL_YEAR_TO_MONTH;
                MYSQL_TYPES[205] = Type.INTERVAL_DAY_TO_SECOND;
                MYSQL_TYPES[206] = Type.FLOAT;
                MYSQL_TYPES[207] = Type.STRING;
                MYSQL_TYPES[208] = Type.STRING;
                MYSQL_TYPES[209] = Type.STRING;

                MYSQL_TYPES[255] = Type.GEOMETRY;
                MYSQL_TYPES[254] = Type.STRING;
                MYSQL_TYPES[253] = Type.STRING;
                MYSQL_TYPES[252] = Type.BLOB;
                MYSQL_TYPES[251] = Type.BLOB;
                MYSQL_TYPES[250] = Type.BLOB;
                MYSQL_TYPES[249] = Type.BLOB;
                MYSQL_TYPES[248] = Type.SET;
                MYSQL_TYPES[247] = Type.ENUM;
                MYSQL_TYPES[246] = Type.DECIMAL;
                MYSQL_TYPES[245] = Type.JSON;
            }

            /**
             * oracle logminer won't output lob type value
             *
             * @return true if field is lob type
             */
            public boolean isOracleLobType() {
                return this.type >= 249 && this.type <= 253;
            }

            /**
             * Get the enumerated type of the field.
             *
             * @return the enumerated type of the field.
             */
            public final Type getType() {
                if ((type > 16 && type < 199) || (type > 209 && type < 245)) {
                    return Type.UNKOWN;
                } else {
                    return MYSQL_TYPES[type];
                }
            }

            public boolean isChangeValue() {
                return changeValue;
            }

            public boolean isPrev() {
                return prev;
            }

            public void setPrev(boolean prev) {
                this.prev = prev;
            }

            /**
             * Get the value of the field.
             *
             * @return the value {@link ByteString}
             */
            public final ByteString getValue() {
                return value;
            }

            public void setValue(ByteString v) {
                value = v;
            }

            /**
             * Abstract needed data for a field from the input stream.
             *
             * @param reader the DataInputStream.
             * @param recordEncoding is the encoding of the field value.
             * @throws IOException if an I/O error occurs
             */
            public void mergeFrom(final DataInputStream reader, final String recordEncoding)
                    throws IOException {

                /* Read field name. */
                name = reader.readLine();
                if (name.isEmpty()) {
                    /* Read the line separator. */
                    clear();
                    return;
                }

                /* Read field type */
                type = Integer.parseInt(reader.readLine());

                /* Read the length of field value. */
                length = Long.parseLong(reader.readLine());

                /* Inherit the encoding from Record. */
                encoding = recordEncoding;

                /* Read the field value in bytes. */
                if (length != -1) {
                    byte[] valueBytes = new byte[(int) length];
                    reader.readFully(valueBytes);
                    value = new ByteString(valueBytes, (int) length);
                } else {
                    value = null;
                }

                /* Read Linux-format line separator "\n" */
                if (reader.readByte() == '\r') {
                    reader.readByte();
                }
            }

            /** Clear the field. */
            public void clear() {
                type = 17; // unknown
                name = null;
                length = 0;
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("Field name: " + name + System.getProperty("line.separator"));
                builder.append("Field type: " + type + System.getProperty("line.separator"));
                builder.append("Field length: " + length + System.getProperty("line.separator"));
                if (value != null) {
                    if ("binary".equalsIgnoreCase(encoding)) {
                        builder.append(
                                "Field value(binary): "
                                        + Arrays.toString(value.getBytes())
                                        + System.getProperty("line.separator"));
                    } else {
                        builder.append(
                                "Field value: "
                                        + value.toString(encoding)
                                        + System.getProperty("line.separator"));
                    }
                } else {
                    builder.append("Field value: " + "null" + System.getProperty("line.separator"));
                }
                return builder.toString();
            }
        } // End of Field

        /** */
        public Record() {
            ending = false;
            attributes = new HashMap<String, String>();
        }

        boolean isEnding() {
            return ending;
        }

        /* Show whether decoding a record is completed. */
        private boolean ending = false;

        private String regionId;

        /**
         * Abstract record information from the DataInputStream.
         *
         * @param reader is the input stream.
         * @throws IOException if an I/O error occurs
         */
        public void mergeFrom(final DataInputStream reader) throws IOException {

            String line;
            boolean first = true;

            /* Read record attributes. */
            while (!(line = reader.readLine()).isEmpty()) {
                String[] kv = StringUtils.split(line, ':');
                if (2 != kv.length) {
                    // Bug fix:trace id may contains ':'. Split by ':' and drop tuple contains more
                    // than 2 content lead to the miss of trace id.
                    if (kv.length > 2 && StringUtils.equals(kv[0], TRACEID_STRING)) {
                        kv[1] = line.substring(line.indexOf(':') + 1);
                    } else {
                        continue;
                    }
                }
                addAttribute(kv[0], kv[1]);
                first = false;
            }

            if (first) {
                ending = true;
                return;
            }
            // parse primary key
            String textPKs = getPrimaryKeys();
            List<String> pkList = Collections.emptyList();
            if (textPKs != null && !textPKs.isEmpty()) {
                pkList = Arrays.asList(textPKs.split(","));
            }
            // parse op type
            String stype = getAttribute("record_type");
            type = Type.valueOf(stype.toUpperCase());
            // set timestamp,process heartbeat between tx
            timestamp = getAttribute("timestamp");
            if (getDbType() == DBType.OCEANBASE1) {
                if (type == Type.HEARTBEAT) {
                    globalSafeTimestamp.set(timestamp);
                } else {
                    globalSafeTimestamp.set(getCheckpoint().substring(2));
                }
            } else {
                if (type == Type.BEGIN) {
                    globalSafeTimestamp.set(timestamp);
                    txEnd.set(false);
                }
                if (txEnd.get()) {
                    globalSafeTimestamp.set(timestamp);
                }
                // set txEnd
                if (type == Type.COMMIT || type == Type.ROLLBACK) {
                    txEnd.set(true);
                }
            }
            safeTimestamp = new String(globalSafeTimestamp.get());
            fields = new ArrayList<Field>();
            while (true) {
                Field field = new Field();
                field.mergeFrom(reader, getAttribute("record_encoding"));
                if (field.name == null) {
                    break;
                } else if (textPKs != null && !textPKs.isEmpty()) {
                    if (pkList.contains(field.name)) {
                        field.primaryKey = true;
                    }
                }
                fields.add(field);
            }

            String fieldsEncodings = getAttribute("fields_enc");
            if (fieldsEncodings != null && !fieldsEncodings.isEmpty()) {
                String[] encodings = fieldsEncodings.split(",", -1);
                if (encodings.length == fields.size()) {
                    for (int i = 0; i < encodings.length; i++) {
                        String enc = encodings[i];
                        Field field = fields.get(i);
                        logTypeHelper.correctField(field, enc);
                    }
                } else if (encodings.length * 2 == fields.size()) {
                    for (int i = 0; i < encodings.length; i++) {
                        String enc = encodings[i];
                        Field field1 = fields.get(i * 2);
                        Field field2 = fields.get(i * 2 + 1);
                        logTypeHelper.correctField(field1, enc);
                        logTypeHelper.correctField(field2, enc);
                    }
                }
                // ignore if mistake
            }
        } // End of Record.mergeFrom

        /* Record type. */
        public enum Type {
            // INSERT
            INSERT(0),
            // UPDATE
            UPDATE(1),
            // DELETE
            DELETE(2),
            // REPLACE
            REPLACE(3),
            // HEARTBEAT
            HEARTBEAT(4),
            // CONSISTENCY_TEST
            CONSISTENCY_TEST(5),
            // BEGIN
            BEGIN(6),
            // COMMIT
            COMMIT(7),
            // DDL
            DDL(8),
            // ROLLBACK
            ROLLBACK(9),
            // DML
            DML(10),
            // UNKNOWN
            UNKNOWN(11),
            // INDEX_INSERT
            INDEX_INSERT(128),
            // INDEX_UPDATE
            INDEX_UPDATE(129),
            // INDEX_DELETE
            INDEX_DELETE(130),
            // INDEX_REPLACE
            INDEX_REPLACE(131);

            final int _value;

            Type(int value) {
                _value = value;
            }

            public int value() {
                return _value;
            }

            public static Type valueOf(int value) {
                for (Type type : Type.values()) {
                    if (type.value() == value) {
                        return type;
                    }
                }

                return Type.UNKNOWN;
            }
        }

        /**
         * Get the type of the record in insert, delete, update and heartbeat.
         *
         * @return the type of the record.
         */
        public Type getOpt() {
            return type;
        }

        public String getId() {
            return getAttribute("record_id");
        }

        public String getDbName() {
            return getAttribute("db");
        }

        public String getTableName() {
            return getAttribute("table_name");
        }

        public String getCheckpoint() {
            return getAttribute("checkpoint");
        }

        @Deprecated
        public String getMetadataVersion() {
            return getAttribute("meta");
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getTimestampUsec() throws IOException {
            return null;
        }

        public String getSafeTimestamp() {
            return safeTimestamp;
        }

        public String getServerId() {
            return getAttribute("instance");
        }

        public String getPrevId() {
            return getAttribute("prev_id");
        }

        public String getServerSeq() {
            return getAttribute("server_id");
        }

        public String getPrevServerSeq() {
            return getAttribute("prev_server_id");
        }

        /**
         * Get tuples of index. For example, column 1 is the primary key and column 2 and 3 are
         * unique keys. The returned format is the list of two arrays, the first array is {0} and
         * the seconds array is {1,2}
         *
         * @return the tuples of index for primary and unique constraints.
         */
        public List<int[]> getPrimaryAndUniqueConstraintColumnIndexTuples() {
            return null;
        }

        public String getPrimaryKeys() {
            return getAttribute("primary");
        }

        // Fake
        public boolean isKeyChange() {
            return false;
        }

        public List<String> getPrimaryKeysList() {
            List<String> pks = new ArrayList<String>();
            return pks;
        }

        public String getTraceInfo() {
            return "";
        }

        public String getOB10UniqueId() throws Exception {
            return null;
        }

        public String getUniqueColNames() {
            return getAttribute("unique");
        }

        public DBType getDbType() {
            String type = getAttribute("source_type");
            if (StringUtils.isEmpty(type)) {
                return DBType.UNKNOWN;
            }
            if ("mysql".equalsIgnoreCase(type)) {
                return DBType.MYSQL;
            } else if ("oceanbase".equalsIgnoreCase(type)) {
                return DBType.OCEANBASE;
            } else if ("oracle".equalsIgnoreCase(type)) {
                return DBType.ORACLE;
            } else if ("hbase".equalsIgnoreCase(type)) {
                return DBType.HBASE;
            } else if ("oceanbase_1_0".equalsIgnoreCase(type)) {
                return DBType.OCEANBASE1;
            } else if ("db2".equalsIgnoreCase(type)) {
                return DBType.DB2;
            }
            return DBType.UNKNOWN;
        }

        public boolean isQueryBack() {
            String cate = getAttribute("source_category");
            if ("full_recorded".equalsIgnoreCase(cate)
                    || "part_recorded".equalsIgnoreCase(cate)
                    || "full_faked".equalsIgnoreCase(cate)) {
                return false;
            } else {
                return true;
            }
        }

        /**
         * Now the api takes on different behavior between MYSQL and OCEANBASE, for MYSQL, it
         * returns true because the record is the LAST record in the logevent, while for OCEANBASE,
         * it is true because the record is the first one. TBD: server for mysql need change its
         * behavior the same as OCEANBASE
         *
         * @return true if this is the first record
         */
        public boolean isFirstInLogEvent() {
            String isFirstLogEvent = getAttribute("logevent");
            return "1".equals(isFirstLogEvent);
        }

        public String getAttribute(final String key) {
            return attributes.get(key);
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public int getFieldCount() {
            getFieldList();
            if (fields == null) {
                return 0;
            }
            return fields.size();
        }

        /**
         * Get the field list.
         *
         * @return the field list.
         */
        public List<Field> getFieldList() {
            return fields;
        }

        /**
         * filedParseListener
         *
         * @param fieldParseListener fieldParseListener
         * @throws Exception if an exception occurs
         */
        public void fieldListParse(FieldParseListener fieldParseListener) throws Exception {}

        /**
         * Set the type of the record.
         *
         * @param type one in insert, delete, update and heartbeat.
         */
        public void setType(Type type) {
            this.type = type;
        }

        /**
         * Add one attribute to the record.
         *
         * @param key the name of the attribute.
         * @param value the value of the attribute.
         */
        public void addAttribute(final String key, final String value) {
            attributes.put(key, value);
        }

        public byte[] getRawData() {
            return null;
        }

        public String getThreadId() throws Exception {
            return getAttribute("threadid");
        }

        public String getTraceId() throws Exception {
            return getAttribute("traceid");
        }

        public void parse(final byte[] data) throws Exception {
            throw new IOException(Record.class.getName() + " not support parse from raw data");
        }

        public long getMessageUniqueId() throws Exception {
            return hash64(this.getMessageUniqueIdStr());
        }

        public String getMessageUniqueIdStr() throws Exception {
            DBType dbType = getDbType();
            this.checkDBType(dbType);
            StringBuilder messageId = new StringBuilder();
            if (dbType == DBType.MYSQL) {
                messageId.append(getServerId());
            }

            messageId.append("/").append(this.getCommonPart()).append("/");
            if (dbType == DBType.OCEANBASE1) {
                messageId.append("/");
            } else {
                String checkpoint = getCheckpoint();
                messageId
                        .append(checkpoint.substring(checkpoint.indexOf('@') + 1))
                        .append("/")
                        .append(checkpoint, 0, checkpoint.indexOf('@'));
            }

            messageId.append("/");
            if (dbType == DBType.OCEANBASE1) {
                messageId.append(getOB10UniqueId());
            }

            messageId.append("/").append(getTimestamp());
            return messageId.toString();
        }

        private void checkDBType(DBType dbType) {
            if (dbType != DBType.MYSQL
                    && dbType != DBType.OCEANBASE
                    && dbType != DBType.OCEANBASE1
                    && dbType != DBType.ORACLE
                    && dbType != DBType.DB2) {
                throw new IllegalStateException(
                        "dbType [" + dbType + "] is not valid for messageId");
            }
        }

        private String getCommonPart() {
            String dbType = getDbType().toString();
            String opType = getOpt().toString();
            String dbName = getDbName();
            String tableName = getTableName();
            return dbType + "/" + opType + "/" + dbName + "/" + tableName;
        }

        private static long hash64(String string) {
            long h = 1125899906842597L;
            int len = string.length();

            for (int i = 0; i < len; ++i) {
                h = 31L * h + (long) string.charAt(i);
            }

            return h;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (Entry<String, String> entry : attributes.entrySet()) {
                builder.append(entry.getKey() + ":" + entry.getValue());
                builder.append(System.getProperty("line.separator"));
            }
            builder.append(System.getProperty("line.separator"));
            if (null != fields) {
                for (Field field : fields) {
                    builder.append(field.toString());
                }
            }
            builder.append(System.getProperty("line.separator"));
            return builder.toString();
        }
    } // End of Record

    /* Record list. */
    private final List<Record> records;

    /** Constructor of DataMessage, the type is 100 by default. */
    public DataMessage() {
        super();
        type = 100;
        records = new ArrayList<Record>();
    }

    /**
     * Get the number of all records in the message.
     *
     * @return the number of records.
     */
    public int getRecordCount() {
        return records.size();
    }

    /**
     * Get the list of records.
     *
     * @return the list of records.
     */
    public List<Record> getRecordList() {
        return records;
    }

    /**
     * Construct the message from DataInputStream.
     *
     * @param reader is the DataInputStream.
     * @param regionId is the region id.
     * @throws IOException if an IOException occurs.
     */
    public void mergeFrom(final DataInputStream reader, String regionId) throws IOException {
        do {
            Record record = new Record();
            record.mergeFrom(reader);
            record.setRegionId(regionId);
            if (record.isEnding()) {
                break;
            }
            records.add(record);
        } while (true);
    }

    @Override
    public void clear() {
        super.clear();
        records.clear();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        for (Record record : records) {
            builder.append(record.toString());
        }
        builder.append(System.getProperty("line.separator"));
        return builder.toString();
    }

    public void addRecord(Record r) {
        records.add(r);
    }
}
