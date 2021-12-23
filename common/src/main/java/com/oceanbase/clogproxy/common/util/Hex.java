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


import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.commons.codec.DecoderException;

/** This class is used to convert hexadecimal strings. */
public final class Hex {

    /**
     * Returns a multi-line hexadecimal dump of the array that is easy to read by humans.
     *
     * @param array An array of bytes
     * @return A multi-line hexadecimal dump string
     */
    public static String dump(byte[] array) {
        return dump(array, 0, array.length);
    }

    /**
     * Returns a multi-line hexadecimal dump of the specified sub-region of bytes that is easy to
     * read by humans.
     *
     * @param bytes An array of bytes
     * @param offset The offset of the sub-region start position
     * @param length The length of the sub-region
     * @return A multi-line hexadecimal dump string
     */
    public static String dump(byte[] bytes, int offset, int length) {
        return ByteBufUtil.prettyHexDump(Unpooled.wrappedBuffer(bytes, offset, length));
    }

    /**
     * Converts an array of bytes into a string representing the hexadecimal values of each byte in
     * order.
     *
     * @param bytes An array of bytes
     * @return A String containing uppercase hexadecimal characters
     */
    public static String str(byte[] bytes) {
        return org.apache.commons.codec.binary.Hex.encodeHexString(bytes, false);
    }

    /**
     * Converts a String representing hexadecimal values into an array of bytes of those same
     * values.
     *
     * @param hexStr A String representing hexadecimal values
     * @return An array of bytes
     */
    public static byte[] toBytes(String hexStr) {
        try {
            return org.apache.commons.codec.binary.Hex.decodeHex(hexStr);
        } catch (DecoderException e) {
            return null;
        }
    }
}
