package it.unitn.apcm.blasco.mnemosyne.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.*;
import java.util.Arrays;

import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.DB_URL;
import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.hashPassword;

public record User(byte[] hashedUsername, byte[] hashedPassword, byte[] salt) implements AutoCloseable {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }
    }

    public static boolean areUserCredentialValid(byte[] hashedUsername, byte[] hashedPassword) throws NoSuchAlgorithmException, NoSuchProviderException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM users WHERE hashed_username = ?"
            )) {
                stmt.setBytes(1, hashedUsername);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        byte[] empty256bits = new byte[32];
                        byte[] empty64bits = new byte[8];
                        MessageDigest.isEqual(
                                hashPassword(
                                        empty256bits,
                                        empty64bits
                                ),
                                empty256bits
                        );
                        return false;
                    } else {
                        return MessageDigest.isEqual(
                                hashPassword(hashedPassword, rs.getBytes("salt")),
                                rs.getBytes("hashed_password")
                        );
                    }
                }
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw e;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean isUsernameInDB(byte[] hashedUsername) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE hashed_username = ?")) {
                stmt.setBytes(1, hashedUsername);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        }
    }

    public void insertUserInDB() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "INSERT INTO users (hashed_username, hashed_password, salt) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBytes(1, this.hashedUsername);
                stmt.setBytes(2, this.hashedPassword);
                stmt.setBytes(3, this.salt);
                stmt.executeUpdate();
            }
        }
    }

    @Override
    public void close() {
        Arrays.fill(this.hashedUsername, (byte) 0);
        Arrays.fill(this.hashedPassword, (byte) 0);
        Arrays.fill(this.salt, (byte) 0);
    }
}
