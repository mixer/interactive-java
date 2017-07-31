/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mixer.interactive.util;

/**
 * <p>Encodes signed and unsigned values using a common variable-length
 * scheme, found for example in
 * <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">Google's Protocol Buffers</a>.
 * It uses fewer bytes to encode smaller values, but will use slightly more bytes to encode large values.</p>
 *
 * <p>Signed values are further encoded using so-called zig-zag encoding
 * in order to make them "compatible" with variable-length encoding.</p>
 *
 * <p>Copied from <a target="_blank" href="https://github.com/addthis/stream-lib/blob/644e4e9be40e0eb69a9aef6acee6ca28643f174b/src/main/java/com/clearspring/analytics/util/Varint.java">https://github.com/addthis/stream-lib/blob/644e4e9be40e0eb69a9aef6acee6ca28643f174b/src/main/java/com/clearspring/analytics/util/Varint.java</a></p>
 *
 * @author      unascribed
 *
 * @since       1.0.0
 */
public class Varint {

    /**
     * Private constructor to prevent instantiation of an utility class.
     *
     * @since   1.0.0
     */
    private Varint() {
        // NO-OP
    }

    /**
     * Encodes a value using the variable-length encoding from
     * <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">Google's Protocol Buffers</a>.
     * Zig-zag is not used, so input must not be negative. This method treats negative input as like a
     * large unsigned value.
     *
     * @param   value
     *          Value to encode
     *
     * @return  Encoded value (as an array of bytes)
     *
     * @since   1.0.0
     */
    public static byte[] writeUnsignedVarInt(int value) {
        byte[] byteArrayList = new byte[10];
        int i = 0;
        while ((value & 0xFFFFFF80) != 0L) {
            byteArrayList[i++] = ((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        byteArrayList[i] = ((byte) (value & 0x7F));
        byte[] out = new byte[i + 1];
        for (; i >= 0; i--) {
            out[i] = byteArrayList[i];
        }
        return out;
    }

    /**
     * Decodes a value using the variable-length encoding from
     * <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html">Google's Protocol Buffers</a>
     *
     * @param   bytes
     *          Encoded byte array
     *
     * @return  Decoded value
     *
     * @since   1.0.0
     */
    public static int readUnsignedVarInt(byte[] bytes) {
        int value = 0;
        int i = 0;
        byte rb = Byte.MIN_VALUE;
        for (byte b : bytes) {
            rb = b;
            if ((b & 0x80) == 0) {
                break;
            }
            value |= (b & 0x7f) << i;
            i += 7;
            if (i > 35) {
                throw new IllegalArgumentException("Variable length quantity is too long");
            }
        }
        return value | (rb << i);
    }
}