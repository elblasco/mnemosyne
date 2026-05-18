package it.unitn.apcm.blasco.mnemosyne.utils;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;

import java.security.SecureRandom;
import java.security.Security;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public record EncryptedFile(String owner, byte[] ciphertext, String fileName, byte[] tag, byte[] nonce) {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public EncryptedFile(PlainFile plainFile, byte[] key, String owner) throws InvalidCipherTextException {
        this(owner, plainFile.name(), encryptInternal(plainFile.content(), key));
    }

    public void insertIntoDB(Connection conn) throws SQLException {
        if (existsInDB(conn, owner, fileName)) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE files SET ciphertext = ?, tag = ?, nonce = ?  WHERE owner = ? AND fileName = ?"
            )) {
                stmt.setBytes(1, ciphertext);
                stmt.setBytes(2, tag);
                stmt.setBytes(3, nonce);
                stmt.setString(4, owner);
                stmt.setString(5, fileName);
                stmt.executeUpdate();
            }
        } else {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO files (owner, ciphertext, fileName, tag, nonce) VALUES (?, ?, ?, ?, ?)"
            )) {
                stmt.setString(1, this.owner);
                stmt.setBytes(2, this.ciphertext);
                stmt.setString(3, this.fileName);
                stmt.setBytes(4, this.tag);
                stmt.setBytes(5, this.nonce);
                stmt.executeUpdate();
            }
        }
    }

    public static List<String> getFileList(Connection conn, String owner) throws SQLException {
        String sql = "SELECT fileName FROM files WHERE owner = ?";
        ArrayList<String> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, owner);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            rs = null;
            return result;
        }
    }

    public static EncryptedFile getFromDB(Connection conn, String owner, String fileName) throws SQLException {
        if (existsInDB(conn, owner, fileName)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM files WHERE owner = ? AND fileName = ?"
            );
            stmt.setString(1, owner);
            stmt.setString(2, fileName);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return new EncryptedFile(owner, rs.getBytes(2), fileName, rs.getBytes(4), rs.getBytes(5));
        } else {
            return null;
        }
    }

    public static void invalidateTag(Connection conn, String owner, String fileName) throws SQLException {
        if (existsInDB(conn, owner, fileName)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE files SET tag = ? WHERE owner = ? AND fileName = ?"
            );
            byte[] invalidTag = new byte[16];
            new SecureRandom().nextBytes(invalidTag);
            stmt.setBytes(1, invalidTag);
            stmt.setString(2, owner);
            stmt.setString(3, fileName);
            stmt.executeUpdate();
        }
    }

    public static void deleteEncryptedFile(Connection conn, String owner, String fileName) throws SQLException {
        if (existsInDB(conn, owner, fileName)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM files WHERE owner = ? AND fileName = ?"
            );
            stmt.setString(1, owner);
            stmt.setString(2, fileName);
            stmt.executeUpdate();
        }
    }

    private static boolean existsInDB(Connection conn, String owner, String fileName) throws SQLException {
        String sql = "SELECT * FROM files WHERE owner = ? AND fileName = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, owner);
            stmt.setString(2, fileName);
            return stmt.executeQuery().next();
        }
    }

    private EncryptedFile(String owner, String fileName, byte[][] parts) {
        this(
                owner,
                // ciphertext
                parts[0],
                fileName,
                // tag
                parts[1],
                // nonce
                parts[2]
        );
    }


    private static byte[][] encryptInternal(byte[] plaintext, byte[] key) throws InvalidCipherTextException {
        byte[] nonce = new byte[12];
        new SecureRandom().nextBytes(nonce);

        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        cipher.init(true, new AEADParameters(new KeyParameter(key), 128, nonce));

        byte[] output = new byte[cipher.getOutputSize(plaintext.length)];
        int len = cipher.processBytes(plaintext, 0, plaintext.length, output, 0);
        cipher.doFinal(output, len);

        int ctLen = output.length - 16;
        byte[][] result = new byte[][]{
                Arrays.copyOfRange(output, 0, ctLen),
                Arrays.copyOfRange(output, ctLen, output.length),
                nonce
        };

        ctLen = 0;
        output = null;
        key = null;

        return result;
    }
}
