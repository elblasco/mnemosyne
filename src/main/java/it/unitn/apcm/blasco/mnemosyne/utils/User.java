package it.unitn.apcm.blasco.mnemosyne.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.hashPassword;

public record User(String username, byte[] hashedPassword, byte[] salt) {

    public static User getUserFromDB(Connection conn, String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString(1),
                            rs.getBytes(2),
                            rs.getBytes(3)
                    );
                } else {
                    throw new SQLException("User not found");
                }
            }
        }
    }

    public static boolean isUserLogged(Connection conn, String username, byte[] plainPassword) throws NoSuchAlgorithmException, NoSuchProviderException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE username = ?"
        )) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    byte[] tmp = {0x00};
                    MessageDigest.isEqual(
                            hashPassword(
                                    tmp,
                                    tmp
                            ),
                            tmp
                    );
                    return false;
                } else {
                    byte[] storedHash = rs.getBytes("hashed_password");
                    byte[] salt = rs.getBytes("salt");
                    byte[] computedHash = hashPassword(plainPassword, salt);
                    return MessageDigest.isEqual(
                            computedHash,
                            storedHash
                    );
                }
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw e;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean IsUserInDB(Connection conn, String username) {
        try {
            getUserFromDB(conn, username);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public void insertUserInDB(Connection conn) throws SQLException {
        String sql = "INSERT INTO users (username, hashed_password, salt) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, this.username);
            stmt.setBytes(2, this.hashedPassword);
            stmt.setBytes(3, this.salt);
            stmt.executeUpdate();
        }
    }
}
