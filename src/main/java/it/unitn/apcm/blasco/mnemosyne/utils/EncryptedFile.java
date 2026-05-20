package it.unitn.apcm.blasco.mnemosyne.utils;

import org.bouncycastle.crypto.InvalidCipherTextException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.*;

public record EncryptedFile(String owner, byte[] ciphertext, String fileName, byte[] tag, byte[] nonce)
        implements AutoCloseable {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }
    }

    public EncryptedFile(PlainFile plainFile, byte[] key, String owner) throws InvalidCipherTextException,
            NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        this(owner, plainFile.name(), encrypt(plainFile.content(), key));
    }

    public void insertIntoDB() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (existsInDB(this.owner, this.fileName)) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE files SET ciphertext = ?, tag = ?, nonce = ?  WHERE owner = ? AND fileName = ?"
                )) {
                    stmt.setBytes(1, this.ciphertext);
                    stmt.setBytes(2, this.tag);
                    stmt.setBytes(3, this.nonce);
                    stmt.setString(4, this.owner);
                    stmt.setString(5, this.fileName);
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
    }

    public static List<String> getFileList(String owner) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            ArrayList<String> result = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement("SELECT fileName FROM files WHERE owner = ?")) {
                stmt.setString(1, owner);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    result.add(rs.getString(1));
                }
                return result;
            }
        }
    }

    public static EncryptedFile getFromDB(String owner, String fileName) throws SQLException {
        if (existsInDB(owner, fileName)) {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM files WHERE owner = ? AND fileName = ?"
                )) {
                    stmt.setString(1, owner);
                    stmt.setString(2, fileName);
                    ResultSet rs = stmt.executeQuery();
                    rs.next();
                    return new EncryptedFile(owner, rs.getBytes(2), fileName, rs.getBytes(4), rs.getBytes(5));
                }
            }
        } else {
            throw new SQLException("File not found");
        }
    }

    public static void invalidateTag(String owner, String fileName) throws SQLException,
            NoSuchAlgorithmException, NoSuchProviderException {
        if (existsInDB(owner, fileName)) {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                try (
                        PreparedStatement stmt = conn.prepareStatement(
                                "UPDATE files SET tag = ? WHERE owner = ? AND fileName = ?"
                        )) {
                    stmt.setBytes(1, getRandomBytes(16));
                    stmt.setString(2, owner);
                    stmt.setString(3, fileName);
                    stmt.executeUpdate();
                }
            }
        }
    }

    public static void deleteEncryptedFile(String owner, String fileName) throws SQLException {
        if (existsInDB(owner, fileName)) {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM files WHERE owner = ? AND fileName = ?"
                )) {
                    stmt.setString(1, owner);
                    stmt.setString(2, fileName);
                    stmt.executeUpdate();
                }
            }
        } else {
            throw new SQLException("File not found");
        }
    }

    private static boolean existsInDB(String owner, String fileName) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM files WHERE owner = ? AND fileName = ?")) {
                stmt.setString(1, owner);
                stmt.setString(2, fileName);
                return stmt.executeQuery().next();
            }
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

    @Override
    public void close() {
        Arrays.fill(this.ciphertext, (byte) 0);
        Arrays.fill(this.tag, (byte) 0);
        Arrays.fill(this.nonce, (byte) 0);
    }
}
