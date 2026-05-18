package it.unitn.apcm.blasco.mnemosyne.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

public class Utils {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static final String DB_URL = "jdbc:sqlite:/data/mnemosyne.db";

    /**
     * Hashes a string with SHA-256 using Bouncy Castle.
     *
     * @param password The string to hash.
     * @param salt     Salt to append.
     * @return Hexadecimal representation of the SHA-256 hash.
     * @throws NoSuchAlgorithmException If "SHA-256" is unavailable.
     * @throws NoSuchProviderException  If Bouncy Castle provider is not registered.
     */
    static public byte[] hashPassword(byte[] password, byte[] salt) throws NoSuchAlgorithmException, NoSuchProviderException {
        ByteBuffer buffer = ByteBuffer.wrap(password);
        buffer.put(salt);
        MessageDigest digest = MessageDigest.getInstance("SHA-256", "BC");
        return digest.digest(buffer.array());
    }

    public static void addCookie(String name, String data, HttpServletResponse resp) {
        Cookie userCookie = new Cookie(name, data);
        userCookie.setMaxAge(10);
        userCookie.setHttpOnly(true);
        userCookie.setSecure(true);
        userCookie.setPath("/mnemosyne");
        resp.addCookie(userCookie);
    }
}
