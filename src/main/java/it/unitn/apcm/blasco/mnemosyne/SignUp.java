package it.unitn.apcm.blasco.mnemosyne;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import static it.unitn.apcm.blasco.mnemosyne.Utils.DB_URL;
import static it.unitn.apcm.blasco.mnemosyne.Utils.hashPassword;

@WebServlet(name = "SignUp", value = "/SignUp")
public class SignUp extends HttpServlet {
    //private static final String DB_URL = "jdbc:sqlite:/data/mnemosyne.db";

    public void init() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String usr = req.getParameter("signUpUsr");
        String psw = req.getParameter("signUpPsw");

        if (usr == null || usr.isEmpty() || psw == null || psw.isEmpty()) {
            resp.sendRedirect(req.getHeader("Referer"));
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if(userExists(conn, usr)) {
                resp.sendRedirect(req.getHeader("Referer"));
                return;
            }
            String salt = String.valueOf(System.currentTimeMillis() / 1000L);
            String hashedPassword = hashPassword(psw, salt);

            insertUser(conn, usr, hashedPassword, salt);
            System.out.println("User added successfully");
            req.setAttribute("username", usr);
            req.getRequestDispatcher("home.jsp").forward(req, resp);
        }
        catch (SQLException | NoSuchAlgorithmException e) {
            throw new IOException("Registration failed", e);
        }
        catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    private boolean userExists(Connection conn, String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void insertUser(Connection conn, String username, String hashedPassword, String salt) throws SQLException {
        String sql = "INSERT INTO users (username, hashed_password, salt) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, salt);
            stmt.executeUpdate();
        }
    }
}
