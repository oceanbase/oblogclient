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

package com.oceanbase.clogproxy.common.util;


import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.digest.DigestUtils;

/** Utils class for crypto. */
public class CryptoUtil {

    /** Default cipher key. */
    private static final String KEY = "LogProxy123*";

    /** AES key length. */
    private static final int AES_KEY_SIZE = 256;

    /** GCM tag length. */
    private static final int GCM_TAG_LENGTH = 16;

    /**
     * Create an {@link Encryptor} instance using given cipher key.
     *
     * @param key Cipher key.
     * @return An {@link Encryptor} instance.
     */
    public static Encryptor newEncryptor(String key) {
        return new Encryptor(key);
    }

    /**
     * Create an Encryptor instance using default cipher key.
     *
     * @return An {@link Encryptor} instance.
     */
    public static Encryptor newEncryptor() {
        return new Encryptor(KEY);
    }

    /**
     * This class provides the functionality of encryption and decryption with a specific cipher
     * key.
     */
    public static class Encryptor {

        /** The cipher instance. */
        private Cipher cipher = null; // not thread-safe

        /**
         * The key material of the secret key.
         *
         * @see SecretKeySpec#SecretKeySpec(byte[], String)
         */
        private final byte[] key = new byte[AES_KEY_SIZE / 16];

        /**
         * The IV source buffer.
         *
         * @see GCMParameterSpec#GCMParameterSpec(int, byte[])
         */
        private final byte[] iv = new byte[12];

        /**
         * Constructor.
         *
         * @param cipherKey The cipher key used to generate {@link Encryptor#key} and {@link
         *     Encryptor#iv}.
         */
        private Encryptor(String cipherKey) {
            try {
                cipher = Cipher.getInstance("AES/GCM/NoPadding");
                byte[] cipherBytes = cipherKey.getBytes();
                System.arraycopy(cipherBytes, 0, key, 0, Math.min(key.length, cipherBytes.length));
                System.arraycopy(cipherBytes, 0, iv, 0, Math.min(iv.length, cipherBytes.length));

            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                System.exit(-1);
            }
        }

        /**
         * Encrypt given text.
         *
         * @param text The original text.
         * @return Encrypted data.
         */
        public byte[] encrypt(String text) {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            try {
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
                return cipher.doFinal(text.getBytes());
            } catch (InvalidKeyException
                    | InvalidAlgorithmParameterException
                    | IllegalBlockSizeException
                    | BadPaddingException e) {
                return null;
            }
        }

        /**
         * Decrypt given data.
         *
         * @param cipherText Encrypted data.
         * @return The original string.
         */
        public String decrypt(byte[] cipherText) {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            try {
                cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
                byte[] decryptedText = cipher.doFinal(cipherText);
                return new String(decryptedText);
            } catch (InvalidKeyException
                    | InvalidAlgorithmParameterException
                    | IllegalBlockSizeException
                    | BadPaddingException e) {
                return "";
            }
        }
    }

    /**
     * Compute hash value of given array of bytes.
     *
     * @param bytes The origin array of bytes.
     * @return The array of bytes for the resulting hash value.
     */
    public static byte[] sha1(byte[] bytes) {
        return DigestUtils.sha1(bytes);
    }

    /**
     * Compute hash value of given string.
     *
     * @param text The origin string.
     * @return The array of bytes for the resulting hash value.
     */
    public static byte[] sha1(String text) {
        return DigestUtils.sha1(text);
    }
}
