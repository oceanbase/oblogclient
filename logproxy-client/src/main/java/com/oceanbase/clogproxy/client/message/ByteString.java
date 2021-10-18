/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client.message;

import java.io.UnsupportedEncodingException;

public class ByteString {
    private int    len;

    private int    offset;

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
