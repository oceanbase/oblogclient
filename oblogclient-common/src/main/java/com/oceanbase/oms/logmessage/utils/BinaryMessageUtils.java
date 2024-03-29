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

package com.oceanbase.oms.logmessage.utils;

import com.oceanbase.oms.logmessage.ByteString;
import com.oceanbase.oms.logmessage.enums.DataType;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/** Utils class for binary message. */
public class BinaryMessageUtils {

    private static final int PREFIX_LENGTH = 12;

    /**
     * Get string begin with offset.
     *
     * @param data A bytes array.
     * @param offset Reading offset.
     * @param encoding String encoding.
     * @return Result string.
     * @throws UnsupportedEncodingException When the encoding is not supported.
     */
    public static String getString(byte[] data, int offset, String encoding)
            throws UnsupportedEncodingException {
        ByteBuf wrapByteBuf = Unpooled.wrappedBuffer(data).order(ByteOrder.LITTLE_ENDIAN);
        wrapByteBuf.readerIndex(PREFIX_LENGTH + offset);
        byte t = wrapByteBuf.readByte();
        if ((t & DataType.DC_ARRAY) != 0 || (t & DataType.DC_NULL) != 0) {
            return null;
        }
        int length = (int) wrapByteBuf.readUnsignedInt();
        return new String(wrapByteBuf.array(), PREFIX_LENGTH + 5 + offset, length - 1, encoding);
    }

    /**
     * Get list begin with offset.
     *
     * @param data A bytes array.
     * @param offset Reading offset.
     * @return Result list.
     * @throws IOException If data type is unsigned long.
     */
    public static List getArray(byte[] data, int offset) throws IOException {
        ByteBuf wrapByteBuf = Unpooled.wrappedBuffer(data).order(ByteOrder.LITTLE_ENDIAN);
        wrapByteBuf.readerIndex(PREFIX_LENGTH + offset);
        byte t = wrapByteBuf.readByte();
        if ((t & DataType.DC_ARRAY) == 0) {
            return null;
        }
        int count = (int) wrapByteBuf.readUnsignedInt();
        if (count == 0) {
            return null;
        }
        List lists = new ArrayList(count);
        int type = t & DataType.DT_MASK;
        for (int i = 0; i < count; i++) {
            switch (type) {
                case DataType.DT_INT8:
                    {
                        lists.add(wrapByteBuf.readByte());
                        break;
                    }
                case DataType.DT_UINT8:
                    {
                        lists.add((int) wrapByteBuf.readUnsignedByte());
                        break;
                    }
                case DataType.DT_INT16:
                    {
                        lists.add(wrapByteBuf.readShort());
                        break;
                    }
                case DataType.DT_UINT16:
                    {
                        lists.add((int) wrapByteBuf.readUnsignedShort());
                        break;
                    }
                case DataType.DT_INT32:
                    {
                        lists.add(wrapByteBuf.readInt());
                        break;
                    }
                case DataType.DT_UINT32:
                    {
                        lists.add((long) wrapByteBuf.readUnsignedInt());
                        break;
                    }
                case DataType.DT_INT64:
                    {
                        lists.add((long) wrapByteBuf.readLong());
                        break;
                    }
                case DataType.DT_UINT64:
                    {
                        throw new IOException("Unsupported unsigned long");
                    }
            }
        }
        return lists;
    }

    /**
     * Get ByteString begin with offset.
     *
     * @param data A bytes array.
     * @param offset Reading offset.
     * @return A list of {@link ByteString}.
     */
    public static List<ByteString> getByteStringList(byte[] data, long offset) {
        if (offset == -1) {
            return null;
        }
        ByteBuf wrapByteBuf = Unpooled.wrappedBuffer(data).order(ByteOrder.LITTLE_ENDIAN);
        wrapByteBuf.readerIndex((int) (PREFIX_LENGTH + offset));
        byte t = wrapByteBuf.readByte();
        if ((t & DataType.DC_ARRAY) == 0 || (t & DataType.DT_MASK) != DataType.DT_STRING) {
            throw new RuntimeException("Data type not array or not string");
        }
        int count = wrapByteBuf.readInt();
        if (count == 0) {
            return null;
        }
        int readBytes = 5;
        readBytes += (count + 1) * 4;
        List<ByteString> lists = new ArrayList<>(count);
        int currentOffset = (int) wrapByteBuf.readUnsignedInt();
        int nextOffset;
        for (int i = 0; i < count; i++) {
            nextOffset = (int) wrapByteBuf.readUnsignedInt();
            if (nextOffset == currentOffset) {
                lists.add(null);
            } else {
                lists.add(
                        new ByteString(
                                wrapByteBuf.array(),
                                PREFIX_LENGTH + currentOffset + readBytes + (int) offset,
                                nextOffset - currentOffset - 1));
            }
            currentOffset = nextOffset;
        }
        return lists;
    }
}
