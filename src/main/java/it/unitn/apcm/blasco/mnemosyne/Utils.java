package it.unitn.apcm.blasco.mnemosyne;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Utils {
    protected static final String DB_URL = "jdbc:sqlite:/data/mnemosyne.db";
//    username TEXT NOT NULL PRIMARY KEY,
//    hashed_password TEXT NOT NULL,
//    salt TEXT NOT NULL
    protected record User(String username, String hashedPassword, String Salt) {
    static protected User getUserFromDB(Connection conn, String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3)
                    );
                } else {
                    throw new SQLException("User not found");
                }
            }
        }
    }
}

    static protected String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest((password + salt).getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
