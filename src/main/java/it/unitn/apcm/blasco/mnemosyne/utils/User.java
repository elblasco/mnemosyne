package it.unitn.apcm.blasco.mnemosyne.utils;

import java.sql.*;

// username TEXT NOT NULL PRIMARY KEY,
// hashed_password TEXT NOT NULL,
// salt TEXT NOT NULL
public record User(String username, String hashedPassword, String salt) {

    public static User getUserFromDB(Connection conn, String username) throws SQLException {
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

    public static boolean IsUserInDB(Connection conn, String username) throws SQLException {
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
            stmt.setString(2, this.hashedPassword);
            stmt.setString(3, this.salt);
            stmt.executeUpdate();
        }
    }
}
