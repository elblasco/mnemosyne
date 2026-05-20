package it.unitn.apcm.blasco.mnemosyne.utils;

import jakarta.servlet.http.Cookie;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;

public class Utils {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    protected static final String DB_URL = "jdbc:sqlite:/data/mnemosyne.db";
    private static final String PROVIDER = "BC";
    private static final String TRANSFORMATION = "ChaCha20-Poly1305";
    private static final String KEY_ALG = "ChaCha20";
    private static final String PRNG_ALG = "DEFAULT";
    private static final String HASH_ALG = "SHA-256";

    /**
     * Hashes a string with SHA-256 using Bouncy Castle.
     *
     * @param password The string to hash.
     * @param salt     Salt to append.
     * @return SHA-256 hash
     * @throws NoSuchAlgorithmException If the current hash function is unavailable.
     * @throws NoSuchProviderException  If the current provider is not registered.
     */
    public static byte[] hashPassword(byte[] password, byte[] salt) throws NoSuchAlgorithmException, NoSuchProviderException {
        return MessageDigest
                .getInstance(HASH_ALG, PROVIDER)
                .digest(
                        ByteBuffer
                                .allocate(password.length + salt.length)
                                .put(password)
                                .put(salt)
                                .array()
                );
    }

    /**
     * Encrypt the input plaintext with the give key
     *
     * @param plaintext The data to encrypt.
     * @param key       The key to use for the encryption, this will be extended with a random nonce.
     * @return byte matrix containing [ciphertext, tag, nonce used]
     * @throws NoSuchAlgorithmException           If the current transformation is unavailable.
     * @throws NoSuchProviderException            If the current provider is not registered.
     * @throws NoSuchPaddingException             If current transformation contains a padding scheme that is not available.
     * @throws InvalidKeyException                If the given key is inappropriate for initializing this cipher, or its keysize
     *                                            exceeds the maximum allowable keysize.
     * @throws InvalidAlgorithmParameterException If the given algorithm parameters are inappropriate for the current transformation.
     */
    public static byte[][] encrypt(byte[] plaintext, byte[] key) throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
        byte[] nonce = getRandomBytes(12);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION, PROVIDER);
        cipher.init(
                Cipher.ENCRYPT_MODE,
                new SecretKeySpec(key, KEY_ALG),
                new IvParameterSpec(nonce)
        );

        byte[] output = cipher.doFinal(plaintext);

        int tagLen = 16;
        int cipherTextLen = output.length - tagLen;
        byte[][] result = new byte[][]{
                Arrays.copyOfRange(output, 0, cipherTextLen),
                Arrays.copyOfRange(output, cipherTextLen, output.length),
                nonce
        };
        return result;
    }

    /**
     * Decrypt the input (ciphertext||tag) with the give (key||nonce)
     *
     * @param ciphertext The data to decrypt.
     * @param tag        The integrity check bytes.
     * @param key        The key to use for the encryption, this will be extended with a random nonce.
     * @param nonce      The random nonce that has been appended to the key during the encryption phase
     * @return the plaintext in byte[] format
     * @throws NoSuchAlgorithmException           If the current transformation is unavailable.
     * @throws NoSuchProviderException            If the current provider is not registered.
     * @throws NoSuchPaddingException             If current transformation contains a padding scheme that is not available.
     * @throws InvalidKeyException                If the given key is inappropriate for initializing this cipher, or its keysize
     *                                            exceeds the maximum allowable keysize.
     * @throws InvalidAlgorithmParameterException If the given algorithm parameters are inappropriate for the current transformation.
     */
    public static byte[] decrypt(byte[] ciphertext, byte[] tag, byte[] key, byte[] nonce) throws
            NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION, PROVIDER);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, KEY_ALG), new IvParameterSpec(nonce));

        return cipher.doFinal(
                ByteBuffer
                        .allocate(ciphertext.length + tag.length)
                        .put(ciphertext)
                        .put(tag)
                        .array()
        );
    }

    public static byte[] getRandomBytes(int length) throws NoSuchAlgorithmException, NoSuchProviderException {
        byte[] randomBytes = new byte[length];
        SecureRandom.getInstance(PRNG_ALG, PROVIDER).nextBytes(randomBytes);
        return randomBytes;
    }

    public static byte[] decodeHexBytes(byte[] hexBytes) throws IllegalArgumentException {
        if (hexBytes.length % 2 != 0) {
            throw new IllegalArgumentException("Input length must be even");
        }

        byte[] result = new byte[hexBytes.length / 2];

        for (int i = 0; i < hexBytes.length; i += 2) {
            int high = Character.digit((char) hexBytes[i], 16);
            int low = Character.digit((char) hexBytes[i + 1], 16);

            if (high < 0 || low < 0) {
                throw new IllegalArgumentException("Invalid hex character");
            }

            result[i / 2] = (byte) ((high << 4) | low);
        }

        return result;
    }

    public static Cookie generateCookie(String name, String data) {
        Cookie userCookie = new Cookie(name, data);
        userCookie.setMaxAge(10);
        userCookie.setHttpOnly(true);
        userCookie.setSecure(true);
        userCookie.setPath("/mnemosyne");
        return userCookie;
    }
}
