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

import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Utils class for crypto.
 */
public class CryptoUtil {

    /**
     * default cipher key
     */
    private static final String KEY = "LogProxy123*";

    /**
     * AES key length
     */
    private static final int AES_KEY_SIZE = 256;

    /**
     * GCM tag length
     */
    private static final int GCM_TAG_LENGTH = 16;

    /**
     * create an Encryptor instance using given cipher key
     *
     * @param key cipher key
     * @return an Encryptor instance
     */
    public static Encryptor newEncryptor(String key) {
        return new Encryptor(key);
    }

    /**
     * create an Encryptor instance using default cipher key
     *
     * @return an Encryptor instance
     */
    public static Encryptor newEncryptor() {
        return new Encryptor(KEY);
    }

    public static class Encryptor {
        private Cipher cipher = null;   // not thread-safe
        private byte[] key = new byte[AES_KEY_SIZE / 16];
        private byte[] iv = new byte[12];

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
         * encrypt given text
         *
         * @param text original text
         * @return encrypted data
         */
        public byte[] encrypt(String text) {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            try {
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
                return cipher.doFinal(text.getBytes());
            } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
                System.out.println("failed to encrypt AES 256 GCM: " + e);
                return null;
            }
        }

        /**
         * decrypt given data
         *
         * @param cipherText encrypted data
         * @return the original string
         */
        public String decrypt(byte[] cipherText) {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            try {
                cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
                byte[] decryptedText = cipher.doFinal(cipherText);
                return new String(decryptedText);
            } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
                System.out.println("failed to decrypt AES 256 GCM: " + e);
                return "";
            }
        }
    }

    /**
     * Compute hash value of given array of bytes
     *
     * @param bytes origin array of bytes
     * @return the array of bytes for the resulting hash value
     */
    public static byte[] sha1(byte[] bytes) {
        return DigestUtils.sha1(bytes);
    }

    /**
     * Compute hash value of given string
     *
     * @param text origin string
     * @return the array of bytes for the resulting hash value
     */
    public static byte[] sha1(String text) {
        return DigestUtils.sha1(text);
    }
}
