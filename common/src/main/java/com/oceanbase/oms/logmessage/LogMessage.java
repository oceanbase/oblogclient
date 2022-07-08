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


import com.oceanbase.oms.common.enums.DbTypeEnum;
import com.oceanbase.oms.logmessage.enums.DataType;
import com.oceanbase.oms.logmessage.typehelper.LogTypeHelper;
import com.oceanbase.oms.logmessage.typehelper.LogTypeHelperFactory;
import com.oceanbase.oms.logmessage.utils.BinaryMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogMessage extends DataMessage.Record {
    private static final Logger log = LoggerFactory.getLogger(LogMessage.class);

    public static final String DEFAULT_ENCODING = "ASCII";

    public static final String UTF8_ENCODING = "UTF-8";

    private static final String SEP = System.getProperty("line.separator");

    private static final int OLD_VERSION_2_HEADER_LEN = 88;

    private static final int NEW_VERSION_2_HEADER_LEN = 96;

    private static final int VERSION_3_HEADER_LEN = 104;

    private static final int VERSION_3_1_HEADER_LEN = 120;

    private static final int PREFIX_LENGTH = 12;

    private int brVersion = (byte) 0xff;

    private int srcType = (byte) 0xff;

    private int op = (byte) 0xff;

    private int lastInLogEvent = (byte) 0xff;

    private long srcCategory = -1;

    private long id = -1;

    private long timestamp = -1;

    private long encoding = -1;

    private long instanceOffset = -1;
    private long timeMarkOffset = -1;

    private long dbNameOffset = -1;

    private long tbNameOffset = -1;

    private long colNamesOffset = -1;

    private long colTypesOffset = -1;

    private long fileNameOffset = -1;

    private long fileOffset = -1;

    private long oldColsOffset = -1;

    private long newColsOffset = -1;

    private long pkValOffset = -1;

    private long pkKeysOffset = -1;

    private long ukColsOffset = -1;

    private long colsEncodingOffset = -1;

    private long filterRuleValOffset = -1;

    private long tailOffset = -1;

    private long metaVersion = -1;

    private long colFlagOffset = -1;

    private long colNotNullOffset = -1;

    /** buf parse data */
    private String dbName;

    private String tableName;

    private String serverId;

    private List<Integer> primaryKeyIndexList;

    private String uniqueKeyList;

    private ByteBuf byteBuf;

    private Set<String> keysValue;

    private List<String> pkValues;

    private List<Long> timeMarks = null;

    private boolean keyChange = false;

    /** type size map, used to get array type bytes by type index */
    private static final int[] ELEMENT_ARRAY = {0, 1, 1, 2, 2, 4, 4, 8, 8};

    private static final int BYTE_SIZE = 1;

    private static final int INT_SIZE = Integer.SIZE / Byte.SIZE;

    private final CRC32 crc32 = new CRC32();

    private boolean isCheckCRC = false;

    public LogMessage(boolean isCheckCRC) {
        this.isCheckCRC = isCheckCRC;
    }

    private void setKeyChange(boolean isChange) {
        keyChange = isChange;
    }

    @Override
    public boolean isKeyChange() {
        return keyChange;
    }

    public int getVersion() {
        return brVersion;
    }

    @Override
    public DbTypeEnum getDbType() {
        return DataMessage.parseDBTypeCode(srcType);
    }

    @Override
    public boolean isQueryBack() {
        switch ((int) srcCategory) {
            case 1:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean isFirstInLogEvent() {
        return lastInLogEvent == 1;
    }

    @Override
    public Type getOpt() {
        return Type.valueOf(op);
    }

    @Override
    public String getId() {
        return Long.toString(id);
    }

    @Override
    public String getDbName() {
        if (dbName == null) {
            if ((int) dbNameOffset < 0) {
                dbName = "";
            } else {
                try {
                    dbName =
                            BinaryMessageUtils.getString(
                                    byteBuf.array(), (int) dbNameOffset, UTF8_ENCODING);
                } catch (Exception e) {
                    throw new LogMessageException(e.getMessage(), e.getCause());
                }
            }
        }
        return "".endsWith(dbName) ? null : dbName;
    }

    @Override
    public String getTableName() {
        if (tableName == null) {
            if ((int) tbNameOffset < 0) {
                tableName = "";
            } else {
                try {
                    tableName =
                            BinaryMessageUtils.getString(
                                    byteBuf.array(), (int) tbNameOffset, UTF8_ENCODING);
                } catch (Exception e) {
                    throw new LogMessageException(e.getMessage(), e.getCause());
                }
            }
        }
        return "".endsWith(tableName) ? null : tableName;
    }

    @Override
    public String getCheckpoint() {
        return fileOffset + "@" + fileNameOffset;
    }

    @Override
    public String getTimestamp() {
        return Long.toString(timestamp);
    }

    @Override
    public String getServerId() {
        if (serverId == null) {
            if ((int) instanceOffset < 0) {
                serverId = "";
            } else {
                try {
                    serverId =
                            BinaryMessageUtils.getString(
                                    byteBuf.array(), (int) instanceOffset, DEFAULT_ENCODING);
                } catch (Exception e) {
                    throw new LogMessageException(e.getMessage(), e.getCause());
                }
            }
        }
        return "".endsWith(serverId) ? null : serverId;
    }

    @Override
    public void fieldListParse(FieldParseListener fieldParseListener) throws Exception {
        if (colNamesOffset < 0 || colTypesOffset < 0 || oldColsOffset < 0 || newColsOffset < 0) {
            return;
        }

        /*
         * global encoding
         *
         * 对于 DDL 的默认编码改动， DDL DrcMessage 不携带编码信息，只能使用默认编码，但是 ASCII 对于中文处理出错
         * 对于 DDL 默认编码改为 UTF-8，不改变除 DDL 之外其他行为
         */
        String encodingStr = null;
        if (this.getOpt() == Type.DDL) {
            encodingStr = UTF8_ENCODING;
        } else {
            encodingStr =
                    BinaryMessageUtils.getString(byteBuf.array(), (int) encoding, DEFAULT_ENCODING);
        }
        // get column count
        ByteBuf wrapByteBuf =
                Unpooled.wrappedBuffer(byteBuf.array()).order(ByteOrder.LITTLE_ENDIAN);
        wrapByteBuf.readerIndex((int) (PREFIX_LENGTH + colNamesOffset + BYTE_SIZE));
        int count = wrapByteBuf.readInt();
        // op type array
        wrapByteBuf.readerIndex(PREFIX_LENGTH + (int) colTypesOffset);
        byte t = wrapByteBuf.readByte();
        int elementSize = ELEMENT_ARRAY[t & DataType.DT_MASK];
        // encoding
        int colEncodingsCount = 0;
        int currentEncodingOffset = 0;
        if (colsEncodingOffset > 0) {
            wrapByteBuf.readerIndex((int) (PREFIX_LENGTH + colsEncodingOffset + BYTE_SIZE));
            colEncodingsCount = wrapByteBuf.readInt();
            currentEncodingOffset = (int) wrapByteBuf.readUnsignedInt();
        }
        // column name
        wrapByteBuf.readerIndex((int) (PREFIX_LENGTH + colNamesOffset + BYTE_SIZE + INT_SIZE));
        int currentColNameOffset = (int) wrapByteBuf.readUnsignedInt();
        // old col value
        wrapByteBuf.readerIndex((int) (PREFIX_LENGTH + oldColsOffset + BYTE_SIZE));
        int oldColCount = wrapByteBuf.readInt();
        int currentOldColOffset = -1;
        // Bug fix: if newCol count or old Count is 0, then the following offset should not be read;
        if (0 != oldColCount) {
            currentOldColOffset = (int) wrapByteBuf.readUnsignedInt();
        }
        // new col value
        wrapByteBuf.readerIndex((int) (PREFIX_LENGTH + newColsOffset + BYTE_SIZE));
        int newColCount = wrapByteBuf.readInt();
        int currentNewColOffset = -1;
        if (0 != newColCount) {
            currentNewColOffset = (int) wrapByteBuf.readUnsignedInt();
        }
        LogTypeHelper logTypeHelper = LogTypeHelperFactory.getInstance(getDbType());
        // start loop
        for (int i = 0; i < count; i++) {
            // get real op type
            int type = 0;
            wrapByteBuf.readerIndex(
                    PREFIX_LENGTH + (int) colTypesOffset + BYTE_SIZE + INT_SIZE + i * elementSize);
            switch (elementSize) {
                case 1:
                    type = wrapByteBuf.readUnsignedByte();
                    break;
                case 2:
                    type = wrapByteBuf.readUnsignedShort();
                    break;
                case 4:
                    type = (int) wrapByteBuf.readUnsignedInt();
                    break;
                case 8:
                    type = (int) wrapByteBuf.readLong();
                    break;
            }
            boolean notNull = false;
            if (fieldParseListener.needSchemaInfo()) {
                if (colNotNullOffset > 0) {
                    wrapByteBuf.readerIndex(
                            PREFIX_LENGTH
                                    + (int) colNotNullOffset
                                    + BYTE_SIZE
                                    + INT_SIZE
                                    + i * elementSize);
                    notNull = wrapByteBuf.readBoolean();
                }
            }
            // get real encoding
            String realEncoding = encodingStr;

            // now deliver have fix encoding offset bug, encoding has been decoded correctly
            // add db2 compatible code for new version db2 reader
            // old else will also saved for old version store
            // this code will deprecated in future release
            // correct oracle code if oralce reader has update delivier version or correct type code
            if (colEncodingsCount > 0) {
                wrapByteBuf.readerIndex(
                        (int)
                                (PREFIX_LENGTH
                                        + colsEncodingOffset
                                        + BYTE_SIZE
                                        + INT_SIZE
                                        + (i + 1) * INT_SIZE));
                int nextEncodingOffset = (int) wrapByteBuf.readUnsignedInt();
                ByteString encodingByteString =
                        new ByteString(
                                wrapByteBuf.array(),
                                PREFIX_LENGTH
                                        + currentEncodingOffset
                                        + BYTE_SIZE
                                        + INT_SIZE
                                        + (count + 1) * INT_SIZE
                                        + (int) colsEncodingOffset,
                                nextEncodingOffset - currentEncodingOffset - 1);
                realEncoding = encodingByteString.toString();
                currentEncodingOffset = nextEncodingOffset;
            }
            realEncoding = logTypeHelper.correctEncoding(type, realEncoding);
            String columnName = null;
            if (fieldParseListener.needSchemaInfo()) {
                type = logTypeHelper.correctCode(type, realEncoding);

                // colName
                wrapByteBuf.readerIndex(
                        (int)
                                (PREFIX_LENGTH
                                        + colNamesOffset
                                        + BYTE_SIZE
                                        + INT_SIZE
                                        + (i + 1) * INT_SIZE));
                int nextColNameOffset = (int) wrapByteBuf.readUnsignedInt();
                ByteString colNameByteString =
                        new ByteString(
                                wrapByteBuf.array(),
                                PREFIX_LENGTH
                                        + currentColNameOffset
                                        + BYTE_SIZE
                                        + INT_SIZE
                                        + (count + 1) * INT_SIZE
                                        + (int) colNamesOffset,
                                nextColNameOffset - currentColNameOffset - 1);
                columnName = colNameByteString.toString();
                currentColNameOffset = nextColNameOffset;
            }
            // old col
            if (oldColCount != 0) {
                wrapByteBuf.readerIndex(
                        (int)
                                (PREFIX_LENGTH
                                        + oldColsOffset
                                        + BYTE_SIZE
                                        + INT_SIZE
                                        + (i + 1) * INT_SIZE));
                int nextOldColOffset = (int) wrapByteBuf.readUnsignedInt();
                ByteString value = null;
                if (nextOldColOffset != currentOldColOffset) {
                    value =
                            new ByteString(
                                    wrapByteBuf.array(),
                                    PREFIX_LENGTH
                                            + currentOldColOffset
                                            + BYTE_SIZE
                                            + INT_SIZE
                                            + (count + 1) * INT_SIZE
                                            + (int) oldColsOffset,
                                    nextOldColOffset - currentOldColOffset - 1);
                }
                if (fieldParseListener.needSchemaInfo()) {
                    fieldParseListener.parseNotify(
                            columnName, type, realEncoding, value, notNull, true);
                } else {
                    fieldParseListener.parseNotify(type, value, realEncoding, true);
                }
                currentOldColOffset = nextOldColOffset;
            }
            // new col
            if (newColCount != 0) {
                wrapByteBuf.readerIndex(
                        (int)
                                (PREFIX_LENGTH
                                        + newColsOffset
                                        + BYTE_SIZE
                                        + INT_SIZE
                                        + (i + 1) * INT_SIZE));
                int nextNewColOffset = (int) wrapByteBuf.readUnsignedInt();
                ByteString value = null;
                if (currentNewColOffset != nextNewColOffset) {
                    value =
                            new ByteString(
                                    wrapByteBuf.array(),
                                    PREFIX_LENGTH
                                            + currentNewColOffset
                                            + BYTE_SIZE
                                            + INT_SIZE
                                            + (count + 1) * INT_SIZE
                                            + (int) newColsOffset,
                                    nextNewColOffset - currentNewColOffset - 1);
                }
                if (fieldParseListener.needSchemaInfo()) {
                    fieldParseListener.parseNotify(
                            columnName, type, realEncoding, value, notNull, false);
                } else {
                    fieldParseListener.parseNotify(type, value, realEncoding, false);
                }
                currentNewColOffset = nextNewColOffset;
            }
        }
    }

    @Override
    public synchronized List<Field> getFieldList() {
        try {
            if (fields == null) {
                if (colNamesOffset < 0
                        || colTypesOffset < 0
                        || oldColsOffset < 0
                        || newColsOffset < 0) {
                    return fields;
                }
                LogTypeHelper logTypeHelper = LogTypeHelperFactory.getInstance(getDbType());
                /*
                 * global encoding
                 *
                 * 对于 DDL 的默认编码改动， DDL DrcMessage 不携带编码信息，只能使用默认编码，但是 ASCII 对于中文处理出错
                 * 对于 DDL 默认编码改为 UTF-8，不改变除 DDL 之外其他行为
                 */
                String encodingStr = null;
                if (this.getOpt() == Type.DDL) {
                    encodingStr = UTF8_ENCODING;
                } else {
                    encodingStr =
                            BinaryMessageUtils.getString(
                                    byteBuf.array(), (int) encoding, DEFAULT_ENCODING);
                }
                // pk info
                List<Integer> pks = null;
                if ((int) pkKeysOffset > 0) {
                    pks = (BinaryMessageUtils.getArray(byteBuf.array(), (int) pkKeysOffset));
                }
                // get column count
                ByteBuf wrapByteBuf =
                        Unpooled.wrappedBuffer(byteBuf.array()).order(ByteOrder.LITTLE_ENDIAN);
                wrapByteBuf.readerIndex((int) (PREFIX_LENGTH + colNamesOffset + BYTE_SIZE));
                int count = wrapByteBuf.readInt();
                fields = new ArrayList<Field>(count);
                // op type array
                wrapByteBuf.readerIndex(PREFIX_LENGTH + (int) colTypesOffset);
                byte t = wrapByteBuf.readByte();
                int elementSize = ELEMENT_ARRAY[t & DataType.DT_MASK];
                // encoding
                int colEncodingsCount = 0;
                int currentEncodingOffset = 0;
                if (colsEncodingOffset > 0) {
                    wrapByteBuf.readerIndex((int) (PREFIX_LENGTH + colsEncodingOffset + BYTE_SIZE));
                    colEncodingsCount = wrapByteBuf.readInt();
                    currentEncodingOffset = (int) wrapByteBuf.readUnsignedInt();
                }
                // column name
                wrapByteBuf.readerIndex(
                        (int) (PREFIX_LENGTH + colNamesOffset + BYTE_SIZE + INT_SIZE));
                int currentColNameOffset = (int) wrapByteBuf.readUnsignedInt();
                // old col value
                wrapByteBuf.readerIndex((int) (PREFIX_LENGTH + oldColsOffset + BYTE_SIZE));
                int oldColCount = wrapByteBuf.readInt();
                int currentOldColOffset = -1;
                // Bug fix: if newCol count or old Count is 0, then the following offset should not
                // be read;
                if (0 != oldColCount) {
                    currentOldColOffset = (int) wrapByteBuf.readUnsignedInt();
                }
                // new col value
                wrapByteBuf.readerIndex((int) (PREFIX_LENGTH + newColsOffset + BYTE_SIZE));
                int newColCount = wrapByteBuf.readInt();
                int currentNewColOffset = -1;
                if (0 != newColCount) {
                    currentNewColOffset = (int) wrapByteBuf.readUnsignedInt();
                }
                // start loop
                for (int i = 0; i < count; i++) {
                    // get pk boolean
                    boolean isPk = false;
                    if (pks != null && pks.contains(i)) {
                        isPk = true;
                    }
                    // get real op type
                    int type = 0;
                    wrapByteBuf.readerIndex(
                            PREFIX_LENGTH
                                    + (int) colTypesOffset
                                    + BYTE_SIZE
                                    + INT_SIZE
                                    + i * elementSize);
                    switch (elementSize) {
                        case 1:
                            type = wrapByteBuf.readUnsignedByte();
                            break;
                        case 2:
                            type = wrapByteBuf.readUnsignedShort();
                            break;
                        case 4:
                            type = (int) wrapByteBuf.readUnsignedInt();
                            break;
                        case 8:
                            type = (int) wrapByteBuf.readLong();
                            break;
                    }
                    // get col flag
                    int flag = 0;
                    if (colFlagOffset > 0) {
                        wrapByteBuf.readerIndex(
                                PREFIX_LENGTH
                                        + (int) colFlagOffset
                                        + BYTE_SIZE
                                        + INT_SIZE
                                        + i * elementSize);
                        flag = wrapByteBuf.readUnsignedByte();
                    }
                    boolean notNull = false;
                    if (colNotNullOffset > 0) {
                        wrapByteBuf.readerIndex(
                                PREFIX_LENGTH
                                        + (int) colNotNullOffset
                                        + BYTE_SIZE
                                        + INT_SIZE
                                        + i * elementSize);
                        notNull = wrapByteBuf.readBoolean();
                    }
                    // get real encoding
                    String realEncoding = encodingStr;

                    // now deliver have fix encoding offset bug, encoding has been decoded correctly
                    // add db2 compatible code for new version db2 reader
                    // old else will also saved for old version store
                    // this code will deprecated in future release
                    // correct oracle code if oralce reader has update delivier version or correct
                    // type code
                    if (colEncodingsCount > 0) {
                        wrapByteBuf.readerIndex(
                                (int)
                                        (PREFIX_LENGTH
                                                + colsEncodingOffset
                                                + BYTE_SIZE
                                                + INT_SIZE
                                                + (i + 1) * INT_SIZE));
                        int nextEncodingOffset = (int) wrapByteBuf.readUnsignedInt();
                        ByteString encodingByteString =
                                new ByteString(
                                        wrapByteBuf.array(),
                                        PREFIX_LENGTH
                                                + currentEncodingOffset
                                                + BYTE_SIZE
                                                + INT_SIZE
                                                + (count + 1) * INT_SIZE
                                                + (int) colsEncodingOffset,
                                        nextEncodingOffset - currentEncodingOffset - 1);
                        realEncoding = encodingByteString.toString();
                        currentEncodingOffset = nextEncodingOffset;
                    }
                    realEncoding = logTypeHelper.correctEncoding(type, realEncoding);
                    type = logTypeHelper.correctCode(type, realEncoding);

                    // colName
                    wrapByteBuf.readerIndex(
                            (int)
                                    (PREFIX_LENGTH
                                            + colNamesOffset
                                            + BYTE_SIZE
                                            + INT_SIZE
                                            + (i + 1) * INT_SIZE));
                    int nextColNameOffset = (int) wrapByteBuf.readUnsignedInt();
                    ByteString colNameByteString =
                            new ByteString(
                                    wrapByteBuf.array(),
                                    PREFIX_LENGTH
                                            + currentColNameOffset
                                            + BYTE_SIZE
                                            + INT_SIZE
                                            + (count + 1) * INT_SIZE
                                            + (int) colNamesOffset,
                                    nextColNameOffset - currentColNameOffset - 1);
                    String columnName = colNameByteString.toString();
                    currentColNameOffset = nextColNameOffset;
                    // old col
                    if (oldColCount != 0) {
                        wrapByteBuf.readerIndex(
                                (int)
                                        (PREFIX_LENGTH
                                                + oldColsOffset
                                                + BYTE_SIZE
                                                + INT_SIZE
                                                + (i + 1) * INT_SIZE));
                        int nextOldColOffset = (int) wrapByteBuf.readUnsignedInt();
                        ByteString value = null;
                        if (nextOldColOffset != currentOldColOffset) {
                            value =
                                    new ByteString(
                                            wrapByteBuf.array(),
                                            PREFIX_LENGTH
                                                    + currentOldColOffset
                                                    + BYTE_SIZE
                                                    + INT_SIZE
                                                    + (count + 1) * INT_SIZE
                                                    + (int) oldColsOffset,
                                            nextOldColOffset - currentOldColOffset - 1);
                        }
                        Field field = new Field(columnName, type, realEncoding, value, isPk);
                        field.setFlag(flag);
                        field.setNotNull(notNull);
                        fields.add(field);
                        field.setPrev(true);
                        currentOldColOffset = nextOldColOffset;
                    }
                    // new col
                    if (newColCount != 0) {
                        wrapByteBuf.readerIndex(
                                (int)
                                        (PREFIX_LENGTH
                                                + newColsOffset
                                                + BYTE_SIZE
                                                + INT_SIZE
                                                + (i + 1) * INT_SIZE));
                        int nextNewColOffset = (int) wrapByteBuf.readUnsignedInt();
                        ByteString value = null;
                        if (currentNewColOffset != nextNewColOffset) {
                            value =
                                    new ByteString(
                                            wrapByteBuf.array(),
                                            PREFIX_LENGTH
                                                    + currentNewColOffset
                                                    + BYTE_SIZE
                                                    + INT_SIZE
                                                    + (count + 1) * INT_SIZE
                                                    + (int) newColsOffset,
                                            nextNewColOffset - currentNewColOffset - 1);
                        }
                        Field field = new Field(columnName, type, realEncoding, value, isPk);
                        field.setFlag(flag);
                        field.setNotNull(notNull);
                        fields.add(field);
                        field.setPrev(false);
                        currentNewColOffset = nextNewColOffset;
                    }
                }
            }

        } catch (Exception e) {
            fields = null;
            throw new LogMessageException(e.getMessage(), e);
        }

        return fields;
    }

    @Override
    public int getFieldCount() {
        List<Field> fields = getFieldList();
        if (fields == null) {
            return 0;
        }
        return fields.size();
    }

    public List<Integer> getPrimaryKeyIndex() {
        try {
            if (primaryKeyIndexList == null) {
                if ((int) pkKeysOffset < 0) {
                    primaryKeyIndexList = new ArrayList<>();
                } else {
                    primaryKeyIndexList =
                            BinaryMessageUtils.getArray(byteBuf.array(), (int) pkKeysOffset);
                }
            }
        } catch (Exception e) {
            throw new LogMessageException(e.getMessage(), e.getCause());
        }
        return primaryKeyIndexList;
    }

    @Override
    public void parse(final byte[] data) throws Exception {
        ByteBuf inner = Unpooled.wrappedBuffer(data, 0, data.length).order(ByteOrder.LITTLE_ENDIAN);
        setByteBuf(inner);
    }

    /**
     * Get the primary data to avoid parsing
     *
     * @return primary data
     */
    @Override
    public byte[] getRawData() {
        if (byteBuf == null) {
            return null;
        }

        return byteBuf.array();
    }

    /**
     * 解析buffer
     *
     * @param byteBuf ByteBuf
     * @throws Exception 解析出错
     */
    public void setByteBuf(ByteBuf byteBuf) throws Exception {
        this.byteBuf = byteBuf;
        // omit first 12 bytes
        byteBuf.readerIndex(PREFIX_LENGTH);
        if ((byteBuf.readByte() & DataType.DT_MASK) != DataType.DT_UINT8) {
            throw new Exception("parse error");
        }
        long count = byteBuf.readInt();
        boolean old = false;
        switch ((int) count) {
            case OLD_VERSION_2_HEADER_LEN:
                old = true;
                break;
            case VERSION_3_1_HEADER_LEN:
            case VERSION_3_HEADER_LEN:
            case NEW_VERSION_2_HEADER_LEN:
                break;
            default:
                throw new Exception("");
        }
        brVersion = byteBuf.readUnsignedByte();
        srcType = byteBuf.readUnsignedByte();
        op = byteBuf.readUnsignedByte();
        lastInLogEvent = byteBuf.readByte();
        srcCategory = byteBuf.readInt();
        id = byteBuf.readLong();
        timestamp = byteBuf.readLong();
        encoding = byteBuf.readInt();
        instanceOffset = byteBuf.readInt();
        // get timeMark
        timeMarkOffset = byteBuf.readInt();
        dbNameOffset = byteBuf.readInt();
        tbNameOffset = byteBuf.readInt();
        colNamesOffset = byteBuf.readInt();
        colTypesOffset = byteBuf.readInt();

        if (!old) {
            pkValOffset = byteBuf.readInt();
            fileNameOffset = byteBuf.readLong();
            fileOffset = byteBuf.readLong();
            if (fileNameOffset < -1 || fileOffset < -1) {
                throw new IOException(
                        "f: "
                                + fileNameOffset
                                + " and o: "
                                + fileOffset
                                + " should both be signed integer");
            }
            oldColsOffset = byteBuf.readInt();
            newColsOffset = byteBuf.readInt();
        } else {
            fileNameOffset = byteBuf.readInt();
            fileOffset = byteBuf.readInt();
            oldColsOffset = byteBuf.readInt();
            newColsOffset = byteBuf.readInt();
            pkValOffset = byteBuf.readInt();
        }

        pkKeysOffset = byteBuf.readInt();
        ukColsOffset = byteBuf.readInt();

        if (brVersion > 1) {
            colsEncodingOffset = byteBuf.readLong();
        }
        // thread id& trace id
        if (brVersion == 3) {
            filterRuleValOffset = byteBuf.readInt();
            tailOffset = byteBuf.readInt();

            long version = id >> 56;
            if (version >= 1) {
                metaVersion = byteBuf.readInt();
                colFlagOffset = byteBuf.readInt();
            }
            if (version >= 2) {
                colNotNullOffset = byteBuf.readInt();
            }
        }

        // timestamp,process heartbeat between tx
        Type type = Type.valueOf(op);
        String ts = Long.toString(timestamp);
        DbTypeEnum dbTypeEnum = getDbType();
        if (dbTypeEnum == DbTypeEnum.OB_MYSQL || dbTypeEnum == DbTypeEnum.OB_ORACLE) {
            globalSafeTimestamp.set(String.valueOf(fileNameOffset));
        } else {
            if (type == Type.BEGIN) {
                globalSafeTimestamp.set(ts);
                txEnd.set(false);
            }
            if (txEnd.get()) {
                globalSafeTimestamp.set(ts);
            }
            // set txEnd
            if (type == Type.COMMIT || type == Type.ROLLBACK) {
                txEnd.set(true);
            }
        }
        safeTimestamp = globalSafeTimestamp.get();
        if (isCheckCRC) {
            checkCRC();
        }
    }

    private void checkCRC() throws IOException {
        long value = getCRCValue();
        if (value == 0L) {
            return;
        }
        crc32.update(byteBuf.array(), 0, byteBuf.array().length - 4);
        long actual = crc32.getValue();
        crc32.reset();
        if (value != actual) {
            throw new IOException("crc 32 check failed,expect:" + value + ",actual:" + actual);
        }
    }

    @Override
    public String getTraceId() {
        List<ByteString> list =
                BinaryMessageUtils.getByteStringList(byteBuf.array(), filterRuleValOffset);
        if (list == null || list.size() == 0 || list.size() < 3) {
            return null;
        }
        ByteString traceId = list.get(2);
        return traceId == null ? null : traceId.toString();
    }

    @Override
    public String getOB10UniqueId() {
        List<ByteString> list =
                BinaryMessageUtils.getByteStringList(byteBuf.array(), filterRuleValOffset);
        if (list == null || list.size() == 0 || list.size() < 3) {
            return null;
        }
        ByteString ob10UniqueId = list.get(1);
        return ob10UniqueId == null ? null : ob10UniqueId.toString();
    }

    @Override
    public String getThreadId() throws Exception {
        long threadId = 0L;
        if (tailOffset == -1) {
            return null;
        }
        List<Integer> list = BinaryMessageUtils.getArray(byteBuf.array(), (int) tailOffset);
        if (list == null || list.size() == 0) {
            return null;
        }
        threadId += (long) list.get(0);
        threadId += ((long) list.get(1)) << 8;
        threadId += ((long) list.get(2)) << 16;
        threadId += ((long) list.get(3)) << 24;
        return String.valueOf(threadId);
    }

    private List<List<String>> getKeys(int valueOffset, List<ByteString> keys) {
        if (valueOffset == -1) {
            return null;
        }
        List<List<String>> result = new ArrayList<List<String>>();
        ByteBuf wrapByteBuf =
                Unpooled.wrappedBuffer(byteBuf.array()).order(ByteOrder.LITTLE_ENDIAN);

        // get field count
        wrapByteBuf.readerIndex(PREFIX_LENGTH + valueOffset + 1);
        int fieldCount = wrapByteBuf.readInt();

        // parse
        for (ByteString key : keys) {
            String keyStr = key.toString();
            int m = 0;
            while (true) {
                int i = keyStr.indexOf('(', m);
                if (i == -1) {
                    break;
                }
                int j = keyStr.indexOf(')', i);
                if (j == -1) {
                    log.error("Parse key error");
                    return null;
                }
                m = j;
                String[] parts = keyStr.substring(i + 1, j).split(",");
                StringBuilder sb = new StringBuilder();
                List<String> item = new ArrayList<String>();
                result.add(item);
                for (String indexStr : parts) {
                    int index = Integer.parseInt(indexStr);
                    wrapByteBuf.readerIndex(PREFIX_LENGTH + valueOffset + 5 + index * 4);
                    int start = (int) wrapByteBuf.readUnsignedInt();
                    int end = (int) wrapByteBuf.readUnsignedInt();
                    // uk perhaps null
                    if (end - start == 0) {
                        item.add(null);
                        continue;
                    }
                    String k =
                            new ByteString(
                                            wrapByteBuf.array(),
                                            PREFIX_LENGTH
                                                    + valueOffset
                                                    + 5
                                                    + (fieldCount + 1) * 4
                                                    + start,
                                            end - start - 1)
                                    .toString();
                    sb.append(k);
                    item.add(k);
                }
                if (sb.length() > 0) {
                    keysValue.add(sb.toString());
                } else {
                    keysValue.add(null);
                }
            }
        }
        return result;
    }

    /**
     * 获得pk uk value,combine
     *
     * @return key集合
     */
    @Override
    public Set<String> getKeysValue() {
        List<List<String>> prev = new ArrayList<List<String>>();
        List<List<String>> next = new ArrayList<List<String>>();

        try {
            if (keysValue != null) {
                return keysValue;
            }
            if (colNamesOffset < 0
                    || colTypesOffset < 0
                    || oldColsOffset < 0
                    || newColsOffset < 0) {
                return null;
            }

            // get key str
            keysValue = new HashSet<String>();
            List<ByteString> keys =
                    BinaryMessageUtils.getByteStringList(byteBuf.array(), (int) pkValOffset);
            if (keys == null || keys.size() == 0) {
                return null;
            }
            // get value offset by op type
            switch (getOpt()) {
                case INSERT:
                case REPLACE:
                case INDEX_INSERT:
                case INDEX_REPLACE:
                    next = getKeys((int) newColsOffset, keys);
                    break;
                case DELETE:
                case INDEX_DELETE:
                    prev = getKeys((int) oldColsOffset, keys);
                    break;
                case UPDATE:
                case INDEX_UPDATE:
                    switch (getDbType()) {
                        case OB_MYSQL:
                        case OB_ORACLE:
                            prev.addAll(getKeys((int) oldColsOffset, keys));
                            next.addAll(getKeys((int) newColsOffset, keys));
                            if (!prev.equals(next)) {
                                setKeyChange(true);
                            }
                            break;
                        case UNKNOWN:
                            next.addAll(getKeys((int) newColsOffset, keys));
                            break;
                    }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return keysValue;
    }

    @Override
    public List<String> getPrimaryValues() {
        try {
            List<Field> filedList = getFieldList();
            List<String> primaryKeys = getPrimaryKeyValue();
            if (primaryKeys == null) {
                primaryKeys = new ArrayList<String>();
            }
            Map<String, String> keyValue = new HashMap<String, String>();
            if (filedList != null) {
                for (Field field : filedList) {
                    if ("binary".equalsIgnoreCase(field.getEncoding())) {
                        keyValue.put(
                                field.getFieldname().toLowerCase(),
                                field.getValue() == null
                                        ? null
                                        : new String(field.getValue().getBytes()));
                    } else {
                        keyValue.put(
                                field.getFieldname().toLowerCase(),
                                field.getValue() == null
                                        ? null
                                        : field.getValue().toString(field.getEncoding()));
                    }
                }
            }
            List<String> primaryValues = new ArrayList<String>(primaryKeys.size());
            for (String primaryKey : primaryKeys) {
                primaryValues.add(keyValue.get(primaryKey.toLowerCase()));
            }
            return primaryValues;
        } catch (Exception e) {
            throw new LogMessageException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 获取主键value
     *
     * @return 主键列的列表
     */
    @Override
    public List<String> getPrimaryKeyValue() {
        try {
            if (pkValues != null) {
                return pkValues;
            }
            if (colNamesOffset < 0 || pkKeysOffset < 0) {
                return null;
            }
            pkValues = new ArrayList<String>();
            List<Integer> pks = BinaryMessageUtils.getArray(byteBuf.array(), (int) pkKeysOffset);
            List<ByteString> names =
                    BinaryMessageUtils.getByteStringList(byteBuf.array(), colNamesOffset);
            if (pks != null) {
                for (int idx : pks) {
                    pkValues.add(names.get(idx).toString(DEFAULT_ENCODING));
                }
            }
            return pkValues;

        } catch (Exception e) {
            throw new LogMessageException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Get tuples of index. For example, column 1 is the primary key and column 2 and 3 are unique
     * keys. The returned format is the list of two arrays, the first array is {0} and the seconds
     * array is {1,2}
     *
     * @return the tuples of index for primary and unique constraints.
     */
    @Override
    public List<int[]> getPrimaryAndUniqueConstraintColumnIndexTuples() {
        List<int[]> tuples = new ArrayList<int[]>();
        try {
            if ((int) pkValOffset > 0) {
                List<ByteString> rawConstraintByteString =
                        BinaryMessageUtils.getByteStringList(byteBuf.array(), pkValOffset);
                if (rawConstraintByteString != null && !rawConstraintByteString.isEmpty()) {
                    /**
                     * The raw format is "(0,1),(2,3)" or "(", the last one is for empty primary or
                     * unique constraint
                     */
                    for (int i = 0; i < rawConstraintByteString.size(); i++) {
                        String rawConstraintStringArray = rawConstraintByteString.get(i).toString();
                        for (String rawConstraintString : rawConstraintStringArray.split("\\),")) {
                            if (StringUtils.isNotEmpty(rawConstraintString)) {
                                int m = 0;
                                while (m < rawConstraintString.length()) {
                                    int leftIndex = rawConstraintString.indexOf('(', m);
                                    if (leftIndex == -1) {
                                        break;
                                    }
                                    int rightIndex = rawConstraintString.indexOf(')', leftIndex);
                                    if (rightIndex == -1) {
                                        if (rawConstraintString.length() == 1) {
                                            throw new IOException(
                                                    "Missing index and ) for constraints: "
                                                            + rawConstraintByteString);
                                        } else {
                                            /* throw new IOException("Missing ] for constraints: " + rawConstraintByteString
                                            + "the correct format shoud like (0,1),(2,3) or ( "); */
                                            rightIndex = rawConstraintString.length();
                                        }
                                    }
                                    m = rightIndex;
                                    String[] parts =
                                            rawConstraintString
                                                    .substring(leftIndex + 1, rightIndex)
                                                    .split(",");
                                    if (parts != null && parts.length > 0) {
                                        int[] tuple = new int[parts.length];
                                        for (int j = 0; j < parts.length; j++) {
                                            tuple[j] = Integer.valueOf(parts[j]).intValue();
                                        }
                                        tuples.add(tuple);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new LogMessageException(e);
        }

        return tuples;
    }

    @Override
    public List<ByteString> getFirstPKValue() {
        try {
            if ((int) pkValOffset < 0) {
                return null;
            } else {
                return BinaryMessageUtils.getByteStringList(byteBuf.array(), pkValOffset);
            }

        } catch (Exception e) {
            throw new LogMessageException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public List<String> getPrimaryKeysList() {
        List<String> primaryKeyStringList = getPrimaryKeyValue();
        List<String> pkKeyName = new ArrayList<String>();
        if (primaryKeyStringList != null) {
            for (String idx : primaryKeyStringList) {
                pkKeyName.add(idx);
            }
            return pkKeyName;
        } else {
            return null;
        }
    }

    @Override
    public String getPrimaryKeys() {
        List<String> primaryKeyStringList = getPrimaryKeyValue();
        if (primaryKeyStringList != null) {
            StringBuilder pkKeyName = new StringBuilder();
            for (String idx : primaryKeyStringList) {
                if (pkKeyName.length() != 0) {
                    pkKeyName.append(",");
                }
                pkKeyName.append(idx);
            }
            return pkKeyName.toString();
        } else {
            return "";
        }
    }

    public long getMetaVersion() {
        return this.metaVersion;
    }

    @Override
    public String getUniqueColNames() {
        try {
            if (uniqueKeyList == null) {
                if ((int) ukColsOffset < 0) {
                    uniqueKeyList = "";
                    return uniqueKeyList;
                } else {
                    List<Integer> uks =
                            BinaryMessageUtils.getArray(byteBuf.array(), (int) ukColsOffset);
                    List<ByteString> names =
                            BinaryMessageUtils.getByteStringList(byteBuf.array(), colNamesOffset);
                    StringBuilder ukKeyName = new StringBuilder();
                    if (uks != null && names != null) {
                        for (int idx : uks) {
                            if (ukKeyName.length() != 0) {
                                ukKeyName.append(",");
                            }
                            ukKeyName.append(names.get(idx).toString(DEFAULT_ENCODING));
                        }
                    }
                    uniqueKeyList = ukKeyName.toString();
                    return uniqueKeyList;
                }
            }
        } catch (Exception e) {
            throw new LogMessageException(e.getMessage(), e.getCause());
        }
        return uniqueKeyList;
    }

    public List<Long> getTimeMarks() throws IOException {
        if (timeMarkOffset == -1) {
            return null;
        } else {
            if (timeMarks == null) {
                timeMarks = BinaryMessageUtils.getArray(byteBuf.array(), (int) timeMarkOffset);
            }
            return timeMarks;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("type:" + getOpt()).append(SEP);
        builder.append("record_id:" + getId()).append(SEP);
        builder.append("db:" + getDbName()).append(SEP);
        builder.append("tb:" + getTableName()).append(SEP);
        builder.append("serverId:" + getServerId()).append(SEP);
        builder.append("checkpoint:" + getCheckpoint()).append(SEP);
        builder.append("primary_value:" + getPrimaryKeys()).append(SEP);
        builder.append("unique_keys:" + getUniqueColNames()).append(SEP);
        builder.append(SEP);
        getFieldList();
        if (fields != null) {
            for (Field field : fields) {
                builder.append(field.toString());
            }
        }
        builder.append(SEP);
        return builder.toString();
    }

    private long getCRCValue() throws IOException {
        long crcValue = 0L;
        if (tailOffset == -1) {
            return 0L;
        }
        List<Integer> list = BinaryMessageUtils.getArray(byteBuf.array(), (int) tailOffset);
        if (list == null || list.size() != 12) {
            return 0L;
        }
        crcValue += (long) list.get(8);
        crcValue += ((long) list.get(9)) << 8;
        crcValue += ((long) list.get(10)) << 16;
        crcValue += ((long) list.get(11)) << 24;
        return crcValue;
    }

    @Override
    public String getTimestampUsec() throws IOException {
        long usecs = 0L;
        if (tailOffset == -1) {
            return "0";
        }
        List<Integer> list = BinaryMessageUtils.getArray(byteBuf.array(), (int) tailOffset);
        if (list == null || list.size() != 12) {
            return "0";
        }
        usecs += (long) list.get(4);
        usecs += ((long) list.get(5)) << 8;
        usecs += ((long) list.get(6)) << 16;
        usecs += ((long) list.get(7)) << 24;
        return Long.toString(usecs);
    }

    public String getEncodingStr() {
        try {
            return BinaryMessageUtils.getString(byteBuf.array(), (int) encoding, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new LogMessageException(e.getMessage(), e.getCause());
        }
    }

    public long getFileNameOffset() {
        return fileNameOffset;
    }

    public long getFileOffset() {
        return fileOffset;
    }

    public ByteBuf getByteBuff() {
        return byteBuf;
    }

    public void releaseContents() {
        // release reference to let gc collect mem
        this.fields = null;
        this.pkValues = null;
        this.keysValue = null;
        this.primaryKeyIndexList = null;
    }
}
