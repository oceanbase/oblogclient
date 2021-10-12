/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.common.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class Decoder {

    public static String decodeStringInt(ByteBuf buffer) {
        if (buffer.readableBytes() < Integer.BYTES) {
            return null;
        }
        buffer.markReaderIndex();
        int length = buffer.readInt();
        if (buffer.readableBytes() < length) {
            buffer.resetReaderIndex();
            return null;
        }
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        String str = new String(bytes);
        if (str.isEmpty()) {
            throw new RuntimeException("decode string is null or empty");
        }
        return str;
    }

    public static String decodeStringByte(ByteBuf buffer) {
        if (buffer.readableBytes() < Byte.BYTES) {
            return null;
        }
        buffer.markReaderIndex();
        short length = buffer.readByte();
        if (buffer.readableBytes() < length) {
            buffer.resetReaderIndex();
            return null;
        }
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        String str = new String(bytes);
        if (str.isEmpty()) {
            throw new RuntimeException("decode string is null or empty");
        }
        return str;
    }

    public static ByteBuf encodeStringInt(String string) {
        if (string == null || string.length() == 0) {
            throw new RuntimeException("encode string is null or empty");
        }
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer(4 + string.length());
        byteBuf.writeInt(string.length());
        byteBuf.writeBytes(string.getBytes());
        return byteBuf;
    }
}
