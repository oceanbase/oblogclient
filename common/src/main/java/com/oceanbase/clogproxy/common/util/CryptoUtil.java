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
                System.out.println("failed to init AES key generator, exit!!! : " + e);
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
                System.out.println("failed to encrypt AES 256 GCM: " + e);
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
                System.out.println("failed to decrypt AES 256 GCM: " + e);
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
