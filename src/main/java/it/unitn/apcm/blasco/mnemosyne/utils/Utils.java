package it.unitn.apcm.blasco.mnemosyne.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
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
     * @return         Hexadecimal representation of the SHA-256 hash.
     * @throws NoSuchAlgorithmException If "SHA-256" is unavailable.
     * @throws NoSuchProviderException  If Bouncy Castle provider is not registered.
     */
    static public String hashPassword(String password, String salt) throws NoSuchAlgorithmException, NoSuchProviderException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256", "BC");
        byte[] hashBytes = digest.digest((password + salt).getBytes(StandardCharsets.UTF_8));
        return Hex.toHexString(hashBytes);
    }
}
