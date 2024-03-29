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

import java.io.UnsupportedEncodingException;

/**
 * ByteString store an array of bytes and take over all related transfers, such as judge if it
 * should be null, empty or in some an encoding.
 */
public class ByteString {
    private int len;

    private int offset;

    private byte[] bytes;

    public ByteString(byte[] bytes, int len) {
        this.bytes = bytes;
        this.len = len;
    }

    public ByteString(byte[] bytes, int offset, int len) {
        this.bytes = bytes;
        this.len = len;
        this.offset = offset;
    }

    /**
     * Convert the bytes to any encoding.
     *
     * @param encoding the target encoding.
     * @return the encoded string.
     */
    public String toString(final String encoding) {

        if (len == 0) {
            return "";
        }

        if ("binary".equalsIgnoreCase(encoding)) {
            throw new IllegalArgumentException(
                    "field encoding: binary, use getBytes() instead of toString()");
        }

        String realEncoding = encoding;
        if (encoding.isEmpty() || "null".equalsIgnoreCase(encoding)) {
            realEncoding = "ASCII";
        } else if ("utf8mb4".equalsIgnoreCase(encoding)) {
            realEncoding = "utf8";
        } else if ("latin1".equalsIgnoreCase(encoding)) {
            realEncoding = "cp1252";
        } else if ("latin2".equalsIgnoreCase(encoding)) {
            realEncoding = "iso-8859-2";
        }
        try {
            return new String(bytes, offset, len, realEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        if (len == 0) {
            return "";
        }
        byte[] byteArray = this.bytes;
        char[] charArray = new char[len];
        for (int i = 0; i < len; i++) {
            charArray[i] = (char) byteArray[i + offset];
        }
        return String.valueOf(charArray);
    }

    public byte[] getBytes() {
        byte[] t = new byte[len];
        System.arraycopy(bytes, offset, t, 0, len);
        return t;
    }

    public int getLen() {
        return len;
    }

    public int getOffset() {
        return offset;
    }

    public byte[] getRawBytes() {
        return bytes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ByteString)) {
            return false;
        }
        ByteString other = (ByteString) obj;
        if (this.getLen() != other.getLen()) {
            return false;
        }
        for (int i = 0; i < getLen(); i++) {
            byte x = bytes[offset + i];
            byte y = other.getRawBytes()[other.getOffset() + i];
            if (x != y) {
                return false;
            }
        }
        return true;
    }
}
